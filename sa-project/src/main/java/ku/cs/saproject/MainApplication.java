package ku.cs.saproject;

import javafx.application.Application;
import javafx.stage.Stage;
import ku.cs.database.DbConnect;
import ku.cs.services.FXRouter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApplication extends Application {

    // ประกาศตัว Logger สำหรับบันทึกข้อผิดพลาด
    private static final Logger logger = Logger.getLogger(MainApplication.class.getName());

    @Override
    public void start(Stage stage) { // [แก้ไข 1] ลบ throws IOException ออก
        try {
            // 1. (ต้องมาก่อน) สร้างตารางทั้งหมด
            DbConnect.initializeDatabase();

            // 2. (ต้องมาทีหลัง) สร้างข้อมูลเริ่มต้น
            DbConnect.seedInitialData();

            // 3. (มาทีหลังสุด) ตั้งค่า UI และเปิดหน้าแรก
            FXRouter.bind(this, stage, "SA Project", 1280, 720);
            configRoutes();

            FXRouter.goTo("login");

        } catch (Exception e) {
            // [แก้ไข 2] ใช้ Logger แทน e.printStackTrace()
            System.err.println("เกิดข้อผิดพลาดในการเริ่มแอป: " + e.getMessage());
            logger.log(Level.SEVERE, "Application start error", e);
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
        FXRouter.when("report-view",  viewPath + "report-view-form.fxml");
        FXRouter.when("report-edit",  viewPath + "report-edit-form.fxml");
        // FXRouter.when("report-send",  viewPath + "report-send-form.fxml"); // ถ้ามีไฟล์นี้
        FXRouter.when("instructor-notification-page", viewPath + "instructor-notification-page.fxml");

        viewPath = "ku/cs/views/pilot/";
        FXRouter.when("pilot-main-page", viewPath + "pilot-main-page.fxml");
        FXRouter.when("pilot-resign-page", viewPath + "pilot-resign-page.fxml");
        FXRouter.when("resign-form-page", viewPath + "resign-create-form.fxml");
        FXRouter.when("pilot-schedule-page", viewPath + "pilot-schedule-page.fxml");
        FXRouter.when("pilot-report-page", viewPath + "pilot-report-page.fxml");
        FXRouter.when("pilot-notification-page", viewPath + "pilot-notification-page.fxml");

        viewPath = "ku/cs/views/supervisor/";
        FXRouter.when("supervisor-main-page", viewPath + "supervisor-main-page.fxml");
        FXRouter.when("supervisor-schedule-page", viewPath + "supervisor-schedule-page.fxml");
        FXRouter.when("supervisor-report-page", viewPath + "supervisor-report-page.fxml");
        FXRouter.when("supervisor-report-detail", "ku/cs/views/supervisor/supervisor-report-detail.fxml");
        FXRouter.when("supervisor-create-schedule", "ku/cs/views/supervisor/supervisor-create-schedule.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}