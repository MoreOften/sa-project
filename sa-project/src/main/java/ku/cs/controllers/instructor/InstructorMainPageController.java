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
    @FXML ImageView instructorImageView;
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
            System.err.println("Error: User " + loggedInUser.getUsername() + " is not an Instructor.");
        }

        // เรียกเมธอดแสดงผล
        showData();
    }

    /**
     * เมธอดสำหรับนำข้อมูลจาก Model มาแสดงบน View (Label)
     */
    private void showData() {
        if (currentInstructor != null) {
            nameLabel.setText(currentInstructor.getName());
            emailLabel.setText(currentInstructor.getEmail());
            roleLabel.setText(currentInstructor.getRole());
            idLabel.setText(currentInstructor.getInstructorID());
            // (โหลดรูปภาพเพิ่มตรงนี้ได้)
        } else {
            nameLabel.setText("Error: No data");
        }
    }

    public void clearLabel() {
        nameLabel.setText("");
        emailLabel.setText("");
        idLabel.setText("");
        roleLabel.setText("");
    }

    // --- แก้ไขชื่อเมธอดให้ตรงกับ FXML (handle...) ---

    @FXML
    public void handleHomepageButton() {
        try {
            FXRouter.goTo("instructor-main-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleScheduleButton() {
        try {
            FXRouter.goTo("instructor-schedule-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReportButton() {
        try {
            FXRouter.goTo("instructor-report-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleNotificationButton() {
        try {
            // อย่าลืมไปเพิ่ม route "instructor-notification-page" ใน MainApplication.java ด้วยนะครับ
            FXRouter.goTo("instructor-notification-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleLogoutButton() {
        try {
            UserSession.getInstance().clearSession(); // เคลียร์ Session ก่อนออก
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}