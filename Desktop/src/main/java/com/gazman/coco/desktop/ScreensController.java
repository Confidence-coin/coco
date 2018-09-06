package com.gazman.coco.desktop;

import com.gazman.coco.desktop.popups.PopupBuilder;
import com.gazman.lifecycle.Singleton;
import com.sun.istack.internal.NotNull;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Ilya Gazman on 3/8/2018.
 */
public class ScreensController implements Singleton {
    final Screen rootScreen = new Screen("/root_screen.fxml");
    public final Screen passwordScreen = new Screen("/password_screen.fxml", false);
    public final Screen loginScreen = new Screen("/login_screen.fxml", false);
    public final Screen welcomeScreen = new Screen("/welcome_layout.fxml", false);
    public final Screen mainScreen = new Screen("/main_screen.fxml");
    public final Screen miningScreen = new Screen("/mining_screen.fxml");
    public final Screen exportWalletScreen = new Screen("/export_wallet_screen.fxml");
    public final Screen smartContractsScreen = new Screen("/smart_contracts_screen.fxml");
    public final Screen restoreWalletScreen = new Screen("/restore_wallet_layout.fxml");
    public final Screen sendCoinsScreen = new Screen("/send_coins_screen.fxml");


    public final Screen sendCoinsPreviewScreen = new Screen("/send_coins_screen.fxml");


    private VBox root;
    private ToolBar toolBar;
    private LinkedList<Screen> screenStock = new LinkedList<>();
    private Screen activeScreen;
    public Stage stage;
    private Scene scene;





    public void init(VBox root, ToolBar toolBar) {
        this.root = root;
        this.toolBar = toolBar;
    }

    public void init(Stage stage, Scene scene) {
        this.stage = stage;
        this.scene = scene;
    }

    public Stage getStage() {
        return stage;
    }

    public Scene getScene() {
        return scene;
    }

    public void goBack() {
        screenStock.pop();
        invalidate();
    }

    private void invalidate() {
        if (screenStock.size() == 0) {
            System.exit(1);
        }
        Screen screen = screenStock.peek();
        if (screen != activeScreen) {
            activeScreen = screen;
            root.getChildren().clear();
            root.getChildren().add(screen.getView());
        }
    }

    public class Screen {
        private Parent parent;
        private String path;
        private final boolean showToolbar;

        public Screen(String path) {
            this(path, true);
        }

        public Screen(String path, boolean showToolbar) {
            this.path = path;
            this.showToolbar = showToolbar;
        }

        public void open() {
            toolBar.setVisible(showToolbar);
            screenStock.remove(this);
            screenStock.push(this);
            invalidate();
        }

        public void close() {
            screenStock.remove(this);
            invalidate();
        }

        @NotNull Parent getView() {
            if (parent == null) {
                parent = load(path);
            }
            return parent;
        }

        private Parent load(String path) {
            try {
                return FXMLLoader.load(getClass().getResource(path));
            } catch (IOException e) {
                throw new Error("Error loading screen " + path, e);
            }
        }
    }

}
