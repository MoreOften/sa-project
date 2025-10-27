package ku.cs.saproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ku.cs.services.FXRouter;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXRouter.bind(this, stage, "CS211 Project", 1280, 720);
        configRoutes();

        FXRouter.goTo("login");

    }

    private void configRoutes() {
        String viewPath;
    }

    public static void main(String[] args) {
        launch();
    }
}