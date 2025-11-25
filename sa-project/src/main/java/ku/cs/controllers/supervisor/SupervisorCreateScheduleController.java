package ku.cs.controllers.supervisor;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.schedule.ScheduleRepository;

import java.io.IOException;
import java.util.UUID;

public class SupervisorCreateScheduleController {

    @FXML private ChoiceBox<Pilot> pilotChoiceBox;
    @FXML private ChoiceBox<Instructor> instructorChoiceBox;
    @FXML private TextField programTextField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeTextField;
    @FXML private TextField simulatorTextField;
    @FXML private Label errorLabel;

    private PilotRepository pilotRepository;
    private InstructorRepository instructorRepository;
    private ScheduleRepository scheduleRepository;
    private Supervisor currentSupervisor;

    @FXML
    public void initialize() {
        pilotRepository = new PilotRepository();
        instructorRepository = new InstructorRepository();
        scheduleRepository = new ScheduleRepository();

        // ดึง Supervisor ปัจจุบัน
        User user = UserSession.getInstance().getCurrentUser();
        if (user instanceof Supervisor) {
            this.currentSupervisor = (Supervisor) user;
        }

        setupChoiceBoxes();
    }

    private void setupChoiceBoxes() {
        // ตั้งค่า Pilot ChoiceBox ให้แสดงชื่อ
        pilotChoiceBox.setItems(FXCollections.observableArrayList(pilotRepository.getAllPilots()));
        pilotChoiceBox.setConverter(new StringConverter<Pilot>() {
            @Override public String toString(Pilot p) { return p == null ? "" : p.getName(); }
            @Override public Pilot fromString(String string) { return null; }
        });

        // ตั้งค่า Instructor ChoiceBox ให้แสดงชื่อ
        instructorChoiceBox.setItems(FXCollections.observableArrayList(instructorRepository.getAllInstructors()));
        instructorChoiceBox.setConverter(new StringConverter<Instructor>() {
            @Override public String toString(Instructor i) { return i == null ? "" : i.getName(); }
            @Override public Instructor fromString(String string) { return null; }
        });
    }

    @FXML
    public void onSubmitButtonClick() {
        try {
            // 1. รับค่าจาก Form
            Pilot selectedPilot = pilotChoiceBox.getValue();
            Instructor selectedInstructor = instructorChoiceBox.getValue();
            String program = programTextField.getText();
            String date = (datePicker.getValue() != null) ? datePicker.getValue().toString() : "";
            String time = timeTextField.getText();
            String simulator = simulatorTextField.getText();

            // 2. ตรวจสอบข้อมูลว่าง
            if (selectedPilot == null || selectedInstructor == null || program.isEmpty() || date.isEmpty() || time.isEmpty()) {
                errorLabel.setText("กรุณากรอกข้อมูลให้ครบถ้วน");
                return;
            }

            // 3. สร้าง Schedule Object
            String scheduleId = "SC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Schedule schedule = new Schedule(
                    scheduleId,
                    currentSupervisor.getSupervisorID(),
                    selectedInstructor.getInstructorID(),
                    selectedPilot.getPilotID(),
                    "PL_NONE", // Pilot 2 ว่างไว้ก่อนตาม Flow รูปภาพ
                    "Scheduled",
                    program,
                    date,
                    time,
                    simulator
            );

            // 4. บันทึกลง Database
            scheduleRepository.addSchedule(schedule);

            // 5. กลับไปหน้าตาราง
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "สร้างตารางฝึกสำเร็จ!");
            alert.showAndWait();
            onScheduleButtonClick();

        } catch (Exception e) {
            errorLabel.setText("เกิดข้อผิดพลาด: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Navigation ---
    @FXML public void onHomepageButtonClick() { navigate("supervisor-main-page"); }
    @FXML public void onScheduleButtonClick() { navigate("supervisor-schedule-page"); }
    @FXML public void onReportButtonClick() { navigate("supervisor-report-page"); }
    @FXML public void onLogoutButtonClick() {
        UserSession.getInstance().clearSession();
        navigate("login");
    }

    private void navigate(String route) {
        try { FXRouter.goTo(route); } catch (IOException e) { e.printStackTrace(); }
    }
}