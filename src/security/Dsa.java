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
public class Dsa {

    /** Creates a new instance of Dsa */
    public Dsa() {
    }

    public static void generateKeyPair(String keyId) {

        System.out.println("Generating 1024-bit DSA key pair " + keyId + "...");

        try {
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
            FileOutputStream privFos = new FileOutputStream(keyId + ".privkey");
            privFos.write(encodedPrivKey);
            privFos.close();
            FileOutputStream pubFos = new FileOutputStream(keyId + ".pubkey");
            pubFos.write(encodedPubKey);
            pubFos.close();

            // Save encoded keys to Java source code.
            PrintWriter privWriter = new PrintWriter(keyId + ".privkey.java");
            privWriter.print("byte[] " + keyId + "PrivKey = { " + encodedPrivKey[0]);
            for (int i = 1; i < encodedPrivKey.length; ++i) {
                privWriter.print(", " + encodedPrivKey[i]);
            }
            privWriter.println(" };");
            privWriter.close();

            PrintWriter pubWriter = new PrintWriter(keyId + ".pubkey.java");
            pubWriter.print("byte[] " + keyId + "PubKey = { " + encodedPubKey[0]);
            for (int i = 1; i < encodedPubKey.length; ++i) {
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
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void sign(String keyId, String filename) {

        System.out.println("Signing file " + filename + " with key " + keyId + "...");

        try {
            // Read encoded private key.
            FileInputStream privFis = new FileInputStream(keyId + ".privkey");
            byte[] encodedPrivKey = new byte[privFis.available()];
            privFis.read(encodedPrivKey);
            privFis.close();

            // Decode private key.
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encodedPrivKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

            // Sign the file.
            Signature signature = Signature.getInstance("SHA1withDSA", "SUN");
            signature.initSign(privKey);

            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
            byte[] buffer = new byte[4000];
            int size;

            while (0 < (size = in.read(buffer))) {
                signature.update(buffer, 0, size);
            }
            in.close();
            byte[] encodedSignature = signature.sign();
            int signatureSize = encodedSignature.length;

            // Store signature and file contents.
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + keyId + ".sign"));

            out.write(signatureSize & 0xFF);
            out.write((signatureSize >> 8) & 0xFF);
            out.write((signatureSize >> 16) & 0xFF);
            out.write((signatureSize >> 24) & 0xFF);
            out.write(encodedSignature);

            in = new BufferedInputStream(new FileInputStream(filename));
            while (0 < (size = in.read(buffer))) {
                out.write(buffer, 0, size);
            }
            out.close();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public static byte[] verify(String keyId, String filename) {

        byte[] data = null;

        try {
            // Read encoded public key.
            System.out.println("Reading public key " + keyId + "...");
            FileInputStream pubFis = new FileInputStream(keyId + ".pubkey");
            byte[] encodedPubKey = new byte[pubFis.available()];
            pubFis.read(encodedPubKey);
            pubFis.close();

            data = verify(encodedPubKey, filename);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        return data;
    }

    public static byte[] verify(byte[] encodedPubKey, String filename) {

        byte[] data = null;

        try {
            // Decode public key.
            System.out.println("Decoding public key...");
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encodedPubKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

            data = verify(pubKey, filename);
        }
        catch (Exception e) {
            System.out.println(e);            
        }

        return data;
    }

    public static byte[] verify(PublicKey pubKey, String filename) {

        System.out.println("Verifying signature of file " + filename + "...");
        byte[] data = null;

        try {
            // Read signature and file contents.
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
            int signatureSize = 0;

            signatureSize = in.read();
            signatureSize += in.read() << 8;
            signatureSize += in.read() << 16;
            signatureSize += in.read() << 24;
            System.out.println("Signature size: " + signatureSize);

            byte[] encodedSignature = new byte[signatureSize];
            in.read(encodedSignature, 0, signatureSize);

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            OutputStream out = new BufferedOutputStream(byteArray);
            byte[] buffer = new byte[4000];
            int size;

            while (0 < (size = in.read(buffer))) {
                out.write(buffer, 0,  size);
            }
            out.flush();
            in.close();
            byte[] file = byteArray.toByteArray();

            // Verify the signature.
            Signature signature = Signature.getInstance("SHA1withDSA", "SUN");
            signature.initVerify(pubKey);
            signature.update(file);

            if (signature.verify(encodedSignature)) {
                System.out.println("Signature verification successful.");
                data = file;
            }
            else {
                System.out.println("Signature verification failed.");
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }

        return data;        
    }
}