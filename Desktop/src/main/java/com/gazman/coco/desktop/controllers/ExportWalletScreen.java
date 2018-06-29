package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Created by Ilya Gazman on 6/23/2018.
 */
public class ExportWalletScreen {
    public TextField secretKey;
    private WalletModel walletModel = Factory.inject(WalletModel.class);

    @FXML
    public void initialize(){
        secretKey.setText(walletModel.getPrivateKey());
    }
}
