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
        FXRouter.bind(this, stage, "SA Project", 1280, 720);
        configRoutes();

        FXRouter.goTo("login");

    }

    private void configRoutes() {
        String viewPath;

        viewPath = "ku/cs/views/login/";
        FXRouter.when("login", viewPath + "login-view.fxml");
        FXRouter.when("register", viewPath + "register-view.fxml");
        FXRouter.when("set-password", viewPath + "set-password.fxml");


        viewPath = "ku/cs/views/instructor/";
        FXRouter.when("instructor-main-page", viewPath + "instructor-main-page.fxml");
        FXRouter.when("instructor-schedule-page", viewPath + "instructor-schedule-page.fxml");
        FXRouter.when("instructor-report-page", viewPath + "instructor-report-page.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}