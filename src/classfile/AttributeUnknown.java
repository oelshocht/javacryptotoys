/* Copyright (c) 2006 Olivier Elshocht
 *
 * Attribute.java
 *
 * Created on 25 novembre 2006, 20:00
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
/* package */ class AttributeUnknown extends Attribute
{
    // attribute_info {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u1 info[attribute_length];
    // }
    private byte[]                    mInfo;

    public AttributeUnknown(ClassFile                 aClass,
                            ConstantPool.ConstantUtf8 aName,
                            byte[]                    aInfo)
    {
        super(aClass, aName);
        mInfo = aInfo;

        mIsValid = false;
        mValidityErrors.add("unknown attribute " + aName.toString());
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        super.store(aOut);
        aOut.write(mInfo);
    }

    public int getLength()
    {
        int length = 0;
        length += 2;                                  // u2 attribute_name_index;
        length += 4;                                  // u4 attribute_length;
        length += mInfo.length;                       // u1 info[attribute_length];

        return length;
    }
}
