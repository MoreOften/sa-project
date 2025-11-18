package ku.cs.controllers.pilot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory; // *** NEW IMPORT ***
import ku.cs.models.pilot.Pilot;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.instructor.InstructorRepository; // *** NEW IMPORT ***

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

// CHANGE TABLE TYPE TO USE VIEW MODEL
public class PilotSchedulePageController {

    // --- Sidebar Menu ---
    @FXML private Button homepageButton;
    @FXML private Button scheduleButton;
    @FXML private Button reportButton;
    @FXML private Button resignButton;
    @FXML private Button logoutButton;

    // --- Schedule Table (MUST USE VIEW MODEL TYPE) ---
    @FXML private TableView<PilotScheduleView> scheduleTableView; // *** UPDATED TYPE ***
    // *** UPDATED COLUMN TYPES TO MATCH VIEW MODEL ***
    @FXML private TableColumn<PilotScheduleView, String> scheduleIdColumn;
    @FXML private TableColumn<PilotScheduleView, String> dateColumn;
    @FXML private TableColumn<PilotScheduleView, String> timeColumn;
    @FXML private TableColumn<PilotScheduleView, String> trainingProgramColumn;
    @FXML private TableColumn<PilotScheduleView, String> instructorColumn;

    private ObservableList<PilotScheduleView> scheduleList; // *** USE VIEW MODEL ***
    private ScheduleRepository scheduleRepository;
    private Pilot currentPilot;
    private PilotRepository pilotRepository;
    private InstructorRepository instructorRepository; // *** NEW REPO ***


    @FXML
    public void initialize() {
        System.out.println("PilotSchedulePageController initialized.");

        pilotRepository = new PilotRepository();
        scheduleRepository = new ScheduleRepository();
        instructorRepository = new InstructorRepository(); // *** INITIALIZE REPO ***

        // **UNIFIED DATA LOGIC FIX**
        Object data = FXRouter.getData();

        if (data instanceof Pilot) {
            this.currentPilot = (Pilot) data;
        } else if (data instanceof User) {
            User user = (User) data;
            this.currentPilot = pilotRepository.findPilotByUsername(user.getUsername());
        }
        // End Fix

        scheduleList = FXCollections.observableArrayList();
        setupTableColumns(); // *** CALL SETUP METHOD ***

        if (currentPilot != null) {
            scheduleTableView.setItems(scheduleList);
            loadScheduleData();
        } else {
            System.err.println("Error: Pilot data is null. Cannot initialize schedule data.");
        }
    }

    /**
     * ตั้งค่า CellValueFactory เพื่อผูกข้อมูลใน Model กับ Column
     */
    private void setupTableColumns() {
        // *** MAPPING TO PilotScheduleView PROPERTIES ***
        scheduleIdColumn.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("scheduleDate"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("scheduleTime"));
        trainingProgramColumn.setCellValueFactory(new PropertyValueFactory<>("practiceProgram"));
        instructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
    }

    private void loadScheduleData() {
        if (currentPilot == null || currentPilot.getPilotID() == null) {
            System.err.println("Cannot load schedule data: currentPilot or pilot ID is null.");
            return;
        }

        scheduleList.clear();

        // 1. ดึง Schedule ทั้งหมดของ Pilot ที่ล็อกอินอยู่ (ใช้ PilotID)
        List<Schedule> schedules = scheduleRepository.findSchedulesByPilot(currentPilot.getPilotID());

        // 2. วนลูปเพื่อดึง "ชื่อ" ของ Instructor
        for (Schedule s : schedules) {
            String instructorId = s.getInstructorId();
            String instructorName = "N/A";

            // ใช้ findInstructorByUsername เพราะ ID ของ Instructor ก็คือ Username ของเขา
            ku.cs.models.instructor.Instructor instructor = instructorRepository.findInstructorByUsername(instructorId);
            if (instructor != null) {
                instructorName = instructor.getName();
            }

            // 3. สร้าง View Model และเพิ่มลงใน List
            scheduleList.add(new PilotScheduleView(s, instructorName));
        }

        System.out.println("Loaded " + scheduleList.size() + " schedules for pilot ID: " + currentPilot.getPilotID());
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

    //
    // VVVV INNER CLASS FOR VIEW MODEL VVVV
    //

    /**
     * คลาสภายในสำหรับเป็น "โมเดลแสดงผล" ในตาราง Pilot
     * ต้องมี Getters ที่ตรงกับ PropertyValueFactory ใน FXML
     */
    public static class PilotScheduleView {
        private String scheduleId;
        private String scheduleDate;
        private String scheduleTime;
        private String practiceProgram;
        private String instructorName; // ชื่อ Instructor

        public PilotScheduleView(Schedule schedule, String instructorName) {
            this.scheduleId = schedule.getScheduleId();
            this.scheduleDate = schedule.getScheduleDate();
            this.scheduleTime = schedule.getScheduleTime();
            this.practiceProgram = schedule.getPracticeProgram();
            this.instructorName = instructorName;
        }

        // --- (สำคัญมาก) Getters สำหรับ PropertyValueFactory ---
        public String getScheduleId() { return scheduleId; }
        public String getScheduleDate() { return scheduleDate; }
        public String getScheduleTime() { return scheduleTime; }
        public String getPracticeProgram() { return practiceProgram; }
        public String getInstructorName() { return instructorName; }
    }
}