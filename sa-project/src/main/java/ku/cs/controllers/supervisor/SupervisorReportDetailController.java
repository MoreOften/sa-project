package ku.cs.controllers.supervisor;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.report.Report;
import ku.cs.models.report.ReportStatus;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.report.ReportRepository;

import java.io.IOException;

public class SupervisorReportDetailController {

    @FXML private Label reportIdLabel;
    @FXML private Label pilotNameLabel;
    @FXML private Label instructorNameLabel;
    @FXML private Label resultLabel;
    @FXML private Label statusLabel;
    @FXML private Label notesLabel;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;

    private Report currentReport;
    private ReportRepository reportRepository;
    private PilotRepository pilotRepository;
    private InstructorRepository instructorRepository;
    private Supervisor currentSupervisor;

    @FXML
    public void initialize() {
        reportRepository = new ReportRepository();
        pilotRepository = new PilotRepository();
        instructorRepository = new InstructorRepository();

        // โหลด Supervisor ที่ Login
        User user = UserSession.getInstance().getCurrentUser();
        if (user instanceof Supervisor) {
            this.currentSupervisor = (Supervisor) user;
        }

        // รับ reportId ที่ส่งมาจากหน้าตาราง
        Object data = FXRouter.getData();
        if (data instanceof String) {
            String reportId = (String) data;
            loadReportData(reportId);
        }


    }

    private void loadReportData(String reportId) {
        currentReport = reportRepository.findReportById(reportId);
        if (currentReport != null) {
            reportIdLabel.setText(currentReport.getReportId());
            resultLabel.setText(currentReport.getReportResult());
            statusLabel.setText(currentReport.getApprovalStatus().toString());
            notesLabel.setText(currentReport.getReportNotes());

            // หาชื่อ Pilot
            Pilot pilot = pilotRepository.findPilotById(currentReport.getPilotId());
            pilotNameLabel.setText(pilot != null ? pilot.getName() : "Unknown");

            // หาชื่อ Instructor
            Instructor instructor = instructorRepository.findInstructorById(currentReport.getInstructorId());
            instructorNameLabel.setText(instructor != null ? instructor.getName() : "Unknown");

            // ถ้าสถานะไม่ใช่ Pending Review ให้ปิดปุ่ม (อนุมัติซ้ำไม่ได้)
            if (currentReport.getApprovalStatus() != ReportStatus.PENDING_REVIEW) {
                approveButton.setDisable(true);
                rejectButton.setDisable(true);
            }
        }
    }

    @FXML
    public void onApproveButtonClick() {
        updateReportStatus(ReportStatus.APPROVED);
    }

    @FXML
    public void onRejectButtonClick() {
        updateReportStatus(ReportStatus.REJECTED);
    }

    private void updateReportStatus(ReportStatus newStatus) {
        if (currentReport == null) return;

        // 1. อัปเดตสถานะใน Model
        currentReport.setApprovalStatus(newStatus);

        // 2. บันทึกลง Database via Repository
        reportRepository.save(currentReport);

        // 3. แจ้งเตือน
        showAlert("Success", "Report status updated to: " + newStatus);

        // 4. ปิดปุ่ม
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        statusLabel.setText(newStatus.toString());

        // 5. กลับไปหน้าตาราง (Optional)
        onBackButtonClick();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML public void onBackButtonClick() {
        try { FXRouter.goTo("supervisor-report-page"); } catch (IOException e) { e.printStackTrace(); }
    }

    // --- Sidebar Navigation ---
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