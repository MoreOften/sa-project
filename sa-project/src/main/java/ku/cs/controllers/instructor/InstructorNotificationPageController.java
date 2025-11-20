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

    // FXML Elements (ต้องตรงกับ fx:id ในไฟล์ FXML ของ Instructor)
    @FXML private ListView<Notification> notificationListView;

    @FXML private Label detailTimestampLabel1;  // สำหรับ "ผู้ส่ง:"
    @FXML private Label detailTimestampLabel11; // สำหรับ "ตำแหน่ง:"
    @FXML private Label detailTypeLabel;        // สำหรับ "เรื่อง:"
    @FXML private Label detailTimestampLabel;   // สำหรับ "วันที่:"
    @FXML private TextArea detailContentTextArea;

    // Sidebar Buttons
    @FXML private Button homepageButton;
    @FXML private Button scheduleButton;
    @FXML private Button reportButton;
    @FXML private Button notificationButton; // ปุ่มนี้อาจจะต้องเพิ่มใน FXML ของ Instructor ถ้ายังไม่มี
    @FXML private Button logoutButton;
    // หมายเหตุ: Instructor ปกติไม่มีปุ่ม Resign ถ้าใน FXML ไม่มีให้ลบ @FXML นี้ออก
    // @FXML private Button resignButton;

    // Services & Data
    private Instructor currentInstructor;
    private InstructorRepository instructorRepository;
    private NotificationRepository notificationRepository;
    private ObservableList<Notification> notificationList;

    @FXML
    public void initialize() {
        // 1. Initialize Services
        instructorRepository = new InstructorRepository();
        notificationRepository = new NotificationRepository();

        // 2. Load Instructor Data
        loadInstructorData();

        if (this.currentInstructor != null) {
            // 3. Setup Notification List View
            setupNotificationList();

            // 4. Load Notifications from Repository
            loadNotifications();

            // 5. Setup Listener for item selection
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

    // --- Data Loading and Display ---

    private void loadInstructorData() {
        Object data = FXRouter.getData();
        if (data instanceof Instructor) {
            this.currentInstructor = (Instructor) data;
        } else if (data instanceof User) {
            User user = (User) data;
            this.currentInstructor = instructorRepository.findInstructorByUsername(user.getUsername());
        }
    }

    private void setupNotificationList() {
        notificationList = FXCollections.observableArrayList();
        notificationListView.setItems(notificationList);

        // Custom Cell Factory: เน้นข้อความที่ยังไม่ได้อ่านด้วยตัวหนา
        notificationListView.setCellFactory(lv -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    // แสดงหัวข้อ + timestamp
                    setText(item.getSubject() + " [" + item.getTimestamp().toLocalDate() + "]");

                    // Style: Bold สำหรับข้อความที่ยังไม่อ่าน
                    if (item.isRead()) {
                        setStyle("-fx-font-weight: normal;");
                    } else {
                        setStyle("-fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadNotifications() {
        if (currentInstructor == null) return;

        // ดึงการแจ้งเตือนทั้งหมดของ Instructor คนนี้
        List<Notification> fetchedNotifs = notificationRepository.getNotificationsByUsername(currentInstructor.getUsername());

        notificationList.clear();
        notificationList.addAll(fetchedNotifs);
    }

    private void showNotificationDetail(Notification notification) {
        detailTimestampLabel1.setText("ผู้ส่ง: " + notification.getType());
        // ปรับ Logic ตรงตำแหน่งได้ตามต้องการ
        detailTimestampLabel11.setText("ตำแหน่ง: " + (notification.getType().equals("System") ? "ระบบ" : "ไม่ทราบ"));

        detailTypeLabel.setText("เรื่อง: " + notification.getSubject());
        detailTimestampLabel.setText("วันที่: " + notification.getTimestamp().toString());
        detailContentTextArea.setText(notification.getContent());

        // ถ้าต้องการให้คลิกแล้ว Mark as read ทันที สามารถเพิ่ม logic ตรงนี้ได้
        // notification.setRead(true);
        // notificationRepository.updateNotification(notification);
    }

    private void clearNotificationDetail() {
        detailTimestampLabel1.setText("ผู้ส่ง:");
        detailTimestampLabel11.setText("ตำแหน่ง:");
        detailTypeLabel.setText("เรื่อง:");
        detailTimestampLabel.setText("วันที่:");
        detailContentTextArea.setText("");
    }

    // --- Sidebar Handlers ---

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