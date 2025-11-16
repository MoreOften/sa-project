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
import ku.cs.models.user.User; // **NEW: Import User for type checking**
import ku.cs.services.FXRouter;
import ku.cs.services.pilot.PilotRepository; // **NEW: Import PilotRepository if needed for lookup**
import ku.cs.services.schedule.ScheduleRepository;

import java.io.IOException;
import java.time.LocalDate;

public class PilotSchedulePageController {

    // --- Sidebar Menu ---
    @FXML private Button homepageButton;
    @FXML private Button scheduleButton;
    @FXML private Button reportButton;
    @FXML private Button resignButton;
    @FXML private Button logoutButton;

    // --- Schedule Table (ต้องตรงกับ Model Schedule) ---
    @FXML private TableView<Schedule> scheduleTableView;
    @FXML private TableColumn<Schedule, LocalDate> dateColumn;
    @FXML private TableColumn<Schedule, String> timeColumn;
    @FXML private TableColumn<Schedule, String> programColumn;
    @FXML private TableColumn<Schedule, String> instructorColumn;

    private ObservableList<Schedule> scheduleList;
    private ScheduleRepository scheduleRepository;
    private Pilot currentPilot;
    private PilotRepository pilotRepository; // **NEW: Needed for data lookup if only User object is passed**


    @FXML
    public void initialize() {
        System.out.println("PilotSchedulePageController initialized.");

        pilotRepository = new PilotRepository(); // Initialize pilot repository
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

        scheduleList = FXCollections.observableArrayList();

        if (currentPilot != null) {
            scheduleTableView.setItems(scheduleList);
            loadScheduleData();
        } else {
            System.err.println("Error: Pilot data is null. Cannot initialize schedule data.");
            // You might want to display an error or navigate back to login
        }
    }

    private void loadScheduleData() {
        if (currentPilot == null || currentPilot.getUsername() == null) {
            System.err.println("Cannot load schedule data: currentPilot or username is null.");
            return;
        }

        scheduleList.clear();

        // [EDIT] เรียกใช้ Repository และส่ง username
//        scheduleList.addAll(scheduleRepository.getSchedulesForPilot(currentPilot.getUsername()));

        System.out.println("Loaded " + scheduleList.size() + " schedules for pilot: " + currentPilot.getUsername());
    }

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
            // แก้ปลายทางเป็นหน้าของ pilot (No navigation needed, already on this page, but safe to call)
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