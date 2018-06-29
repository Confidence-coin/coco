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
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ilya Gazman on 1/27/2018.
 */
public class MinerService implements Singleton {

    private boolean mining;
    private ProgressCallback callback;
    private ExecutorService mainExecutor;
    private ExecutorService minersExecutor;
    private ExecutorService speedExecutor = Executors.newSingleThreadExecutor();

    private ArrayList<Miner> miners = new ArrayList<>();
    private BaseSettings baseSettings = Factory.inject(BaseSettings.class);
    private LinkedList<LoopData> loops = new LinkedList<>();

    {
        loops.add(new LoopData(0));
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
                public void onProgress(int loopsCount) {
                    updateSpeed(loopsCount);
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

    private void updateSpeed(int loopsCount) {
        speedExecutor.submit(() -> {
            if (loopsCount == -1) {
                loops.clear();
                loops.add(new LoopData(0));
            } else {
                loops.add(new LoopData(loopsCount));
                while (loops.size() > getThreadCount() * 10) {
                    loops.remove();
                }
                loops.removeIf(loopData -> System.currentTimeMillis() - 1000 * 100 > loopData.creationTime);
            }
            double totalLoops = 0;
            long minTime = Long.MAX_VALUE;
            long maxTime = 0;
            for (LoopData loop : loops) {
                totalLoops += loop.loopsCount;
                if (loop.creationTime < minTime) {
                    minTime = loop.creationTime;
                }
                if (loop.creationTime > maxTime) {
                    maxTime = loop.creationTime;
                }
            }
            long deltaTimeMilliseconds = maxTime - minTime + 1;

            long loopsPerSecond = Math.round(totalLoops * 1000 / deltaTimeMilliseconds);
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
        updateSpeed(-1);
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

    private class LoopData {
        final int loopsCount;
        final long creationTime = System.currentTimeMillis();

        public LoopData(int loopsCount) {
            this.loopsCount = loopsCount;
        }
    }
}
