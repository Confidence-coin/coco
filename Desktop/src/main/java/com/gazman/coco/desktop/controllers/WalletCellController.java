package com.gazman.coco.desktop.controllers;

import com.gazman.coco.core.api.ListItem;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;


public class WalletCellController {
    public Button select;
    public TextField textField;
    public AnchorPane rootView;
    public String walletName;

    public Node source;


    @FXML
    public void initialize() {
        select.setOnAction(event -> {
            source = select.getParent();
            walletName = textField.getText();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();

        });

    }


}


















