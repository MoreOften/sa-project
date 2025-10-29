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
import ku.cs.services.user.UserRepository;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.supervisor.SupervisorRepository;

import ku.cs.services.FXRouter;
import java.io.IOException;

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
        try {
            String usernameText = giveUsernameTextField.getText();
            String passwordText = givePasswordTextField.getText();

            // --- เปลี่ยนจากการ login ด้วย List มาเป็น Repository ---
            // เมธอด .login() ใน Repository จะทำการ query ฐานข้อมูล
            User user = userRepository.login(usernameText, passwordText);
            // --- สิ้นสุดการเปลี่ยนแปลง ---

            if (user != null && user.getHasAccess()) {
                handleLoginBasedOnRole(user);
            }
            else if (user != null && user.getHasAccess() == false) {
                errorLabel.setText("You are banned.");
            }
            else {
                errorLabel.setText("Wrong username or password");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
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

//        if (pilot.getFirstTimeLogin()) {
//            // ไม่ต้องมีการ writeData() ที่นี่
//            // หน้า "set-password" จะเป็นคนรับผิดชอบในการอัปเดต DB
//            FXRouter.goTo("set-password", user);
//        } else {
//            FXRouter.goTo("home-pilot", user);
//        }

        // ลบ userListDatasource.writeData(userList) ออก
        // (การอัปเดต 'last_login' ควรเกิดขึ้นภายใน userRepository.login())
    }


    private void handleInstructorLogin(User user) throws IOException {
        // ค้นหา Instructor จาก DB ผ่าน Repository
        Instructor instructor = instructorRepository.findInstructorByUsername(user.getUsername());

        if (instructor == null) {
            errorLabel.setText("Instructor profile not found for user: " + user.getUsername());
            return;
        }

//        if (instructor.getFirstTimeLogin()) {
//            // หน้า "set-password" จะเป็นคนรับผิดชอบในการอัปเดต DB
//            FXRouter.goTo("set-password", user);
//        } else {
//            FXRouter.goTo("home-instructor", user);
//        }
        // ลบ ...Datasource.writeData(...) ออก
    }


    private void handleSupervisorLogin(User user) throws IOException {
        // ค้นหา Supervisor จาก DB ผ่าน Repository
        Supervisor supervisor = supervisorRepository.findSupervisorByUsername(user.getUsername());

        if (supervisor == null) {
            errorLabel.setText("Supervisor profile not found for user: " + user.getUsername());
            return;
        }

//        if (supervisor.getFirstTimeLogin()) {
//            // หน้า "set-password" จะเป็นคนรับผิดชอบในการอัปเดต DB
//            FXRouter.goTo("set-password", user);
//        } else {
//            FXRouter.goTo("home-supervisor", user);
//        }
        // ลบ ...Datasource.writeData(...) ออก
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