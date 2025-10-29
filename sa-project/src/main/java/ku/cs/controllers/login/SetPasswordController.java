package ku.cs.controllers.login;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;

// --- ลบ import ของ Datasource และ List ทั้งหมด ---
// ...

// --- เพิ่ม import ของ Repositories ---
import ku.cs.services.user.UserRepository;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.supervisor.SupervisorRepository;

import java.io.IOException;

public class SetPasswordController {
    @FXML private TextField givePasswordTextField;
    @FXML private TextField giveConfirmPasswordTextField;
    @FXML private Label errorLabel;

    // ข้อมูล User ที่ส่งมาจากหน้า Login
    private User user;
    private String role;

    // --- เปลี่ยนจาก List/Datasource เป็น Repositories ---
    private UserRepository userRepository;
    private PilotRepository pilotRepository;
    private InstructorRepository instructorRepository;
    private SupervisorRepository supervisorRepository;
    // --- สิ้นสุดการเปลี่ยนแปลง ---


    @FXML
    public void initialize() {
        errorLabel.setText("");

        // ดึงข้อมูล user ที่ส่งมาจาก FXRouter
        user = (User) FXRouter.getData();
        if (user == null) {
            errorLabel.setText("Error: Cannot get user data.");
            return;
        }
        role = user.getRole();

        // --- สร้าง instance ของ Repositories ---
        userRepository = new UserRepository();
        pilotRepository = new PilotRepository();
        instructorRepository = new InstructorRepository();
        supervisorRepository = new SupervisorRepository();
        // --- สิ้นสุดการเปลี่ยนแปลง ---
    }


    @FXML
    public void onConfirmButtonClick() {
        String passwordText = givePasswordTextField.getText();
        String confirmPasswordText = giveConfirmPasswordTextField.getText();

        if (passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
            errorLabel.setText("Please fill in both fields.");
            return;
        }

        if (passwordText.equals(confirmPasswordText)) {
            try {
                // 1. อัปเดตรหัสผ่านในตาราง users หลัก
                userRepository.updatePassword(user.getUsername(), passwordText);

                // 2. อัปเดตรหัสผ่าน (ถ้ามี) และสถานะ firstTimeLogin ในตารางของแต่ละ Role
                switch (role) {
                    case "pilot":
                        // แก้ไข typo (pilq) และเปลี่ยนไปใช้ Repository
                        pilotRepository.updatePasswordAndStatus(user.getUsername(), passwordText);
                        FXRouter.goTo("home-pilot", user);
                        break;
                    case "instructor":
                        instructorRepository.updatePasswordAndStatus(user.getUsername(), passwordText);
                        FXRouter.goTo("home-instructor", user);
                        break;
                    case "supervisor":
                        supervisorRepository.updatePasswordAndStatus(user.getUsername(), passwordText);
                        FXRouter.goTo("home-supervisor", user);
                        break;
                    default:
                        errorLabel.setText("Unknown user role: " + role);
                        break;
                }

            } catch (IOException e) {
                // Catch นี้สำหรับ FXRouter.goTo()
                throw new RuntimeException(e);
            } catch (Exception e) {
                // Catch นี้สำหรับข้อผิดพลาดจาก Database ที่ Repository อาจโยนมา
                errorLabel.setText("Database update failed: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Passwords do not match");
        }
    }


    @FXML
    public void onBackButtonClick() {
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}