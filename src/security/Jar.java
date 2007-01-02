/* Copyright (c) 2007 Olivier Elshocht
 *
 * Jar.java
 *
 * Created on 1 janvier 2007, 20:50
 */

package security;

import java.io.*;
import java.util.*;
import java.util.jar.*;

import classfile.*;

/**
 *
 * @author Olivier Elshocht
 */
public class Jar
{
    // PUBLIC INTERFACE

    public static void list(String aFile)
    {
        try
        {
            JarInputStream in = new JarInputStream(new BufferedInputStream(new FileInputStream(aFile)));
            JarEntry entry = null;

            while (null != (entry = in.getNextJarEntry()))
            {
                System.out.println(entry.getName());
            }

            in.close();
        }
        catch (IOException e)
        {
            System.out.println("Exception reading jar file: " + e);
        }
    }

    public static void copy(String aSourceFile, String aDestFile)
    {
        try
        {
            JarInputStream in = new JarInputStream(new BufferedInputStream(new FileInputStream(aSourceFile)));
            Manifest manifest = in.getManifest();
            JarOutputStream out = null;

            if (null == manifest)
            {
                System.out.println("Creating jar " + aDestFile + " with no manifest ...");
                out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(aDestFile)));
            }
            else
            {
                System.out.println("Creating jar " + aDestFile + " with manifest " + manifest + " ...");
                out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(aDestFile)), manifest);
            }

            JarEntry entry = null;
            while (null != (entry = in.getNextJarEntry()))
            {
                System.out.println("Adding entry " + entry.getName() + " ...");
                out.putNextEntry(entry);
                byte[] content = readStream(in);
                out.write(content, 0, content.length);
            }

            in.close();
            out.close();
        }
        catch (IOException e)
        {
            System.out.println("Exception copying jar file: " + e);
        }
    }

    public static void crypt(String aSourceFile, String aDestFile)
    {
        try
        {
            // Create new jar file.
            JarInputStream in = new JarInputStream(new BufferedInputStream(new FileInputStream(aSourceFile)));
            Manifest manifest = in.getManifest();
            JarOutputStream out = null;

            if (null == manifest)
            {
                System.out.println("Creating jar " + aDestFile + " with no manifest ...");
                out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(aDestFile)));
            }
            else
            {
                System.out.println("Creating jar " + aDestFile + " with manifest " + manifest + " ...");
                out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(aDestFile)), manifest);
            }

            // Add directory entry for utf8 package.
            System.out.println("Adding utf8/ ...");
            out.putNextEntry(new JarEntry("utf8/"));

            // Crypt/copy all entries.
            JarEntry entry = null;
            while (null != (entry = in.getNextJarEntry()))
            {
                String name = entry.getName();
                byte[] content = readStream(in);

                if (name.endsWith(".class"))
                {
                    System.out.println("Crypting entry " + name + " ...");

                    // Save class to temp file.
                    File tmpFile = File.createTempFile("tmp", ".class");
                    OutputStream tmpOutputStream = new FileOutputStream(tmpFile);
                    tmpOutputStream.write(content);
                    tmpOutputStream.close();

                    // Read class file and check validity.
                    ClassFile classFile = new ClassFile(tmpFile.getAbsolutePath());
                    List<String> errors = classFile.getValidityErrors();
                    if ((!classFile.isValid()) || (0 != errors.size()))
                    {
                        System.out.println("Cannot crypt invalid class file " + name);
                        System.out.println("Validity errors: ");
                        for (String error : errors)
                        {
                            System.out.println(error);
                        }
                        break;
                    }

                    // Crypt class file.
                    try
                    {
                        classFile.cryptStrings();
                        errors = classFile.getValidityErrors();
                        if ((!classFile.isValid()) || (0 != errors.size()))
                        {
                            System.out.println("Cannot save invalid crypted class file " + name);
                            System.out.println("Validity errors: ");
                            for (String error : errors)
                            {
                                System.out.println(error);
                            }
                            break;
                        }
                    }
                    catch (ClassFileException e)
                    {
                        System.out.println("Exception crypting class " + name + ": " + e);
                        break;
                    }

                    // Save crypted class file to temp file.
                    try
                    {
                        classFile.store(tmpFile.getAbsolutePath());
                    }
                    catch (ClassFileException e)
                    {
                        System.out.println("Exception storing crypted class " + name + ": " + e);
                        break;
                    }

                    // Read crypted class file.
                    InputStream tmpInputStream = new FileInputStream(tmpFile);
                    content = readStream(tmpInputStream);
                    tmpInputStream.close();

                    // Add crypted class file to jar.
                    out.putNextEntry(new JarEntry(name));
                    out.write(content, 0, content.length);

                    // Delete temp file.
                    tmpFile.delete();
                }
                else
                {
                    System.out.println("Copying entry " + name + " ...");
                    out.putNextEntry(entry);
                    out.write(content, 0, content.length);
                }
            }

            // Add entry for Utf8 class
            System.out.println("Adding utf8/Utf8.class ...");
            InputStream tmpInputStream = Jar.class.getResourceAsStream("/utf8/Utf8.class");
            if (null == tmpInputStream)
            {
                System.out.println("Resource /utf8/Utf8.class not found");
            }
            else
            {
                byte[] buffer = readStream(tmpInputStream);
                tmpInputStream.close();
                out.putNextEntry(new JarEntry("utf8/Utf8.class"));
                out.write(buffer);
            }

            // Close streams.
            in.close();
            out.close();
        }
        catch (IOException e)
        {
            System.out.println("Exception copying jar file: " + e);
        }
    }


    // PRIVATE IMPLEMENTATION

    private static byte[] readStream(InputStream in)
                             throws IOException
    {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        OutputStream out = new BufferedOutputStream(byteArray);
        byte[] buffer = new byte[4000];
        int size;

        while (0 < (size = in.read(buffer, 0, 4000)))
        {
            out.write(buffer, 0,  size);
        }
        out.flush();

        return byteArray.toByteArray();
    }
}
