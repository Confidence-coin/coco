package com.gazman.coco.core.utils;

/**
 * Created by Ilya Gazman on 1/21/2018.
 */
public class VersionUtils {

    public static byte[] getVersion(int blockId) {
        switch (blockId / 8640) { // 60 days
            case 0:
                return Utils.uuidToByteArray("cd1c01cc-edd8-429f-b60c-d427e1faf9ee");
            default:
                return null;
        }
    }
}
