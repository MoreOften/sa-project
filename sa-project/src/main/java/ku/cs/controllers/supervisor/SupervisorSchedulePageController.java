package ku.cs.controllers.supervisor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert; // เพิ่ม
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.user.UserRepository;

// --- [Import ที่ต้องเพิ่ม] ---
import ku.cs.services.email.EmailService;
import ku.cs.services.notification.NotificationRepository;
import ku.cs.models.notification.Notification;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.supervisor.SupervisorRepository;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.instructor.Instructor;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SupervisorSchedulePageController {

    @FXML private ImageView logoImageView;
    @FXML private TableView<ScheduleView> scheduleTableView;
    @FXML private TableColumn<ScheduleView, String> colScheduleId;
    @FXML private TableColumn<ScheduleView, String> colPilot1;
    @FXML private TableColumn<ScheduleView, String> colPilot2;
    @FXML private TableColumn<ScheduleView, String> colDateTime;
    @FXML private TableColumn<ScheduleView, String> colProgram;
    @FXML private TableColumn<ScheduleView, String> colInstructor;

    private Supervisor currentSupervisor;
    private ScheduleRepository scheduleRepository;
    private UserRepository userRepository;
    private ObservableList<ScheduleView> scheduleViewList = FXCollections.observableArrayList();

    // --- [เพิ่มตัวแปร Services] ---
    private PilotRepository pilotRepository;
    private InstructorRepository instructorRepository;
    private NotificationRepository notificationRepository;
    private EmailService emailService;

    @FXML
    public void initialize() {
        // 1. ดึงข้อมูล Supervisor ที่ login อยู่
        User loggedInUser = UserSession.getInstance().getCurrentUser();

        if (loggedInUser instanceof Supervisor) {
            this.currentSupervisor = (Supervisor) loggedInUser;
        }

        // 2. สร้าง instance ของ Repositories เดิม
        scheduleRepository = new ScheduleRepository();
        userRepository = new UserRepository();

        // --- [เพิ่มการสร้าง Instance ใหม่] ---
        pilotRepository = new PilotRepository();
        instructorRepository = new InstructorRepository();
        notificationRepository = new NotificationRepository();
        // สร้าง EmailService (ต้องส่ง Repository เข้าไปตาม Constructor)
        emailService = new EmailService(instructorRepository, new SupervisorRepository());

        // 3. ตั้งค่าคอลัมน์ในตาราง
        setupTableColumns();

        // 4. โหลดข้อมูลตาราง (ถ้ามี Supervisor)
        if (this.currentSupervisor != null) {
            loadScheduleData();
        }
    }

    // ... (เมธอด setupTableColumns, loadScheduleData เดิม ไม่ต้องแก้) ...
    private void setupTableColumns() {
        colScheduleId.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        colPilot1.setCellValueFactory(new PropertyValueFactory<>("pilot1Name"));
        colPilot2.setCellValueFactory(new PropertyValueFactory<>("pilot2Name"));
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colInstructor.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
    }

    private void loadScheduleData() {
        scheduleViewList.clear();
        List<Schedule> schedules = scheduleRepository.findSchedulesBySupervisor(currentSupervisor.getSupervisorID());
        for (Schedule s : schedules) {
            User pilot1 = userRepository.findUserByUsername(s.getPilotId1());
            User pilot2 = userRepository.findUserByUsername(s.getPilotId2());
            User instructor = userRepository.findUserByUsername(s.getInstructorId());

            String p1Name = (pilot1 != null) ? pilot1.getName() : "N/A";
            String p2Name = (pilot2 != null) ? pilot2.getName() : "N/A";
            String instructorName = (instructor != null) ? instructor.getName() : "N/A";

            scheduleViewList.add(new ScheduleView(s, p1Name, p2Name, instructorName));
        }
        scheduleTableView.setItems(scheduleViewList);
    }

    // --- [เพิ่มเมธอดปุ่ม Notify และ Helper Function] ---
    @FXML
    public void onNotifyButtonClick() {
        // 1. ตรวจสอบว่ามีการเลือกตารางหรือไม่
        ScheduleView selectedView = scheduleTableView.getSelectionModel().getSelectedItem();
        if (selectedView == null) {
            showAlert("Warning", "กรุณาเลือกตารางที่ต้องการแจ้งเตือน");
            return;
        }

        // 2. ดึงข้อมูล Schedule ตัวเต็มจาก ID
        Schedule schedule = scheduleRepository.findScheduleById(selectedView.getScheduleId());
        if (schedule == null) return;

        // 3. ดึงข้อมูล Instructor และ Pilot
        Instructor instructor = instructorRepository.findInstructorById(schedule.getInstructorId());
        Pilot pilot1 = pilotRepository.findPilotById(schedule.getPilotId1());
        Pilot pilot2 = pilotRepository.findPilotById(schedule.getPilotId2());

        // 4. ส่ง In-App Notification
        String subject = "แจ้งเตือนการฝึก: " + schedule.getPracticeProgram();
        String content = "คุณมีตารางฝึกวันที่ " + schedule.getScheduleDate() + " เวลา " + schedule.getScheduleTime();
        String senderId = currentSupervisor.getUsername();

        if (instructor != null) createNotification(instructor.getUsername(), senderId, subject, content);
        if (pilot1 != null) createNotification(pilot1.getUsername(), senderId, subject, content);
        if (pilot2 != null) createNotification(pilot2.getUsername(), senderId, subject, content);

        // 5. ส่ง Email (เรียกใช้เมธอดที่เราเพิ่งเพิ่มใน EmailService)
        String instructorEmail = (instructor != null) ? instructor.getEmail() : null;
        String p1Email = (pilot1 != null) ? pilot1.getEmail() : null;
        String p2Email = (pilot2 != null) ? pilot2.getEmail() : null;

        emailService.sendScheduleNotification(schedule, instructorEmail, p1Email, p2Email);

        showAlert("Success", "ส่งการแจ้งเตือนเรียบร้อยแล้ว");
    }

    private void createNotification(String recipientId, String senderId, String subject, String content) {
        // ใช้ Constructor ที่มี 5 parameters ตาม Notification Model ที่มีอยู่
        Notification notif = new Notification(recipientId, senderId, subject, content, "Schedule Alert");
        notificationRepository.save(notif);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ... (เมธอด Navigation อื่นๆ และ Inner Class ScheduleView คงเดิม) ...
    @FXML public void onHomepageButtonClick() { navigate("supervisor-main-page"); }
    @FXML public void onScheduleButtonClick() { loadScheduleData(); }
    @FXML public void onReportButtonClick() { navigate("supervisor-report-page"); }
    @FXML public void onLogoutButtonClick() {
        UserSession.getInstance().clearSession();
        try { FXRouter.goTo("login"); } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML public void onCreateScheduleButtonClick() {
        try { FXRouter.goTo("supervisor-create-schedule"); } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigate(String route) {
        try { FXRouter.goTo(route, currentSupervisor); } catch (IOException e) { e.printStackTrace(); }
    }

    public static class ScheduleView {
        private String scheduleId;
        private String pilot1Name;
        private String pilot2Name;
        private String dateTime;
        private String programName;
        private String instructorName;

        public ScheduleView(Schedule schedule, String pilot1Name, String pilot2Name, String instructorName) {
            this.scheduleId = schedule.getScheduleId();
            this.pilot1Name = pilot1Name;
            this.pilot2Name = pilot2Name;
            this.programName = schedule.getPracticeProgram();
            this.instructorName = instructorName;

            if (schedule.getScheduleDate() != null && schedule.getScheduleTime() != null) {
                this.dateTime = schedule.getScheduleDate() + " " + schedule.getScheduleTime();
            } else {
                this.dateTime = "N/A";
            }
        }

        public String getScheduleId() { return scheduleId; }
        public String getPilot1Name() { return pilot1Name; }
        public String getPilot2Name() { return pilot2Name; }
        public String getDateTime() { return dateTime; }
        public String getProgramName() { return programName; }
        public String getInstructorName() { return instructorName; }
    }
}