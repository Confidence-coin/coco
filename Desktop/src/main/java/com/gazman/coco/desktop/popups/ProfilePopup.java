package com.gazman.coco.desktop.popups;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfilePopup {


    public void display() throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("/ProfilePopup.fxml"));
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Your profile");
        window.setScene(new Scene(root, 400, 500));
        window.show();
    }
}
