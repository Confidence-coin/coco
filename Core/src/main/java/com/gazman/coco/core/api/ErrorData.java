package com.gazman.coco.core.api;

/**
 * Created by Ilya Gazman on 2/25/2018.
 */
public class ErrorData {
    public String error;
    public int code;

    public ErrorData(String error, int code) {
        this.error = error;
        this.code = code;
    }

    @Override
    public String toString() {
        return "ErrorData{" +
                "error='" + error + '\'' +
                ", code=" + code +
                '}';
    }
}
