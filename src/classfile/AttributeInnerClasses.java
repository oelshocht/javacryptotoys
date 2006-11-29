/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeInnerClasses.java
 *
 * Created on 29 novembre 2006, 17:52
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
/* package */ class AttributeInnerClasses extends Attribute
{
    // InnerClasses_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 number_of_classes;
    //     {    u2 inner_class_info_index;
    //          u2 outer_class_info_index;
    //          u2 inner_name_index;
    //          u2 inner_class_access_flags;
    //     } classes[number_of_classes];
    // }
    private InnerClassInfo[] mInnerClasses;

    public AttributeInnerClasses(ClassFile                 aClass,
                                 ConstantPool.ConstantUtf8 aName,
                                 byte[]                    aInfo)
                          throws IOException
    {
        super(aClass, aName);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        int nbClasses= in.readUnsignedShort();
        mInnerClasses = new InnerClassInfo[nbClasses];
        for (int i = 0; i < nbClasses; ++i)
        {
            mInnerClasses[i] = new InnerClassInfo(in);
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mInnerClasses.length);
        for (int i = 0; i < mInnerClasses.length; ++i)
        {
            mInnerClasses[i].store(aOut);
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
            length += (  2                                //     {    u2 inner_class_info_index;
                       + 2                                //          u2 outer_class_info_index;
                       + 2                                //          u2 inner_name_index;
                       + 2                                //          u2 inner_class_access_flags;
                          ) * mInnerClasses.length;       //     } classes[number_of_classes];
        }

        return length;
    }

    public void dump()
    {
        super.dump();
        for (int i = 0; i < mInnerClasses.length; ++i)
        {
            mInnerClasses[i].dump();
        }
    }


    // INNER CLASSES : classes structure

    private class InnerClassInfo
    {
        private static final int ACC_PUBLIC     = 0x0001; // Marked or implicitly public in source.
        private static final int ACC_PRIVATE    = 0x0002; // Marked private in source.
        private static final int ACC_PROTECTED  = 0x0004; // Marked protected in source.
        private static final int ACC_STATIC     = 0x0008; // Marked or implicitly static in source.
        private static final int ACC_FINAL      = 0x0010; // Marked final in source.
        private static final int ACC_INTERFACE  = 0x0200; // Was an interface in source.
        private static final int ACC_ABSTRACT   = 0x0400; // Marked or implicitly abstract in source.
        private static final int ACC_SYNTHETIC  = 0x1000; // Declared synthetic; Not present in the source code.
        private static final int ACC_ANNOTATION = 0x2000; // Declared as an annotation type.
        private static final int ACC_ENUM       = 0x4000; // Declared as an enum type.

        private ConstantPool.ConstantClass mInnerClass;
        private ConstantPool.ConstantClass mOuterClass;
        private ConstantPool.ConstantUtf8  mName;
        private int                        mAccessFlags;

        public InnerClassInfo(DataInputStream aIn)
                       throws IOException
        {
            int innerClassIndex                      = aIn.readUnsignedShort();
            if (0 == innerClassIndex)
            {
                // No inner class.
                mInnerClass = null;
            }
            else
            {
                ConstantPool.Constant innerClassConstant = mClass.getConstantPool().getByIndex(innerClassIndex);
                if (innerClassConstant instanceof ConstantPool.ConstantClass)
                {
                    mInnerClass = (ConstantPool.ConstantClass) innerClassConstant;
                }
                else
                {
                    mIsValid = false;
                    mValidityErrors.add(AttributeInnerClasses.this.toString()
                                        + ".classes: invalid inner class constant #"
                                        + innerClassIndex
                                        + ((null != innerClassConstant) ? (" //" + innerClassConstant.toString()) : ""));
                }
            }

            int outerClassIndex = aIn.readUnsignedShort();
            if (0 == outerClassIndex)
            {
                // No outer class.
                mOuterClass = null;
            }
            else
            {
                ConstantPool.Constant outerClassConstant = mClass.getConstantPool().getByIndex(outerClassIndex);
                if (outerClassConstant instanceof ConstantPool.ConstantClass)
                {
                    mOuterClass = (ConstantPool.ConstantClass) outerClassConstant;
                }
                else
                {
                    mIsValid = false;
                    mValidityErrors.add(AttributeInnerClasses.this.toString()
                                        + ".classes: invalid outer class constant #"
                                        + outerClassIndex
                                        + ((null != outerClassConstant) ? (" //" + outerClassConstant.toString()) : ""));
                }
            }

            int nameIndex                      = aIn.readUnsignedShort();
            if (0 == nameIndex)
            {
                // Anonymous inner class.
                mName = null;
            }
            else
            {
                ConstantPool.Constant nameConstant = mClass.getConstantPool().getByIndex(nameIndex);
                if (nameConstant instanceof ConstantPool.ConstantUtf8)
                {
                    mName = (ConstantPool.ConstantUtf8) nameConstant;
                }
                else
                {
                    mIsValid = false;
                    mValidityErrors.add(AttributeInnerClasses.this.toString()
                                        + ".classes: invalid name constant #"
                                        + nameIndex
                                        + ((null != nameConstant) ? (" //" + nameConstant.toString()) : ""));
                }
            }

            mAccessFlags = aIn.readUnsignedShort();
        }

        public void store(DataOutputStream aOut)
                   throws IOException
        {
            aOut.writeShort((null != mInnerClass) ? mInnerClass.getIndex() : 0);
            aOut.writeShort((null != mOuterClass) ? mOuterClass.getIndex() : 0);
            aOut.writeShort((null != mName) ? mName.getIndex() : 0);
            aOut.writeShort(mAccessFlags);
        }

        public void dump()
        {
            System.out.format("  Inner class: %s %s: %s.%s\n",
                              innerClassAccessFlags(),
                              ((null != mName) ? mName.toString() : "(anonymous)"),
                              ((null != mOuterClass) ? mOuterClass.toString() : "(no outer class)"),
                              ((null != mInnerClass) ? mInnerClass.toString() : "(no inner class)"));
        }

        public String innerClassAccessFlags()
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
            if (0 != (mAccessFlags & ACC_SYNTHETIC))
            {
                buffer.append("(synthetic) ");
            }
            if (0 != (mAccessFlags & ACC_ANNOTATION))
            {
                buffer.append("(annotation) ");
            }
            if (0 != (mAccessFlags & ACC_ENUM))
            {
                buffer.append("(enum) ");
            }
            return buffer.toString().trim();
        }
    }
}
