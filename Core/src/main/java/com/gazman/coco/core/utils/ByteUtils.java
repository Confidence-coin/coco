package com.gazman.coco.core.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Created by Ilya Gazman on 1/21/2018.
 */
public class ByteUtils {
    private static final BigInteger BASE = BigInteger.valueOf(256);

    public static boolean equals(byte[] a, byte b[]) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] toByteArray(BigInteger value) {
        return toByteArray(value, -1);
    }

    public static byte[] toByteArray(double value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putDouble(value);
        return buffer.array();
    }

    public static byte[] toByteArray(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(value);
        return buffer.array();
    }

    public static byte[] toByteArray(byte[]... items) {
        int length = 0;
        for (byte[] item : items) {
            length += item.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(length);
        for (byte[] item : items) {
            buffer.put(item);
        }
        return buffer.array();
    }

    public static BigInteger toBigInteger(byte[] input) {
        return new BigInteger(input);
    }

    public static byte[] toByteArray(BigInteger input, int fixedSize) {
        if (input.compareTo(BigInteger.ZERO) < 0) {
            throw new Error("Negative values not supported");
        }
        byte[] bytes = input.toByteArray();
        byte[] out = new byte[fixedSize != -1 ? fixedSize : bytes.length];
        for (int i = 0; i < out.length && i < bytes.length; i++) {
            out[out.length - i - 1] = bytes[bytes.length - i - 1];
        }
        return out;
    }

    /**
     * if a > b return 1 else if a < b return -1 else return 0
     */
    public static int compare(byte[] a, byte[] b) {
        int aLength = a.length;
        int bLength = b.length;

        for (int i = a.length - 1; i >= 0 && a[i] == 0; i--) {
            aLength--;
        }

        for (int i = b.length - 1; i >= 0 && b[i] == 0; i--) {
            bLength--;
        }

        if (aLength > bLength) {
            return 1;
        } else if (bLength > aLength) {
            return -1;
        }

        for (int k = 0; k < aLength; k++) {
            int A = a[k] & 0xff;
            int B = b[k] & 0xff;
            if (A > B) {
                return 1;
            }
            if (A < B) {
                return -1;
            }
        }
        return 0;
    }

    public static String toByteString(byte[] value) {
        return toByteString(value, false);
    }

    public static String toByteString(byte[] value, boolean signed) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : value) {
            StringBuilder singleByte = new StringBuilder(signed ? (b + "") : ((b & 0xff) + " "));
            while (singleByte.length() < 4) {
                singleByte.append(" ");
            }
            stringBuilder.append(singleByte);
        }
        return stringBuilder.toString();
    }

    public static boolean isContainNullOrEmpty(byte[]... values) {
        for (byte[] value : values) {
            if (value == null || value.length == 0) {
                return true;
            }
        }
        return false;
    }
}
