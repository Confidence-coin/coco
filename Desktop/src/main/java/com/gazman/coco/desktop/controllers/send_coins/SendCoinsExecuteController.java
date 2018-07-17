package com.gazman.coco.desktop.controllers.send_coins;

import com.gazman.coco.core.api.ErrorData;
import com.gazman.coco.core.api.SummeryData;
import com.gazman.coco.desktop.ScreensController;
import com.gazman.coco.desktop.miner.requests.CocoRequest;
import com.gazman.coco.desktop.miner.transactions.Transaction1To1Request;
import com.gazman.coco.desktop.popups.PopupBuilder;
import com.gazman.coco.desktop.settings.ClientSettings;
import com.gazman.coco.desktop.utils.CoinUtils;
import com.gazman.lifecycle.Factory;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import org.bitcoinj.core.Base58;

/**
 * Created by Ilya Gazman on 7/15/2018.
 */
public class SendCoinsExecuteController {
    public TextArea textBox;
    private TransactionsModel transactionsModel = Factory.inject(TransactionsModel.class);
    private ScreensController screensController = Factory.inject(ScreensController.class);

    @FXML
    public void initialize() {
        StringBuilder report = new StringBuilder();
        for (TransactionData transactionData : transactionsModel.getTransactionDatas()) {
            report.append(CoinUtils.toCoinsString(transactionData.amount)).append(" -> ").append(transactionData.recipient).append("\n");
        }
        report.append("\n Total transaction fees: ").append(CoinUtils.toCoinsString(transactionsModel.summeryData.totalFees));
        textBox.setText(report.toString());
    }


    public void onCancel(MouseEvent mouseEvent) {
        transactionsModel.clear();
        screensController.mainScreen.open();
    }

    public void onExecute(MouseEvent mouseEvent) {
        TransactionData transactionData = transactionsModel.getTransactionDatas().get(0);
        new Transaction1To1Request(ClientSettings.instance.defaultPoolData)
                .setAmount(transactionData.amount)
                .setReceiverId(Base58.decode(transactionData.recipient))
                .setPath("transaction")
                .setCallback(new CocoRequest.Callback<SummeryData>() {
                    @Override
                    public void onSuccess(SummeryData data) {
                        transactionsModel.clear();
                        Factory.inject(PopupBuilder.class)
                                .setMessage("You transaction been submitted to the pool and will be executed shortly")
                                .setTitle("Success")
                                .setPositiveButtonCallback(event -> screensController.sendCoinsPreviewScreen.open())
                                .execute();
                    }

                    @Override
                    public void onError(ErrorData errorData) {
                        Factory.inject(PopupBuilder.class)
                                .setMessage(errorData.code + "> " + errorData.error)
                                .setTitle("Error")
                                .execute();
                    }
                })
                .execute();
    }
}
