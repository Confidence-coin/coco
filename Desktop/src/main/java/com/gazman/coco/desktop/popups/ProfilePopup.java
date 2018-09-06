package com.gazman.coco.desktop.popups;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProfilePopup {
    public void display() {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Your profile");
        window.setMinHeight(300);
        window.setMaxHeight(300);


        Label label = new Label();
        label.setText("Your wallets:");
        Button CreateWallet = new Button("Create Wallet");


        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(label, CreateWallet);


        Scene scene = new Scene(layout, 300, 300);
        window.setScene(scene);
        window.show();


    }
}
