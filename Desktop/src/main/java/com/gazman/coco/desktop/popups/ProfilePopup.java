package com.gazman.coco.desktop.popups;
import com.gazman.coco.desktop.ScreensController;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.utils.Command;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ProfilePopup extends PopupBuilder {
    public void display(){
        Stage window = new Stage();
        PopupBuilder popupBuilder=new PopupBuilder();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Your profile");
        window.setMinHeight(300);
        window.setMaxHeight(300);


        Label label=new Label();
        label.setText("Your wallets:");
        Button CreateWallet=new Button("Create Wallet");


        window.getIcons().add(new Image("/resources/p.png"));


        VBox layout =new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(label,CreateWallet);


        Scene scene =new Scene(layout,300,300);
        window.setScene(scene);
        window.show();






    }
}
