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

        resultChoiceBox.getItems().addAll("Pending", "Passed", "Failed");

        Object data = FXRouter.getData();
        if (data instanceof String) {
            String reportId = (String) data;
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
        if (currentResult != null && !currentResult.isEmpty()) {
            resultChoiceBox.setValue(currentResult);
        } else {
            resultChoiceBox.setValue("Pending");
        }
    }

    // [แก้ไข] เปลี่ยนชื่อเมธอดให้ตรงกับ Error (handleEditButtonAction)
    @FXML
    public void handleEditButtonAction() {
        if (currentReport == null) return;

        try {
            currentReport.setReportResult(resultChoiceBox.getValue());
            currentReport.setReportNotes(notesTextArea.getText());

            reportRepository.save(currentReport);

            showAlert("Success", "แก้ไขข้อมูล Report เรียบร้อยแล้ว");
            FXRouter.goTo("instructor-report-page");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "บันทึกข้อมูลไม่สำเร็จ: " + e.getMessage());
        }
    }

    @FXML
    public void handleReturnButtonAction() {
        try {
            FXRouter.goTo("instructor-report-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        try {
            FXRouter.goTo("instructor-main-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleScheduleButton() {
        try {
            FXRouter.goTo("instructor-schedule-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReportButton() {
        try {
            FXRouter.goTo("instructor-report-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleNotificationButton() {
        try {
            // อย่าลืมไปเพิ่ม route "instructor-notification-page" ใน MainApplication.java ด้วยนะครับ
            FXRouter.goTo("instructor-notification-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleLogoutButton() {
        try {
            UserSession.getInstance().clearSession(); // เคลียร์ Session ก่อนออก
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void navigateTo(String route) {
        try {
            FXRouter.goTo(route);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}