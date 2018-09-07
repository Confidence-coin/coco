package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class PopupController {
    public ListView listview;
    public Button addwallet;
    public PieChart piechart;
    public Label size;
    WalletModel walletModel = Factory.inject(WalletModel.class);

    @FXML
    public void initialize() {
        addwallet.setOnMouseClicked(event -> {
            walletModel.CreateWallet();
            listview.getFixedCellSize();
            listview.getItems().add("Wallet " + walletModel.WalletSize());
            size.setText("Total Wallets:  " + walletModel.WalletSize());
        });
        size.setText("Wallet Size " + walletModel.WalletSize());
        listview.getItems().add("Wallet 1");

    }


}
