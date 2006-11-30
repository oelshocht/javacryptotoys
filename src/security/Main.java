/* Copyright (c)2006 Olivier Elshocht
 *
 * Main.java
 *
 * Created on 1 novembre 2006, 17:09
 */

package security;

import classfile.ClassFile;
import java.io.*;
import java.util.*;
import test.*;

/**
 *
 * @author Olivier Elshocht
 */
public class Main implements Cloneable
{
    private final int mTestField = 0xDEAD;

    public Main()
    {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            // Parse arguments and options.
            boolean isValidCommand = false;

            if (0 != args.length)
            {
                if (args[0].equals("genkey"))
                {
                    if (2 == args.length)
                    {
                        isValidCommand = true;
                        Dsa.generateKeyPair(args[1]);
                    }
                }
                else if (args[0].equals("sign"))
                {
                    if (3 == args.length)
                    {
                        isValidCommand = true;
                        Dsa.sign(args[1], args[2]);
                    }
                }
                else if (args[0].equals("verify"))
                {
                    if (3 == args.length)
                    {
                        isValidCommand = true;                        
                        byte[] data = Dsa.verify(args[1], args[2]);
                        System.out.println("Data: " + data);
                    }
                }
                else if (args[0].equals("check"))
                {
                    if (2 == args.length)
                    {
                        isValidCommand = true;
                        byte[] securityPubKey = { 48, -126, 1, -72, 48, -126, 1, 44, 6, 7, 42, -122, 72, -50, 56, 4, 1, 48, -126, 1, 31, 2, -127, -127, 0, -3, 127, 83, -127, 29, 117, 18, 41, 82, -33, 74, -100, 46, -20, -28, -25, -10, 17, -73, 82, 60, -17, 68, 0, -61, 30, 63, -128, -74, 81, 38, 105, 69, 93, 64, 34, 81, -5, 89, 61, -115, 88, -6, -65, -59, -11, -70, 48, -10, -53, -101, 85, 108, -41, -127, 59, -128, 29, 52, 111, -14, 102, 96, -73, 107, -103, 80, -91, -92, -97, -97, -24, 4, 123, 16, 34, -62, 79, -69, -87, -41, -2, -73, -58, 27, -8, 59, 87, -25, -58, -88, -90, 21, 15, 4, -5, -125, -10, -45, -59, 30, -61, 2, 53, 84, 19, 90, 22, -111, 50, -10, 117, -13, -82, 43, 97, -41, 42, -17, -14, 34, 3, 25, -99, -47, 72, 1, -57, 2, 21, 0, -105, 96, 80, -113, 21, 35, 11, -52, -78, -110, -71, -126, -94, -21, -124, 11, -16, 88, 28, -11, 2, -127, -127, 0, -9, -31, -96, -123, -42, -101, 61, -34, -53, -68, -85, 92, 54, -72, 87, -71, 121, -108, -81, -69, -6, 58, -22, -126, -7, 87, 76, 11, 61, 7, -126, 103, 81, 89, 87, -114, -70, -44, 89, 79, -26, 113, 7, 16, -127, -128, -76, 73, 22, 113, 35, -24, 76, 40, 22, 19, -73, -49, 9, 50, -116, -56, -90, -31, 60, 22, 122, -117, 84, 124, -115, 40, -32, -93, -82, 30, 43, -77, -90, 117, -111, 110, -93, 127, 11, -6, 33, 53, 98, -15, -5, 98, 122, 1, 36, 59, -52, -92, -15, -66, -88, 81, -112, -119, -88, -125, -33, -31, 90, -27, -97, 6, -110, -117, 102, 94, -128, 123, 85, 37, 100, 1, 76, 59, -2, -49, 73, 42, 3, -127, -123, 0, 2, -127, -127, 0, -97, -1, 8, 40, -41, 40, 24, 73, 16, -91, 24, 55, -33, -128, 79, 87, 29, 22, -94, 24, 106, 53, -96, -119, 55, 5, 120, -59, -42, 11, -57, -123, 17, 87, 76, -50, -76, -60, 99, 26, 79, 34, 75, 101, 118, 121, -94, 86, -34, 79, 93, 113, 102, -7, -108, 99, -92, -51, 74, 25, 55, -69, -100, -66, -29, -8, 55, -85, 62, -20, -48, -56, 98, -29, 4, -31, 53, 16, 78, 60, 105, -14, -4, 91, 47, 45, 126, 106, 11, 76, 122, 86, -81, -128, 75, -85, 86, -113, 72, 104, 43, -25, 49, 31, 24, 88, -112, 41, -109, 27, 24, 29, 126, 110, -119, -11, -43, 45, -106, -47, 87, 12, -33, -101, 70, 49, 123, 48 };
                        Dsa.verify(securityPubKey, args[1]);
                    }
                }
                else if ((args[0].equals("class")) && (1 != args.length))
                {
                    if (2 == args.length)
                    {
                        isValidCommand = true;
                        String filename = args[1];

                        System.out.println("Loading class file " + filename + "...");
                        ClassFile classFile = new ClassFile(args[1]);
                        System.out.println();

                        System.out.println("Validity errors:");
                        dump(classFile.getValidityErrors());
                        System.out.println();

                        System.out.println("Dumping class file...");
                        classFile.dump();
                        System.out.println();

                        System.out.println("Validity errors:");
                        dump(classFile.getValidityErrors());
                        System.out.println();
                    }
                    else if (args[1].equals("check"))
                    {
                        isValidCommand = true;
                        String filename = args[2];

                        System.out.println("Loading class file " + filename + "...");
                        ClassFile classFile = new ClassFile(filename);
                        System.out.println();

                        System.out.println("Validity errors:");
                        dump(classFile.getValidityErrors());
                        System.out.println();
                    }
                    else if (args[1].equals("crypt"))
                    {
                        isValidCommand = true;
                        String filename = args[2];

                        System.out.println("Loading class file " + filename + "...");
                        ClassFile classFile = new ClassFile(filename);
                        System.out.println();

                        System.out.println("Validity errors:");
                        dump(classFile.getValidityErrors());
                        System.out.println();

                        if (!classFile.isValid())
                        {
                            System.out.println("Error: cannot crypt invalid class file");
                        }
                        else
                        {
                            System.out.println("Crypting class file strings...");
                            classFile.cryptStrings();
                            System.out.println();

                            System.out.println("Saving class file..");
                            classFile.store(filename);
                            System.out.println();
                        }
                    }
                }
                else if (args[0].equals("test"))
                {
                    isValidCommand = true;
                    Test.main(args);
                }
            }

            if (!isValidCommand)
            {
                // If arguments are not valid, print usage.
                System.out.println("Usage: security COMMAND [OPTION...]");
                System.out.println();
                System.out.println("Commands:");
                System.out.println("    genkey KEYID                Generate a DSA key pair.");
                System.out.println("    sign   KEYID FILE           Sign file with specified private key.");
                System.out.println("    verify KEYID FILE           Verify file signature with specified public key.");
                System.out.println("    check  FILE                 Verify file signature with default public key.");
                System.out.println();
                System.out.println("    class CLASSFILE             Dump the specified class file.");
                System.out.println("    class check CLASSFILE       Check the specified class file.");
                System.out.println("    class crypt CLASSFILE       Encrypt string constants.");
                System.out.println();
                System.out.println("    test                        Run test suite.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void dump(List<String> list)
    {
        if (0 == list.size())
        {
            System.out.println("None.");
        }
        else
        {
            for (String item : list)
            {
                System.out.println(item);
            }
        }
    }
}
