package com.gazman.coco.desktop.controllers.send_coins;

import com.gazman.coco.core.api.ErrorData;
import com.gazman.coco.core.api.StatusData;
import com.gazman.coco.core.utils.StringUtils;
import com.gazman.coco.desktop.ScreensController;
import com.gazman.coco.desktop.miner.requests.CocoRequest;
import com.gazman.coco.desktop.miner.requests.StatusRequest;
import com.gazman.coco.desktop.miner.transactions.Transaction1To1Request;
import com.gazman.coco.desktop.popups.PopupBuilder;
import com.gazman.coco.desktop.settings.ClientSettings;
import com.gazman.lifecycle.Factory;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.bitcoinj.core.Base58;

/**
 * Created by Ilya Gazman on 6/23/2018.
 */
public class SendCoinsController {
    public TextField address;
    public TextField amount;
    public CheckBox allCheckBox;

    private ScreensController screensController = Factory.inject(ScreensController.class);


    public void onContinue(MouseEvent mouseEvent) {
        screensController.sendCoinsPreviewScreen.open();
    }

    public void onAddTransaction(MouseEvent mouseEvent) {
        if (StringUtils.isNullOrEmpty(address.getText())) {
            showError("Please add address");
            return;
        }

        if (StringUtils.isNullOrEmpty(amount.getText())) {
            showError("Please specify amount");
            return;
        }

        new StatusRequest()
                .setCallback(new CocoRequest.Callback<StatusData>() {
                    @Override
                        public void onSuccess(StatusData data) {
                        new Transaction1To1Request(ClientSettings.instance.defaultPoolData, data.blockHash)
                                .setAmount(Double.parseDouble(amount.getText()))
                                .setReceiverId(Base58.decode(address.getText()))
                                .setPath("transaction")
                                .setCallback(new CocoRequest.Callback<String>() {
                                    @Override
                                    public void onSuccess(String data) {
                                        System.out.println(data);
                                    }

                                    @Override
                                    public void onError(ErrorData errorData) {
                                        System.out.println(errorData);
                                    }
                                })
                                .execute();
                    }

                    @Override
                    public void onError(ErrorData errorData) {
                        System.out.println(errorData);
                    }
                })
                .execute();
    }

    private void showError(String errorMessage) {
        Factory.inject(PopupBuilder.class)
                .setMessage(errorMessage)
                .setTitle("Error")
                .execute();
    }
}
