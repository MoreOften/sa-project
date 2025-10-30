package ku.cs.controllers.pilot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.schedule.Schedule;
import ku.cs.services.DataBox; // (สมมติว่ามี)
import ku.cs.services.schedule.ScheduleRepository; // [EDIT] ใช้ Repository

import java.time.LocalDate;

public class PilotSchedulePageController {

    // --- Sidebar Menu ---
    @FXML private Button homepageButton;
    // ... (ปุ่มอื่นๆ) ...
    @FXML private Button logoutButton;

    // --- Schedule Table (ต้องตรงกับ Model Schedule) ---
    @FXML private TableView<Schedule> scheduleTableView;
    @FXML private TableColumn<Schedule, LocalDate> dateColumn;
    @FXML private TableColumn<Schedule, String> timeColumn;
    @FXML private TableColumn<Schedule, String> programColumn;  // (ชื่อใน FXML ไม่ต้องตรงกับ Model)
    @FXML private TableColumn<Schedule, String> instructorColumn; // (ชื่อใน FXML ไม่ต้องตรงกับ Model)

    private ObservableList<Schedule> scheduleList;
    private ScheduleRepository scheduleRepository; // [EDIT]
    private Pilot currentPilot;

    @FXML
    public void initialize() {
        System.out.println("PilotSchedulePageController initialized.");

        // (ข้อสมมติ) ดึง Pilot ที่ login อยู่ (ซึ่งควรจะมี username)
        currentPilot = (Pilot) DataBox.get("currentPilot");

        scheduleRepository = new ScheduleRepository(); // [EDIT]
        scheduleList = FXCollections.observableArrayList();
        scheduleTableView.setItems(scheduleList);

        loadScheduleData();
    }

    private void loadScheduleData() {
        if (currentPilot == null || currentPilot.getUsername() == null) { // [EDIT]
            System.err.println("Cannot load schedule data: currentPilot or username is null.");
            return;
        }

        scheduleList.clear();

        // [EDIT] เรียกใช้ Repository และส่ง username
        scheduleList.addAll(scheduleRepository.getSchedulesForPilot(currentPilot.getUsername()));

        System.out.println("Loaded " + scheduleList.size() + " schedules for pilot: " + currentPilot.getUsername());
    }

    // --- Sidebar Handlers ---
    @FXML void handleHomepageButton(ActionEvent event) { /* ... */ }
    @FXML void handleScheduleButton(ActionEvent event) { /* ... */ }
    @FXML void handleReportButton(ActionEvent event) { /* ... */ }
    @FXML void handleResignButton(ActionEvent event) { /* ... */ }
    @FXML void handleLogoutButton(ActionEvent event) { /* ... */ }
}