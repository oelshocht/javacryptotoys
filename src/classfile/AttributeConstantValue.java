/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeConstantValue.java
 *
 * Created on 26 novembre 2006, 0:44
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
public class AttributeConstantValue extends Attribute
{
    // ConstantValue_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 constantvalue_index;
    // }
    private ConstantPool.Constant mConstantValue;

    public AttributeConstantValue(ClassFile                 aClass,
                                  ConstantPool.ConstantUtf8 aName,
                                  byte[]                    aInfo)
                          throws IOException
    {
        super(aClass, aName);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        int constantValueIndex = in.readUnsignedShort();
        mConstantValue         = aClass.getConstantPool().getByIndex(constantValueIndex);
        if (   (null != mConstantValue)
            && (ConstantPool.STRING  != mConstantValue.getType())
            && (ConstantPool.INTEGER != mConstantValue.getType())
            && (ConstantPool.LONG    != mConstantValue.getType())
            && (ConstantPool.FLOAT   != mConstantValue.getType())
            && (ConstantPool.DOUBLE  != mConstantValue.getType()))
        {
            mIsValid = false;
            mValidityErrors.add(toString() + ": invalid constantvalue_index #" + constantValueIndex);
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mConstantValue.getIndex());
    }

    public int getLength()
    {
        return 8;
    }

    public void dump()
    {
        super.dump();
        System.out.format("  Constant value: %s\n", mConstantValue.toString());
    }
}

