package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;


import java.io.IOException;


public class PopupController {
    public ListView<WalletData> listView;
    private ObservableList<WalletData> myWallets = FXCollections.observableArrayList();
    public Button addWalletButton;
    public PieChart piechart;
    public Label size;


    private WalletModel walletModel = Factory.inject(WalletModel.class);
    private WalletCellController walletCellController = Factory.inject(WalletCellController.class);


    @FXML
    public void initialize() {
        listView.setCellFactory(param -> {
            try {
                return new EditableCell();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });

        addWalletButton.setOnMouseClicked(event -> {
            walletModel.CreateWallet();
            listView.getFixedCellSize();
            myWallets.add(new WalletData());


            listView.setItems(myWallets);
            size.setText("Total Wallets:  " + walletModel.walletSize());
        });
        myWallets.add(new WalletData());
        listView.setItems(myWallets);
        size.setText("Wallet Size " + walletModel.walletSize());


    }

    private class EditableCell extends ListCell<WalletData> {


        private final WalletCellController controller;
        ListCell<WalletData> cell = new ListCell<>();


        EditableCell() throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/selectButton.fxml"));
            Node graphic = loader.load();
            controller = loader.getController();
            setGraphic(graphic);
            cell.setUserData(walletCellController.textField);


        }

        @Override
        protected void updateItem(WalletData item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {

                controller.rootView.setVisible(false);

            } else {


                controller.rootView.setVisible(true);


            }
        }
    }

    class WalletData {
    }


}



