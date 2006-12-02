/* Copyright (c) 2006 Olivier Elshocht
 *
 * Utf8.java
 *
 * Created on 17 novembre 2006, 0:42
 */

package utf8;

import java.io.*;
import java.util.*;

/**
 *
 * @author Olivier Elshocht
 */
public class Utf8
{
    public final static String utf8(String aString)
    {
        char[] characters = aString.toCharArray();
        Random rnd = new Random(characters.length);
        for (int i = 0; i < characters.length; ++i)
        {
            characters[i] ^= rnd.nextInt();
        }
        return new String(characters);
    }
}
