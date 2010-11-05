/* Copyright (c) 2010 Olivier Elshocht
 *
 * AttributeStackMapTable.java
 *
 * Created 2010-11-05
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
/* package */ class AttributeStackMapTable extends Attribute
{
    private AttributeCode mAttributeCode;

    // stack_map {
    //     attribute StackMapTable
    //     u2 attribute_name_index;
    //     u4 attribute_length
    //     u2 number_of_entries;
    //     stack_map_frame entries[number_of_entries];
    // }
    private byte[] mInfo;

    public AttributeStackMapTable(ClassFile                 aClass,
                                  AttributeCode             aAttributeCode,
                                  ConstantPool.ConstantUtf8 aName,
                                  byte[]                    aInfo)
                           throws IOException
    {
        super(aClass, aName);
        mIsStored = false;
        mAttributeCode = aAttributeCode;
        mInfo = aInfo;
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        throw new RuntimeException("Cannot store AttributeStackMapTable");
        //super.store(aOut);
        //aOut.write(mInfo);
    }

    public int getLength()
    {
        return 0;
//        int length = 0;
//        length += 2;                                  // u2 attribute_name_index;
//        length += 4;                                  // u4 attribute_length;
//        length += mInfo.length;                       // u1 info[attribute_length];
//
//        return length;
    }
}
