package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;


public class PopupController {
    public ListView<String> listView;
    public Button addWalletButton;
    public PieChart piechart;
    public Label size;
    private WalletModel walletModel = Factory.inject(WalletModel.class);

    @FXML
    public void initialize() {
        listView.setCellFactory(param -> new EditableCell());
        addWalletButton.setOnMouseClicked(event -> {
            walletModel.CreateWallet();
            listView.getFixedCellSize();
            listView.getItems().add("");
            size.setText("Total Wallets:  " + walletModel.WalletSize());
        });
        size.setText("Wallet Size " + walletModel.WalletSize());
        listView.getItems().add("Wallet 1");
    }

    private class EditableCell extends ListCell<String> {

        private final HBox hBox;
        TextField textField = new TextField();

        Button select = new Button("Select");

        EditableCell() {
            textField.setText("Wallet" + (walletModel.WalletSize()));

            hBox = new HBox();
            hBox.getChildren().addAll(textField, select);
            setGraphic(hBox);
        }

        @Override
        protected void updateItem(String hbox, boolean empty) {
            super.updateItem(hbox, empty);

            if (empty) {
                hBox.setVisible(false);

            } else {
                new TextField().setText("Wallet " + walletModel.WalletSize() + 1);
                hBox.setVisible(true);


            }
        }
    }


}
