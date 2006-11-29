/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeSourceFile.java
 *
 * Created on 26 novembre 2006, 1:02
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
public class AttributeSourceFile extends Attribute
{
    // SourceFile_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 sourcefile_index;
    // }
    private ConstantPool.ConstantUtf8 mSourceFile;

    public AttributeSourceFile(ClassFile                 aClass,
                               ConstantPool.ConstantUtf8 aName,
                               byte[]                    aInfo)
                        throws IOException
    {
        super(aClass, aName);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aInfo));

        int sourceFileIndex = in.readUnsignedShort();
        ConstantPool.Constant sourceFileConstant = aClass.getConstantPool().getByIndex(sourceFileIndex);
        if (sourceFileConstant instanceof ConstantPool.ConstantUtf8)
        {
            mSourceFile = (ConstantPool.ConstantUtf8) sourceFileConstant;
        }
        else
        {
            mIsValid = false;
            mValidityErrors.add(  toString() + ": invalid sourcefile_index #"
                                + sourceFileIndex
                                + ((null != sourceFileConstant) ? (" //" + sourceFileConstant.toString()) : ""));
        }

        in.close();
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.writeShort(mSourceFile.getIndex());
    }

    public int getLength()
    {
        return 8;
    }

    public void dump()
    {
        super.dump();
        System.out.format("  Source file: %s\n", mSourceFile.toString());
    }
}
