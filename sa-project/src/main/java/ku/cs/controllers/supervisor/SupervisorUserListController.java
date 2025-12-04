package ku.cs.controllers.supervisor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.pilot.PilotRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SupervisorUserListController {

    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private TextField searchTextField;
    @FXML private TableView<UserView> userTableView;
    @FXML private TableColumn<UserView, String> colId;
    @FXML private TableColumn<UserView, String> colName;
    @FXML private TableColumn<UserView, String> colUsername;
    @FXML private TableColumn<UserView, String> colEmail;
    @FXML private TableColumn<UserView, String> colStatus;
    @FXML private TableColumn<UserView, String> colAvailable;

    private PilotRepository pilotRepository;
    private InstructorRepository instructorRepository;
    private ObservableList<UserView> userList;
    private Supervisor currentSupervisor;

    @FXML
    public void initialize() {
        pilotRepository = new PilotRepository();
        instructorRepository = new InstructorRepository();
        userList = FXCollections.observableArrayList();

        // 1. ตั้งค่า ComboBox
        userTypeComboBox.getItems().addAll("Pilot", "Instructor");
        userTypeComboBox.setValue("Pilot"); // ค่าเริ่มต้น
        userTypeComboBox.setOnAction(event -> loadData()); // โหลดข้อมูลใหม่เมื่อเปลี่ยนค่า

        // 2. ตั้งค่าคอลัมน์
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        // 3. โหลดข้อมูลครั้งแรก
        loadData();

        // 4. ตั้งค่าการค้นหา (Search Filter)
        FilteredList<UserView> filteredData = new FilteredList<>(userList, p -> true);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                // ค้นหาจาก Name หรือ ID
                if (user.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getId().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        SortedList<UserView> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTableView.comparatorProperty());
        userTableView.setItems(sortedData);
    }

    private void loadData() {
        userList.clear();
        String selectedType = userTypeComboBox.getValue();

        if ("Pilot".equals(selectedType)) {
            List<Pilot> pilots = pilotRepository.getAllPilots(); // ต้องมี method นี้ใน PilotRepo
            for (Pilot p : pilots) {
                // แปลงสถานะ Available เป็นข้อความ
                String avail = "1".equals(p.getPilotIsAvailable()) ? "Yes" : "No";
                // ใช้ Pilot ID
                userList.add(new UserView(p.getPilotID(), p.getName(), p.getUsername(),
                        p.getEmail(), p.getPilotStatus(), avail));
            }
        } else if ("Instructor".equals(selectedType)) {
            List<Instructor> instructors = instructorRepository.getAllInstructors();
            for (Instructor i : instructors) {
                // Instructor ไม่มี status ย่อยเหมือน Pilot (อาจใช้ Role แทน)
                // และต้องมี getter สำหรับ instructorID
                userList.add(new UserView(i.getInstructorID(), i.getName(), i.getUsername(),
                        i.getEmail(), "Active", "Yes"));
            }
        }
    }

    @FXML
    public void onCheckStatusClick() {
        // เรียกใช้ Logic จาก PilotRepository (Use Case 5)
        pilotRepository.checkAndResetPilotStatus();

        // โหลดข้อมูลในตารางใหม่เพื่อให้เห็นการเปลี่ยนแปลง (Available: Yes/No)
        loadData();

        // แจ้งเตือน
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("System Message");
        alert.setHeaderText(null);
        alert.setContentText("ตรวจสอบและอัปเดตสถานะนักบินเรียบร้อยแล้ว (Use Case 5)");
        alert.showAndWait();
    }

    // --- Navigation Handlers ---
    @FXML public void onHomepageButtonClick() { navigate("supervisor-main-page"); }
    @FXML public void onScheduleButtonClick() { navigate("supervisor-schedule-page"); }
    @FXML public void onReportButtonClick() { navigate("supervisor-report-page"); }
    @FXML public void onLogoutButtonClick() {
        UserSession.getInstance().clearSession();
        navigate("login");
    }

    private void navigate(String route) {
        try { FXRouter.goTo(route); } catch (IOException e) { e.printStackTrace(); }
    }

    // --- Inner Class สำหรับแสดงผลในตาราง (Model View) ---
    public static class UserView {
        private String id;
        private String name;
        private String username;
        private String email;
        private String status;
        private String available;

        public UserView(String id, String name, String username, String email, String status, String available) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.email = email;
            this.status = status;
            this.available = available;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public String getAvailable() { return available; }
    }
}