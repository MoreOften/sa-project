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
import java.util.List;
import java.util.UUID;

public class SupervisorCreateScheduleController {

    // (แก้ไข) เปลี่ยนชื่อและเพิ่มตัวแปรสำหรับ Pilot 2
    @FXML private ChoiceBox<Pilot> pilot1ChoiceBox;
    @FXML private ChoiceBox<Pilot> pilot2ChoiceBox;

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

        User user = UserSession.getInstance().getCurrentUser();
        if (user instanceof Supervisor) {
            this.currentSupervisor = (Supervisor) user;
        }

        setupChoiceBoxes();
    }

    private void setupChoiceBoxes() {
        // ดึงข้อมูล Pilots ทั้งหมด
        List<Pilot> allPilots = pilotRepository.getAllPilots();

        // 1. ตั้งค่า Pilot 1 ChoiceBox
        pilot1ChoiceBox.setItems(FXCollections.observableArrayList(allPilots));
        pilot1ChoiceBox.setConverter(createPilotStringConverter());

        // 2. ตั้งค่า Pilot 2 ChoiceBox
        pilot2ChoiceBox.setItems(FXCollections.observableArrayList(allPilots));
        pilot2ChoiceBox.setConverter(createPilotStringConverter());

        // 3. ตั้งค่า Instructor ChoiceBox
        instructorChoiceBox.setItems(FXCollections.observableArrayList(instructorRepository.getAllInstructors()));
        instructorChoiceBox.setConverter(new StringConverter<Instructor>() {
            @Override public String toString(Instructor i) { return i == null ? "" : i.getName(); }
            @Override public Instructor fromString(String string) { return null; }
        });
    }

    private StringConverter<Pilot> createPilotStringConverter() {
        return new StringConverter<Pilot>() {
            @Override public String toString(Pilot p) { return p == null ? "" : p.getName(); }
            @Override public Pilot fromString(String string) { return null; }
        };
    }

    @FXML
    public void onSubmitButtonClick() {
        try {
            // 1. รับค่าจาก Form
            Pilot selectedPilot1 = pilot1ChoiceBox.getValue();
            Pilot selectedPilot2 = pilot2ChoiceBox.getValue();
            Instructor selectedInstructor = instructorChoiceBox.getValue();
            String program = programTextField.getText();
            String date = (datePicker.getValue() != null) ? datePicker.getValue().toString() : "";
            String time = timeTextField.getText();
            String simulator = simulatorTextField.getText();

            // 2. ตรวจสอบข้อมูล (Validation)
            if (selectedPilot1 == null || selectedPilot2 == null || selectedInstructor == null ||
                    program.isEmpty() || date.isEmpty() || time.isEmpty()) {
                errorLabel.setText("กรุณากรอกข้อมูลให้ครบถ้วน (ต้องเลือก Pilot ทั้ง 2 คน)");
                return;
            }

            // ตรวจสอบว่า Pilot ทั้ง 2 คนไม่ใช่คนเดียวกัน
            if (selectedPilot1.getPilotID().equals(selectedPilot2.getPilotID())) {
                errorLabel.setText("Pilot คนที่ 1 และคนที่ 2 ต้องไม่ใช่คนเดียวกัน");
                return;
            }

            // 3. สร้าง Schedule Object
            String scheduleId = "SC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Schedule schedule = new Schedule(
                    scheduleId,
                    currentSupervisor.getSupervisorID(),
                    selectedInstructor.getInstructorID(),
                    selectedPilot1.getPilotID(), // ใส่ Pilot 1
                    selectedPilot2.getPilotID(), // ใส่ Pilot 2
                    "Scheduled",
                    program,
                    date,
                    time,
                    simulator
            );

            // 4. ตรวจสอบความว่าง (Availability Check)
            // เช็ค Instructor
            boolean isInstructorAvailable = scheduleRepository.checkAvailability(
                    date, time, selectedInstructor.getInstructorID(), "CHECK_INST_ONLY" // ใช้ Dummy Pilot ID เพื่อเช็คแค่ Instructor
            );

            // เช็ค Pilot 1
            boolean isPilot1Available = scheduleRepository.checkAvailability(
                    date, time, "CHECK_PILOT_ONLY", selectedPilot1.getPilotID()
            );

            // เช็ค Pilot 2
            boolean isPilot2Available = scheduleRepository.checkAvailability(
                    date, time, "CHECK_PILOT_ONLY", selectedPilot2.getPilotID()
            );

            if (!isInstructorAvailable || !isPilot1Available || !isPilot2Available) {
                String conflictMsg = "ตารางเวลาไม่ว่าง:\n";
                if (!isInstructorAvailable) conflictMsg += "- Instructor ติดภารกิจ\n";
                if (!isPilot1Available) conflictMsg += "- Pilot 1 ติดภารกิจ\n";
                if (!isPilot2Available) conflictMsg += "- Pilot 2 ติดภารกิจ\n";

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ตารางเวลาไม่ว่าง (Schedule Conflict)");
                alert.setHeaderText(null);
                alert.setContentText(conflictMsg + "กรุณาเลือกวันหรือเวลาอื่น");
                alert.showAndWait();
                return;
            }

            // 5. บันทึกลง Database
            scheduleRepository.addSchedule(schedule);

            // 6. แจ้งเตือนและกลับไปหน้าตาราง
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "สร้างตารางฝึกสำเร็จ!");
            alert.showAndWait();
            onScheduleButtonClick();

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
    @FXML
    public void onUserInfoButtonClick() {
        navigate("supervisor-user-list");
    }

    private void navigate(String route) {
        try { FXRouter.goTo(route); } catch (IOException e) { e.printStackTrace(); }
    }
}