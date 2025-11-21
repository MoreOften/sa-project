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
import ku.cs.services.user.UserRepository; // เพิ่ม Import

import java.io.IOException;
import java.util.List;

public class InstructorNotificationPageController {

    // FXML Elements
    @FXML private ListView<Notification> notificationListView;

    @FXML private Label detailTimestampLabel1;  // สำหรับ "ผู้ส่ง:"
    @FXML private Label detailTimestampLabel11; // สำหรับ "ตำแหน่ง:"
    @FXML private Label detailTypeLabel;        // สำหรับ "เรื่อง:"
    @FXML private Label detailTimestampLabel;   // สำหรับ "วันที่:"
    @FXML private TextArea detailContentTextArea;


    // Services & Data
    private Instructor currentInstructor;
    private InstructorRepository instructorRepository;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository; // Field ใหม่
    private ObservableList<Notification> notificationList;

    @FXML
    public void initialize() {
        instructorRepository = new InstructorRepository();
        notificationRepository = new NotificationRepository();
        userRepository = new UserRepository(); // Initialize Repository

        loadInstructorData();

        if (this.currentInstructor != null) {
            setupNotificationList();
            loadNotifications();

            notificationListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            showNotificationDetail(newValue);
                            if (!newValue.isRead()) { // ทำเครื่องหมายว่าอ่านแล้วทันที
                                newValue.setRead(true);
                                notificationListView.refresh();
                                notificationRepository.markAsRead(newValue.getNotificationId());
                            }
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
        } else if (data instanceof User user) {
            this.currentInstructor = instructorRepository.findInstructorByUsername(user.getUsername());
        }
    }

    private void setupNotificationList() {
        notificationList = FXCollections.observableArrayList();
        notificationListView.setItems(notificationList);

        notificationListView.setCellFactory(lv -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    // แสดง notificationType + notificationId
                    setText(item.toString());

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
        List<Notification> fetchedNotifs = notificationRepository.getNotificationsByUsername(currentInstructor.getUsername());
        notificationList.clear();
        notificationList.addAll(fetchedNotifs);
    }

    private void showNotificationDetail(Notification notification) {
        // ดึงข้อมูลผู้ส่ง (Sender)
        User sender = userRepository.findUserByUsername(notification.getSenderId());
        String senderName = (sender != null) ? sender.getName() : "ไม่พบผู้ส่ง";
        String senderRole = (sender != null) ? sender.getRole() : "N/A";

        // ผู้ส่ง: senderId.getName
        detailTimestampLabel1.setText("ผู้ส่ง: " + senderName);

        // ตำแหน่ง: senderId.getRole
        detailTimestampLabel11.setText("ตำแหน่ง: " + senderRole);

        // เรื่อง: notificationSubject
        detailTypeLabel.setText("เรื่อง: " + notification.getNotificationSubject());

        // วันที่: notificationTimestamp
        detailTimestampLabel.setText("วันที่: " + notification.getNotificationTimestamp().toString());

        // รายละเอียด: notificationContent
        detailContentTextArea.setText(notification.getNotificationContent());

        // (ถ้ามีการเลือก ให้ทำเครื่องหมายว่าอ่านแล้วใน Model เพื่อให้สีเปลี่ยน)
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationListView.refresh();
            notificationRepository.markAsRead(notification.getNotificationId());
        }
    }

    private void clearNotificationDetail() {
        detailTimestampLabel1.setText("ผู้ส่ง:");
        detailTimestampLabel11.setText("ตำแหน่ง:");
        detailTypeLabel.setText("เรื่อง:");
        detailTimestampLabel.setText("วันที่:");
        detailContentTextArea.setText("");
    }

    // --- Sidebar Handlers (ยังคงเดิม) ---

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