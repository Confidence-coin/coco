package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

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
            listView.getItems().add("Wallet " + walletModel.WalletSize());
            size.setText("Total Wallets:  " + walletModel.WalletSize());
        });
        size.setText("Wallet Size " + walletModel.WalletSize());
        listView.getItems().add("Wallet 1");
    }

    private class EditableCell extends ListCell<String>{

        private final TextField textField;

        EditableCell(){
            textField = new TextField();
            setGraphic(textField);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if(empty){
                textField.setVisible(false);
            }
            else{
                textField.setVisible(true);
                textField.setText(item);
            }
        }
    }


}
