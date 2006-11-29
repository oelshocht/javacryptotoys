/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeLocalVariableTypeTable.java
 *
 * Created on 29 novembre 2006, 17:07
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
/* package */ class AttributeLocalVariableTypeTable extends Attribute
{
    private AttributeCode mAttributeCode;

    // LocalVariableTypeTable_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 local_variable_type_table_length;
    //     {    u2 start_pc;
    //          u2 length;
    //          u2 name_index;
    //          u2 signature_index;
    //          u2 index;
    //     } local_variable_type_table[local_variable_type_table_length];
    // }
    private LocalVariableTypeInfo[] mLocalVariableTypeTable;

    public AttributeLocalVariableTypeTable(ClassFile                 aClass,
                                           AttributeCode             aAttributeCode,
                                           ConstantPool.ConstantUtf8 aName,
                                           byte[]                    aInfo)
                                    throws IOException
    {
        super(aClass, aName);
        mAttributeCode = aAttributeCode;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        int localVariableTypeTableLength = in.readUnsignedShort();
        mLocalVariableTypeTable = new LocalVariableTypeInfo[localVariableTypeTableLength];
        for (int i = 0; i < localVariableTypeTableLength; ++i)
        {
            mLocalVariableTypeTable[i] = new LocalVariableTypeInfo(in);
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mLocalVariableTypeTable.length);
        for (int i = 0; i < mLocalVariableTypeTable.length; ++i)
        {
            mLocalVariableTypeTable[i].store(aOut);
        }
    }

    public int getLength()
    {
        int length = 0;
        if (isValid())
        {
            length += 2;                                      // u2 attribute_name_index;
            length += 4;                                      // u4 attribute_length;
            length += 2;                                      // u2 local_variable_table_length;
            length += (  2                                    // {    u2 start_pc;
                       + 2                                    //      u2 length;
                       + 2                                    //      u2 name_index;
                       + 2                                    //      u2 signature_index;
                       + 2                                    //          u2 index;
                          ) * mLocalVariableTypeTable.length; // } local_variable_type_table[local_variable_type_table_length];
        }

        return length;
    }

    public void dump()
    {
        super.dump();
        for (int i = 0; i < mLocalVariableTypeTable.length; ++i)
        {
            mLocalVariableTypeTable[i].dump();
        }
    }


    // LOCAL VARIABLE TYPES : local variable type table structure

    private class LocalVariableTypeInfo
    {
        private ByteCode.Instruction       mStartPc;
        private ByteCode.Instruction       mEndPc;
        private ConstantPool.ConstantUtf8  mName;
        private ConstantPool.ConstantUtf8  mSignature;
        private int                        mIndex;

        public LocalVariableTypeInfo(DataInputStream aIn)
                              throws IOException
        {
            int startPcIndex = aIn.readUnsignedShort();
            mStartPc         = mAttributeCode.getCode().getByOffset(startPcIndex);
            if (null == mStartPc)
            {
                mIsValid = false;
                mValidityErrors.add(AttributeLocalVariableTypeTable.this.toString()
                                    + ".local_variable_type_table: invalid start_pc "
                                    + startPcIndex);
            }

            int endPcIndex = startPcIndex + aIn.readUnsignedShort();
            mEndPc         = mAttributeCode.getCode().getByOffset(endPcIndex);
            if ((mAttributeCode.getCode().size() < endPcIndex) || (null == mEndPc))
            {
                mIsValid = false;
                mValidityErrors.add(AttributeLocalVariableTypeTable.this.toString()
                                    + ".local_variable_type_table: invalid length "
                                    + (endPcIndex - startPcIndex));
            }

            int nameIndex                      = aIn.readUnsignedShort();
            ConstantPool.Constant nameConstant = mClass.getConstantPool().getByIndex(nameIndex);
            if (nameConstant instanceof ConstantPool.ConstantUtf8)
            {
                mName = (ConstantPool.ConstantUtf8) nameConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add(AttributeLocalVariableTypeTable.this.toString()
                                    + ".local_variable_type_table: invalid name constant #"
                                    + nameIndex
                                    + ((null != nameConstant) ? (" //" + nameConstant.toString()) : ""));
            }

            int signatureIndex                      = aIn.readUnsignedShort();
            ConstantPool.Constant signatureConstant = mClass.getConstantPool().getByIndex(signatureIndex);
            if (signatureConstant instanceof ConstantPool.ConstantUtf8)
            {
                mSignature = (ConstantPool.ConstantUtf8) signatureConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add(AttributeLocalVariableTypeTable.this.toString()
                                    + ".local_variable_type_table: invalid descriptor constant #"
                                    + signatureIndex
                                    + ((null != signatureConstant) ? (" //" + signatureConstant.toString()) : ""));
            }

            mIndex = aIn.readUnsignedShort();
        }

        public void store(DataOutputStream aOut)
                   throws IOException
        {
            aOut.writeShort(mStartPc.getOffset());
            aOut.writeShort(mEndPc.getOffset() - mStartPc.getOffset());
            aOut.writeShort(mName.getIndex());
            aOut.writeShort(mSignature.getIndex());
            aOut.writeShort(mIndex);
        }

        public void dump()
        {
            System.out.format("  Local variable type %d: %s:%s -> start=%d: length=%d\n",
                              mIndex,
                              mName.toString(),
                              mSignature.toString(),
                              mStartPc.getOffset(),
                              mEndPc.getOffset() - mStartPc.getOffset());
        }
    }
}
