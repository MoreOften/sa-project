package ku.cs.controllers.instructor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;

import java.io.IOException;

public class InstructorMainPageController {
    @FXML Label nameLabel;
    @FXML Label emailLabel;
    @FXML Label idLabel;
    @FXML Label roleLabel;
    @FXML
    ImageView instructorImageView;
    @FXML ImageView logoImageView;

    Instructor currentInstructor;

    @FXML
    public void initialize() {
        clearLabel();

        // 1. ดึง User "กลาง" จาก Session
        User loggedInUser = UserSession.getInstance().getCurrentUser();

        // 2. ตรวจสอบและแปลงประเภท (Casting)
        if (loggedInUser instanceof Instructor) {
            this.currentInstructor = (Instructor) loggedInUser;
        } else if (loggedInUser != null) {
            // ... (โค้ดส่วนอื่นของคุณ) ...
        }

        // VVVV (เพิ่มบรรทัดนี้เข้าไป) VVVV
        showData(); // <-- เรียกเมธอดแสดงผล
    }

    /**
     * เมธอดสำหรับนำข้อมูลจาก Model มาแสดงบน View (Label)
     */
    private void showData() {
        if (currentInstructor != null) {
            // (5) ใช้ Getter จาก Model (ที่สืบทอดมาจาก User)
            nameLabel.setText(currentInstructor.getName());
            emailLabel.setText(currentInstructor.getEmail());
            roleLabel.setText(currentInstructor.getRole());

            // (6) ใช้ Getter เฉพาะของ Instructor
            idLabel.setText(currentInstructor.getInstructorID());

            // (โหลดรูปภาพ)

        } else {
            // ถ้า currentInstructor เป็น null (ซึ่งไม่ควรเกิดถ้า Login ถูก)
            nameLabel.setText("Error: No data");
        }
    }

    public void clearLabel() {
        nameLabel.setText("");
        emailLabel.setText("");
        idLabel.setText("");
        roleLabel.setText("");
    }

    public void onHomepageButtonClick() {
        try {
            FXRouter.goTo("instructor-main-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onScheduleButtonClick() {
        try {
            FXRouter.goTo("instructor-schedule-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onReportButtonClick() {
        try {
            FXRouter.goTo("instructor-report-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onLogoutButtonClick() {
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
