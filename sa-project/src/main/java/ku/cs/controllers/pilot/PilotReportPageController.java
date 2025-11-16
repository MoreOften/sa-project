package ku.cs.controllers.pilot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.user.User; // **NEW: Import User for type checking**
import ku.cs.services.FXRouter;
import ku.cs.services.pilot.PilotRepository; // **NEW: Import PilotRepository if needed for lookup**
import ku.cs.services.schedule.ScheduleRepository;

import java.io.IOException;
import java.time.LocalDate;

public class PilotReportPageController {

    // --- Sidebar Menu ---
    @FXML private Button homepageButton;
    @FXML private Button scheduleButton;
    @FXML private Button reportButton;
    @FXML private Button resignButton;
    @FXML private Button logoutButton;

    // --- Report View ---
    @FXML private TableView<Schedule> reportTableView;
    @FXML private TableColumn<Schedule, LocalDate> dateColumn;
    @FXML private TableColumn<Schedule, String> programColumn;
    @FXML private TableColumn<Schedule, String> instructorColumn;
    @FXML private TableColumn<Schedule, String> gradeColumn;

    @FXML private TextArea feedbackTextArea;

    private ObservableList<Schedule> completedScheduleList;
    private ScheduleRepository scheduleRepository;
    private Pilot currentPilot;
    // **NEW: If the Pilot object isn't passed directly, we'll need this to look it up**
    private PilotRepository pilotRepository;


    @FXML
    public void initialize() {
        System.out.println("PilotReportPageController initialized.");

        pilotRepository = new PilotRepository(); // Initialize repository
        scheduleRepository = new ScheduleRepository();

        // **FIX: UNIFIED DATA LOGIC**
        Object data = FXRouter.getData();

        if (data instanceof Pilot) {
            // Case 1: Pilot object passed directly (from another sidebar button)
            this.currentPilot = (Pilot) data;
        } else if (data instanceof User) {
            // Case 2: Base User object passed (unlikely for internal navigation, but safe to check)
            User user = (User) data;
            this.currentPilot = pilotRepository.findPilotByUsername(user.getUsername());
        }
        // End Fix

        completedScheduleList = FXCollections.observableArrayList();

        if (currentPilot != null) {
            reportTableView.setItems(completedScheduleList);

            // โหลดข้อมูล Report
            loadReportData();

            // [IMPORTANT] เพิ่ม Listener สำหรับการเลือกแถวใน TableView
            reportTableView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> showReportDetails(newValue)
            );
        } else {
            System.err.println("Error: Pilot data is null. Cannot load report page data.");
            // You might want to navigate to an error page or back to login here.
        }

        // เคลียร์ feedbackTextArea ในตอนเริ่มต้น
        showReportDetails(null);
    }

    /**
     * โหลดข้อมูลตารางเรียนที่ "เสร็จสิ้นแล้ว"
     */
    private void loadReportData() {
        if (currentPilot == null || currentPilot.getUsername() == null) {
            System.err.println("Cannot load report data: currentPilot or username is null.");
            return;
        }

        completedScheduleList.clear();

        // [IMPORTANT] เราจะเรียกใช้เมธอดใหม่จาก Repository
        // completedScheduleList.addAll(scheduleRepository.getCompletedSchedulesForPilot(currentPilot.getUsername()));

        System.out.println("Loaded " + completedScheduleList.size() + " completed reports for pilot: " + currentPilot.getUsername());
    }

    /**
     * แสดง Feedback ใน TextArea เมื่อผู้ใช้คลิกเลือกแถว
     * @param schedule รายการ Schedule ที่ถูกเลือก (หรือ null ถ้าไม่เลือก)
     */
    private void showReportDetails(Schedule schedule) {
        if (schedule != null) {
            // [IMPORTANT] เราต้องเพิ่ม field "instructorFeedback" ใน Model
            // feedbackTextArea.setText(schedule.getInstructorFeedback());
        } else {
            feedbackTextArea.clear();
            feedbackTextArea.setPromptText("Please select a report from the table to see details.");
        }
    }

    // --- Sidebar Handlers ---
    // (logic การเปลี่ยนหน้า FXRouter ของคุณถูกต้องแล้ว โดยมีการส่ง currentPilot)

    @FXML
    public void handleHomepageButton() {
        try {
            // แก้ปลายทางเป็นหน้าของ pilot
            FXRouter.goTo("pilot-main-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleScheduleButton() {
        try {
            // แก้ปลายทางเป็นหน้าของ pilot
            FXRouter.goTo("pilot-schedule-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReportButton() {
        try {
            // แก้ปลายทางเป็นหน้าของ pilot
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
}