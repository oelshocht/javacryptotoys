/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeLocalVariableTable.java
 *
 * Created on 25 novembre 2006, 23:46
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
public class AttributeLocalVariableTable extends Attribute
{
    private AttributeCode mAttributeCode;

    // LocalVariableTable_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 local_variable_table_length;
    //     {    u2 start_pc;
    //          u2 length;
    //          u2 name_index;
    //          u2 descriptor_index;
    //          u2 index;
    //     } local_variable_table[local_variable_table_length];
    // }
    private LocalVariableInfo[]       mLocalVariableTable;

    public AttributeLocalVariableTable(ClassFile                 aClass,
                                       AttributeCode             aAttributeCode,
                                       ConstantPool.ConstantUtf8 aName,
                                       byte[]                    aInfo)
                                throws IOException
    {
        super(aClass, aName);
        mAttributeCode = aAttributeCode;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        int localVariableTableLength = in.readUnsignedShort();
        mLocalVariableTable = new LocalVariableInfo[localVariableTableLength];
        for (int i = 0; i < localVariableTableLength; ++i)
        {
            mLocalVariableTable[i] = new LocalVariableInfo(in);
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mLocalVariableTable.length);
        for (int i = 0; i < mLocalVariableTable.length; ++i)
        {
            mLocalVariableTable[i].store(aOut);
        }
    }

    public int getLength()
    {
        int length = 0;
        if (isValid())
        {
            length += 2;                                  // u2 attribute_name_index;
            length += 4;                                  // u4 attribute_length;
            length += 2;                                  // u2 local_variable_table_length;
            length += (  2                                // {    u2 start_pc;
                       + 2                                //      u2 length;
                       + 2                                //      u2 name_index;
                       + 2                                //      u2 descriptor_index;
                       + 2                                //          u2 index;
                          ) * mLocalVariableTable.length; // } local_variable_table[local_variable_table_length];
        }

        return length;
    }

    public void dump()
    {
        super.dump();
        for (int i = 0; i < mLocalVariableTable.length; ++i)
        {
            mLocalVariableTable[i].dump();
        }
    }


    // LOCAL VARIABLES : local variable table structure

    private class LocalVariableInfo
    {
        private ByteCode.Instruction       mStartPc;
        private ByteCode.Instruction       mEndPc;
        private ConstantPool.ConstantUtf8  mName;
        private ConstantPool.ConstantUtf8  mDescriptor;
        private int                        mIndex;

        public LocalVariableInfo(DataInputStream aIn)
                       throws IOException
        {
            int startPcIndex = aIn.readUnsignedShort();
            mStartPc         = mAttributeCode.getCode().getByOffset(startPcIndex);
            if (null == mStartPc)
            {
                mIsValid = false;
                mValidityErrors.add(AttributeLocalVariableTable.this.toString()
                                    + ".local_variable_table: invalid start_pc "
                                    + startPcIndex);
            }

            int endPcIndex = startPcIndex + aIn.readUnsignedShort();
            mEndPc         = mAttributeCode.getCode().getByOffset(endPcIndex);
            if ((mAttributeCode.getCode().size() < endPcIndex) || (null == mEndPc))
            {
                mIsValid = false;
                mValidityErrors.add(AttributeLocalVariableTable.this.toString()
                                    + ".local_variable_table: invalid length "
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
                mValidityErrors.add(AttributeLocalVariableTable.this.toString()
                                    + ".local_variable_table: invalid name constant #"
                                    + nameIndex
                                    + ((null != nameConstant) ? (" //" + nameConstant.toString()) : ""));
            }

            int descriptorIndex                      = aIn.readUnsignedShort();
            ConstantPool.Constant descriptorConstant = mClass.getConstantPool().getByIndex(descriptorIndex);
            if (descriptorConstant instanceof ConstantPool.ConstantUtf8)
            {
                mDescriptor = (ConstantPool.ConstantUtf8) descriptorConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add(AttributeLocalVariableTable.this.toString()
                                    + ".local_variable_table: invalid descriptor constant #"
                                    + descriptorIndex
                                    + ((null != descriptorConstant) ? (" //" + descriptorConstant.toString()) : ""));
            }

            mIndex = aIn.readUnsignedShort();
        }

        public void store(DataOutputStream aOut)
                   throws IOException
        {
            aOut.writeShort(mStartPc.getOffset());
            aOut.writeShort(mEndPc.getOffset() - mStartPc.getOffset());
            aOut.writeShort(mName.getIndex());
            aOut.writeShort(mDescriptor.getIndex());
            aOut.writeShort(mIndex);
        }

        public void dump()
        {
            System.out.format("  Local variable %d: %s:%s -> start=%d: length=%d\n",
                              mIndex,
                              mName.toString(),
                              mDescriptor.toString(),
                              mStartPc.getOffset(),
                              mEndPc.getOffset() - mStartPc.getOffset());
        }
    }
}
