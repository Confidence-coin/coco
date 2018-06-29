package com.gazman.coco.desktop.controllers;

import com.gazman.coco.core.hash.Sha256Hash;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Created by Ilya Gazman on 6/21/2018.
 */
public class PasswordUtils {
    public static final String PASSWORD_FILE = "password.txt";

    public static char[] password(String password){
        byte[] hash = Sha256Hash.hash(password);
        for (int i = 0; i < 100; i++) {
            hash = Sha256Hash.hash(hash);
        }

        return bytesToChars(hash);
    }

    private static char[] bytesToChars(byte[] bytes) {
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 1);
        buffer.put(bytes);
        buffer.put((byte) 0); // makes a valid UTF-8
        CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(bytes));
        return charBuffer.array();
    }
}
