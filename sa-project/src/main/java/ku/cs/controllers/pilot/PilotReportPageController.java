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
import ku.cs.services.DataBox;
import ku.cs.services.schedule.ScheduleRepository;

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

    @FXML
    public void initialize() {
        System.out.println("PilotReportPageController initialized.");

        currentPilot = (Pilot) DataBox.get("currentPilot");
        scheduleRepository = new ScheduleRepository();
        completedScheduleList = FXCollections.observableArrayList();

        reportTableView.setItems(completedScheduleList);

        // โหลดข้อมูล Report
        loadReportData();

        // [IMPORTANT] เพิ่ม Listener สำหรับการเลือกแถวใน TableView
        reportTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showReportDetails(newValue)
        );

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
        // (ดู "การเปลี่ยนแปลงที่จำเป็น" ในข้อถัดไป)
//        completedScheduleList.addAll(scheduleRepository.getCompletedSchedulesForPilot(currentPilot.getUsername()));

        System.out.println("Loaded " + completedScheduleList.size() + " completed reports for pilot: " + currentPilot.getUsername());
    }

    /**
     * แสดง Feedback ใน TextArea เมื่อผู้ใช้คลิกเลือกแถว
     * @param schedule รายการ Schedule ที่ถูกเลือก (หรือ null ถ้าไม่เลือก)
     */
    private void showReportDetails(Schedule schedule) {
        if (schedule != null) {
            // [IMPORTANT] เราต้องเพิ่ม field "instructorFeedback" ใน Model
            // (ดู "การเปลี่ยนแปลงที่จำเป็น" ในข้อถัดไป)
//            feedbackTextArea.setText(schedule.getInstructorFeedback());
        } else {
            feedbackTextArea.clear();
            feedbackTextArea.setPromptText("Please select a report from the table to see details.");
        }
    }

    // --- Sidebar Handlers ---
    // (ใส่ logic การเปลี่ยนหน้า FXRouter ของคุณที่นี่)

    @FXML void handleHomepageButton(ActionEvent event) { /* ... */ }
    @FXML void handleScheduleButton(ActionEvent event) { /* ... */ }
    @FXML void handleReportButton(ActionEvent event) { System.out.println("Already on Report Page."); }
    @FXML void handleResignButton(ActionEvent event) { /* ... */ }
    @FXML void handleLogoutButton(ActionEvent event) { /* ... */ }
}