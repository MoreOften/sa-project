package ku.cs.controllers.supervisor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import ku.cs.models.report.Report;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
// (เพิ่ม) Import Repositories ที่ต้องใช้
import ku.cs.services.report.ReportRepository;
import ku.cs.services.user.UserRepository;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SupervisorReportPageController {

    @FXML private TableView<ReportView> reportTableView;
    @FXML private TableColumn<ReportView, String> colNo;
    @FXML private TableColumn<ReportView, String> colInstructor;
    @FXML private TableColumn<ReportView, String> colCreateDate;
    @FXML private TableColumn<ReportView, String> colStatus;

    private Supervisor currentSupervisor;
    private ReportRepository reportRepository;
    private UserRepository userRepository;
    private ObservableList<ReportView> reportViewList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. ดึง Supervisor ที่ login อยู่
        User loggedInUser = UserSession.getInstance().getCurrentUser();
        if (loggedInUser instanceof Supervisor) {
            this.currentSupervisor = (Supervisor) loggedInUser;
        } else {
            System.err.println("SupervisorReportPage Error: User in session is not a Supervisor.");
        }

        // 2. สร้าง Repositories
        reportRepository = new ReportRepository();
        userRepository = new UserRepository(); // (ใช้สำหรับดึง "ชื่อ" Instructor)

        // 3. ตั้งค่าตาราง
        setupTableColumns();
        loadReportData();
    }

    /**
     * ผูกคอลัมน์ใน FXML กับตัวแปรใน Inner Class (ReportView)
     */
    private void setupTableColumns() {
        colNo.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        colInstructor.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        colCreateDate.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    /**
     * โหลด Report ทั้งหมดมาแสดง
     */
    private void loadReportData() {
        reportViewList.clear();

        // Supervisor จะเห็น Report ทั้งหมด
        List<Report> allReports = reportRepository.getAllReports();

        for (Report report : allReports) {
            // ดึง "ชื่อ" ของ Instructor จาก ID
            User instructor = userRepository.findUserByUsername(report.getInstructorId());
            String instructorName = (instructor != null) ? instructor.getName() : report.getInstructorId();

            // เพิ่มลงใน List ที่จะแสดงผล
            reportViewList.add(new ReportView(report, instructorName));
        }

        reportTableView.setItems(reportViewList);
    }

    // --- Navigation Handlers ---

    @FXML
    public void onHomepageButtonClick() {
        try {
            FXRouter.goTo("supervisor-main-page", currentSupervisor);
        } catch (IOException e) {
            System.err.println("Error navigating to supervisor-main-page: " + e.getMessage());
        }
    }

    @FXML
    public void onScheduleButtonClick() {
        try {
            FXRouter.goTo("supervisor-schedule-page", currentSupervisor);
        } catch (IOException e) {
            System.err.println("Error navigating to supervisor-schedule-page: " + e.getMessage());
        }
    }

    @FXML
    public void onReportButtonClick() {
        // อยู่หน้านี้แล้ว (อาจจะ refresh)
        loadReportData();
    }

    @FXML
    public void onLogoutButtonClick() {
        try {
            UserSession.getInstance().clearSession(); // เคลียร์ Session
            FXRouter.goTo("login");
        } catch (IOException e) {
            System.err.println("Error logging out: " + e.getMessage());
        }
    }


    /**
     * (Inner Class) สำหรับเป็นโมเดลแสดงผลในตาราง
     * เพื่อแปลงข้อมูล (เช่น LocalDateTime) ให้อยู่ในรูป String ที่อ่านง่าย
     */
    public static class ReportView {
        private String reportId;
        private String instructorName;
        private String createDate;
        private String status;

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        public ReportView(Report report, String instructorName) {
            // (อาจจะย่อ ID ให้สั้นลง)
            this.reportId = report.getReportId().substring(0, 8) + "...";
            this.instructorName = instructorName;
            this.status = report.getStatus();

            if (report.getCreateDate() != null) {
                this.createDate = report.getCreateDate().format(formatter);
            } else {
                this.createDate = "N/A";
            }
        }

        // --- Getters (สำคัญมากสำหรับ PropertyValueFactory) ---
        public String getReportId() { return reportId; }
        public String getInstructorName() { return instructorName; }
        public String getCreateDate() { return createDate; }
        public String getStatus() { return status; }
    }
}