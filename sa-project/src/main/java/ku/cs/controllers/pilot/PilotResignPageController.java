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
import ku.cs.services.pilot.ResignationService;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.supervisor.SupervisorRepository;
import ku.cs.services.email.EmailService;
import ku.cs.services.notification.NotificationRepository;
import javafx.scene.control.Alert;

import java.io.IOException;

public class PilotResignPageController {

    @FXML Label pilotNameLabel;
    @FXML private TextArea reasonTextArea;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML ImageView instructorImageView;
    @FXML ImageView logoImageView;

    private Pilot currentPilot;
    private PilotRepository pilotRepository;
    private UserRepository userRepository;

    private ResignationService resignationService;
    private ScheduleRepository scheduleRepository;
    private InstructorRepository instructorRepository;
    private SupervisorRepository supervisorRepository;
    private NotificationRepository notificationRepository;

    @FXML
    public void initialize() {
        pilotRepository = new PilotRepository();
        userRepository = new UserRepository();

        scheduleRepository = new ScheduleRepository();
        instructorRepository = new InstructorRepository();
        supervisorRepository = new SupervisorRepository();
        notificationRepository = new NotificationRepository();

        EmailService emailService = new EmailService(instructorRepository, supervisorRepository);
        resignationService = new ResignationService(pilotRepository, scheduleRepository, emailService, notificationRepository);

        errorLabel.setText("");

        Object data = FXRouter.getData();

        if (data instanceof Pilot) {
            this.currentPilot = (Pilot) data;
        } else if (data instanceof User) {
            User user = (User) data;
            this.currentPilot = pilotRepository.findPilotByUsername(user.getUsername());
        }

        if (this.currentPilot != null) {
            showPilotData();
        } else {
            clearLabel();
            pilotNameLabel.setText("Error: Cannot get user data.");
        }
    }

    private void showPilotData() {
        pilotNameLabel.setText(currentPilot.getName());
    }

    public void clearLabel() {
        pilotNameLabel.setText("");
        if (errorLabel != null) errorLabel.setText("");
    }

    @FXML
    public void handleConfirmResignButton(ActionEvent event) {

        String plainPassword = passwordField.getText();
        User user = userRepository.findUserByUsername(currentPilot.getUsername());

        errorLabel.setText("");

        if (currentPilot == null) {
            errorLabel.setText("⚠️ ข้อผิดพลาดทางระบบ: ไม่พบข้อมูลผู้ใช้งานปัจจุบัน.");
            return;
        }

        if (plainPassword.isEmpty()) {
            errorLabel.setText("⚠️ กรุณากรอกรหัสผ่านปัจจุบันเพื่อยืนยันการลาออก.");
            return;
        }

        if (!user.validatePassword(plainPassword)) {
            errorLabel.setText("❌ รหัสผ่านไม่ถูกต้อง กรุณาตรวจสอบและลองอีกครั้ง.");
            passwordField.setText("");
            return;
        }

        System.out.println("Pilot: " + currentPilot.getUsername() + " is resigning.");

        boolean success = resignationService.processResignation(currentPilot.getPilotID());

        if (success) {
            try {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("การลาออกเสร็จสมบูรณ์ ตารางฝึกที่เกี่ยวข้องถูกยกเลิกแล้ว");
                successAlert.showAndWait();

                FXRouter.goTo("pilot_resign_page");
            } catch (IOException e) {
                System.err.println("Error navigating after successful resignation.");
                throw new RuntimeException(e);
            }
        } else {
            errorLabel.setText("❌ การลาออกล้มเหลวเนื่องจากข้อผิดพลาดในระบบ.");
        }
    }

    @FXML
    public void handleCancelButton() {
        try {
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