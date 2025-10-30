package ku.cs.controllers.pilot;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.pilot.PilotRepository;

import java.io.IOException;

public class PilotResignPageController {
    @FXML
    Label nameLabel;
    @FXML Label emailLabel;
    @FXML Label idLabel;
    @FXML Label roleLabel;

    // (ImageViews นี่ยังไม่ได้ใช้งาน แต่เก็บไว้ได้ครับ)
    @FXML
    ImageView instructorImageView;
    @FXML ImageView logoImageView;

    // --- เพิ่มตัวแปรสำหรับเก็บข้อมูล Pilot ---
    private Pilot currentPilot;
    private PilotRepository pilotRepository;

    @FXML
    public void initialize() {
        pilotRepository = new PilotRepository();

        // 1. ดึงข้อมูล User (ที่ส่งมาจากหน้า Login)
        User user = (User) FXRouter.getData();

        if (user != null) {
            // 2. ใช้ username จาก User ไปค้นหาข้อมูล Pilot ทั้งหมด
            this.currentPilot = pilotRepository.findPilotByUsername(user.getUsername());

            if (this.currentPilot != null) {
                // 3. แสดงข้อมูล Pilot ที่ถูกต้อง
                showPilotData();
            } else {
                clearLabel();
                nameLabel.setText("Error: Pilot profile not found.");
            }
        } else {
            clearLabel();
            nameLabel.setText("Error: Cannot get user data.");
        }
    }

    // --- เมธอดสำหรับแสดงข้อมูล (แยกออกมาให้ชัดเจน) ---
    private void showPilotData() {
        // (เมธอดเหล่านี้ .getName(), .getEmail(), .getPilotID()
        // ต้องมีอยู่ใน Model Pilot.java ของคุณ
        // ซึ่งเราได้ออกแบบไว้ตอนสร้าง PilotRepository แล้ว)
        nameLabel.setText(currentPilot.getName());
        emailLabel.setText(currentPilot.getEmail());
        idLabel.setText(currentPilot.getPilotID()); // สมมติว่าเมธอดนี้คืนค่า ID ของ Pilot
        roleLabel.setText(currentPilot.getRole());  // จะแสดงค่า "pilot"
    }

    public void clearLabel() {
        nameLabel.setText("");
        emailLabel.setText("");
        idLabel.setText("");
        roleLabel.setText("");
    }

    // --- แก้ไขชื่อเมธอดให้ตรงกับ FXML (handle...Button) ---
    // --- และแก้ไขปลายทาง FXRouter ให้เป็นของ "pilot" ---

    @FXML
    public void handleHomepageButton() {
        // ไม่ต้องทำอะไร เพราะนี่คือหน้า Homepage อยู่แล้ว
        // หรือจะให้ refresh ข้อมูลก็ได้
        showPilotData();
    }

    @FXML
    public void handleScheduleButton() { // แก้ชื่อจาก onScheduleButtonClick
        try {
            // แก้ปลายทางเป็นหน้าของ pilot
            FXRouter.goTo("pilot-schedule-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReportButton() { // แก้ชื่อจาก onReportButtonClick
        try {
            // แก้ปลายทางเป็นหน้าของ pilot
            FXRouter.goTo("pilot-report-page.fxml", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // (เพิ่มปุ่ม Resign ที่เห็นใน FXML Screenshot ของคุณ)
    @FXML
    public void handleResignButton() {
        try {
            FXRouter.goTo("pilot-resign-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    public void handleLogoutButton() { // แก้ชื่อจาก onLogoutButtonClick
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
