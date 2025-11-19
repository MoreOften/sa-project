package ku.cs.controllers.pilot;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.user.UserRepository;

import java.io.IOException;

public class PilotResignPageController {

    // **FIXED:** Changed to pilotNameLabel to match FXML's fx:id
    @FXML Label pilotNameLabel;

    // FXML elements from pilot-resign-page.fxml
    @FXML private TextArea reasonTextArea;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // (ImageViews)
    @FXML ImageView instructorImageView;
    @FXML ImageView logoImageView;

    private Pilot currentPilot;
    private PilotRepository pilotRepository;
    private UserRepository userRepository;

    @FXML
    public void initialize() {
        pilotRepository = new PilotRepository();
        userRepository = new UserRepository();

        errorLabel.setText("");


        // **UNIFIED DATA LOGIC FIX:** Prioritize checking for Pilot object
        Object data = FXRouter.getData();

        if (data instanceof Pilot) {
            // Case 1: Pilot object passed directly (from another sidebar button)
            this.currentPilot = (Pilot) data;
        } else if (data instanceof User) {
            // Case 2: Base User object passed (likely from initial login) - fetch full profile
            User user = (User) data;
            this.currentPilot = pilotRepository.findPilotByUsername(user.getUsername());
        }

        if (this.currentPilot != null) {
            showPilotData();
        } else {
            clearLabel();
            // Use pilotNameLabel to display error message
            pilotNameLabel.setText("Error: Cannot get user data.");
        }
    }

    private void showPilotData() {
        // **FIXED:** Use pilotNameLabel
        pilotNameLabel.setText(currentPilot.getName());
    }

    public void clearLabel() {
        // **FIXED:** Use pilotNameLabel
        pilotNameLabel.setText("");
        // Clear other fields
        if (errorLabel != null) errorLabel.setText("");
    }

//    @FXML
//    public void handleConfirmResignButton(ActionEvent event) {
//        // Add your resignation logic here (e.g., validation, database removal)
//        System.out.println("Pilot: " + currentPilot.getUsername() + " is resigning.");
//
//        try {
//            // Placeholder: Go to login after resignation
//            FXRouter.goTo("pilot-resign-page");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @FXML
    public void handleConfirmResignButton(ActionEvent event) {

        // 1. รับรหัสผ่านที่ผู้ใช้กรอก
        String plainPassword = passwordField.getText();


        User user = userRepository.findUserByUsername(currentPilot.getUsername());

        // 2. ล้างข้อความผิดพลาดเดิม และตรวจสอบว่า currentPilotUser ถูกโหลดมาแล้ว
        errorLabel.setText("");

        if (currentPilot == null) {
            errorLabel.setText("⚠️ ข้อผิดพลาดทางระบบ: ไม่พบข้อมูลผู้ใช้งานปัจจุบัน.");
            return;
        }

        // 3. ตรวจสอบความว่างเปล่า
        if (plainPassword.isEmpty()) {
            errorLabel.setText("⚠️ กรุณากรอกรหัสผ่านปัจจุบันเพื่อยืนยันการลาออก.");
            return;
        }

        // 4. ตรวจสอบรหัสผ่าน
        // **สำคัญ:** ในระบบจริง ต้องใช้การแฮช (Hashing) เช่น BCrypt ในการเปรียบเทียบรหัสผ่าน
        // ในที่นี้ เราใช้เมธอด getPassword() ของ User model เพื่อจำลองการเปรียบเทียบ
        if (!user.validatePassword(plainPassword)) {

            // รหัสผ่านไม่ถูกต้อง: แสดงข้อผิดพลาดและยกเลิกการลาออก
            errorLabel.setText("❌ รหัสผ่านไม่ถูกต้อง กรุณาตรวจสอบและลองอีกครั้ง.");
            passwordField.setText(""); // ล้างรหัสผ่านที่กรอกเพื่อความปลอดภัย
            return;
        }

        // --- รหัสผ่านถูกต้อง: ดำเนินการตามตรรกะการลาออก (Resignation Logic) ---
        System.out.println("Pilot: " + currentPilot.getUsername() + " is resigning.");

        try {
            // [*** วางโค้ดเรียก Resignation Service ของคุณที่นี่ ***]
            // Example: resignationService.processResignation(currentPilotUser.getPilotID());

            // Placeholder: ไปหน้า Login หลังจากลาออกสำเร็จ
            FXRouter.goTo("pilot-resign-page");

        } catch (IOException e) {
            System.err.println("Error navigating after successful resignation.");
            throw new RuntimeException(e);
        }
    }

    // **FIXED:** Added handleCancelButton
    @FXML
    public void handleCancelButton() {
        try {
            // Navigate back to the pilot's main page
            FXRouter.goTo("pilot-main-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to navigate to main page.", e);
        }
    }


    // --- Sidebar Handlers ---

    @FXML
    public void handleHomepageButton() {
        try {
            FXRouter.goTo("pilot-main-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleScheduleButton() {
        try {
            FXRouter.goTo("pilot-schedule-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReportButton() {
        try {
            FXRouter.goTo("pilot-report-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleNotificationButton() {
        try {
            // **Crucial:** Always pass the currentPilot object
            FXRouter.goTo("pilot-notification-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleResignButton() {
        try {
            FXRouter.goTo("pilot-resign-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    public void handleLogoutButton() {
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}