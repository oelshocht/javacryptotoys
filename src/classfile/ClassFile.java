/* Copyright (c) 2006 Olivier Elshocht
 *
 * ClassFile.java
 *
 * Created on 15 novembre 2006, 22:51
 */

package classfile;

import java.io.*;
import java.util.*;

/**
 * This implementation is based on:
 *
 * The JavaTM Virtual Machine Specification, Second Edition
 * Chapter 4 (http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html):
 *
 * @author Olivier Elshocht
 */
public class ClassFile
{
    // ==================== CLASS FILE : ClassFile structure ====================

    private static final int ACC_PUBLIC    = 0x0001; // Declared public; may be accessed from outside its package.
    private static final int ACC_FINAL     = 0x0010; // Declared final; no subclasses allowed.
    private static final int ACC_SUPER     = 0x0020; // Treat superclass methods specially when invoked by the invokespecial instruction.
    private static final int ACC_INTERFACE = 0x0200; // Is an interface, not a class.
    private static final int ACC_ABSTRACT  = 0x0400; // Declared abstract; may not be instantiated.

    // Internal data.
    private boolean      mIsValid        = true;
    private List<String> mValidityErrors = new ArrayList<String>();

    // ClassFile {
    //     u4 magic;
    //     u2 minor_version;
    //     u2 major_version;
    //     u2 constant_pool_count;
    //     cp_info constant_pool[constant_pool_count-1];
    //     u2 access_flags;
    //     u2 this_class;
    //     u2 super_class;
    //     u2 interfaces_count;
    //     u2 interfaces[interfaces_count];
    //     u2 fields_count;
    //     field_info fields[fields_count];
    //     u2 methods_count;
    //     method_info methods[methods_count];
    //     u2 attributes_count;
    //     attribute_info attributes[attributes_count];
    // }

    // Header.
    private int                          mMagic;
    private int                          mMinorVersion;
    private int                          mMajorVersion;

    // Constants.
    ConstantPool                 mConstantPool;

    // Class definition.
    private int                          mAccessFlags;
    private ConstantPool.ConstantClass   mThisClass;
    private ConstantPool.ConstantClass   mSuperClass;
    private ConstantPool.ConstantClass[] mInterfaces;

    // Fields, methods and attributes.
    private Field[]                      mFields;
    private Method[]                     mMethods;
    private Attribute[]                  mAttributes;


    // ==================== CLASS FILE : PUBLIC METHODS ====================

    public ClassFile(String aFile)
    {
        try
        {
            DataInputStream in = new DataInputStream(new FileInputStream(aFile));

            // Header.
            mMagic        = in.readInt();
            mMinorVersion = in.readUnsignedShort();
            mMajorVersion = in.readUnsignedShort();

            // Constants.
            int constantPoolCount = in.readUnsignedShort();
            mConstantPool         = new ConstantPool(constantPoolCount, in);
            if (!mConstantPool.isValid())
            {
                mIsValid = false;
                for (String error : mConstantPool.getValidityErrors())
                {
                    mValidityErrors.add("constant_pool: " + error);
                }
            }

            // Class definition.
            mAccessFlags                             = in.readUnsignedShort();
            // this_class.
            int thisClassIndex                       = in.readUnsignedShort();
            ConstantPool.Constant thisClassConstant  = mConstantPool.getByIndex(thisClassIndex);
            if (thisClassConstant instanceof ConstantPool.ConstantClass)
            {
                mThisClass = (ConstantPool.ConstantClass)thisClassConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add("this_class: invalid constant #"
                                    + thisClassIndex
                                    + ((null != thisClassConstant) ? (" //" + thisClassConstant.toString()) : ""));
            }
            // super_class.
            int superClassIndex                      = in.readUnsignedShort();
            ConstantPool.Constant superClassConstant = mConstantPool.getByIndex(superClassIndex);
            if (superClassConstant instanceof ConstantPool.ConstantClass)
            {
                mSuperClass = (ConstantPool.ConstantClass) superClassConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add("super_class: invalid constant #"
                                    + superClassIndex 
                                    + ((null != superClassConstant) ? (" //" + superClassConstant.toString()) : ""));
            }
            // interfaces.
            int interfacesCount = in.readUnsignedShort();
            mInterfaces         = new ConstantPool.ConstantClass[interfacesCount];
            for (int i = 0; i < interfacesCount; ++i)
            {
                int interfaceIndex                      = in.readUnsignedShort();
                ConstantPool.Constant interfaceConstant = mConstantPool.getByIndex(interfaceIndex);
                if (interfaceConstant instanceof ConstantPool.ConstantClass)
                {
                    mInterfaces[i] = (ConstantPool.ConstantClass) interfaceConstant;
                }
                else
                {
                    mIsValid = false;
                    mValidityErrors.add("interfaces[" + i + "]: invalid constant #"
                                        + interfaceIndex
                                        + ((null != interfaceConstant) ? (" //" + interfaceConstant.toString()) : ""));
                }
            }

            // Fields, methods and attributes.
            int fieldsCount = in.readUnsignedShort();
            mFields         = new Field[fieldsCount];
            for (int i = 0; i < fieldsCount; ++i)
            {
                mFields[i] = new Field(i, in);
            }

            int methodsCount = in.readUnsignedShort();
            mMethods         = new Method[methodsCount];
            for (int i = 0; i < methodsCount; ++i)
            {
                mMethods[i] = new Method(i, in);
            }

            int attributesCount = in.readUnsignedShort();
            mAttributes         = new Attribute[attributesCount];
            for (int i = 0; i < attributesCount; ++i)
            {
                Attribute attribute = Attribute.parse(this, in);
                mAttributes[i] = attribute;
                if (!attribute.isValid())
                {
                    mIsValid = false;
                    for (String error : attribute.getValidityErrors())
                    {
                        mValidityErrors.add("attributes[" + i + "]: " + error);
                    }
                }
            }

            in.close();
        }
        catch (IOException e)
        {
            mIsValid = false;
            mValidityErrors.add("IOException reading class file: " + e);
        }
    }

    public void store(String aFile)
               throws IOException, ClassFileException
    {
        if (!mIsValid)
        {
            throw new ClassFileException("Cannot store invalid class file");
        }

        DataOutputStream out = new DataOutputStream(new FileOutputStream(aFile));

        // Header.
        out.writeInt(mMagic);
        out.writeShort(mMinorVersion);
        out.writeShort(mMajorVersion);

        // Constants.
        out.writeShort(mConstantPool.size());
        mConstantPool.store(out);

        // Class definition.
        out.writeShort(mAccessFlags);
        out.writeShort(mThisClass.getIndex());
        out.writeShort(mSuperClass.getIndex());
        out.writeShort(mInterfaces.length);
        for (int i = 0; i < mInterfaces.length; ++i)
        {
            out.writeShort(mInterfaces[i].getIndex());
        }

        // Fields, methods and attributes.
        out.writeShort(mFields.length);
        for (int i = 0; i < mFields.length; ++i)
        {
            mFields[i].store(out);
        }

        out.writeShort(mMethods.length);
        for (int i = 0; i < mMethods.length; ++i)
        {
            mMethods[i].store(out);
        }

        out.writeShort(mAttributes.length);
        for (int i = 0; i < mAttributes.length; ++i)
        {
            mAttributes[i].store(out);
        }
        out.close();
    }

    public boolean isValid()
    {
        return mIsValid;
    }

    public List<String> getValidityErrors()
    {
        return new ArrayList<String>(mValidityErrors);
    }

    public void addValidityError(String error)
    {
        mIsValid = false;
        mValidityErrors.add(error);
    }

    public ConstantPool getConstantPool()
    {
        return mConstantPool;
    }

    public void cryptStrings()
                      throws IOException, ClassFileException
    {
        // Add crypt method to constant pool.
        ConstantPool.ConstantUtf8        methodClassName   = mConstantPool.addUtf8("utf8/Utf8");
        ConstantPool.ConstantUtf8        methodName        = mConstantPool.addUtf8("utf8");
        ConstantPool.ConstantUtf8        methodDescriptor  = mConstantPool.addUtf8("(Ljava/lang/String;)Ljava/lang/String;");
        ConstantPool.ConstantClass       methodClass       = mConstantPool.addClass(methodClassName);
        ConstantPool.ConstantNameAndType methodNameAndType = mConstantPool.addNameAndType(methodName, methodDescriptor);
        ConstantPool.ConstantMethodRef   methodRef         = mConstantPool.addMethodRef(methodClass, methodNameAndType);

        // Create byte code for instruction invoke static classfile/Utf8.cryptString:(Ljava/land/String;)Ljava/lang/String;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(ByteCode.INVOKESTATIC);
        dos.writeShort(methodRef.getIndex());
        dos.flush();
        byte[] byteCode = bos.toByteArray();
        
        // Patch code.
        for (Method method : mMethods)
        {
            System.out.println("Processing method " + method.toString());
            for (Attribute attribute : method.mAttributes)
            {
                if (attribute instanceof AttributeCode)
                {
                    AttributeCode attrCode = (AttributeCode) attribute;
                    ByteCode code = attrCode.getCode();
                    ListIterator<ByteCode.Instruction> iterator = code.getCode().listIterator();
                    while (iterator.hasNext())
                    {
                        ByteCode.Instruction instruction = iterator.next();
                        if (ByteCode.LDC == instruction.getOpcode())
                        {
                            ByteCode.Instruction.Operand op = instruction.getOperands().get(0);
                            ConstantPool.Constant constant  = mConstantPool.getByIndex(op.getConstantIndex());
                            if (constant instanceof ConstantPool.ConstantString)
                            {
                                ConstantPool.ConstantString constantString = (ConstantPool.ConstantString) constant;
                                System.out.println("=> Patching instruction " + instruction.toString());
                                int index = iterator.nextIndex();
                                code.add(index, ByteCode.INVOKESTATIC, methodRef);
                                iterator = code.getCode().listIterator(index+1);
                            }
                        }
                    }
                }
            }
        }

        // Crypt constant pool strings.
        mConstantPool.cryptStrings();
    }

    public void dump()
    {
        // Header.
        System.out.format("magic               = 0x%X\n", mMagic);
        System.out.format("minor_version       = %d\n",   mMinorVersion);
        System.out.format("major_version       = %d\n",   mMajorVersion);
        System.out.println();

        // Constants.
        System.out.println("constant_pool_count = " + mConstantPool.size());
        mConstantPool.dump();
        System.out.println();

        // Class definition.
        System.out.format("access_flags        = 0x%X\n", mAccessFlags);
        System.out.format("this_class          = #%d //%s\n", mThisClass.getIndex(), mThisClass.toString());
        System.out.format("super_class         = #%d //%s\n", mSuperClass.getIndex(), mSuperClass.toString());
        System.out.format("interfaces_count    = %d\n", mInterfaces.length);
        for (int i = 0; i < mInterfaces.length; ++i)
        {
            System.out.format("interfaces[%d]       = #%d //%s\n", i,  mInterfaces[i].getIndex(), mInterfaces[i].toString());
        }
        System.out.println();

        System.out.format( "%s %s\n", classAccessFlags(), mThisClass.toString());
        System.out.format( "  extends %s\n", mSuperClass.toString());
        if (0 != mInterfaces.length)
        {
            System.out.format("  implements %s", mInterfaces[0].toString());
            for (int i = 1; i < mInterfaces.length; ++i)
            {
                System.out.format(", %s", mInterfaces[i].toString());
            }
            System.out.println();
        }
        System.out.println();

        // Fields, methods and attributes.
        System.out.format("fields_count        = %d\n", mFields.length);
        for (int i = 0; i < mFields.length; ++i)
        {
            mFields[i].dump();
        }
        System.out.println();

        System.out.format("methods_count       = %d\n", mMethods.length);
        for (int i = 0; i < mMethods.length; ++i)
        {
            System.out.println();
            mMethods[i].dump();
        }
        System.out.println();

        System.out.format("attributes_count    = %d\n", mAttributes.length);
        for (int i = 0; i < mAttributes.length; ++i)
        {
            mAttributes[i].dump();
        }
    }

    public String classAccessFlags()
    {
        StringBuffer buffer = new StringBuffer();
        if (0 != (mAccessFlags & ACC_PUBLIC))
        {
            buffer.append("public ");
        }
        if (0 != (mAccessFlags & ACC_FINAL))
        {
            buffer.append("final ");
        }
        if (0 != (mAccessFlags & ACC_ABSTRACT))
        {
            buffer.append("abstract ");
        }
        if (0 != (mAccessFlags & ACC_INTERFACE))
        {
            buffer.append("interface ");
        }
        else
        {
            buffer.append("class ");
        }
        return buffer.toString().trim();
    }


    // ==================== FIELDS : field_info structure ====================

    private class Field
    {
        private static final int ACC_PUBLIC =    0x0001; // Declared public; may be accessed from outside its package.
        private static final int ACC_PRIVATE =   0x0002; // Declared private; usable only within the defining class.
        private static final int ACC_PROTECTED = 0x0004; // Declared protected; may be accessed within subclasses.
        private static final int ACC_STATIC =    0x0008; // Declared static.
        private static final int ACC_FINAL =     0x0010; // Declared final; no further assignment after initialization.
        private static final int ACC_VOLATILE =  0x0040; // Declared volatile; cannot be cached.
        private static final int ACC_TRANSIENT = 0x0080; // Declared transient; not written or read by a persistent object manager.        private int         index;

        // Internal data.
        private int                       mIndex;

        // field_info {
        //     u2 access_flags;
        //     u2 name_index;
        //     u2 descriptor_index;
        //     u2 attributes_count;
        //     attribute_info attributes[attributes_count];
        // }
        private int                       mAccessFlags;
        private ConstantPool.ConstantUtf8 mName;
        private ConstantPool.ConstantUtf8 mDescriptor;
        private Attribute[]               mAttributes;

        public Field(int             aIndex,
                     DataInputStream aIn)
              throws IOException
        {
            mIndex       = aIndex;
            mAccessFlags = aIn.readUnsignedShort();

            int nameIndex                      = aIn.readUnsignedShort();
            ConstantPool.Constant nameConstant = mConstantPool.getByIndex(nameIndex);
            if (nameConstant instanceof ConstantPool.ConstantUtf8)
            {
                mName = (ConstantPool.ConstantUtf8)nameConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add("fields[" + mIndex + "]: invalid name constant #"
                                    + nameIndex
                                    + ((null != nameConstant) ? (" //" + nameConstant.toString()) : ""));
            }

            int descriptorIndex                      = aIn.readUnsignedShort();
            ConstantPool.Constant descriptorConstant = mConstantPool.getByIndex(descriptorIndex);
            if (descriptorConstant instanceof ConstantPool.ConstantUtf8)
            {
                mDescriptor = (ConstantPool.ConstantUtf8)descriptorConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add("fields[" + mIndex + "]: invalid name constant #"
                                    + descriptorIndex
                                    + ((null != descriptorConstant) ? (" //" + descriptorConstant.toString()) : ""));
            }

            int attributesCount = aIn.readUnsignedShort();
            mAttributes         = new Attribute[attributesCount];
            for (int i = 0; i < attributesCount; ++i)
            {
                Attribute attribute = Attribute.parse(ClassFile.this, aIn);
                mAttributes[i] = attribute;
                if (!attribute.isValid())
                {
                    mIsValid = false;
                    for (String error : attribute.getValidityErrors())
                    {
                        mValidityErrors.add("fields[" + mIndex + "].attributes[" + i + "]: " + error);
                    }
                }
            }
        }

        public void store(DataOutputStream aOut)
                   throws IOException, ClassFileException
        {
            aOut.writeShort(mAccessFlags);
            aOut.writeShort(mName.getIndex());
            aOut.writeShort(mDescriptor.getIndex());

            aOut.writeShort(mAttributes.length);
            for (int i = 0; i < mAttributes.length; ++i)
            {
                mAttributes[i].store(aOut);
            }
        }

        public void dump()
        {
            System.out.format("fields[%d] (0x%X) = #%d:#%d\n%s\n",
                              mIndex,
                              mAccessFlags,
                              mName.getIndex(),
                              mDescriptor.getIndex(),
                              toString());

            for (int i = 0; i < mAttributes.length; ++i)
            {
                mAttributes[i].dump();
            }
        }

        public String toString()
        {
            return String.format("%s %s:%s",
                                 fieldAccessFlags(),
                                 mName.toString(),
                                 mDescriptor.toString());
        }

        public String fieldAccessFlags()
        {
            StringBuffer buffer = new StringBuffer();
            if (0 != (mAccessFlags & ACC_PUBLIC))
            {
                buffer.append("public ");
            }
            if (0 != (mAccessFlags & ACC_PRIVATE))
            {
                buffer.append("private ");
            }
            if (0 != (mAccessFlags & ACC_PROTECTED))
            {
                buffer.append("protected ");
            }
            if (0 != (mAccessFlags & ACC_STATIC))
            {
                buffer.append("static ");
            }
            if (0 != (mAccessFlags & ACC_FINAL))
            {
                buffer.append("final ");
            }
            if (0 != (mAccessFlags & ACC_VOLATILE))
            {
                buffer.append("volatile ");
            }
            if (0 != (mAccessFlags & ACC_TRANSIENT))
            {
                buffer.append("transient ");
            }
            return buffer.toString().trim();
        }
    }


    // ==================== METHODS : method_info structure ====================

    private class Method
    {
        private static final int ACC_PUBLIC       = 0x0001; // Declared public; may be accessed from outside its package.
        private static final int ACC_PRIVATE      = 0x0002; // Declared private; accessible only within the defining class.
        private static final int ACC_PROTECTED    = 0x0004; // Declared protected; may be accessed within subclasses.
        private static final int ACC_STATIC       = 0x0008; // Declared static.
        private static final int ACC_FINAL        = 0x0010; // Declared final; may not be overridden.
        private static final int ACC_SYNCHRONIZED = 0x0020; // Declared synchronized; invocation is wrapped in a monitor lock.
        private static final int ACC_NATIVE       = 0x0100; // Declared native; implemented in a language other than Java.
        private final static int ACC_ABSTRACT     = 0x0400; // Declared abstract; no implementation is provided.
        private static final int ACC_STRICT       = 0x0800; // Declared strictfp; floating-point mode is FP-strict

        // Internal data.
        private int                       mIndex;

        // method_info {
        //     u2 access_flags;
        //     u2 name_index;
        //     u2 descriptor_index;
        //     u2 attributes_count;
        //     attribute_info attributes[attributes_count];
        // }
        private int                       mAccessFlags;
        private ConstantPool.ConstantUtf8 mName;
        private ConstantPool.ConstantUtf8 mDescriptor;
        private Attribute[]               mAttributes;

        public Method(int             aIndex,
                      DataInputStream aIn)
               throws IOException
        {
            mIndex       = aIndex;
            mAccessFlags = aIn.readUnsignedShort();

            int nameIndex                      = aIn.readUnsignedShort();
            ConstantPool.Constant nameConstant = mConstantPool.getByIndex(nameIndex);
            if (nameConstant instanceof ConstantPool.ConstantUtf8)
            {
                mName = (ConstantPool.ConstantUtf8)nameConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add("methods[" + mIndex + "]: invalid name constant #"
                                    + nameIndex
                                    + ((null != nameConstant) ? (" //" + nameConstant.toString()) : ""));
            }

            int descriptorIndex                      = aIn.readUnsignedShort();
            ConstantPool.Constant descriptorConstant = mConstantPool.getByIndex(descriptorIndex);
            if (descriptorConstant instanceof ConstantPool.ConstantUtf8)
            {
                mDescriptor                              = (ConstantPool.ConstantUtf8)descriptorConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add("methods[" + mIndex + "]: invalid descriptor constant #"
                                    + descriptorIndex
                                    + ((null != descriptorConstant) ? (" //" + descriptorConstant.toString()) : ""));
            }

            int attributesCount = aIn.readUnsignedShort();
            mAttributes         = new Attribute[attributesCount];
            for (int i = 0; i < attributesCount; ++i)
            {
                Attribute attribute = Attribute.parse(ClassFile.this, aIn);
                mAttributes[i] = attribute;
                if (!attribute.isValid())
                {
                    mIsValid = false;
                    for (String error : attribute.getValidityErrors())
                    {
                        mValidityErrors.add("methods[" + mIndex + "].attributes[" + i + "]: " + error);
                    }
                }
            }
        }

        public void store(DataOutputStream aOut)
                   throws IOException, ClassFileException
        {
            aOut.writeShort(mAccessFlags);
            aOut.writeShort(mName.getIndex());
            aOut.writeShort(mDescriptor.getIndex());

            aOut.writeShort(mAttributes.length);
            for (int i = 0; i < mAttributes.length; ++i)
            {
                mAttributes[i].store(aOut);
            }
        }

        public void dump()
        {
            System.out.format("methods[%d] (0x%X) = #%d:#%d\n%s\n",
                              mIndex,
                              mAccessFlags,
                              mName.getIndex(),
                              mDescriptor.getIndex(),
                              toString());

            for (int i = 0; i < mAttributes.length; ++i)
            {
                mAttributes[i].dump();
            }
        }

        public String toString()
        {
            return String.format("%s %s:%s",
                                 methodAccessFlags(),
                                 mName.toString(),
                                 mDescriptor.toString());
        }

        public String methodAccessFlags()
        {
            StringBuffer buffer = new StringBuffer();
            if (0 != (mAccessFlags & ACC_PUBLIC))
            {
                buffer.append("public ");
            }
            if (0 != (mAccessFlags & ACC_PRIVATE))
            {
                buffer.append("private ");
            }
            if (0 != (mAccessFlags & ACC_PROTECTED))
            {
                buffer.append("protected ");
            }
            if (0 != (mAccessFlags & ACC_STATIC))
            {
                buffer.append("static ");
            }
            if (0 != (mAccessFlags & ACC_FINAL))
            {
                buffer.append("final ");
            }
            if (0 != (mAccessFlags & ACC_SYNCHRONIZED))
            {
                buffer.append("synchronized ");
            }
            if (0 != (mAccessFlags & ACC_NATIVE))
            {
                buffer.append("native ");
            }
            if (0 != (mAccessFlags & ACC_ABSTRACT))
            {
                buffer.append("abstract ");
            }
            if (0 != (mAccessFlags & ACC_STRICT))
            {
                buffer.append("strict ");
            }
            return buffer.toString().trim();
        }
    }
}
