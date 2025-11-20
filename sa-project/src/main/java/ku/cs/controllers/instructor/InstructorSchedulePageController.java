package ku.cs.controllers.instructor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.schedule.ScheduleRepository; // (1) Import Repo

import java.io.IOException;
import java.util.List;

// (3) เปลี่ยนจาก Schedule เป็น ScheduleView
public class InstructorSchedulePageController {
    @FXML TableView<ScheduleView> scheduleTableView;
    @FXML ImageView logoImageView;
    Instructor currentInstructor;

    // (4) เพิ่ม @FXML สำหรับ TableColumn ทั้ง 6
    @FXML private TableColumn<ScheduleView, String> colScheduleId;
    @FXML private TableColumn<ScheduleView, String> colPilot1;
    @FXML private TableColumn<ScheduleView, String> colPilot2;
    @FXML private TableColumn<ScheduleView, String> colDateTime;
    @FXML private TableColumn<ScheduleView, String> colProgram;
    @FXML private TableColumn<ScheduleView, String> colSupervisor;

    // (5) เพิ่ม Repositories และ ObservableList
    private ScheduleRepository scheduleRepository;
    private PilotRepository pilotRepository;
    private final ObservableList<ScheduleView> scheduleViewList = FXCollections.observableArrayList();

    public void initialize() {
        User loggedInUser = UserSession.getInstance().getCurrentUser();

        System.out.println("SCHEDULE_PAGE: กำลังโหลด..."); // <-- DEBUG 7

        if (loggedInUser instanceof Instructor) {
            this.currentInstructor = (Instructor) loggedInUser;
            System.out.println("SCHEDULE_PAGE: พบ Instructor ใน Session: " + currentInstructor.getUsername()); // <-- DEBUG 8
        } else if (loggedInUser != null) {
            System.err.println("SCHEDULE_PAGE: Error! ข้อมูลใน Session คือ " + loggedInUser.getClass().getName() + " ไม่ใช่ Instructor"); // <-- DEBUG 9
        } else {
            System.err.println("SCHEDULE_PAGE: Error! ไม่พบข้อมูลใดๆ ใน Session (UserSession = null)"); // <-- DEBUG 10
        }

        scheduleRepository = new ScheduleRepository();
        pilotRepository = new PilotRepository();
        setupTableColumns();

        if (this.currentInstructor != null) {
            System.out.println("SCHEDULE_PAGE: currentInstructor ไม่ใช่ null, กำลังโหลดข้อมูล..."); // <-- DEBUG 11
            loadScheduleData();
        } else {
            System.err.println("SCHEDULE_PAGE: currentInstructor เป็น null, ไม่สามารถโหลดข้อมูลได้"); // <-- DEBUG 12
        }
    }

    /**
     * ตั้งค่า CellValueFactory เพื่อผูกข้อมูลใน Model กับ Column
     */
    private void setupTableColumns() {
        colScheduleId.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        colPilot1.setCellValueFactory(new PropertyValueFactory<>("pilot1Name"));
        colPilot2.setCellValueFactory(new PropertyValueFactory<>("pilot2Name"));
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("practiceProgram"));
        colSupervisor.setCellValueFactory(new PropertyValueFactory<>("supervisorId"));
    }

    /**
     * โหลดข้อมูล Schedule จาก Repository
     * และแปลงเป็น ScheduleView (ที่มีชื่อ)
     */
    private void loadScheduleData() {
        scheduleViewList.clear();

        // 1. ดึง Schedule ทั้งหมดของ Instructor ที่ล็อกอินอยู่
        List<Schedule> schedules = scheduleRepository.findSchedulesByInstructor(currentInstructor.getInstructorID());

        // 2. วนลูปเพื่อดึง "ชื่อ" ของ Pilot และ Supervisor
        for (Schedule s : schedules) {
            // (นี่คือการทำ N+1 Query ซึ่งสำหรับโปรเจกต์ขนาดเล็กถือว่ายอมรับได้)
            Pilot pilot1 = pilotRepository.findPilotById(s.getPilotId1());
            Pilot pilot2 = pilotRepository.findPilotById(s.getPilotId2());

            // 3. (แก้ไขตามคำขอ) ใช้ ID ของ Supervisor
            String supervisorId = s.getSupervisorId();

            // 4. จัดการกรณีหา User ไม่เจอ (เช่น ถูกลบ)
            String p1Name = (pilot1 != null) ? pilot1.getName() : "N/A";
            String p2Name = (pilot2 != null) ? pilot2.getName() : "N/A";

            // 5. สร้าง ScheduleView และเพิ่มลงใน List
            scheduleViewList.add(new ScheduleView(s, p1Name, p2Name, supervisorId));
        }

        // 6. แสดงผลบนตาราง
        scheduleTableView.setItems(scheduleViewList);
    }

    @FXML
    public void handleHomepageButton() {
        try {
            FXRouter.goTo("instructor-main-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleScheduleButton() {
        try {
            FXRouter.goTo("instructor-schedule-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReportButton() {
        try {
            FXRouter.goTo("instructor-report-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleNotificationButton() {
        try {
            // อย่าลืมไปเพิ่ม route "instructor-notification-page" ใน MainApplication.java ด้วยนะครับ
            FXRouter.goTo("instructor-notification-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleLogoutButton() {
        try {
            UserSession.getInstance().clearSession(); // เคลียร์ Session ก่อนออก
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //
    // VVVV (8) คลาสภายในสำหรับ View Model VVVV
    //

    /**
     * คลาสภายใน (Inner Class) สำหรับเป็น "โมเดลแสดงผล" ในตาราง
     * คลาสนี้จะเก็บ "ชื่อ" แทน "ID"
     */
    public static class ScheduleView {
        private final String scheduleId;
        private final String pilot1Name;
        private final String pilot2Name;
        private final String dateTime;
        private final String practiceProgram;
        private final String supervisorId;

        public ScheduleView(Schedule schedule, String pilot1Name, String pilot2Name, String supervisorId) {
            this.scheduleId = schedule.getScheduleId();
            this.pilot1Name = pilot1Name;
            this.pilot2Name = pilot2Name;
            this.supervisorId = supervisorId;
            this.practiceProgram = schedule.getPracticeProgram();

            // แปลง LocalDateTime เป็น String ที่อ่านง่าย
            if (schedule.getScheduleDate() != null && !schedule.getScheduleDate().isEmpty()) {
                this.dateTime = schedule.getScheduleDate() + " " + schedule.getScheduleTime();
            } else {
                this.dateTime = "N/A";
            }
        }

        // --- (สำคัญมาก) Getters สำหรับ PropertyValueFactory ---
        public String getScheduleId() { return scheduleId; }
        public String getPilot1Name() { return pilot1Name; }
        public String getPilot2Name() { return pilot2Name; }
        public String getDateTime() { return dateTime; }
        public String getPracticeProgram() { return practiceProgram; }
        public String getSupervisorId() { return supervisorId; }
    }
}