package ku.cs.controllers.login;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ku.cs.models.user.User;
// *** ลบ imports ที่เกี่ยวกับ FileDatasource ออก ***

// *** สมมติว่าคุณมี Model เหล่านี้ (จากโค้ดเดิมของคุณ) ***
import ku.cs.models.pilot.Pilot;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.supervisor.Supervisor;

// *** เพิ่ม imports สำหรับ Repository ที่จะสร้างขึ้นใหม่ ***
// (เราจะสร้างคลาสเหล่านี้ในขั้นตอนถัดไป)
import ku.cs.services.UserSession;
import ku.cs.services.user.UserRepository;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.supervisor.SupervisorRepository;

import ku.cs.services.FXRouter;
import java.io.IOException;
import java.time.LocalDateTime;

public class LoginController {
    @FXML private TextField giveUsernameTextField;
    @FXML private TextField givePasswordTextField;
    @FXML private Label errorLabel;
    @FXML private ImageView loginLogoImageView;

    // --- เปลี่ยนจาก List และ Datasource มาเป็น Repository ---
    // เราไม่จำเป็นต้องเก็บ List ไว้ใน Controller อีกต่อไป
    // Repository จะทำหน้าที่ดึงข้อมูลจาก DB เมื่อต้องการ
    private UserRepository userRepository;
    private PilotRepository pilotRepository;
    private InstructorRepository instructorRepository;
    private SupervisorRepository supervisorRepository;
    // --- สิ้นสุดการเปลี่ยนแปลง ---

    @FXML private void initialize() {
        errorLabel.setText("");
        Image image = new Image(getClass().getResource("/images/login-logo.png").toString());
        loginLogoImageView.setImage(image);

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
        String plainPassword = givePasswordTextField.getText();

        // 1. ค้นหา User จาก Repo
        User user = userRepository.findUserByUsername(username);

        if (user != null) {
            // 2. (ตรรกะใหม่) ให้ Model ตรวจสอบรหัสผ่านเอง
            if (user.validatePassword(plainPassword)) {
                // Login สำเร็จ!

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
            }
        } else {
            // Username ผิด
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
        // ค้นหา Pilot จาก DB ผ่าน Repository
        Pilot pilot = pilotRepository.findPilotByUsername(user.getUsername());

        if (pilot == null) {
            errorLabel.setText("Pilot profile not found for user: " + user.getUsername());
            return;
        }

        // สำคัญ: เมื่อเจอแล้ว ให้ไปยังหน้า Pilot
        // และ "ส่ง" object pilot ที่เพิ่งเจอ ไปให้หน้าถัดไป
        FXRouter.goTo("pilot-home-page");
    }


    private void handleInstructorLogin(User user) throws IOException {
        // ค้นหา Instructor จาก DB ผ่าน Repository
        Instructor instructor = instructorRepository.findInstructorByUsername(user.getUsername());

        if (instructor == null) {
            errorLabel.setText("Instructor profile not found for user: " + user.getUsername());
            return;
        }

        // สำคัญ: เมื่อเจอแล้ว ให้ไปยังหน้า Instructor
        // และ "ส่ง" object instructor ที่เพิ่งเจอ ไปให้หน้าถัดไป
        FXRouter.goTo("instructor-home-page");
    }


    private void handleSupervisorLogin(User user) throws IOException {
        // ค้นหา Supervisor จาก DB ผ่าน Repository
        Supervisor supervisor = supervisorRepository.findSupervisorByUsername(user.getUsername());

        if (supervisor == null) {
            errorLabel.setText("Supervisor profile not found for user: " + user.getUsername());
            return;
        }

        // สำคัญ: เมื่อเจอแล้ว ให้ไปยังหน้า Supervisor
        // และ "ส่ง" object supervisor ที่เพิ่งเจอ ไปให้หน้าถัดไป
        FXRouter.goTo("supervisor-home-page");
    }

    // --- สิ้นสุดการแก้ไข ---


    @FXML
    public void onRegisterButtonClick() {
        try {
            FXRouter.goTo("register");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}