package ku.cs.controllers.pilot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.report.Report;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.report.ReportRepository;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.instructor.InstructorRepository;

import java.io.IOException;
import java.util.List;

public class PilotReportPageController {

    // --- Sidebar Menu ---
    @FXML private Button homepageButton;
    @FXML private Button scheduleButton;
    @FXML private Button reportButton;
    @FXML private Button resignButton;
    @FXML private Button logoutButton;

    // --- Report View (FIXED TYPE) ---
    @FXML private TableView<PilotReportView> reportTableView;
    @FXML private TableColumn<PilotReportView, String> reportIdColumn;
    // This column in FXML is now mapped to Training Program
    @FXML private TableColumn<PilotReportView, String> pilotNameColumn;
    @FXML private TableColumn<PilotReportView, String> instructorNameColumn;
    @FXML private TableColumn<PilotReportView, String> resultColumn;

    @FXML private TextArea reportNoteTextArea;

    // --- Services & Data (FIXED TYPE) ---
    private ObservableList<PilotReportView> reportViewList;
    private ReportRepository reportRepository;
    private ScheduleRepository scheduleRepository;
    private Pilot currentPilot;
    private PilotRepository pilotRepository;
    private InstructorRepository instructorRepository;

    @FXML
    public void initialize() {
        System.out.println("PilotReportPageController initialized.");

        pilotRepository = new PilotRepository();
        scheduleRepository = new ScheduleRepository();
        reportRepository = new ReportRepository();
        instructorRepository = new InstructorRepository();

        // Load Pilot data from FXRouter
        Object data = FXRouter.getData();

        if (data instanceof Pilot) {
            this.currentPilot = (Pilot) data;
        } else if (data instanceof User) {
            User user = (User) data;
            this.currentPilot = pilotRepository.findPilotByUsername(user.getUsername());
        }

        reportViewList = FXCollections.observableArrayList();
        setupTableColumns();

        if (currentPilot != null) {
            reportTableView.setItems(reportViewList);
            loadReportData();

            // Add Listener for table row selection
            reportTableView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> showReportDetails(newValue)
            );
        } else {
            System.err.println("Error: Pilot data is null. Cannot load report page data.");
        }

        showReportDetails(null);
    }

    private void setupTableColumns() {
        // Mapped to properties in PilotReportView
        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        // This column shows the Training Program name
        pilotNameColumn.setCellValueFactory(new PropertyValueFactory<>("pilotName"));
        instructorNameColumn.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("reportResult"));
    }

    /**
     * Loads report data for the current pilot and populates the table using the View Model.
     */
    private void loadReportData() {
        if (currentPilot == null || currentPilot.getPilotID() == null) {
            System.err.println("Cannot load report data: currentPilot or pilot ID is null.");
            return;
        }

        reportViewList.clear();

        // 1. Get all reports for the current pilot (Relies on the new method in ReportRepository)
        List<Report> reports = reportRepository.findReportsByPilotId(currentPilot.getPilotID());

        // 2. Loop through reports to fetch related names/programs
        for (Report r : reports) {
            // Find Instructor Name
            Pilot pilot = pilotRepository.findPilotById(r.getPilotId());
            String pilotName = (pilot != null) ? pilot.getName() : "N/A";


            Instructor instructor = instructorRepository.findInstructorById(r.getInstructorId());
            String instructorName = (instructor != null) ? instructor.getName() : "N/A";

//            // Find Training Program Name from Schedule
//            Schedule schedule = scheduleRepository.findScheduleById(r.getScheduleId());
//            String programName = (schedule != null) ? schedule.getPracticeProgram() : "Schedule N/A";

            // 3. Create PilotReportView and add to list
            reportViewList.add(new PilotReportView(r, pilotName, instructorName));
        }

        System.out.println("Loaded " + reportViewList.size() + " reports for pilot ID: " + currentPilot.getPilotID());
        reportTableView.setItems(reportViewList);
    }

    /**
     * Show report notes (feedback) when a row is selected.
     */
    private void showReportDetails(PilotReportView reportView) {
        if (reportView != null) {
            // Retrieve the full Report object (needed for reportNotes/feedback)
            Report fullReport = reportRepository.findReportById(reportView.getReportId());
            if (fullReport != null) {
                reportNoteTextArea.setText(fullReport.getReportNotes());
            } else {
                reportNoteTextArea.setText("Error: Report notes not found.");
            }
        } else {
            reportNoteTextArea.clear();
            reportNoteTextArea.setPromptText("Please select a report from the table to see details.");
        }
    }

    // --- Sidebar Handlers (UNCHANGED) ---
    @FXML
    public void handleHomepageButton() {
        try { FXRouter.goTo("pilot-main-page", currentPilot); } catch (IOException e) { throw new RuntimeException(e); }
    }
    @FXML
    public void handleScheduleButton() {
        try { FXRouter.goTo("pilot-schedule-page", currentPilot); } catch (IOException e) { throw new RuntimeException(e); }
    }
    @FXML
    public void handleReportButton() {
        try { FXRouter.goTo("pilot-report-page", currentPilot); } catch (IOException e) { throw new RuntimeException(e); }
    }
    @FXML
    public void handleResignButton() {
        try { FXRouter.goTo("pilot-resign-page", currentPilot); } catch (IOException e) { throw new RuntimeException(e); }
    }
    @FXML
    public void handleLogoutButton() {
        try { FXRouter.goTo("login"); } catch (IOException e) { throw new RuntimeException(e); }
    }

    //
    // VVVV INNER CLASS FOR VIEW MODEL VVVV
    //

    /**
     * View Model class to hold combined data from Report, Schedule, and Instructor
     * for display in the Pilot Report TableView.
     */
    public static class PilotReportView {
        private String reportId;
        private String pilotName; // Training Program Name
        private String instructorName;  // Instructor Name
        private String reportResult;    // Report Result (Passed/Failed/Pending)

        public PilotReportView(Report report, String pilotName, String instructorName) {
            this.reportId = report.getReportId();
            this.pilotName = pilotName;
            this.instructorName = instructorName;
            this.reportResult = report.getReportResult();
        }

        // --- Getters (Crucial for PropertyValueFactory) ---
        public String getReportId() { return reportId; }
        public String getPilotName() { return pilotName; }
        public String getInstructorName() { return instructorName; }
        public String getReportResult() { return reportResult; }
    }

    @FXML
    public void handleNotificationButton() {
        try {
            // **Crucial:** Always pass the currentPilot object
            FXRouter.goTo("pilot-notification-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}