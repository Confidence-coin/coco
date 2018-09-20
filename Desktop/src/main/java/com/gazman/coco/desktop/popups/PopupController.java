package com.gazman.coco.desktop.popups;


import com.gazman.coco.desktop.controllers.WalletCellController;
import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.IOException;


public class PopupController  {

    public Button addWalletButton;
    public PieChart piechart;
    public Label size;
    private WalletModel walletModel = Factory.inject(WalletModel.class);
    private WalletCellController walletCellController=Factory.inject(WalletCellController.class);
    @FXML
    public ListView<WalletModel.WalletData> listView = new ListView<>(walletModel.myWallets);
    public Event event;


    @FXML
    public void initialize() {
        listView.setEditable(true);

        listView.setVisible(true);
        listView.setItems(walletModel.myWallets);

        listView.setCellFactory(param -> {
            try {
                return new EditableCell();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        listView.layout();




        addWalletButton.setOnMouseClicked(event -> {
            walletModel.createWallet();
            listView.getFixedCellSize();
            size.setText("Total Wallets:  " + walletModel.walletSize());
        });
        if (walletModel.myWallets.size() == 0) {
            walletModel.initializeWalletData();
            walletModel.myWallets.add(walletModel.initializeWalletData());
        }
        size.setText("Wallet Size " + walletModel.walletSize());
    }

    static class EditableCell extends ListCell<WalletModel.WalletData> {

        private final WalletCellController controller;


        EditableCell() throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/selectButton.fxml"));
            Node graphic = loader.load();
            controller = loader.getController();
            setGraphic(graphic);

        }

        @Override
        protected void updateItem(WalletModel.WalletData item, boolean empty) {


            if (empty) {

                controller.rootView.setVisible(false);

            } else {
                controller.textField.setText(item.getName());
                controller.rootView.setVisible(true);


            }
        }
    }
    public  void onMousrClicked(){
    }




}






