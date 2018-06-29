package com.gazman.coco.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Ilya Gazman on 1/26/2018.
 */
public final class MultiByteInteger {

    public static final int MAX_BYTE_SIZE = 4;
    public static final int MIN_BYTE_SIZE = 1;
    public static int MAX_SIZE = 1_073_741_823;
    public static int MIN_SIZE = 0;

    private static final byte LEADING_BIT = (byte) 192;
    private static final byte SINGLE_BYTE = (byte) 0;
    private static final byte DOUBLE_BYTE = (byte) 64;
    private static final byte TRIPLE_BYTE = (byte) 128;
    private static final byte SQUARE_BYTE = (byte) 192;

    private MultiByteInteger() {
    }

    public static int parse(InputStream inputStream) throws IOException {
        byte[] data = new byte[4];
        if (inputStream.read(data, 0, 1) != 1) {
            return -1;
        }
        int type = data[0] & LEADING_BIT;
        if (type == SINGLE_BYTE) {
            return data[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(4);
        switch (type) {
            case DOUBLE_BYTE:
                if (inputStream.read(data, 1, 1) != 1) {
                    return -1;
                }
                buffer.put(2, (byte) (data[0] ^ DOUBLE_BYTE));
                buffer.put(3, data[1]);
                buffer.rewind();
                return buffer.getInt();
            case TRIPLE_BYTE:
                if (inputStream.read(data, 1, 2) != 2) {
                    return -1;
                }
                buffer.put(1, (byte) (data[0] ^ TRIPLE_BYTE));
                buffer.put(2, data[1]);
                buffer.put(3, data[2]);
                return buffer.getInt();
            case SQUARE_BYTE:
                if (inputStream.read(data, 1, 3) != 3) {
                    return -1;
                }
                buffer.put(0, (byte) (data[0] ^ SQUARE_BYTE));
                buffer.put(1, data[1]);
                buffer.put(2, data[2]);
                buffer.put(3, data[3]);
                buffer.rewind();
                return buffer.getInt();
        }

        return -1;
    }

    public static int parse(byte[] data) {
        int type = data[0] & LEADING_BIT;
        if (type == SINGLE_BYTE) {
            return data[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(4);
        switch (type) {
            case DOUBLE_BYTE:
                buffer.put(2, (byte) (data[0] ^ DOUBLE_BYTE));
                buffer.put(3, data[1]);
                buffer.rewind();
                return buffer.getInt();
            case TRIPLE_BYTE:
                buffer.put(1, (byte) (data[0] ^ TRIPLE_BYTE));
                buffer.put(2, data[1]);
                buffer.put(3, data[2]);
                return buffer.getInt();
            case SQUARE_BYTE:
                buffer.put(0, (byte) (data[0] ^ SQUARE_BYTE));
                buffer.put(1, data[1]);
                buffer.put(2, data[2]);
                buffer.put(3, data[3]);
                buffer.rewind();
                return buffer.getInt();
        }

        return -1;
    }

    public static int parse(ByteBuffer inputBuffer) {
        byte _0 = inputBuffer.get();
        int type = _0 & LEADING_BIT;
        if (type == SINGLE_BYTE) {
            return _0;
        }
        ByteBuffer buffer = ByteBuffer.allocate(4);
        switch (type) {
            case DOUBLE_BYTE:
                buffer.put(2, (byte) (_0 ^ DOUBLE_BYTE));
                buffer.put(3, inputBuffer.get());
                buffer.rewind();
                return buffer.getInt();
            case TRIPLE_BYTE:
                buffer.put(1, (byte) (_0 ^ TRIPLE_BYTE));
                buffer.put(2, inputBuffer.get());
                buffer.put(3, inputBuffer.get());
                return buffer.getInt();
            case SQUARE_BYTE:
                buffer.put(0, (byte) (_0 ^ SQUARE_BYTE));
                buffer.put(1, inputBuffer.get());
                buffer.put(2, inputBuffer.get());
                buffer.put(3, inputBuffer.get());
                buffer.rewind();
                return buffer.getInt();
        }

        return -1;
    }

    public static int calculateByteLength(int value) {
        return encode(value).length;
    }

    public static int calculateByteLength(double sMbi) {
        return calculateByteLength((int) (sMbi / 100_000_000));
    }

    public static byte[] encode(double sMbi) {
        return encode((int) Math.round(sMbi * 100_000_000));
    }

    public static byte[] encode(int value) {
        if (!isValid(value)) {
            throw new Error("value out of range " + value);
        }
        if (value < 64) {
            return new byte[]{(byte) value};
        }
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        if (value < 16384) {
            return new byte[]{(byte) (buffer.get(2) | DOUBLE_BYTE), buffer.get(3)};
        }
        if (value < 4194304) {
            return new byte[]{(byte) (buffer.get(1) | TRIPLE_BYTE), buffer.get(2), buffer.get(3)};
        }
        if (value < 1073741824) {
            return new byte[]{(byte) (buffer.get(0) | SQUARE_BYTE), buffer.get(1), buffer.get(2), buffer.get(3)};
        }
        throw new Error("unsupported value " + value);
    }

    public static boolean isValid(int value) {
        return value >= 0 && value <= MAX_SIZE;
    }
}
