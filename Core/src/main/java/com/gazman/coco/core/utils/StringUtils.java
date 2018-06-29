package com.gazman.coco.core.utils;

/**
 * Created by Ilya Gazman on 6/26/2018.
 */
public class StringUtils {

    public static boolean isNullOrEmpty(String...args){
        for (String arg : args) {
            if(arg == null || arg.isEmpty()){
                return true;
            }
        }
        return false;
    }
}
