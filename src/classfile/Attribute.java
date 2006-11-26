/* Copyright (c) 2006 Olivier Elshocht
 *
 * Attribute.java
 *
 * Created on 25 novembre 2006, 20:00
 */

package classfile;

import java.io.*;
import java.util.*;

/**
 *
 * @author Olivier Elshocht
 */
/* package */ abstract class Attribute
{
    // ==================== STATIC FACTORY METHODS  ====================

    public static final String SOURCE_FILE          = "SourceFile";
    public static final String CONSTANT_VALUE       = "ConstantValue";
    public static final String CODE                 = "Code";
    public static final String LINE_NUMBER_TABLE    = "LineNumberTable";
    public static final String LOCAL_VARIABLE_TABLE = "LocalVariableTable";

    public static Attribute parse(ClassFile       aClass,
                                  DataInputStream aIn)
                           throws IOException
    {
        // Read the attribute name index, length, and info.
        int nameIndex = aIn.readUnsignedShort();
        int length    = aIn.readInt();
        byte[] info   = new byte[length];
        aIn.readFully(info);

        // Retrieve the attribute name.
        ConstantPool.ConstantUtf8 name = null;
        ConstantPool.Constant nameConstant = aClass.getConstantPool().getByIndex(nameIndex);
        if (nameConstant instanceof ConstantPool.ConstantUtf8)
        {
            name = (ConstantPool.ConstantUtf8)nameConstant;
        }

        // Create attribute instance.
        if (null == name)
        {
            return new AttributeInvalid(aClass, nameIndex, info);
        }
        else if (name.toString().equals(SOURCE_FILE))
        {
            return new AttributeSourceFile(aClass, name, info);
        }
        else if (name.toString().equals(CONSTANT_VALUE))
        {
            return new AttributeConstantValue(aClass, name, info);
        }
        else if (name.toString().equals(CODE))
        {
            return new AttributeCode(aClass, name, info);
        }
        else
        {
            return new AttributeUnknown(aClass, name, info);
        }
    }

    public static Attribute parse(ClassFile       aClass,
                                  AttributeCode   aAttributeCode,
                                  DataInputStream aIn)
                           throws IOException
    {
        // Read the attribute name index, length, and info.
        int nameIndex = aIn.readUnsignedShort();
        int length    = aIn.readInt();
        byte[] info   = new byte[length];
        aIn.readFully(info);

        // Retrieve the attribute name.
        ConstantPool.ConstantUtf8 name = null;
        ConstantPool.Constant nameConstant = aClass.getConstantPool().getByIndex(nameIndex);
        if (nameConstant instanceof ConstantPool.ConstantUtf8)
        {
            name = (ConstantPool.ConstantUtf8)nameConstant;
        }

        // Create attribute instance.
        if (null == name)
        {
            return new AttributeInvalid(aClass, nameIndex, info);
        }
        else if (name.toString().equals(LINE_NUMBER_TABLE))
        {
            return new AttributeLineNumberTable(aClass, aAttributeCode, name, info);
        }
        else if (name.toString().equals(LOCAL_VARIABLE_TABLE))
        {
            return new AttributeLocalVariableTable(aClass, aAttributeCode, name, info);
        }
        else
        {
            return new AttributeUnknown(aClass, name, info);
        }
    }

    // ==================== GENERIC ATTRIBUTE  ====================

    protected boolean                   mIsValid = true;
    protected List<String>              mValidityErrors = new ArrayList<String>();
    protected ClassFile                 mClass;

    // attribute_info {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u1 info[attribute_length];
    // }
    private ConstantPool.ConstantUtf8 mName;
    private int                       mNameIndex;
 
    protected Attribute(ClassFile                 aClass,
                        ConstantPool.ConstantUtf8 aName)
    {
        mClass     = aClass;
        mName      = aName;
        mNameIndex = 0;
    }

    protected Attribute(ClassFile aClass,
                        int       aNameIndex)
    {
        mClass     = aClass;
        mName      = null;
        mNameIndex = aNameIndex;
    }

    public final boolean isValid()
    {
        return mIsValid;
    }

    public final List<String> getValidityErrors()
    {
        return new ArrayList<String>(mValidityErrors);
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        if (!mIsValid)
        {
            throw new ClassFileException("Cannot store invalid attribute");
        }

        aOut.writeShort(getNameIndex());
        aOut.writeInt(getLength() - 6);
    }

    public int getNameIndex()
    {
        if (null == mName)
        {
            return mNameIndex;
        }
        else
        {
            return mName.getIndex();
        }
    }

    public String getName()
    {
        if (null == mName)
        {
            return "invalid name_index #" + mNameIndex;
        }
        else
        {
            return mName.toString();
        }
    }

    public abstract int getLength();

    public final String toString()
    {
        return String.format("attribute(name=#%d:\"%s\", length=%d)",
                             getNameIndex(),
                             getName(),
                             getLength() - 6);        
    }

    public void dump()
    {
        System.out.println(toString());
    }
}
