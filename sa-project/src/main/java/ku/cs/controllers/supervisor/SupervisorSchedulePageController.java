package ku.cs.controllers.supervisor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

import java.io.IOException;
import java.time.LocalDateTime;
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
    @FXML private TableColumn<ScheduleView, String> colInstructor; // ใน FXML คือ "Instructor ID" แต่เราจะแสดงเป็น "Instructor Name"

    private Supervisor currentSupervisor;
    private ScheduleRepository scheduleRepository;
    private UserRepository userRepository;
    private ObservableList<ScheduleView> scheduleViewList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. ดึงข้อมูล Supervisor ที่ login อยู่
        User loggedInUser = UserSession.getInstance().getCurrentUser();

        if (loggedInUser instanceof Supervisor) {
            this.currentSupervisor = (Supervisor) loggedInUser;
        } else if (loggedInUser != null) {
            System.err.println("SupervisorSchedulePage Error: User in session is " + loggedInUser.getClass().getName() + ", not Supervisor.");
        } else {
            System.err.println("SupervisorSchedulePage Error: No user data in session.");
        }

        // 2. สร้าง instance ของ Repositories
        scheduleRepository = new ScheduleRepository();
        userRepository = new UserRepository();

        // 3. ตั้งค่าคอลัมน์ในตาราง
        setupTableColumns();

        // 4. โหลดข้อมูลตาราง (ถ้ามี Supervisor)
        if (this.currentSupervisor != null) {
            loadScheduleData();
        } else {
            System.err.println("SupervisorSchedulePage: currentSupervisor เป็น null, ไม่สามารถโหลดข้อมูลได้");
        }
    }

    /**
     * ตั้งค่า CellValueFactory เพื่อผูกข้อมูลใน Model (ScheduleView) กับ Column
     */
    private void setupTableColumns() {
        colScheduleId.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        colPilot1.setCellValueFactory(new PropertyValueFactory<>("pilot1Name"));
        colPilot2.setCellValueFactory(new PropertyValueFactory<>("pilot2Name"));
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colInstructor.setCellValueFactory(new PropertyValueFactory<>("instructorName")); // ผูกกับ "instructorName"
    }

    /**
     * โหลดข้อมูล Schedule จาก Repository
     * และแปลงเป็น ScheduleView (ที่มีชื่อ)
     */
    private void loadScheduleData() {
        scheduleViewList.clear();

        // 1. ดึง Schedule ทั้งหมดที่ Supervisor คนนี้สร้าง
        // [ข้อควรระวัง] คุณต้องเพิ่มเมธอด findSchedulesBySupervisor ใน ScheduleRepository เอง
        // List<Schedule> schedules = scheduleRepository.findSchedulesBySupervisor(currentSupervisor.getSupervisorID());

        // [การจำลองข้อมูล] เนื่องจากเมธอดยังไม่มี ผมจะสร้าง List ว่างๆ ไว้ก่อน
        // เมื่อคุณเพิ่มเมธอดใน Repository แล้ว ให้ลบ 2 บรรทัดล่าง แล้ว uncomment บรรทัดบน
        List<Schedule> schedules = new java.util.ArrayList<>();
        System.err.println("[TODO] SupervisorSchedulePage: โปรดเพิ่มเมธอด findSchedulesBySupervisor ใน ScheduleRepository");


        // 2. วนลูปเพื่อดึง "ชื่อ" ของ Pilot และ Instructor
        for (Schedule s : schedules) {
            User pilot1 = userRepository.findUserByUsername(s.getPilotId1());
            User pilot2 = userRepository.findUserByUsername(s.getPilotId2());
            User instructor = userRepository.findUserByUsername(s.getInstructorId());

            // 3. จัดการกรณีหา User ไม่เจอ
            String p1Name = (pilot1 != null) ? pilot1.getName() : "N/A";
            String p2Name = (pilot2 != null) ? pilot2.getName() : "N/A";
            String instructorName = (instructor != null) ? instructor.getName() : "N/A"; // (จาก ID เป็น Name)

            // 4. สร้าง ScheduleView และเพิ่มลงใน List
            scheduleViewList.add(new ScheduleView(s, p1Name, p2Name, instructorName));
        }

        // 5. แสดงผลบนตาราง
        scheduleTableView.setItems(scheduleViewList);
    }

    // --- (Inner Class) คลาสสำหรับแสดงผลในตาราง ---
    // (คัดลอกมาจาก InstructorSchedulePageController และดัดแปลงเล็กน้อย)

    public static class ScheduleView {
        private String scheduleId;
        private String pilot1Name;
        private String pilot2Name;
        private String dateTime;
        private String programName;
        private String instructorName; // เปลี่ยนจาก supervisorId เป็น instructorName

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        public ScheduleView(Schedule schedule, String pilot1Name, String pilot2Name, String instructorName) {
            this.scheduleId = schedule.getScheduleId();
            this.pilot1Name = pilot1Name;
            this.pilot2Name = pilot2Name;
            this.programName = schedule.getProgramName();
            this.instructorName = instructorName; // รับชื่อ Instructor

            if (schedule.getTrainingTimestamp() != null) {
                this.dateTime = schedule.getTrainingTimestamp().format(formatter);
            } else {
                this.dateTime = "N/A";
            }
        }

        // --- Getters (สำคัญมากสำหรับ PropertyValueFactory) ---
        public String getScheduleId() { return scheduleId; }
        public String getPilot1Name() { return pilot1Name; }
        public String getPilot2Name() { return pilot2Name; }
        public String getDateTime() { return dateTime; }
        public String getProgramName() { return programName; }
        public String getInstructorName() { return instructorName; } // Getter สำหรับ Instructor
    }


    // --- (Navigation) เมธอดสำหรับปุ่ม Sidebar ---

    @FXML
    public void onHomepageButtonClick() {
        try {
            // [ข้อควรระวัง] คุณต้องเพิ่ม Route "supervisor-main-page" ใน MainApplication.java
            FXRouter.goTo("supervisor-main-page", currentSupervisor);
        } catch (IOException e) {
            System.err.println("Error navigating to supervisor-main-page: " + e.getMessage());
        }
    }

    @FXML
    public void onScheduleButtonClick() {
        // อยู่หน้านี้แล้ว ไม่ต้องทำอะไร (หรือจะ refresh ก็ได้)
        System.out.println("Already on Supervisor Schedule Page.");
        loadScheduleData(); // (ตัวอย่างการ refresh)
    }

    @FXML
    public void onReportButtonClick() {
        try {
            // [ข้อควรระวัง] คุณต้องเพิ่ม Route "supervisor-report-page" ใน MainApplication.java
            FXRouter.goTo("supervisor-report-page", currentSupervisor);
        } catch (IOException e) {
            System.err.println("Error navigating to supervisor-report-page: " + e.getMessage());
        }
    }

    @FXML
    public void onLogoutButtonClick() {
        try {
            UserSession.getInstance().clearSession(); // เคลียร์ Session
            FXRouter.goTo("login");
        } catch (IOException e) {
            System.err.println("Error logging out: " + e.getMessage());
        }
    }
}