package ku.cs.controllers.supervisor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ku.cs.models.supervisor.Supervisor; // (1) เปลี่ยนเป็น Supervisor
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession; // (2) Import UserSession

import java.io.IOException;

public class SupervisorMainPageController {
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label idLabel;
    @FXML private Label roleLabel;

    private Supervisor currentSupervisor; //

    @FXML
    public void initialize() {
        clearLabel();

        // (4) ดึง User "กลาง" จาก Session
        User loggedInUser = UserSession.getInstance().getCurrentUser();

        // (5) ตรวจสอบและแปลงประเภท (Casting)
        if (loggedInUser instanceof Supervisor) {
            this.currentSupervisor = (Supervisor) loggedInUser;
        } else if (loggedInUser != null) {
            // กรณีนี้ไม่ควรเกิด ถ้า LoginController ส่งข้อมูลมาถูก
            System.err.println("SupervisorMainPage Error: User in session is " + loggedInUser.getClass().getName() + ", not Supervisor.");
        } else {
            System.err.println("SupervisorMainPage Error: No user data in session.");
        }

        showData(); // (6) เรียกเมธอดแสดงผล
    }

    /**
     * เมธอดสำหรับนำข้อมูลจาก Model มาแสดงบน View (Label)
     */
    private void showData() {
        if (currentSupervisor != null) {
            // (7) ใช้ Getter จาก Model (ที่สืบทอดมาจาก User)
            nameLabel.setText(currentSupervisor.getName());
            emailLabel.setText(currentSupervisor.getEmail());
            roleLabel.setText(currentSupervisor.getRole());

            // (8) ใช้ Getter เฉพาะของ Supervisor
            // (หมายเหตุ: คุณต้องแก้ไข Supervisor.java และ SupervisorRepository.java
            // ให้ทำงานเหมือนกับ Pilot/Instructor เพื่อให้ .getSupervisorID() ทำงานถูกต้อง)
            idLabel.setText(currentSupervisor.getSupervisorID());
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

    // (9) แก้ไขการนำทาง (Navigation) ให้ตรงกับ FXML

    public void onHomepageButtonClick() {
        // อยู่หน้านี้แล้ว ไม่ต้องทำอะไร หรือ refresh
        showData();
    }

    public void onScheduleButtonClick() {
        try {
            // (คุณต้องเพิ่ม Route นี้ใน MainApplication.java ด้วย)
            FXRouter.goTo("supervisor-schedule-page", currentSupervisor);
        } catch (IOException e) {
            System.err.println("Error navigating to supervisor-schedule-page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onReportButtonClick() {
        try {
            // (คุณต้องเพิ่ม Route นี้ใน MainApplication.java ด้วย)
            FXRouter.goTo("supervisor-report-page", currentSupervisor);
        } catch (IOException e) {
            System.err.println("Error navigating to supervisor-report-page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onLogoutButtonClick() {
        try {
            UserSession.getInstance().clearSession(); // (10) เคลียร์ Session ก่อน
            FXRouter.goTo("login");
        } catch (IOException e) {
            System.err.println("Error logging out: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onUserInfoButtonClick() {
        try {
            FXRouter.goTo("supervisor-user-list");
        } catch (IOException e) {
            System.err.println("ไปที่หน้า User Info ไม่ได้");
            e.printStackTrace();
        }
    }
}