/* Copyright (c) 2006 Olivier Elshocht
 *
 * Dsa.java
 *
 * Created on 1 novembre 2006, 17:27
 */

package security;

import java.io.*;
import java.security.*;
import java.security.spec.*;

/**
 *
 * @author Olivier Elshocht
 */
public class Dsa
{
    public static void generateKeyPair(String aKeyId)
    {
        System.out.println("Generating 1024-bit DSA key pair " + aKeyId + "...");

        try
        {
            // Generate key pair.
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);

            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey privKey = keyPair.getPrivate();
            PublicKey pubKey = keyPair.getPublic();
            byte[] encodedPrivKey = privKey.getEncoded();
            byte[] encodedPubKey = pubKey.getEncoded();

            // Save encoded keys to file.
            FileOutputStream privFos = new FileOutputStream(aKeyId + ".privkey");
            privFos.write(encodedPrivKey);
            privFos.close();
            FileOutputStream pubFos = new FileOutputStream(aKeyId + ".pubkey");
            pubFos.write(encodedPubKey);
            pubFos.close();

            // Save encoded keys to Java source code.
            PrintWriter privWriter = new PrintWriter(aKeyId + ".privkey.java");
            privWriter.print("byte[] " + aKeyId + "PrivKey = { " + encodedPrivKey[0]);
            for (int i = 1; i < encodedPrivKey.length; ++i)
            {
                privWriter.print(", " + encodedPrivKey[i]);
            }
            privWriter.println(" };");
            privWriter.close();

            PrintWriter pubWriter = new PrintWriter(aKeyId + ".pubkey.java");
            pubWriter.print("byte[] " + aKeyId + "PubKey = { " + encodedPubKey[0]);
            for (int i = 1; i < encodedPubKey.length; ++i)
            {
                pubWriter.print(", " + encodedPubKey[i]);
            }
            pubWriter.println(" };");
            pubWriter.close();

            // Print some info.
            System.out.println("Private key algorithm: " + privKey.getAlgorithm());
            System.out.println("Private key format: " + privKey.getFormat());
            System.out.println("Public key algorithm: " + pubKey.getAlgorithm());
            System.out.println("Public key format: " + pubKey.getFormat());
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public static void sign(String aKeyId,
                            String aFilename)
    {
        System.out.println("Signing file " + aFilename + " with key " + aKeyId + "...");

        try
        {
            // Read encoded private key.
            FileInputStream privFis = new FileInputStream(aKeyId + ".privkey");
            byte[] encodedPrivKey = new byte[privFis.available()];
            privFis.read(encodedPrivKey);
            privFis.close();

            // Decode private key.
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encodedPrivKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

            // Read data.
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(aFilename));
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            OutputStream out = new BufferedOutputStream(byteArray);
            byte[] buffer = new byte[4000];
            int size;

            while (0 < (size = in.read(buffer)))
            {
                out.write(buffer, 0,  size);
            }
            out.flush();
            in.close();
            byte[] data = byteArray.toByteArray();

            // Sign the data.
            Signature signature = Signature.getInstance("SHA1withDSA", "SUN");
            signature.initSign(privKey);
            signature.update(data);
            byte[] encodedSignature = signature.sign();
            int signatureSize = encodedSignature.length;

            // Save signed data to file.
            FileOutputStream dataFos = new FileOutputStream(aFilename + "." + aKeyId + ".sign");
            dataFos.write(signatureSize & 0xFF);
            dataFos.write((signatureSize >> 8) & 0xFF);
            dataFos.write((signatureSize >> 16) & 0xFF);
            dataFos.write((signatureSize >> 24) & 0xFF);
            dataFos.write(encodedSignature);
            dataFos.write(data);
            dataFos.close();

            // Save signed data to Java source code.
            PrintWriter dataWriter = new PrintWriter(aFilename + "." + aKeyId + ".sign.java");
            dataWriter.print("byte[] " + aKeyId + "SignedData = { "
                             + (signatureSize & 0xFF) + ", "
                             + ((signatureSize >> 8) & 0xFF) + ", "
                             + ((signatureSize >> 16) & 0xFF) + ", "
                             + ((signatureSize >> 24) & 0xFF));
            for (int i = 0; i < encodedSignature.length; ++i)
            {
                dataWriter.print(", " + encodedSignature[i]);
            }
            for (int i = 0; i < data.length; ++i)
            {
                dataWriter.print(", " + data[i]);
            }
            dataWriter.println(" };");
            dataWriter.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public static byte[] verify(String aKeyId,
                                String aFilename)
    {
        byte[] data = null;

        try
        {
            System.out.println("Verifying signature of file " + aFilename + "...");
            InputStream signedData = new BufferedInputStream(new FileInputStream(aFilename));
            data = verify(aKeyId, signedData);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return data;
    }

    public static byte[] verify(byte[] aEncodedPubKey,
                                String aFilename)
    {

        byte[] data = null;

        try
        {
            System.out.println("Verifying signature of file " + aFilename + "...");
            InputStream signedData = new BufferedInputStream(new FileInputStream(aFilename));
            data = verify(aEncodedPubKey, signedData);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return data;
    }

    public static byte[] verify(String      aKeyId,
                                InputStream aSignedData)
    {
        byte[] data = null;

        try
        {
            // Read encoded public key.
            System.out.println("Reading public key " + aKeyId + "...");
            InputStream pubFis = new BufferedInputStream(new FileInputStream(aKeyId + ".pubkey"));
            byte[] encodedPubKey = new byte[pubFis.available()];
            pubFis.read(encodedPubKey);
            pubFis.close();

            data = verify(encodedPubKey, aSignedData);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return data;
    }

    public static byte[] verify(byte[]      aEncodedPubKey,
                                InputStream aSignedData)
    {
        byte[] data = null;

        try
        {
            // Decode public key.
            System.out.println("Decoding public key...");
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(aEncodedPubKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

            data = verify(pubKey, aSignedData);
        }
        catch (Exception e)
        {
            System.out.println(e);            
        }

        return data;
    }

    public static byte[] verify(PublicKey aPubKey,
                                InputStream aSignedData)
    {
        byte[] data = null;

        try
        {
            // Read signature.
            int signatureSize = 0;

            signatureSize = aSignedData.read();
            signatureSize += aSignedData.read() << 8;
            signatureSize += aSignedData.read() << 16;
            signatureSize += aSignedData.read() << 24;
            System.out.println("Signature size: " + signatureSize);

            byte[] encodedSignature = new byte[signatureSize];
            aSignedData.read(encodedSignature, 0, signatureSize);

            // Read data.
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            OutputStream out = new BufferedOutputStream(byteArray);
            byte[] buffer = new byte[4000];
            int size;

            while (0 < (size = aSignedData.read(buffer)))
            {
                out.write(buffer, 0,  size);
            }
            out.flush();
            aSignedData.close();
            byte[] signedData = byteArray.toByteArray();

            // Verify the signature.
            Signature signature = Signature.getInstance("SHA1withDSA", "SUN");
            signature.initVerify(aPubKey);
            signature.update(signedData);

            if (signature.verify(encodedSignature))
            {
                System.out.println("Signature verification successful.");
                data = signedData;
            }
            else
            {
                System.out.println("Signature verification failed.");
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return data;        
    }
}
