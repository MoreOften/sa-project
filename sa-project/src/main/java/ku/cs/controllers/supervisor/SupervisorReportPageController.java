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
        }

        // 2. สร้าง Repositories
        reportRepository = new ReportRepository();
        userRepository = new UserRepository();

        // 3. ตั้งค่าตารางและโหลดข้อมูล
        setupTableColumns();
        setupRowClickListener();
        loadReportData();

        // 4. ✅ เรียกใช้เมธอดดักจับการคลิก (ต้องมีเมธอดนี้อยู่ด้านล่าง)

    }

    private void setupTableColumns() {
        colNo.setCellValueFactory(new PropertyValueFactory<>("displayReportId")); // แก้ให้ใช้ชื่อที่แสดง
        colInstructor.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        colCreateDate.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadReportData() {
        reportViewList.clear();
        List<Report> allReports = reportRepository.findAll();

        for (Report report : allReports) {
            User instructor = userRepository.findUserByUsername(report.getInstructorId());
            String instructorName = (instructor != null) ? instructor.getName() : report.getInstructorId();
            reportViewList.add(new ReportView(report, instructorName));
        }

        reportTableView.setItems(reportViewList);
    }

    // ✅ เพิ่มเมธอดนี้ลงไปในคลาส (ห้ามลืม!)
    private void setupRowClickListener() {
        reportTableView.setOnMouseClicked(event -> {
            // ตรวจสอบว่าเป็นการดับเบิลคลิก (Click Count = 2)
            if (event.getClickCount() == 2) {
                ReportView selectedReport = reportTableView.getSelectionModel().getSelectedItem();
                if (selectedReport != null) {
                    try {
                        // ส่ง ID เต็ม (fullReportId) ไปยังหน้า Detail
                        FXRouter.goTo("supervisor-report-detail", selectedReport.getFullReportId());
                    } catch (IOException e) {
                        System.err.println("ไปที่หน้า supervisor-report-detail ไม่ได้: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // --- Navigation Handlers ---
    @FXML public void onHomepageButtonClick() { navigate("supervisor-main-page"); }
    @FXML public void onScheduleButtonClick() { navigate("supervisor-schedule-page"); }
    @FXML public void onReportButtonClick() { loadReportData(); }
    @FXML public void onLogoutButtonClick() {
        UserSession.getInstance().clearSession();
        navigate("login");
    }

    private void navigate(String route) {
        try { FXRouter.goTo(route, currentSupervisor); }
        catch (IOException e) { e.printStackTrace(); }
    }

    // --- Inner Class (Model สำหรับแสดงผล) ---
    public static class ReportView {
        private String fullReportId;   // ✅ เก็บ ID จริงไว้ใช้ส่งข้อมูล
        private String displayReportId; // ✅ เก็บ ID แบบย่อไว้แสดงผล
        private String instructorName;
        private String createDate;
        private String status;

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        public ReportView(Report report, String instructorName) {
            this.fullReportId = report.getReportId(); // เก็บตัวเต็ม

            // สร้างตัวย่อสำหรับแสดงผล
            if (this.fullReportId.length() > 8) {
                this.displayReportId = this.fullReportId.substring(0, 8) + "...";
            } else {
                this.displayReportId = this.fullReportId;
            }

            this.instructorName = instructorName;
            this.status = report.getApprovalStatus().toString(); // ใช้ Enum.toString()

            if (report.getCreatedAt() != null) {
                this.createDate = report.getCreatedAt().format(formatter);
            } else {
                this.createDate = "N/A";
            }
        }

        // Getters
        public String getFullReportId() { return fullReportId; }
        public String getDisplayReportId() { return displayReportId; }
        public String getInstructorName() { return instructorName; }
        public String getCreateDate() { return createDate; }
        public String getStatus() { return status; }
    }

    @FXML
    public void onUserInfoButtonClick() {
        try {
            FXRouter.goTo("supervisor-user-list");
        } catch (IOException e) {
            System.err.println("ไปที่หน้า User Info ไม่ได้");
            e.printStackTrace();
        }
    }
}