package ku.cs.controllers.login;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField; // <-- 1. เพิ่ม Import
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ku.cs.models.user.User;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.services.UserSession;
import ku.cs.services.user.UserRepository;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.supervisor.SupervisorRepository;
import ku.cs.services.FXRouter;

import java.io.IOException;
// ไม่จำเป็นต้องใช้ LocalDateTime ที่นี่แล้ว (ถ้าไม่ได้ใช้)
// import java.time.LocalDateTime;

public class LoginController {
    @FXML private TextField giveUsernameTextField;
    @FXML private PasswordField givePasswordTextField; // <-- 2. เปลี่ยนเป็น PasswordField
    @FXML private Label errorLabel;
    @FXML private ImageView loginLogoImageView;

    private UserRepository userRepository;
    private PilotRepository pilotRepository;
    private InstructorRepository instructorRepository;
    private SupervisorRepository supervisorRepository;

    @FXML private void initialize() {
        errorLabel.setText("");
        // แก้ไขการโหลดรูปภาพให้ปลอดภัยมากขึ้น (เผื่อ build เป็น .jar)
        try {
            Image image = new Image(getClass().getResource("/images/login-logo.png").toExternalForm());
            loginLogoImageView.setImage(image);
        } catch (Exception e) {
            System.err.println("ไม่สามารถโหลดรูปภาพ login-logo.png");
            e.printStackTrace();
        }


        // --- เปลี่ยนจากการอ่านไฟล์ มาเป็นการสร้าง instance ของ Repository ---
        userRepository = new UserRepository();
        pilotRepository = new PilotRepository();
        instructorRepository = new InstructorRepository();
        supervisorRepository = new SupervisorRepository();
        // --- สิ้นสุดการเปลี่ยนแปลง ---
    }

    @FXML
    public void onLoginButtonClick() {
        String username = giveUsernameTextField.getText();
        String plainPassword = givePasswordTextField.getText(); // .getText() ใช้ได้เหมือนกัน

        // 1. ค้นหา User จาก Repo
        User user = userRepository.findUserByUsername(username);

        if (user != null) {
            // 2. (ตรรกะใหม่) ให้ Model ตรวจสอบรหัสผ่านเอง
            if (user.validatePassword(plainPassword)) {
                // Login สำเร็จ!
                errorLabel.setText(""); // ล้าง error ถ้ามี

                // 4. เก็บ Session
                UserSession.getInstance().setCurrentUser(user);

                try {
                    handleLoginBasedOnRole(user);
                } catch (IOException e) {
                    System.err.println("ไม่สามารถโหลดหน้าหลักตาม Role: " + e.getMessage());
                    e.printStackTrace();
                    errorLabel.setText("ไม่สามารถโหลดหน้าถัดไปได้");
                }
            } else {
                // Password ผิด
                errorLabel.setText("Invalid username or password."); // <-- 3. เพิ่มการแจ้งเตือน
            }
        } else {
            // Username ผิด
            errorLabel.setText("Invalid username or password."); // <-- 3. เพิ่มการแจ้งเตือน
        }
    }


    private void handleLoginBasedOnRole(User user) throws IOException {
        switch (user.getRole()) {
            case "pilot":
                handlePilotLogin(user);
                break;
            case "instructor":
                handleInstructorLogin(user);
                break;
            case "supervisor":
                handleSupervisorLogin(user);
                break;
            default:
                System.out.println(user.getRole());
                errorLabel.setText("Unknown role");
                break;
        }
    }

    // --- เปิดการใช้งานและแก้ไขเมธอด handle...Login ---

    private void handlePilotLogin(User user) throws IOException {
        Pilot pilot = pilotRepository.findPilotByUsername(user.getUsername());
        if (pilot == null) {
            errorLabel.setText("Pilot profile not found for user: " + user.getUsername());
            return;
        }
        // เราสามารถส่งข้อมูล object ไปยัง Controller ถัดไปได้
        FXRouter.goTo("pilot-home-page", pilot);
    }


    private void handleInstructorLogin(User user) throws IOException {
        Instructor instructor = instructorRepository.findInstructorByUsername(user.getUsername());
        if (instructor == null) {
            errorLabel.setText("Instructor profile not found for user: " + user.getUsername());
            return;
        }
        FXRouter.goTo("instructor-main-page", instructor);
    }


    private void handleSupervisorLogin(User user) throws IOException {
        Supervisor supervisor = supervisorRepository.findSupervisorByUsername(user.getUsername());
        if (supervisor == null) {
            errorLabel.setText("Supervisor profile not found for user: " + user.getUsername());
            return;
        }
        FXRouter.goTo("supervisor-home-page", supervisor);
    }

    // --- สิ้นสุดการแก้ไข ---


    @FXML
    public void onRegisterButtonClick() {
        try {
            FXRouter.goTo("register");
        } catch (IOException e) {
            // ควรใช้ System.err.println หรือ Logger แทน throw RuntimeException
            System.err.println("ไม่สามารถไปหน้า register: " + e.getMessage());
            e.printStackTrace();
        }
    }
}