/* Copyright (c) 2006 Olivier Elshocht
 *
 * Attribute.java
 *
 * Created on 25 novembre 2006, 20:00
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
/* package */ class AttributeCode extends Attribute
{
    // Code_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 max_stack;
    //     u2 max_locals;
    //     u4 code_length;
    //     u1 code[code_length];
    //     u2 exception_table_length;
    //     {    u2 start_pc;
    //          u2 end_pc;
    //          u2  handler_pc;
    //          u2  catch_type;
    //     }    exception_table[exception_table_length];
    //     u2 attributes_count;
    //     attribute_info attributes[attributes_count];
    // }
    private int             mMaxStack;
    private int             mMaxLocals;
    private ByteCode        mCode;
    private ExceptionInfo[] mExceptionTable;
    private Attribute[]     mAttributes;

    public AttributeCode(ClassFile                 aClass,
                         ConstantPool.ConstantUtf8 aName,
                         byte[]                    aInfo)
                  throws IOException
    {
        super(aClass, aName);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        mMaxStack  = in.readUnsignedShort();
        mMaxLocals = in.readUnsignedShort();

        int codeLength = in.readInt();
        byte[] code = new byte[codeLength];
        in.readFully(code);
        mCode = new ByteCode(aClass, code);
        if (!mCode.isValid())
        {
            mIsValid = false;
            for (String error: mCode.getValidityErrors())
            {
                mValidityErrors.add(toString() + ": " + error);
            }
        }

        int exceptionTableLength = in.readUnsignedShort();
        mExceptionTable = new ExceptionInfo[exceptionTableLength];
        for (int i = 0; i < exceptionTableLength; ++i)
        {
            mExceptionTable[i] = new ExceptionInfo(in);
        }

        int attributesCount = in.readUnsignedShort();
        mAttributes = new Attribute[attributesCount];
        for (int i = 0; i < attributesCount; ++i)
        {
            Attribute attribute = Attribute.parse(aClass, this, in);
            mAttributes[i] = attribute;
            if (!attribute.isValid())
            {
                mIsValid = false;
                for (String error: attribute.getValidityErrors())
                {
                    mValidityErrors.add(toString() + ".attribute[" + i + "]: " + error);
                }
            }
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mMaxStack);
        aOut.writeShort(mMaxLocals);
        aOut.writeInt(mCode.size());
        mCode.store(aOut);

        aOut.writeShort(mExceptionTable.length);
        for (int i = 0; i < mExceptionTable.length; ++i)
        {
            mExceptionTable[i].store(aOut);
        }

        aOut.writeShort(mAttributes.length);
        for (int i = 0; i < mAttributes.length; ++i)
        {
            mAttributes[i].store(aOut);
        }
    }

    public ByteCode getCode()
    {
        return mCode;
    }

    public int getLength()
    {
        int length = 0;
        if (isValid())
        {
            length += 2;                                  // u2 attribute_name_index;
            length += 4;                                  // u4 attribute_length;
            length += 2;                                  // u2 max_stack;
            length += 2;                                  // u2 max_locals;
            length += 4;                                  // u4 code_length;
            length += mCode.size();                       // u1 code[code_length];
            length += 2;                                  // u2 exception_table_length;
            length += (  2                                // {    u2 start_pc;
                       + 2                                //      u2 end_pc;
                       + 2                                //      u2  handler_pc;
                       + 2                                //      u2  catch_type;
                          ) * mExceptionTable.length;     // }    exception_table[exception_table_length];
            length += 2;                                  // u2 attributes_count;
            for (int i = 0; i < mAttributes.length; ++i)  // attribute_info attributes[attributes_count];
            {
                length += mAttributes[i].getLength();
            }
        }

        return length;
    }

    public void dump()
    {
        super.dump();
        System.out.format("  Code: stack=%d, locals=%d, length=%d\n",
                          mMaxStack,
                          mMaxLocals,
                          mCode.size());
        mCode.dump();
        for (int i = 0; i < mExceptionTable.length; ++i)
        {
            mExceptionTable[i].dump();
        }
        for (int i = 0; i < mAttributes.length; ++i)
        {
            mAttributes[i].dump();
        }
    }


    // EXCEPTIONS : exception table structure

    private class ExceptionInfo
    {
        private ByteCode.Instruction       mStartPc;
        private ByteCode.Instruction       mEndPc;
        private ByteCode.Instruction       mHandlerPc;
        private ConstantPool.ConstantClass mCatchType;

        public ExceptionInfo(DataInputStream aIn)
                      throws IOException
        {
            int startPcIndex = aIn.readUnsignedShort();
            mStartPc         = mCode.getByOffset(startPcIndex);
            if (null == mStartPc)
            {
                mIsValid = false;
                mValidityErrors.add(AttributeCode.this.toString()
                                    + ".exception_table: invalid start_pc "
                                    + startPcIndex);
            }

            int endPcIndex = aIn.readUnsignedShort();
            mEndPc         = mCode.getByOffset(endPcIndex);
            if ((endPcIndex <= startPcIndex) || (mCode.size() < endPcIndex) || (null == mEndPc))
            {
                mIsValid = false;
                mValidityErrors.add(AttributeCode.this.toString()
                                    + ".exception_table: invalid end_pc "
                                    + endPcIndex);
            }

            int handlerPcIndex = aIn.readUnsignedShort();
            mHandlerPc         = mCode.getByOffset(handlerPcIndex);
            if (null == mHandlerPc)
            {
                mIsValid = false;
                mValidityErrors.add(AttributeCode.this.toString()
                                    + ".exception_table: invalid handler_pc "
                                    + handlerPcIndex);
            }

            int catchTypeIndex                      = aIn.readUnsignedShort();
            ConstantPool.Constant catchTypeConstant = mClass.getConstantPool().getByIndex(catchTypeIndex);
            if (catchTypeConstant instanceof ConstantPool.ConstantClass)
            {
                mCatchType = (ConstantPool.ConstantClass) catchTypeConstant;
            }
            else if (0 == catchTypeIndex)
            {
                mCatchType = null;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add(AttributeCode.this.toString()
                                    + ".exception_table: invalid catch_type constant #"
                                    + catchTypeIndex
                                    + ((null == catchTypeConstant) ? "" : (" //" + catchTypeConstant.toString())));
            }
        }

        public void store(DataOutputStream aOut)
                   throws IOException
        {
            aOut.writeShort(mStartPc.getOffset());
            aOut.writeShort(mEndPc.getOffset());
            aOut.writeShort(mHandlerPc.getOffset());
            if (null == mCatchType)
            {
                aOut.writeShort(0);
            }
            else
            {
                aOut.writeShort(mCatchType.getIndex());
            }
        }

        public void dump()
        {
            System.out.format("  Exception [%d:,%d:) -> %d: = #%d //%s\n",
                              mStartPc.getOffset(),
                              mEndPc.getOffset(),
                              mHandlerPc.getOffset(),
                              ((null == mCatchType) ? 0 : mCatchType.getIndex()),
                              ((null == mCatchType) ? "Any" : mCatchType.toString()));
        }
    }
}
