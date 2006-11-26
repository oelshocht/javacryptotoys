/* Copyright (c) 2006 Olivier Elshocht
 *
 * AttributeInvalid.java
 *
 * Created on 26 novembre 2006, 17:57
 */

package classfile;

import java.io.*;

/**
 *
 * @author Olivier Elshocht
 */
/* packate */ class AttributeInvalid extends Attribute
{
    // attribute_info {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u1 info[attribute_length];
    // }
    private byte[] mInfo;

    public AttributeInvalid(ClassFile aClass,
                            int       aNameIndex,
                            byte[]    aInfo)
    {
        super(aClass, aNameIndex);
        mInfo      = aInfo;

        mIsValid = false;
        mValidityErrors.add("invalid attribute name_index #" + aNameIndex);
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
