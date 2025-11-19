package ku.cs.saproject;

import javafx.application.Application;
import javafx.stage.Stage;
import ku.cs.database.DbConnect;
import ku.cs.services.FXRouter;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            // 1. (ต้องมาก่อน) สร้างตารางทั้งหมด
            // เมธอดนี้จะสร้างตาราง users, instructors, ฯลฯ
            DbConnect.initializeDatabase();

            // 2. (ต้องมาทีหลัง) สร้างข้อมูลเริ่มต้น
            // เมธอดนี้จะเรียก userRepository.findUserByUsername ซึ่งตอนนี้ตาราง users ถูกสร้างแล้ว
            DbConnect.seedInitialData();

            // 3. (มาทีหลังสุด) ตั้งค่า UI และเปิดหน้าแรก
            FXRouter.bind(this, stage, "SA Project", 1280, 720);
            configRoutes();

            FXRouter.goTo("login");

        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการเริ่มแอป: " + e.getMessage());
            e.printStackTrace();
        }
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
        FXRouter.when("report-create",  viewPath + "report-create-form.fxml");

        viewPath = "ku/cs/views/pilot/";
        FXRouter.when("pilot-main-page", viewPath + "pilot-main-page.fxml");
        FXRouter.when("pilot-resign-page", viewPath + "pilot-resign-page.fxml");
        FXRouter.when("resign-form-page", viewPath + "resign-create-form.fxml");
        FXRouter.when("pilot-schedule-page", viewPath + "pilot-schedule-page.fxml");
        FXRouter.when("pilot-report-page", viewPath + "pilot-report-page.fxml");
        FXRouter.when("pilot-notification-page", viewPath + "pilot-notification-page.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}