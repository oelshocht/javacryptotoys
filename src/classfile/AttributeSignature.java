/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeSignature.java
 *
 * Created on 29 novembre 2006, 17:25
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
/* package */ class AttributeSignature extends Attribute
{
    // Signature_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 signature_index;
    // }
    private ConstantPool.ConstantUtf8 mSignature;

    public AttributeSignature(ClassFile                 aClass,
                              ConstantPool.ConstantUtf8 aName,
                              byte[]                    aInfo)
                       throws IOException
    {
        super(aClass, aName);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        int signatureIndex = in.readUnsignedShort();
        ConstantPool.Constant signatureConstant = aClass.getConstantPool().getByIndex(signatureIndex);
        if (signatureConstant instanceof ConstantPool.ConstantUtf8)
        {
            mSignature = (ConstantPool.ConstantUtf8) signatureConstant;
            mSignature.incRefCount();
        }
        else
        {
            mIsValid = false;
            mValidityErrors.add(  toString() + ": invalid signature_index #"
                                + signatureIndex
                                + ((null != signatureConstant) ? (" //" + signatureConstant.toString()) : ""));
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mSignature.getIndex());
    }

    public int getLength()
    {
        return 8;
    }

    public void dump()
    {
        super.dump();
        System.out.format("  Signature: %s\n", mSignature.toString());
    }
}
