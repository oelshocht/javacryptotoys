/* Copyright (c) 2006 Olivier Elshocht
 *
 * Utf8.java
 *
 * Created on 17 novembre 2006, 0:42
 */

package classfile;

import java.io.*;
import java.util.*;

/**
 *
 * @author Olivier
 */
public class Utf8 {

    public final static String cryptString(String s) {

        try {
            // Write string to byte array in modified UTF-8 format.
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);
            out.writeUTF(s);
            out.flush();
            byte[] buffer = bos.toByteArray();

            // Read length.
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer));
            int length = in.readUnsignedShort();

            // Crypt string.
            Random rnd = new Random(length);
            for (int i = 2; i < length+2; ++i){
                buffer[i] ^= rnd.nextInt() & 0x0F;
            }

            // Return crypted string.
            in = new DataInputStream(new ByteArrayInputStream(buffer));
            return in.readUTF();
        }
        catch (IOException e) {
            throw new RuntimeException("String operation exception");
        }
    }
}
