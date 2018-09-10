package com.gazman.coco.desktop;

import com.gazman.coco.desktop.popups.ProfilePopup;
import com.gazman.coco.desktop.root.commands.MoveToMainCommand;
import com.gazman.coco.desktop.settings.EncryptionSettings;
import com.gazman.lifecycle.Bootstrap;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.signal.SignalsHelper;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Ilya Gazman on 3/7/2018.
 */
public class RootScreenController extends Application {
    public VBox screensContainer;
    public Button miningButton;
    public Button homeButton;
    public Button smartContractsButton;
    public ToolBar toolBar;
    public ChoiceBox choicebox;
    private ScreensController screensController = Factory.inject(ScreensController.class);
    private MoveToMainCommand moveToMainCommand = Factory.inject(MoveToMainCommand.class);
    private ProfilePopup popup = new ProfilePopup();


    public static void main(String... args) {
        new Bootstrap() {

            @Override
            protected void initClasses() {
                registerClass(EncryptionSettings.class);
            }

            @Override
            protected void initSignals(SignalsHelper signalsHelper) {

            }

            @Override
            protected void initRegistrars() {

            }
        };
        Application.launch(args);
    }

    @FXML
    public void initialize() {

        System.out.println("Init");
        homeButton.setOnMouseClicked(event -> moveToMainCommand.execute());
        miningButton.setOnMouseClicked(event -> screensController.miningScreen.open());
        smartContractsButton.setOnMouseClicked(event -> screensController.smartContractsScreen.open());
        screensController.init(screensContainer, toolBar);
        moveToMainCommand.execute();

    }

    @Override
    public void start(Stage primaryStage) {

        System.out.println("App started");
        primaryStage.setTitle("Coco wallet");
        Scene scene = new Scene(screensController.rootScreen.getView());
        screensController.init(primaryStage, scene);
        primaryStage.setScene(scene);
        primaryStage.show();
        ChoiceBox<String> choiceBox = new ChoiceBox<>();

        choiceBox.getItems().addAll("wallet");

    }

    public void popupOpen() throws IOException {
        popup.display();
    }
}
