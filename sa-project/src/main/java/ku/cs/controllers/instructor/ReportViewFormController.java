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
        if (data instanceof String) {
            String reportId = (String) data;
            loadReportData(reportId);
        } else {
            System.err.println("ReportView: ไม่ได้รับ Report ID");
        }
    }

    private void loadReportData(String reportId) {
        // ค้นหา Report
        Report report = reportRepository.findReportById(reportId);
        if (report == null) return;

        // --- แสดงข้อมูลส่วนที่ 1: จาก Report โดยตรง ---
        reportIdLabel.setText(report.getReportId());
        reportResultLabel.setText(report.getReportResult() != null ? report.getReportResult() : "-");
        reportNotesLabel.setText(report.getReportNotes() != null ? report.getReportNotes() : "-");

        // --- แสดงข้อมูลส่วนที่ 2: ชื่อ Instructor ---
        Instructor instructor = instructorRepository.findInstructorById(report.getInstructorId());
        if (instructor != null) {
            instructorNameLabel.setText(instructor.getName());
        } else {
            instructorNameLabel.setText(report.getInstructorId()); // fallback
        }

        // --- แสดงข้อมูลส่วนที่ 3: จาก Schedule (Program & Supervisor) ---
        Schedule schedule = scheduleRepository.findScheduleById(report.getScheduleId());
        if (schedule != null) {
            trainingProgramLabel.setText(schedule.getPracticeProgram());

            // หาชื่อ Supervisor จาก ID ที่อยู่ใน Schedule
            Supervisor supervisor = supervisorRepository.findSupervisorById(schedule.getSupervisorId());
            if (supervisor != null) {
                supervisorNameLabel.setText(supervisor.getName());
            } else {
                supervisorNameLabel.setText(schedule.getSupervisorId()); // fallback
            }
        }
    }

    @FXML
    public void onReturnButtonClick() {
        try {
            // กลับไปหน้าตาราง Report
            FXRouter.goTo("instructor-report-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --- Sidebar Navigation (ก๊อปปี้มาจากหน้าอื่นได้เลย) ---
    @FXML
    public void onHomepageButtonClick() {
        try { FXRouter.goTo("instructor-main-page"); } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML
    public void onScheduleButtonClick() {
        try { FXRouter.goTo("instructor-schedule-page"); } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML
    public void onReportButtonClick() {
        try { FXRouter.goTo("instructor-report-page"); } catch (IOException e) { e.printStackTrace(); }
    }
    @FXML
    public void onLogoutButtonClick() {
        try {
            UserSession.getInstance().clearSession();
            FXRouter.goTo("login");
        } catch (IOException e) { e.printStackTrace(); }
    }
}