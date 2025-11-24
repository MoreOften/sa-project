package ku.cs.controllers.instructor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

public class ReportViewFormController {

    @FXML private Label reportIdLabel;
    @FXML private Label supervisorNameLabel;
    @FXML private Label instructorNameLabel;
    @FXML private Label trainingProgramLabel;
    @FXML private Label reportResultLabel;
    @FXML private Label reportNotesLabel;

    private ReportRepository reportRepository;
    private ScheduleRepository scheduleRepository;
    private SupervisorRepository supervisorRepository;
    private InstructorRepository instructorRepository;

    public void initialize() {
        // 1. เริ่มต้น Repository ทั้งหมดที่ต้องใช้
        reportRepository = new ReportRepository();
        scheduleRepository = new ScheduleRepository();
        supervisorRepository = new SupervisorRepository();
        instructorRepository = new InstructorRepository();

        // 2. รับ reportId ที่ส่งมาจากหน้าตาราง (ผ่าน FXRouter)
        Object data = FXRouter.getData();
        if (data instanceof String reportId) {
            loadReportData(reportId);
        } else {
            System.err.println("ReportView: ไม่ได้รับ Report ID");
        }
    }

    private void loadReportData(String reportId) {
        // ค้นหา Report
        Report report = reportRepository.findReportById(reportId);
        if (report == null) {
            System.err.println("ReportView: ไม่พบ Report ID: " + reportId);
            return;
        }

        System.out.println("=== DEBUG: Report View ===");
        System.out.println("Report ID: " + report.getReportId());
        System.out.println("Schedule ID: " + report.getScheduleId());

        // --- แสดงข้อมูลส่วนที่ 1: จาก Report โดยตรง ---
        reportIdLabel.setText(report.getReportId());
        reportResultLabel.setText(report.getReportResult() != null ? report.getReportResult() : "-");
        reportNotesLabel.setText(report.getReportNotes() != null ? report.getReportNotes() : "-");

        // --- แสดงข้อมูลส่วนที่ 2: ชื่อ Instructor ---
        Instructor instructor = instructorRepository.findInstructorById(report.getInstructorId());
        if (instructor != null) {
            instructorNameLabel.setText(instructor.getName());
            System.out.println("Instructor ID: " + report.getInstructorId() + ", Name: " + instructor.getName());
        } else {
            instructorNameLabel.setText(report.getInstructorId());
            System.err.println("WARNING: ไม่พบ Instructor ID: " + report.getInstructorId());
        }

        // --- แสดงข้อมูลส่วนที่ 3: จาก Schedule (Program & Supervisor) ---
        Schedule schedule = scheduleRepository.findScheduleById(report.getScheduleId());
        if (schedule != null) {
            trainingProgramLabel.setText(schedule.getPracticeProgram());
            System.out.println("Schedule Found - Program: " + schedule.getPracticeProgram());
            System.out.println("Supervisor ID from Schedule: " + schedule.getSupervisorId());

            // *** Debug: ตรวจสอบการดึงข้อมูล Supervisor ***
            String supervisorId = schedule.getSupervisorId();
            System.out.println("Attempting to find Supervisor with ID: " + supervisorId);

            Supervisor supervisor = supervisorRepository.findSupervisorById(supervisorId);

            if (supervisor != null) {
                System.out.println("✓ Supervisor Found!");
                System.out.println("  - ID: " + supervisor.getSupervisorID());
                System.out.println("  - Username: " + supervisor.getUsername());
                System.out.println("  - Name: " + supervisor.getName());
                supervisorNameLabel.setText(supervisor.getName());
            } else {
                System.err.println("✗ ERROR: Supervisor NOT FOUND for ID: " + supervisorId);
                System.err.println("  Possible issues:");
                System.err.println("  1. ข้อมูลใน DB ไม่ตรงกัน (supervisor_id ใน schedules vs supervisors table)");
                System.err.println("  2. Supervisor ยังไม่ถูก seed ลง database");
                System.err.println("  3. Query ใน findSupervisorById() มีปัญหา");
                supervisorNameLabel.setText(supervisorId + " (Not Found)");
            }
        } else {
            System.err.println("WARNING: ไม่พบ Schedule ID: " + report.getScheduleId());
            trainingProgramLabel.setText("N/A");
            supervisorNameLabel.setText("N/A");
        }

        System.out.println("=== END DEBUG ===");
    }

    @FXML
    public void onReturnButtonClick() {
        try {
            FXRouter.goTo("instructor-report-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- Sidebar Navigation ---
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