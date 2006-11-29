/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeEnclosingMethod.java
 *
 * Created on 29 novembre 2006, 21:09
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
/* packate */ class AttributeEnclosingMethod extends Attribute
{
    // EnclosingMethod_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 class_index
    //     u2 method_index;
    // }
    private ConstantPool.ConstantClass       mClass;
    private ConstantPool.ConstantNameAndType mMethod;

    public AttributeEnclosingMethod(ClassFile                 aClass,
                                    ConstantPool.ConstantUtf8 aName,
                                    byte[]                    aInfo)
                             throws IOException
    {
        super(aClass, aName);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        int classIndex                      = in.readUnsignedShort();
        ConstantPool.Constant classConstant = aClass.getConstantPool().getByIndex(classIndex);
        if (classConstant instanceof ConstantPool.ConstantClass)
        {
            mClass = (ConstantPool.ConstantClass) classConstant;
        }
        else
        {
            mIsValid = false;
            mValidityErrors.add(  toString() + "invalid class constant #"
                                + classIndex
                                + ((null != classConstant) ? (" //" + classConstant.toString()) : ""));
        }

        int methodIndex = in.readUnsignedShort();
        if (0 == methodIndex)
        {
            // No enclosing method.
            mMethod = null;
        }
        else
        {
            ConstantPool.Constant methodConstant = aClass.getConstantPool().getByIndex(methodIndex);
            if (methodConstant instanceof ConstantPool.ConstantNameAndType)
            {
                mMethod = (ConstantPool.ConstantNameAndType) methodConstant;
            }
            else
            {
                mIsValid = false;
                mValidityErrors.add(  toString() + "invalid method constant #"
                                    + methodIndex
                                    + ((null != methodConstant) ? (" //" + methodConstant.toString()) : ""));
            }
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mClass.getIndex());
        aOut.writeShort((null != mMethod) ? mMethod.getIndex() : 0);
    }

    public int getLength()
    {
        return 10;
    }

    public void dump()
    {
        super.dump();
        System.out.format("  Enclosing method: %s.%s\n",
                          mClass.toString(),
                          ((null != mMethod) ? mMethod.toString() : "(no enclosing method)"));
    }
}
