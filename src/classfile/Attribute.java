/* Copyright (c) 2006-2010 Olivier Elshocht
 *
 * Attribute.java
 *
 * Created 2006-11-25
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

    public static final String CONSTANT_VALUE                          = "ConstantValue";                        // 4.8.2
    public static final String CODE                                    = "Code";                                 // 4.8.3
    public static final String STACK_MAP_TABLE                         = "StackMapTable";                        // 4.8.4
    public static final String EXCEPTIONS                              = "Exceptions";                           // 4.8.5
    public static final String INNER_CLASSES                           = "InnerClasses";                         // 4.8.6
    public static final String ENCLOSING_METHOD                        = "EnclosingMethod";                      // 4.8.7
    //public static final String SYNTHETIC                               = "Synthetic";                            // 4.8.8
    public static final String SIGNATURE                               = "Signature";                            // 4.8.9
    public static final String SOURCE_FILE                             = "SourceFile";                           // 4.8.10
    //public static final String SOURCE_DEBUG_EXTENSION                  = "SourceDebugExtension";                 // 4.8.11
    public static final String LINE_NUMBER_TABLE                       = "LineNumberTable";                      // 4.8.12
    public static final String LOCAL_VARIABLE_TABLE                    = "LocalVariableTable";                   // 4.8.13
    public static final String LOCAL_VARIABLE_TYPE_TABLE               = "LocalVariableTypeTable";               // 4.8.14
    //public static final String DEPRECATED                              = "Deprecated";                           // 4.8.15
    //public static final String RUNTIME_VISIBLE_ANNOTATIONS             = "RuntimeVisibleAnnotations";            // 4.8.16
    //public static final String RUNTIME_INVISIBLE_ANNOTATIONS           = "RuntimeInvisibleAnnotations";          // 4.8.17
    //public static final String RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS   = "RuntimeVisibleParameterAnnotations";   // 4.8.18
    //public static final String RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations"; // 4.8.19
    //public static final String ANNOTATION_DEFAULT                      = "AnnotationDefault";                    // 4.8.20


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
            name.incRefCount();
        }

        // Create attribute instance.
        if (null == name)
        {
            return new AttributeInvalid(aClass, nameIndex, info);
        }
        else if (name.toString().equals(CONSTANT_VALUE))
        {
            return new AttributeConstantValue(aClass, name, info);
        }
        else if (name.toString().equals(CODE))
        {
            return new AttributeCode(aClass, name, info);
        }
        else if (name.toString().equals(EXCEPTIONS))
        {
            return new AttributeExceptions(aClass, name, info);
        }
        else if (name.toString().equals(INNER_CLASSES))
        {
            return new AttributeInnerClasses(aClass, name, info);
        }
        else if (name.toString().equals(ENCLOSING_METHOD))
        {
            return new AttributeEnclosingMethod(aClass, name, info);
        }
        else if (name.toString().equals(SIGNATURE))
        {
            return new AttributeSignature(aClass, name, info);
        }
        else if (name.toString().equals(SOURCE_FILE))
        {
            return new AttributeSourceFile(aClass, name, info);
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
            name.incRefCount();
        }

        // Create attribute instance.
        if (null == name)
        {
            return new AttributeInvalid(aClass, nameIndex, info);
        }
        else if (name.toString().equals(STACK_MAP_TABLE))
        {
            return new AttributeStackMapTable(aClass, aAttributeCode, name, info);
        }
        else if (name.toString().equals(LINE_NUMBER_TABLE))
        {
            return new AttributeLineNumberTable(aClass, aAttributeCode, name, info);
        }
        else if (name.toString().equals(LOCAL_VARIABLE_TABLE))
        {
            return new AttributeLocalVariableTable(aClass, aAttributeCode, name, info);
        }
        else if (name.toString().equals(LOCAL_VARIABLE_TYPE_TABLE))
        {
            return new AttributeLocalVariableTypeTable(aClass, aAttributeCode, name, info);
        }
        else
        {
            return new AttributeUnknown(aClass, name, info);
        }
    }

    // ==================== GENERIC ATTRIBUTE  ====================

    protected boolean                   mIsValid = true;
    protected boolean                   mIsStored = true;
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

    public final boolean isStored()
    {
        return mIsStored;
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
