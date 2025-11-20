package ku.cs.controllers.instructor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.notification.Notification;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.notification.NotificationRepository;

import java.io.IOException;
import java.util.List;

public class InstructorNotificationPageController {

    // FXML Elements (เก็บไว้เฉพาะที่ใช้แสดงผลข้อมูล)
    @FXML private ListView<Notification> notificationListView;

    @FXML private Label detailTimestampLabel1;
    @FXML private Label detailTimestampLabel11;
    @FXML private Label detailTypeLabel;
    @FXML private Label detailTimestampLabel;
    @FXML private TextArea detailContentTextArea;

    // [แก้ไข 1] ลบตัวแปรปุ่ม (Button) ที่ไม่ได้ใช้ออกทั้งหมด
    // เพราะการทำงานของปุ่มใช้ผ่านเมธอด handle... ด้านล่างอยู่แล้ว

    // Services & Data
    private Instructor currentInstructor;
    private InstructorRepository instructorRepository;
    private NotificationRepository notificationRepository;
    private ObservableList<Notification> notificationList;

    @FXML
    public void initialize() {
        instructorRepository = new InstructorRepository();
        notificationRepository = new NotificationRepository();

        loadInstructorData();

        if (this.currentInstructor != null) {
            setupNotificationList();
            loadNotifications();

            notificationListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            showNotificationDetail(newValue);
                        } else {
                            clearNotificationDetail();
                        }
                    });
        }
        clearNotificationDetail();
    }

    private void loadInstructorData() {
        Object data = FXRouter.getData();
        if (data instanceof Instructor) {
            this.currentInstructor = (Instructor) data;
        } else if (data instanceof User user) { // [แก้ไข 2] ใช้ Pattern Variable
            // รวมบรรทัดเช็คและแปลง Type ไว้ด้วยกัน
            this.currentInstructor = instructorRepository.findInstructorByUsername(user.getUsername());
        }
    }

    private void setupNotificationList() {
        notificationList = FXCollections.observableArrayList();
        notificationListView.setItems(notificationList);

        // [แก้ไข 3] ใช้ Diamond Operator (<>)
        notificationListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item.getSubject() + " [" + item.getTimestamp().toLocalDate() + "]");
                    if (item.isRead()) {
                        setStyle("-fx-font-weight: normal;");
                    } else {
                        setStyle("-fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    // ... (ส่วนอื่นๆ เหมือนเดิม: loadNotifications, showNotificationDetail, clearNotificationDetail) ...

    private void loadNotifications() {
        if (currentInstructor == null) return;
        List<Notification> fetchedNotifs = notificationRepository.getNotificationsByUsername(currentInstructor.getUsername());
        notificationList.clear();
        notificationList.addAll(fetchedNotifs);
    }

    private void showNotificationDetail(Notification notification) {
        detailTimestampLabel1.setText("ผู้ส่ง: " + notification.getType());
        detailTimestampLabel11.setText("ตำแหน่ง: " + (notification.getType().equals("System") ? "ระบบ" : "ไม่ทราบ"));
        detailTypeLabel.setText("เรื่อง: " + notification.getSubject());
        detailTimestampLabel.setText("วันที่: " + notification.getTimestamp().toString());
        detailContentTextArea.setText(notification.getContent());
    }

    private void clearNotificationDetail() {
        detailTimestampLabel1.setText("ผู้ส่ง:");
        detailTimestampLabel11.setText("ตำแหน่ง:");
        detailTypeLabel.setText("เรื่อง:");
        detailTimestampLabel.setText("วันที่:");
        detailContentTextArea.setText("");
    }

    // --- Sidebar Handlers (ชื่อเมธอดต้องตรงกับ FXML) ---

    @FXML
    public void handleHomepageButton() {
        try {
            FXRouter.goTo("instructor-main-page", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleScheduleButton() {
        try {
            FXRouter.goTo("instructor-schedule-page", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReportButton() {
        try {
            FXRouter.goTo("instructor-report-page", currentInstructor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleNotificationButton() {
        // อยู่ในหน้านี้แล้ว
    }

    @FXML
    public void handleLogoutButton() {
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}