package com.gazman.coco.desktop.popups;

import com.gazman.coco.desktop.ScreensController;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.utils.Command;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Created by Ilya Gazman on 3/21/2018.
 */
public class PopupBuilder implements Command {
    private Stage stage;
    private String titleData, messageData;
    private ScreensController screensController = Factory.inject(ScreensController.class);
    private EventHandler<MouseEvent> positiveButtonCallback;

    public PopupBuilder setPositiveButtonCallback(EventHandler<MouseEvent> positiveButtonCallback) {
        this.positiveButtonCallback = positiveButtonCallback;
        return this;
    }

    public PopupBuilder setTitle(String title) {
        this.titleData = title;
        return this;
    }

    public PopupBuilder setMessage(String message) {
        this.messageData = message;
        return this;
    }

    @Override
    public void execute() {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(new Controller());
        loader.setLocation(getClass().getResource("/popup.fxml"));
        Parent root;

        try {
            root = loader.load();

        } catch (IOException e) {
            e.printStackTrace();
            onError(e);
            return;
        }

        Scene scene = new Scene(root, 250, 150);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initOwner(screensController.getStage());
        stage.setScene(scene);
        stage.show();


        ((Button) loader.getNamespace().get("positiveButton")).setOnMouseClicked(event -> {
            stage.close();
            if (positiveButtonCallback != null) {
                positiveButtonCallback.handle(event);
            }
        });
    }

    private void onError(IOException e) {

    }

    private class Controller {
        Label title;
        Label message;

        @FXML
        public void initialize() {
            title.setText(PopupBuilder.this.titleData);
            message.setText(PopupBuilder.this.messageData);
        }
    }
}
