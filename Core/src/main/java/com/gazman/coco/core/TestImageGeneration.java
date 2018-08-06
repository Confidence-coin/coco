package com.gazman.coco.core;

import com.gazman.coco.core.utils.ByteUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Ilya Gazman on 1/15/2018.
 */
public class TestImageGeneration {


    public static void main(String... args) {
        new TestImageGeneration().start();
    }

    private void start() {
        File root = new File(System.getProperty("user.dir") + "/image.png");
        if (root.exists() && !root.delete()) {
            throw new Error("Can't delete");
        }
        BufferedImage bufferedImage1 = new BufferedImage(2048, 2048, BufferedImage.TYPE_4BYTE_ABGR);
        byte[] data = ((DataBufferByte) bufferedImage1.getRaster().getDataBuffer()).getData();
        Random random = new Random();
        random.nextBytes(data);

        try {
            ImageIO.write(bufferedImage1, "png", root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage bufferedImage2;
        try {
            bufferedImage2 = ImageIO.read(root);
        } catch (IOException e) {
            throw new Error(e);
        }
        byte[] data2 = ((DataBufferByte) bufferedImage2.getRaster().getDataBuffer()).getData();
        System.out.println(ByteUtils.equals(data, data2));
    }

    String byteToBinaryString(byte b) {
        StringBuilder binaryStringBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++)
            binaryStringBuilder.append(((0x80 >>> i) & b) == 0 ? '0' : '1');
        return binaryStringBuilder.toString();
    }


}
