package com.gazman.coco.pool.handlers.work;

import com.gazman.coco.core.utils.MultiByteInteger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Ilya Gazman on 3/4/2018.
 */
@SuppressWarnings("SpellCheckingInspection")
public class BlockRealiser {

    public static void create(byte[] block, String hash) throws IOException, IllegalStateException {
        File root = new File(System.getProperty("user.dir") + "/blocks/" + hash + ".png");
        if (root.exists()) {
            throw new IllegalStateException("Block already exists");
        }

        byte[] length = MultiByteInteger.encode(block.length);
        int size = (int) Math.ceil(Math.sqrt(block.length + length.length) / 2);

        BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
        byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        System.arraycopy(length, 0, data, 0, length.length);
        System.arraycopy(block, 0, data, length.length, block.length);

        ImageIO.write(bufferedImage, "png", root);
    }

    public static byte[] load(String hash) throws IOException, IllegalStateException {
        File root = new File(System.getProperty("user.dir") + "/blocks/" + hash + ".png");
        if (root.exists()) {
            throw new IllegalStateException("Block not found");
        }

        BufferedImage bufferedImage = ImageIO.read(root);
        byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int length = MultiByteInteger.parse(buffer);
        byte[] block = new byte[length];
        buffer.get(block);
        return block;
    }
}
