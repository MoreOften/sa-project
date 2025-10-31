package ku.cs.controllers.instructor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea; // (1) Import TextArea (สันนิษฐานว่าคุณมี)
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.pilot.PilotRepository;       // (2) Import Repositories
import ku.cs.services.schedule.ScheduleRepository; // (2) Import Repositories

import java.io.IOException;
import java.util.List;

public class ReportCreateFormController {
    @FXML private Label instructorNameLabel;
    @FXML private Label trainingProgramLabel;
    @FXML private Label errorLabel;
    @FXML private ImageView logoImageView;
    @FXML private ChoiceBox<Schedule> scheduleIDChoiceBox;
    @FXML private ChoiceBox<Pilot> pilotNameChoiceBox;

    // (3) สันนิษฐานว่าคุณมี TextArea สำหรับกรอกรายละเอียด Report
    // @FXML private TextArea reportDetailsTextArea;

    private Instructor currentInstructor;
    private ScheduleRepository scheduleRepository;
    private PilotRepository pilotRepository;

    // (4) ObservableLists for binding to ChoiceBoxes
    private ObservableList<Schedule> instructorSchedules = FXCollections.observableArrayList();
    private ObservableList<Pilot> schedulePilots = FXCollections.observableArrayList();

    public void initialize() {
        errorLabel.setVisible(false);

        // (5) Initialize Repositories
        scheduleRepository = new ScheduleRepository();
        pilotRepository = new PilotRepository();

        // (6) Load the logged-in user from the session
        loadCurrentUser();

        // (7) Check if instructor is valid before proceeding
        if (this.currentInstructor != null) {
            setupChoiceBoxes();
            loadSchedulesForInstructor();
            addScheduleSelectionListener();
        } else {
            // Handle error - user not found or not an instructor
            errorLabel.setText("Error: Instructor user not found in session.");
            errorLabel.setVisible(true);
            // Disable form elements if user is invalid
            scheduleIDChoiceBox.setDisable(true);
            pilotNameChoiceBox.setDisable(true);
        }
    }

    /**
     * Loads the current user from the UserSession and sets the currentInstructor.
     */
    private void loadCurrentUser() {
        User loggedInUser = UserSession.getInstance().getCurrentUser();
        if (loggedInUser instanceof Instructor) {
            this.currentInstructor = (Instructor) loggedInUser;
            instructorNameLabel.setText(currentInstructor.getName()); // Set real name
        } else if (loggedInUser != null) {
            System.err.println("ReportCreateForm: User is not an Instructor: " + loggedInUser.getUsername());
        } else {
            System.err.println("ReportCreateForm: No user logged in.");
        }
    }

    /**
     * Sets up StringConverters for ChoiceBoxes to display readable names.
     */
    private void setupChoiceBoxes() {
        // --- Setup Schedule ChoiceBox ---
        scheduleIDChoiceBox.setConverter(new StringConverter<Schedule>() {
            @Override
            public String toString(Schedule schedule) {
                // Display Schedule ID and date
                return (schedule != null) ?
                        schedule.getScheduleId() + " (" + schedule.getTrainingTimestamp().toLocalDate() + ")"
                        : "Select Schedule";
            }

            @Override
            public Schedule fromString(String string) {
                return null; // Not needed for selection
            }
        });
        scheduleIDChoiceBox.setItems(instructorSchedules); // Bind list

        // --- Setup Pilot ChoiceBox ---
        pilotNameChoiceBox.setConverter(new StringConverter<Pilot>() {
            @Override
            public String toString(Pilot pilot) {
                // Display Pilot name
                return (pilot != null) ? pilot.getName() : "Select Pilot";
            }

            @Override
            public Pilot fromString(String string) {
                return null; // Not needed for selection
            }
        });
        pilotNameChoiceBox.setItems(schedulePilots); // Bind list
    }

    /**
     * Loads all schedules associated with the current instructor.
     */
    private void loadSchedulesForInstructor() {
        instructorSchedules.clear();
        List<Schedule> schedules = scheduleRepository.findSchedulesByInstructor(currentInstructor.getInstructorID());
        instructorSchedules.addAll(schedules);

        // Set default text if no schedules found
        if (instructorSchedules.isEmpty()) {
            scheduleIDChoiceBox.setDisable(true);
        } else {
            scheduleIDChoiceBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Adds a listener to the scheduleIDChoiceBox.
     * When a schedule is selected, it updates the pilot ChoiceBox and program Label.
     */
    private void addScheduleSelectionListener() {
        scheduleIDChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSchedule, newSchedule) -> {
                    if (newSchedule != null) {
                        // (8) A new schedule is selected, update other fields
                        updateFormForSelectedSchedule(newSchedule);
                    }
                }
        );
    }

    /**
     * Helper method to update the form when a new schedule is selected.
     * @param selectedSchedule The schedule chosen from the ChoiceBox.
     */
    private void updateFormForSelectedSchedule(Schedule selectedSchedule) {
        // (9) Update Training Program Label
        trainingProgramLabel.setText(selectedSchedule.getProgramName());

        // (10) Update Pilot ChoiceBox
        schedulePilots.clear(); // Clear old pilots
        Pilot pilot1 = pilotRepository.findPilotById(selectedSchedule.getPilotId1());
        Pilot pilot2 = pilotRepository.findPilotById(selectedSchedule.getPilotId2());

        if (pilot1 != null) {
            schedulePilots.add(pilot1);
        }
        if (pilot2 != null) {
            schedulePilots.add(pilot2);
        }

        // Set default selection and prompt
        if (schedulePilots.isEmpty()) {
            pilotNameChoiceBox.setDisable(true);
        } else {
            pilotNameChoiceBox.setDisable(false);
            pilotNameChoiceBox.getSelectionModel().selectFirst();
        }
    }

    // (11) --- ADD A SUBMIT BUTTON HANDLER ---
    // คุณต้องเพิ่มปุ่ม "Submit" หรือ "Create" ใน FXML
    // และเชื่อมโยงมายังเมธอดนี้
    @FXML
    private void handleSubmitButtonAction() {
        // Get all selected data
        Schedule selectedSchedule = scheduleIDChoiceBox.getValue();
        Pilot selectedPilot = pilotNameChoiceBox.getValue();
        // String details = reportDetailsTextArea.getText(); // (จาก TextArea)

        // --- Validation ---
        if (selectedSchedule == null) {
            errorLabel.setText("Please select a schedule.");
            errorLabel.setVisible(true);
            return;
        }
        if (selectedPilot == null) {
            errorLabel.setText("Please select a pilot.");
            errorLabel.setVisible(true);
            return;
        }
        /*
        if (details == null || details.trim().isEmpty()) {
            errorLabel.setText("Please enter report details.");
            errorLabel.setVisible(true);
            return;
        }
        */

        // --- All data is valid, create report object ---
        errorLabel.setVisible(false);
        System.out.println("CREATING REPORT:");
        System.out.println("Instructor: " + currentInstructor.getName());
        System.out.println("Schedule: " + selectedSchedule.getScheduleId());
        System.out.println("Pilot: " + selectedPilot.getName());
        // System.out.println("Details: " + details);

        // --- TODO: ---
        // 1. Create a new Report object
        // Report newReport = new Report(..., selectedSchedule, selectedPilot, details, ...);
        // 2. Get a ReportRepository
        // ReportRepository reportRepo = new ReportRepository();
        // 3. Save the report
        // reportRepo.save(newReport);
        // 4. Navigate back to the report list page
        // onReportButtonClick();
    }


    // --- Navigation Methods (No changes, they now work) ---

    public void onHomepageButtonClick() {
        try {
            FXRouter.goTo("instructor-main-page", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onScheduleButtonClick() {
        try {
            FXRouter.goTo("instructor-schedule-page", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onReportButtonClick() {
        try {
            FXRouter.goTo("instructor-report-page", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onLogoutButtonClick() {
        try {
            UserSession.getInstance().clearSession();
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}