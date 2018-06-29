package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.miner.MinerService;
import com.gazman.coco.desktop.root.BaseController;
import com.gazman.lifecycle.Factory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

/**
 * Created by Ilya Gazman on 3/8/2018.
 */
public class MiningScreenController extends BaseController {


    public ChoiceBox<String> pools;
    public ChoiceBox<Integer> threadChoice;
    public ChoiceBox<Integer> threadPriorityChoice;
    public Label speedLabel;
    public Button miningButton;

    private MinerService miner = Factory.inject(MinerService.class);

    @FXML
    public void initialize(){
        initPools();
        initThreadChoice();
        initPriority();
        initProgress();
        initMiningButton();
    }

    private void initProgress() {
        String prefix = "Speed: ";
        speedLabel.setText(prefix + 0);
        miner.setCallback(speed -> {
            if (speed < 1_000_000) {
                speed = speed - speed % 10;
                speedLabel.setText(prefix + (speed / 1000f) + "KH");
            } else if (speed < 1_000_000_000) {
                //1.25M 1250000
                speed = speed - speed % 10000;
                speedLabel.setText(prefix + (speed / 1_000_000f) + "MH");
            } else if (speed < 1_000_000_000_000L) {
                //1.25T 1250000000
                speed = speed - speed % 10000;
                speedLabel.setText(prefix + (speed / 1_000_000_000f) + "TH");
            }
        });
    }

    private void initMiningButton() {
        miningButton.setOnMouseClicked(event -> {
            if (miner.isMining()) {
                miningButton.setText("Start mining");
                miner.stop();
            } else {
                miningButton.setText("Stop mining");
                miner.start();
            }
        });
        miningButton.requestFocus();
    }

    private void initPriority() {
        for (int i = Thread.MIN_PRIORITY; i < Thread.MAX_PRIORITY; i++) {
            threadPriorityChoice.getItems().add( i + 1);
        }
        selectFirst(threadPriorityChoice);
    }

    private void initThreadChoice() {
        int maxThreads = Runtime.getRuntime().availableProcessors() * 2;
        for (int i = 0; i < maxThreads; i++) {
            threadChoice.getItems().add( i + 1);
        }
        selectFirst(threadChoice);
    }

    private void initPools() {
        pools.getItems().addAll("Apple", "nice", "Fish");
        pools.setValue("Select Pool");
        selectFirst(pools);
    }

    private <T> void selectFirst(ChoiceBox<T> choiceBox) {
        choiceBox.setValue(choiceBox.getItems().get(0));
    }
}
