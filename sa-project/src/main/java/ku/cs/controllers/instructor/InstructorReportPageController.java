package ku.cs.controllers.instructor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.report.Report;
import ku.cs.models.report.ReportStatus;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.report.ReportRepository;

import java.io.IOException;
import java.util.List;

public class InstructorReportPageController {
    @FXML TableView<ReportView> reportTableView;
    @FXML
    ImageView logoImageView;

    @FXML private TableColumn<ReportView, String> colScheduleId;
    @FXML private TableColumn<ReportView, String> colPilotName;
    @FXML private TableColumn<ReportView, String> colProgram;
    @FXML private TableColumn<ReportView, ReportStatus> colStatus;

    private Instructor currentInstructor;
    private ReportRepository reportRepository;
    private PilotRepository pilotRepository;
    private ObservableList<ReportView> reportViewList = FXCollections.observableArrayList();

    public void initialize() {
        // (5) โหลด Instructor ที่ล็อกอินอยู่
        User loggedInUser = UserSession.getInstance().getCurrentUser();
        if (loggedInUser instanceof Instructor) {
            this.currentInstructor = (Instructor) loggedInUser;
        } else {
            System.err.println("REPORT_PAGE: ไม่พบ Instructor ใน Session");
            // (ควรจัดการ Error เช่น ปิดหน้านี้ หรือแสดง Label)
        }

        // (6) เริ่มต้น Repositories
        reportRepository = new ReportRepository();
        pilotRepository = new PilotRepository();

        setupTableColumns();
        setupRowClickListener(); // (7) เพิ่มตัวดักจับการคลิก

        if (this.currentInstructor != null) {
            loadReportData(); // (8) โหลดข้อมูล
        }
    }

    private void setupTableColumns() {
        colScheduleId.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        colPilotName.setCellValueFactory(new PropertyValueFactory<>("pilotName"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("trainingProgram"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadReportData() {
        reportViewList.clear();

        // 1. ดึง Report ทั้งหมดของ Instructor ที่ล็อกอินอยู่
        List<Report> reports = reportRepository.findReportsByInstructor(currentInstructor.getInstructorID());

        // 2. วนลูปเพื่อดึง "ชื่อ" ของ Pilot
        for (Report r : reports) {
            Pilot pilot = pilotRepository.findPilotById(r.getPilotId());
            String pilotName = (pilot != null) ? pilot.getName() : "N/A";

            // 3. สร้าง ReportView และเพิ่มลงใน List
            reportViewList.add(new ReportView(r, pilotName));
        }

        // 4. แสดงผลบนตาราง
        reportTableView.setItems(reportViewList);
    }

    private void setupRowClickListener() {
        reportTableView.setOnMouseClicked(event -> {
            // (1) ตรวจสอบว่าเป็นการ "ดับเบิลคลิก" (คลิก 2 ครั้ง)
            if (event.getClickCount() == 2) {
                ReportView selectedReport = reportTableView.getSelectionModel().getSelectedItem();

                if (selectedReport != null) {
                    // (2) เรียกเมธอดที่ร้องขอ
                    handleReportClick(selectedReport);
                }
            }
        });
    }

    private void handleReportClick(ReportView reportView) {
        System.out.println("REPORT_PAGE: คลิกที่ Report ID: " + reportView.getReportId());
        System.out.println("                 สำหรับ Pilot: " + reportView.getPilotName());

        // --- TODO: (ในอนาคต) ---
        // เมื่อคุณสร้างหน้า "report-detail-page"
        // ให้คุณใช้ FXRouter.goTo() ที่นี่เพื่อส่ง reportId ไป
        /*
        try {
            FXRouter.goTo("report-detail-page", reportView.getReportId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    public void onCreateButtonClick() {
        try {
            FXRouter.goTo("report-create", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onSendButtonClick() {
        // (ตัวอย่างการทำงาน) ส่ง Report ที่เลือก
        ReportView selectedReport = reportTableView.getSelectionModel().getSelectedItem();
        if (selectedReport != null && selectedReport.getStatus() == ReportStatus.DRAFT) {
            System.out.println("REPORT_PAGE: กำลังส่ง Report ID: " + selectedReport.getReportId());
            // 1. หา Report ตัวจริง
            Report report = reportRepository.findReportById(selectedReport.getReportId());
            // 2. เปลี่ยนสถานะ
            report.sendToSupervisor();
            // 3. บันทึก
            reportRepository.save(report); // (คุณต้องมีเมธอด save ใน Repository)
            // 4. โหลดข้อมูลใหม่
            loadReportData();
        } else {
            System.out.println("REPORT_PAGE: กรุณาเลือก Report (สถานะ DRAFT) ที่ต้องการส่ง");
        }
    }

    public void onDeleteButtonClick() {
        // (ตัวอย่างการทำงาน) ลบ Report ที่เลือก
        ReportView selectedReport = reportTableView.getSelectionModel().getSelectedItem();
        if (selectedReport != null) {
            System.out.println("REPORT_PAGE: กำลังลบ Report ID: " + selectedReport.getReportId());
            // 1. (ควรมี Pop-up ยืนยัน)
            // 2. ลบออกจาก Repository
            reportRepository.delete(selectedReport.getReportId()); // (คุณต้องมีเมธอด delete)
            // 3. โหลดข้อมูลใหม่ (หรือลบจาก List)
            loadReportData();
        } else {
            System.out.println("REPORT_PAGE: กรุณาเลือก Report ที่ต้องการลบ");
        }
    }

    public void onHomepageButtonClick() {
        try {
            // คุณต้อง "ส่ง" ข้อมูล instructor กลับไปด้วย
            FXRouter.goTo("instructor-main-page", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onScheduleButtonClick() {
        try {
            // หน้านี้คือหน้า Schedule อยู่แล้ว (ปกติปุ่มนี้ควรกดไม่ได้)
            // แต่ถ้าจะให้กดได้ ก็ต้องส่งข้อมูลไปด้วย
            FXRouter.goTo("instructor-schedule-page", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onReportButtonClick() {
        try {
            // ต้อง "ส่ง" ข้อมูล instructor ไปด้วย
            FXRouter.goTo("instructor-report-page", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onLogoutButtonClick() {
        try {
            // (เพิ่ม) ต้องเคลียร์ Session
            UserSession.getInstance().clearSession();
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ReportView {
        private String reportId;
        private String scheduleId;
        private String pilotName;
        private String trainingProgram;
        private ReportStatus status;

        public ReportView(Report report, String pilotName) {
            this.reportId = report.getReportId();
            this.scheduleId = report.getScheduleId();
            this.pilotName = pilotName;
            this.trainingProgram = report.getTrainingProgram();
            this.status = report.getStatus();
        }

        // --- Getters (สำคัญสำหรับ PropertyValueFactory) ---
        public String getReportId() { return reportId; }
        public String getScheduleId() { return scheduleId; }
        public String getPilotName() { return pilotName; }
        public String getTrainingProgram() { return trainingProgram; }
        public ReportStatus getStatus() { return status; }
    }
}
