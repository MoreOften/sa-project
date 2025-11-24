package ku.cs.controllers.instructor;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.report.Report;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.report.ReportRepository;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.supervisor.SupervisorRepository;

import java.io.IOException;

public class ReportEditFormController {

    @FXML private Label reportIdLabel;
    @FXML private Label supervisorNameLabel;
    @FXML private Label instructorNameLabel;
    @FXML private Label trainingProgramLabel;

    @FXML private ChoiceBox<String> resultChoiceBox;
    @FXML private TextArea notesTextArea;

    private ReportRepository reportRepository;
    private ScheduleRepository scheduleRepository;
    private SupervisorRepository supervisorRepository;
    private InstructorRepository instructorRepository;

    private Report currentReport;

    public void initialize() {
        reportRepository = new ReportRepository();
        scheduleRepository = new ScheduleRepository();
        supervisorRepository = new SupervisorRepository();
        instructorRepository = new InstructorRepository();

        // *** เปลี่ยน Choice Box ให้มีแค่ Passed และ Failed ***
        resultChoiceBox.getItems().clear();
        resultChoiceBox.getItems().addAll("Passed", "Failed");

        Object data = FXRouter.getData();
        if (data instanceof String reportId) {
            loadReportData(reportId);
        } else {
            System.err.println("ReportEdit: ไม่ได้รับ Report ID");
        }
    }

    private void loadReportData(String reportId) {
        currentReport = reportRepository.findReportById(reportId);
        if (currentReport == null) {
            showAlert("Error", "ไม่พบข้อมูล Report");
            return;
        }

        reportIdLabel.setText(currentReport.getReportId());

        Instructor instructor = instructorRepository.findInstructorById(currentReport.getInstructorId());
        instructorNameLabel.setText(instructor != null ? instructor.getName() : "Unknown");

        Schedule schedule = scheduleRepository.findScheduleById(currentReport.getScheduleId());
        if (schedule != null) {
            trainingProgramLabel.setText(schedule.getPracticeProgram());
            Supervisor supervisor = supervisorRepository.findSupervisorById(schedule.getSupervisorId());
            supervisorNameLabel.setText(supervisor != null ? supervisor.getName() : "Unknown");
        }

        notesTextArea.setText(currentReport.getReportNotes());

        String currentResult = currentReport.getReportResult();

        // *** ตรวจสอบว่าเคยกรอก Result แล้วหรือไม่ ***
        if (currentResult != null && !currentResult.isEmpty() &&
                !currentResult.equalsIgnoreCase("Pending")) {

            // ถ้ากรอกแล้ว ให้แสดงค่าเดิมและปิด ChoiceBox
            resultChoiceBox.setValue(currentResult);
            resultChoiceBox.setDisable(true); // *** ปิดไม่ให้แก้ไข ***

        } else {
            // ถ้ายังไม่ได้กรอก ให้เลือก "Passed" เป็นค่าเริ่มต้น
            resultChoiceBox.setValue("Passed");
            resultChoiceBox.setDisable(false); // เปิดให้แก้ไขได้
        }
    }

    @FXML
    public void handleEditButtonAction() {
        if (currentReport == null) return;

        try {
            String selectedResult = resultChoiceBox.getValue();
            String notes = notesTextArea.getText();

            // *** ตรวจสอบว่าเลือก Result แล้วหรือยัง ***
            if (selectedResult == null || selectedResult.isEmpty()) {
                showAlert("Warning", "กรุณาเลือกผลการประเมิน (Passed/Failed)");
                return;
            }

            // *** ตรวจสอบว่ามีการเปลี่ยนแปลง Result หรือไม่ ***
            String previousResult = currentReport.getReportResult();
            boolean isResultChanged = !selectedResult.equals(previousResult);

            // อัปเดตข้อมูล Report
            currentReport.setReportResult(selectedResult);
            currentReport.setReportNotes(notes);

            // *** อัปเดตสถานะของ Pilot ตามผลการประเมิน (เฉพาะเมื่อ Result เปลี่ยนแปลง) ***
            if (isResultChanged) {
                updatePilotStatus(currentReport.getPilotId(), selectedResult);
            }

            reportRepository.save(currentReport);

            showAlert("Success", "แก้ไขข้อมูล Report เรียบร้อยแล้ว");
            FXRouter.goTo("instructor-report-page");

        } catch (Exception e) {
            System.err.println("บันทึกข้อมูลไม่สำเร็จ: " + e.getMessage());
            showAlert("Error", "บันทึกข้อมูลไม่สำเร็จ: " + e.getMessage());
        }
    }

    /**
     * อัปเดตสถานะของ Pilot ตามผลการประเมิน
     * @param pilotId ID ของ Pilot
     * @param result ผลการประเมิน (Passed/Failed)
     */
    private void updatePilotStatus(String pilotId, String result) {
        ku.cs.services.pilot.PilotRepository pilotRepository = new ku.cs.services.pilot.PilotRepository();
        ku.cs.models.pilot.Pilot pilot = pilotRepository.findPilotById(pilotId);

        if (pilot == null) {
            System.err.println("ReportEdit: ไม่พบข้อมูล Pilot ID: " + pilotId);
            return;
        }

        if ("Passed".equalsIgnoreCase(result)) {
            // *** ถ้า Passed: ให้ Pilot สามารถฝึกต่อได้ ***
            pilot.setPilotIsAvailable("1"); // true
            System.out.println("Pilot " + pilotId + " ผ่านการประเมิน - สามารถฝึกต่อได้");

        } else if ("Failed".equalsIgnoreCase(result)) {
            // *** ถ้า Failed: ปิดการฝึก, ทำเครื่องหมายว่าตก, และเพิ่มจำนวนครั้งที่ตก ***
            pilot.setPilotIsAvailable("0"); // false
            pilot.setPilotIsFailed("1"); // true

            // เพิ่มจำนวนครั้งที่ตก
            int currentFailCount = 0;
            try {
                String failCountStr = pilot.getPilotFailCount();
                if (failCountStr != null && !failCountStr.isEmpty()) {
                    currentFailCount = Integer.parseInt(failCountStr);
                }
            } catch (NumberFormatException e) {
                System.err.println("ReportEdit: ไม่สามารถแปลง FailCount เป็นตัวเลข");
            }
            pilot.setPilotFailCount(String.valueOf(currentFailCount + 1));

            System.out.println("Pilot " + pilotId + " ไม่ผ่านการประเมิน - ปิดการฝึก, FailCount: " + (currentFailCount + 1));
        }

        // บันทึกการเปลี่ยนแปลงลงฐานข้อมูล
        pilotRepository.updatePilotStatus(pilot);
    }

    @FXML
    public void handleReturnButtonAction() {
        navigateTo("instructor-report-page");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleHomepageButton() {
        navigateTo("instructor-main-page");
    }

    @FXML
    public void handleScheduleButton() {
        navigateTo("instructor-schedule-page");
    }

    @FXML
    public void handleReportButton() {
        navigateTo("instructor-report-page");
    }

    @FXML
    public void handleNotificationButton() {
        navigateTo("instructor-notification-page");
    }

    @FXML
    public void handleLogoutButton() {
        UserSession.getInstance().clearSession();
        navigateTo("login");
    }

    private void navigateTo(String route) {
        try {
            FXRouter.goTo(route);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}