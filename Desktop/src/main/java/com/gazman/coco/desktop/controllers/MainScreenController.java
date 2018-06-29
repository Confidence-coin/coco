package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.root.BaseController;
import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Created by Ilya Gazman on 3/8/2018.
 */
public class MainScreenController extends BaseController {

    public Label walletAddress;
    private WalletModel walletModel = Factory.inject(WalletModel.class);

    @FXML
    public void initialize(){
        walletAddress.setText("Wallet address: " + walletModel.getPublicKey());
    }

    public void sendMoney() {
        screens.sendCoinsScreen.open();
    }

    public void exportWallet() {
        screens.exportWalletScreen.open();
    }

    public void onCopy(MouseEvent mouseEvent) {
        StringSelection stringSelection = new StringSelection(walletModel.getPublicKey());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}
