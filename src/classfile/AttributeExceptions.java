/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeExceptions.java
 *
 * Created on 29 novembre 2006, 18:29
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
/* package */ class AttributeExceptions extends Attribute
{
    // Exceptions_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 number_of_exceptions;
    //     u2 exception_index_table[number_of_exceptions];
    // }
    private ConstantPool.ConstantClass[] mExceptions;

    public AttributeExceptions(ClassFile                 aClass,
                               ConstantPool.ConstantUtf8 aName,
                               byte[]                    aInfo)
                        throws IOException
    {
        super(aClass, aName);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        int nbExceptions = in.readUnsignedShort();
        mExceptions = new ConstantPool.ConstantClass[nbExceptions];
        for (int i = 0; i < nbExceptions; ++i)
        {
            int exceptionIndex = in.readUnsignedShort();
            ConstantPool.Constant exceptionConstant = aClass.getConstantPool().getByIndex(exceptionIndex);
            if (exceptionConstant instanceof ConstantPool.ConstantClass)
            {
                mExceptions[i] = (ConstantPool.ConstantClass) exceptionConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add(  toString() + "exception_index.table[" + i + "]: invalid class constant #"
                                    + exceptionIndex
                                    + ((null != exceptionConstant) ? (" //" + exceptionConstant.toString()) : ""));
            }
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mExceptions.length);
        for (int i = 0; i < mExceptions.length; ++i)
        {
            aOut.writeShort(mExceptions[i].getIndex());
        }
    }

    public int getLength()
    {
        int length = 0;
        if (isValid())
        {
            length += 2;                                  // u2 attribute_name_index;
            length += 4;                                  // u4 attribute_length;
            length += 2;                                  // u2 number_of_exceptions;
            length += 2 * mExceptions.length;             // u2 exception_index_table[number_of_exceptions];
        }

        return length;
    }

    public void dump()
    {
        super.dump();
        for (int i = 0; i < mExceptions.length; ++i)
        {
            System.out.format("  throws %s\n", mExceptions[i].toString());
        }
    }
}
