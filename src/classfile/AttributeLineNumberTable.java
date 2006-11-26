/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeLineNumberTable.java
 *
 * Created on 25 novembre 2006, 22:31
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
public class AttributeLineNumberTable extends Attribute
{
    private AttributeCode mAttributeCode;

    // LineNumberTable_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 line_number_table_length;
    //     {    u2 start_pc;
    //          u2 line_number;
    //     } line_number_table[line_number_table_length];
    // }
    private LineNumberInfo[]          mLineNumberTable;

    public AttributeLineNumberTable(ClassFile                 aClass,
                                    AttributeCode             aAttributeCode,
                                    ConstantPool.ConstantUtf8 aName,
                                    byte[]                    aInfo)
                             throws IOException
    {
        super(aClass, aName);
        mAttributeCode = aAttributeCode;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        int lineNumberTableLength = in.readUnsignedShort();
        mLineNumberTable = new LineNumberInfo[lineNumberTableLength];
        for (int i = 0; i < lineNumberTableLength; ++i)
        {
            mLineNumberTable[i] = new LineNumberInfo(in);
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mLineNumberTable.length);
        for (int i = 0; i < mLineNumberTable.length; ++i)
        {
            mLineNumberTable[i].store(aOut);
        }
    }

    public int getLength()
    {
        int length = 0;
        if (isValid())
        {
            length += 2;                               // u2 attribute_name_index;
            length += 4;                               // u4 attribute_length;
            length += 2;                               // u2 line_number_table_length;
            length += (  2                             // {    u2 start_pc;
                       + 2                             //      u2 line_number;	     
                          ) * mLineNumberTable.length; // } line_number_table[line_number_table_length];
        }

        return length;
    }

    public void dump()
    {
        super.dump();
        for (int i = 0; i < mLineNumberTable.length; ++i)
        {
            mLineNumberTable[i].dump();
        }
    }


    // LINE NUMBERS : line number table structure

    private class LineNumberInfo
    {
        private ByteCode.Instruction mStartPc;
        private int                  mLineNumber;

        public LineNumberInfo(DataInputStream aIn)
                       throws IOException
        {
            int startPcIndex = aIn.readUnsignedShort();
            mStartPc         = mAttributeCode.getCode().getByOffset(startPcIndex);
            if (null == mStartPc)
            {
                mIsValid = false;
                mValidityErrors.add(AttributeLineNumberTable.this.toString()
                                    + ".line_number_table: invalid start_pc "
                                    + startPcIndex);
            }

            mLineNumber = aIn.readUnsignedShort();
        }

        public void store(DataOutputStream aOut)
                   throws IOException
        {
            aOut.writeShort(mStartPc.getOffset());
            aOut.writeShort(mLineNumber);
        }

        public void dump()
        {
            System.out.format("  Line number %d -> %d:\n", mLineNumber, mStartPc.getOffset());
        }
    }
}
