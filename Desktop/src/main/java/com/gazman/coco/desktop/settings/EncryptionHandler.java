package com.gazman.coco.desktop.settings;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * Created by Ilya Gazman on 3/20/2018.
 */
public class EncryptionHandler {

    public static final int IV_LENGTH = 16;

    public void encrypt(char[] password, File outputFile, InputStream input) throws Exception {
        SecureRandom random = SecureRandom.getInstanceStrong();

        byte[] salt = new byte[8];
        random.nextBytes(salt);
        SecretKeySpec secretKey = createSecretKeySpec(password, salt);

        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("data", ".data");

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                out.write(salt);
                out.write(iv);

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

                processFile(cipher, input, out);
            }

            try {
                Files.move(tempFile.toPath(), outputFile.toPath(), StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                Files.move(tempFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

        } finally {
            if (tempFile != null) {
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            }
        }
    }

    public byte[] decrypt(char[] password, File inputFile) throws Exception {
        FileInputStream in = new FileInputStream(inputFile);
        byte[] salt = new byte[8], iv = new byte[IV_LENGTH];
        if (in.read(salt) != 8) {
            throw new Error("Error reading salt");
        }
        if (in.read(iv) != IV_LENGTH) {
            throw new Error("Error reading iv");
        }

        SecretKeySpec secretKey = createSecretKeySpec(password, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        processFile(cipher, in, outputStream);
        return outputStream.toByteArray();
    }

    private SecretKeySpec createSecretKeySpec(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, 10000, 128);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private void processFile(Cipher cipher, InputStream in, OutputStream out) throws Exception {
        byte[] fileBuffer = new byte[1024];
        int len = in.read(fileBuffer);
        while (len != -1) {
            byte[] cipherBuffer = cipher.update(fileBuffer, 0, len);
            if (cipherBuffer != null) {
                out.write(cipherBuffer);
            }
            len = in.read(fileBuffer);
        }
        byte[] cipherBuffer = cipher.doFinal();
        if (cipherBuffer != null) {
            out.write(cipherBuffer);
        }
    }
}
