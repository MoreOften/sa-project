package ku.cs.controllers.instructor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;      // สำหรับสร้างหน้าต่างแจ้งเตือน
import javafx.scene.control.ButtonType; // สำหรับปุ่ม OK/Cancel
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.report.Report;
import ku.cs.models.report.ReportStatus;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.report.ReportRepository;
import ku.cs.services.schedule.ScheduleRepository; // ‼️ เพิ่ม Import
import ku.cs.models.schedule.Schedule; // ‼️ เพิ่ม Import

import java.io.IOException;
import java.util.List;
import java.util.Optional;              // สำหรับรับค่าผลลัพธ์การกดปุ่ม

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
    private ScheduleRepository scheduleRepository;
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
        scheduleRepository = new ScheduleRepository();

        setupTableColumns();
        setupRowClickListener(); // (7) เพิ่มตัวดักจับการคลิก

        if (this.currentInstructor != null) {
            loadReportData(); // (8) โหลดข้อมูล
        }
    }

    private void setupTableColumns() {
        colScheduleId.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        colPilotName.setCellValueFactory(new PropertyValueFactory<>("pilotName"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("practiceProgram"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("approvalStatus"));
    }

    private void loadReportData() {
        reportViewList.clear();

        // 1. ดึง Report ทั้งหมดของ Instructor ที่ล็อกอินอยู่
        List<Report> reports = reportRepository.findReportsByInstructor(currentInstructor.getInstructorID());

        // 2. วนลูปเพื่อดึง "ชื่อ" ของ Pilot
        for (Report r : reports) {
            Pilot pilot = pilotRepository.findPilotById(r.getPilotId());
            String pilotName = (pilot != null) ? pilot.getName() : "N/A";

            Schedule schedule = scheduleRepository.findScheduleById(r.getScheduleId());
            String programName = (schedule != null) ? schedule.getPracticeProgram() : "Schedule N/A";

            // 3. สร้าง ReportView และเพิ่มลงใน List
            reportViewList.add(new ReportView(r, pilotName, programName));
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

    public void onCreateButtonClick() {
        try {
            FXRouter.goTo("report-create", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 1. เมธอดสำหรับ Double Click (ไปหน้า View)
    private void handleReportClick(ReportView reportView) {
        System.out.println("REPORT_PAGE: ดูรายละเอียด Report ID: " + reportView.getReportId());
        try {
            // ส่ง reportId หรือ object ไปที่หน้า View
            // ต้องแน่ใจว่า MainApplication.java มี route "report-view" แล้ว
            FXRouter.goTo("report-view", reportView.getReportId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 2. เมธอดสำหรับปุ่ม Edit (ไปหน้า Edit)
    @FXML
    public void onEditButtonClick() {
        ReportView selectedReport = reportTableView.getSelectionModel().getSelectedItem();

        if (selectedReport == null) {
            // แจ้งเตือนถ้าไม่ได้เลือกแถว
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("กรุณาเลือก Report ที่ต้องการแก้ไข");
            alert.showAndWait();
            return;
        }

        // ตรวจสอบสถานะ: แก้ไขได้เฉพาะ DRAFT เท่านั้น (ตาม Business Logic)
        if (selectedReport.getApprovalStatus() != ReportStatus.DRAFT) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("ไม่สามารถแก้ไข Report ที่ส่งไปแล้วได้");
            alert.showAndWait();
            return;
        }

        try {
            System.out.println("REPORT_PAGE: กำลังไปหน้าแก้ไข Report ID: " + selectedReport.getReportId());
            // ส่ง reportId ไปที่หน้า Edit
            FXRouter.goTo("report-edit", selectedReport.getReportId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onSendButtonClick() {
        // 1. ดึง Report ที่ถูกเลือก
        ReportView selectedReport = reportTableView.getSelectionModel().getSelectedItem();

        // ตรวจสอบว่ามีการเลือก และสถานะเป็น DRAFT (ถึงจะส่งได้)
        if (selectedReport != null && selectedReport.getApprovalStatus() == ReportStatus.DRAFT) {

            // 2. สร้างหน้าต่างยืนยัน (Confirmation Dialog)
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("ยืนยันการส่ง (Send Confirmation)");
            alert.setHeaderText(null);
            alert.setContentText("คุณต้องการส่ง Report ID: " + selectedReport.getReportId() + " ให้ Supervisor ตรวจสอบใช่หรือไม่?");

            // 3. รอรับผลการกดปุ่ม
            Optional<ButtonType> result = alert.showAndWait();

            // 4. ถ้ากด OK ให้ดำเนินการส่ง
            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.out.println("REPORT_PAGE: กำลังส่ง Report ID: " + selectedReport.getReportId());

                // --- Logic การส่ง ---
                Report report = reportRepository.findReportById(selectedReport.getReportId());
                report.sendToSupervisor(); // เปลี่ยนสถานะเป็น PENDING_REVIEW
                reportRepository.save(report); // บันทึกลง DB (เรียก UPDATE)

                // โหลดข้อมูลใหม่
                loadReportData();

                // (ทางเลือก) อาจจะแสดง Alert บอกว่าส่งสำเร็จแล้ว
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("ส่ง Report เรียบร้อยแล้ว");
                successAlert.showAndWait();
            } else {
                System.out.println("REPORT_PAGE: ยกเลิกการส่ง");
            }

        } else {
            // กรณีไม่ได้เลือก หรือสถานะไม่ใช่ DRAFT
            Alert warningAlert = new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Warning");
            warningAlert.setHeaderText(null);

            if (selectedReport == null) {
                warningAlert.setContentText("กรุณาเลือก Report ที่ต้องการส่ง");
            } else {
                warningAlert.setContentText("สามารถส่งได้เฉพาะ Report ที่มีสถานะ DRAFT เท่านั้น");
            }

            warningAlert.showAndWait();
        }
    }

    @FXML
    public void onDeleteButtonClick() {
        // 1. ดึง Report ที่ถูกเลือกจากตาราง
        ReportView selectedReport = reportTableView.getSelectionModel().getSelectedItem();

        if (selectedReport != null) {
            // 2. สร้างหน้าต่างยืนยัน (Confirmation Dialog)
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("ยืนยันการลบ (Delete Confirmation)");
            alert.setHeaderText(null); // ไม่ต้องมี Header
            alert.setContentText("คุณแน่ใจหรือไม่ว่าต้องการลบ Report ID: " + selectedReport.getReportId() + " ?");

            // 3. แสดงหน้าต่างและรอให้ผู้ใช้กดปุ่ม
            Optional<ButtonType> result = alert.showAndWait();

            // 4. ตรวจสอบว่าผู้ใช้กดปุ่ม "OK" (ตกลง) หรือไม่
            if (result.isPresent() && result.get() == ButtonType.OK) {

                // --- ดำเนินการลบจริง ---
                System.out.println("REPORT_PAGE: กำลังลบ Report ID: " + selectedReport.getReportId());

                // เรียก Repository เพื่อลบข้อมูลจาก Database
                reportRepository.delete(selectedReport.getReportId());

                // โหลดข้อมูลใหม่เพื่อให้ตารางอัปเดตทันที
                loadReportData();
            } else {
                // ผู้ใช้กด Cancel หรือปิดหน้าต่าง -> ไม่ทำอะไร
                System.out.println("REPORT_PAGE: ยกเลิกการลบ");
            }

        } else {
            // กรณีไม่ได้เลือกแถวใดๆ ให้แจ้งเตือน
            Alert errorAlert = new Alert(Alert.AlertType.WARNING);
            errorAlert.setTitle("Warning");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("กรุณาเลือก Report ที่ต้องการลบก่อน");
            errorAlert.showAndWait();
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
        private String practiceProgram;
        private ReportStatus approvalStatus;

        public ReportView(Report report, String pilotName, String programName) {
            this.reportId = report.getReportId();
            this.scheduleId = report.getScheduleId();
            this.pilotName = pilotName;
            this.practiceProgram = programName;
            this.approvalStatus = report.getApprovalStatus();
        }

        // --- Getters (สำคัญสำหรับ PropertyValueFactory) ---
        public String getReportId() { return reportId; }
        public String getScheduleId() { return scheduleId; }
        public String getPilotName() { return pilotName; }
        public String getPracticeProgram() { return practiceProgram; }
        public ReportStatus getApprovalStatus() { return approvalStatus; }
    }
}
