package com.gazman.coco.desktop.miner;

import com.gazman.coco.core.api.ErrorData;
import com.gazman.coco.core.api.WorkData;
import com.gazman.coco.core.settings.BaseSettings;
import com.gazman.coco.desktop.miner.requests.CocoRequest;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.Singleton;
import com.gazman.lifecycle.log.Logger;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ilya Gazman on 1/27/2018.
 */
public class MinerService implements Singleton {

    public static final int MAX_SPEED_SAMPLES = 10_000;
    public static final int SAMPLING_TIME_MILLISECONDS = 1000 * 10;
    private boolean mining;
    private ProgressCallback callback;
    private ExecutorService mainExecutor;
    private ExecutorService minersExecutor;
    private ExecutorService speedExecutor = Executors.newSingleThreadExecutor();

    private ArrayList<Miner> miners = new ArrayList<>();
    private BaseSettings baseSettings = Factory.inject(BaseSettings.class);
    private HashMap<Thread, LoopData> loops = new HashMap<>();

    {
        baseSettings.load("MinerSettings.txt");
    }

    private Logger logger = Logger.create("minerService");


    public void setCallback(ProgressCallback callback) {
        this.callback = callback;
    }

    public boolean isMining() {
        return mining;
    }

    public void stop() {
        mainExecutor.submit(this::onStop);
    }

    public void start() {
        init();
        mainExecutor.submit(this::onStart);
    }

    private void onStart() {
        if (mining) {
            return;
        }
        mining = true;
        Factory.inject(GetWorkRequest.class)
                .setCallback(new CocoRequest.Callback<WorkData>() {
                    @Override
                    public void onSuccess(WorkData workData) {
                        onGettingNewWork(workData);
                    }

                    @Override
                    public void onError(ErrorData errorData) {
                        stop();
                    }
                })
                .execute();
    }

    private void restart(WorkData workData) {
        onStop();
        mining = true;
        onGettingNewWork(workData);
    }

    private void onGettingNewWork(WorkData workData) {
        logger.d("onGettingNewWork", workData);
        int threads = getThreadCount();
        AtomicInteger threadId = new AtomicInteger(0);
        minersExecutor = Executors.newFixedThreadPool(threads, r -> {
            int priority = getPriority();
            Thread thread = new Thread(r, "Miner(" + priority + ") " + threadId.incrementAndGet());
            thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
            thread.setPriority(priority);
            return thread;
        });
        for (int i = 0; i < threads; i++) {
            Miner miner = new Miner(workData, i, new MinerCallback() {
                @Override
                public void onProgress(LoopData loopData) {
                    updateSpeed(loopData);
                }

                @Override
                public void onSolutionFound(int blob, long blockTime) {
                    Factory.inject(GetWorkRequest.class)
                            .setCallback(new CocoRequest.Callback<WorkData>() {
                                @Override
                                public void onSuccess(WorkData anotherWorkData) {
                                    if (!workData.equals(anotherWorkData)) {
                                        // avoid stack overflow
                                        mainExecutor.submit(() -> restart(anotherWorkData));
                                    }
                                }

                                @Override
                                public void onError(ErrorData errorData) {

                                }
                            })
                            .setWorkId(workData.workId)
                            .setBlob(blob)
                            .setMinedDate(blockTime)
                            .execute();
                }
            });
            miners.add(miner);
            minersExecutor.submit(miner::start);
        }
    }

    private void updateSpeed(LoopData loopData) {
        loops.put(Thread.currentThread(), loopData);
        speedExecutor.submit(() -> {
            double totalSpeed = 0;
            for (LoopData loop : loops.values()) {
                if(loop.totalTime > 0) {
                    totalSpeed += (double) loop.loopsCount / loop.totalTime;
                }
            }

            long loopsPerSecond = Math.round(totalSpeed * 1000);
            Platform.runLater(() -> callback.onProgress(loopsPerSecond));
        });
    }

    private void onStop() {
        if (!mining) {
            return;
        }
        mining = false;
        for (Miner miner : miners) {
            miner.shutDown();
        }
        miners.clear();
        minersExecutor.shutdown();
        minersExecutor = null;
        loops.clear();
        Platform.runLater(() -> callback.onProgress(0));
    }

    private void init() {
        if (mainExecutor == null) {
            mainExecutor = Executors.newSingleThreadExecutor();
        }
    }

    public interface ProgressCallback {
        void onProgress(long speed);
    }

    public void setThreadCount(int count) {
        baseSettings.writeKey("threads", "" + count);
    }

    public void setPriority(int count) {
        baseSettings.writeKey("threads", "" + count);
    }

    public int getPriority() {
        return baseSettings.readInteger("priority", 6);
    }

    public int getThreadCount() {
        return baseSettings.readInteger("threads", Math.max(1,
                Runtime.getRuntime().availableProcessors() - 1));
    }
}

