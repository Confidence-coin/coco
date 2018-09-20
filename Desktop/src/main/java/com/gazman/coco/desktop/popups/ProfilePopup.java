package com.gazman.coco.desktop.popups;


import com.gazman.coco.desktop.RootScreenController;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfilePopup  {


    public Stage window = new Stage();






   // private void setConnectionListener(OnClick onClick) {
      //  this.onClick = onClick;
    //}

    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ProfilePopup.fxml"));


        window.setTitle("Your profile");
        window.setScene(new Scene(root, 400, 500));
        window.show();




    }



}