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

    // ใน: LoginController.java
    @FXML
    public void onLoginButtonClick() {
        String username = giveUsernameTextField.getText();
        String plainPassword = givePasswordTextField.getText();

        User user = userRepository.findUserByUsername(username);

        if (user != null) {
            if (user.validatePassword(plainPassword)) {
                // Login สำเร็จ!
                try {
                    // ไปเรียกเมธอดแยก Role
                    handleLoginBasedOnRole(user);
                } catch (IOException e) {
                    System.err.println("ไม่สามารถโหลดหน้าหลักตาม Role: " + e.getMessage());
                    e.printStackTrace();
                    errorLabel.setText("ไม่สามารถโหลดหน้าถัดไปได้");
                }
            } else {
                errorLabel.setText("Username หรือ Password ไม่ถูกต้อง");
            }
        } else {
            errorLabel.setText("Username หรือ Password ไม่ถูกต้อง");
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

        // VVVV (1. ต้องมีบรรทัดนี้) VVVV
        UserSession.getInstance().setCurrentUser(pilot); // <-- ต้องเก็บ "pilot"

        FXRouter.goTo("pilot-home-page", pilot);
    }


    private void handleInstructorLogin(User user) throws IOException {
        Instructor instructor = instructorRepository.findInstructorByUsername(user.getUsername());

        if (instructor == null) {
            errorLabel.setText("Instructor profile not found for user: " + user.getUsername());
            return;
        }

        // VVVV (2. ต้องมีบรรทัดนี้) VVVV
        UserSession.getInstance().setCurrentUser(instructor); // <-- ต้องเก็บ "instructor"

        FXRouter.goTo("instructor-main-page", instructor);
    }


    private void handleSupervisorLogin(User user) throws IOException {
        Supervisor supervisor = supervisorRepository.findSupervisorByUsername(user.getUsername());

        if (supervisor == null) {
            errorLabel.setText("Supervisor profile not found for user: " + user.getUsername());
            return;
        }

        // VVVV (3. ต้องมีบรรทัดนี้) VVVV
        UserSession.getInstance().setCurrentUser(supervisor); // <-- ต้องเก็บ "supervisor"

        // --- VVVV แก้ไขบรรทัดนี้ VVVV ---
        FXRouter.goTo("supervisor-main-page", supervisor); // เปลี่ยนจาก "supervisor-home-page"
        // --- ^^^^ สิ้นสุดการแก้ไข ^^^^ ---
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