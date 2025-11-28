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

        // ดึงข้อมูล Supervisor ที่ Login อยู่มาใช้เป็นผู้สร้างตาราง
        User user = UserSession.getInstance().getCurrentUser();
        if (user instanceof Supervisor) {
            this.currentSupervisor = (Supervisor) user;
        }

        setupChoiceBoxes();
    }

    private void setupChoiceBoxes() {
        // 1. ตั้งค่า Pilot ChoiceBox (แสดงชื่อ Pilot แต่เก็บ Object Pilot)
        pilotChoiceBox.setItems(FXCollections.observableArrayList(pilotRepository.getAllPilots()));
        pilotChoiceBox.setConverter(new StringConverter<Pilot>() {
            @Override public String toString(Pilot p) { return p == null ? "" : p.getName(); }
            @Override public Pilot fromString(String string) { return null; }
        });

        // 2. ตั้งค่า Instructor ChoiceBox
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

            // 2. ตรวจสอบข้อมูล (Validation)
            if (selectedPilot == null || selectedInstructor == null || program.isEmpty() || date.isEmpty() || time.isEmpty()) {
                errorLabel.setText("กรุณากรอกข้อมูลให้ครบถ้วน");
                return;
            }

            // 3. สร้าง Schedule Object
            // สร้าง ID อัตโนมัติ เช่น SC-A1B2C3D4
            String scheduleId = "SC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Schedule schedule = new Schedule(
                    scheduleId,
                    currentSupervisor.getSupervisorID(),
                    selectedInstructor.getInstructorID(),
                    selectedPilot.getPilotID(),
                    "PL_NONE", // (Pilot คนที่ 2 ให้ว่างไว้ก่อนถ้า UI มีเลือกแค่คนเดียว)
                    "Scheduled",
                    program,
                    date,
                    time,
                    simulator
            );

            boolean isAvailable = scheduleRepository.checkAvailability(
                    date,
                    time,
                    selectedInstructor.getInstructorID(),
                    selectedPilot.getPilotID()
            );

            if (!isAvailable) {
                // แจ้งเตือนและหยุดการทำงาน
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ตารางเวลาไม่ว่าง (Schedule Conflict)");
                alert.setHeaderText(null);
                alert.setContentText("Instructor หรือ Pilot มีตารางบินในช่วงวันและเวลานี้แล้ว\nกรุณาเลือกวันหรือเวลาอื่น");
                alert.showAndWait();
                return; // *** สำคัญ: หยุดการทำงานทันที ไม่บันทึก ***
            }

            // 4. บันทึกลง Database
            scheduleRepository.addSchedule(schedule);

            // 5. แจ้งเตือนและกลับไปหน้าตาราง
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "สร้างตารางฝึกสำเร็จ!");
            alert.showAndWait();
            onScheduleButtonClick(); // กลับไปหน้าตาราง

        } catch (Exception e) {
            errorLabel.setText("เกิดข้อผิดพลาด: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- ส่วน Navigation Sidebar ---
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