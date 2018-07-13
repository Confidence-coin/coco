package com.gazman.coco.desktop.miner;

/**
 * Created by Ilya Gazman on 7/9/2018.
 */
public class LoopData {
    final int loopsCount;
    final long totalTime;

    public LoopData(int loopsCount, long totalTime) {
        this.loopsCount = loopsCount;
        this.totalTime = totalTime;
    }
}
