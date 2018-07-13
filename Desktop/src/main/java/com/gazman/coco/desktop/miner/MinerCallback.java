package com.gazman.coco.desktop.miner;

/**
 * Created by Ilya Gazman on 3/7/2018.
 */
public interface MinerCallback {

    void onProgress(LoopData loopData);

    void onSolutionFound(int blob, long blockTime);
}
