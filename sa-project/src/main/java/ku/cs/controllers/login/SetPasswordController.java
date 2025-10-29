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
                // 1. (แก้ไข) อัปเดต Model ก่อน
                // user ที่เราได้มาจาก FXRouter จะถูกอัปเดต
                // และเมธอด .setPassword() จะทำการ HASH ให้เองอัตโนมัติ!
                user.setPassword(passwordText);

                // 2. (แก้ไข) ให้ Repository บันทึก Model ที่อัปเดตแล้ว
                // คุณต้องสร้างเมธอด updateUser(User user) ใน UserRepository
                // ซึ่งจะเซฟ Hashed Password (จาก user.getPassword()) ลง DB
                userRepository.updateUser(user);

                // (ลบ) userRepository.updatePassword(user.getUsername(), passwordText); // <--- (วิธีเก่า ไม่ปลอดภัย)

                // 3. (แก้ไข) อัปเดต "สถานะ" ในตาราง Role (ไม่ส่งรหัสผ่านไปแล้ว)
                // เราแค่ต้องการบอกว่า "คนนี้ตั้งรหัสผ่านครั้งแรกแล้ว"
                switch (role) {
                    case "pilot":
                        // คุณต้องสร้างเมธอดนี้ใน PilotRepository
                        pilotRepository.updateStatusAfterFirstLogin(user.getUsername());
                        FXRouter.goTo("home-pilot", user); // หรือส่ง "pilot" profile ไปแทน
                        break;
                    case "instructor":
                        instructorRepository.updateStatusAfterFirstLogin(user.getUsername());
                        FXRouter.goTo("home-instructor", user);
                        break;
                    case "supervisor":
                        supervisorRepository.updateStatusAfterFirstLogin(user.getUsername());
                        FXRouter.goTo("home-supervisor", user);
                        break;
                    default:
                        errorLabel.setText("Unknown user role: " + role);
                        break;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
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