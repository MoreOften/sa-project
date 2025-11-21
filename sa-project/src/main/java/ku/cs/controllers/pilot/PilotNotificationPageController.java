package ku.cs.controllers.pilot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.user.User;
import ku.cs.models.notification.Notification;
import ku.cs.services.FXRouter;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.notification.NotificationRepository;
import ku.cs.services.user.UserRepository; // เพิ่ม Import

import java.io.IOException;
import java.util.List;

public class PilotNotificationPageController {

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
    @FXML private Button resignButton;
    @FXML private Button notificationButton;
    @FXML private Button logoutButton;

    // Services & Data
    private Pilot currentPilot;
    private PilotRepository pilotRepository;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository; // Field ใหม่
    private ObservableList<Notification> notificationList;

    @FXML
    public void initialize() {
        // 1. Initialize Services
        pilotRepository = new PilotRepository();
        notificationRepository = new NotificationRepository();
        userRepository = new UserRepository(); // Initialize Repository

        // 2. Load Pilot Data
        loadPilotData();

        if (this.currentPilot != null) {
            // 3. Setup Notification List View
            setupNotificationList();

            // 4. Load Notifications from Repository
            loadNotifications();

            // 5. Setup Listener for item selection
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

    private void loadPilotData() {
        Object data = FXRouter.getData();
        if (data instanceof Pilot) {
            this.currentPilot = (Pilot) data;
        } else if (data instanceof User) {
            User user = (User) data;
            this.currentPilot = pilotRepository.findPilotByUsername(user.getUsername());
        }
    }

    private void setupNotificationList() {
        notificationList = FXCollections.observableArrayList();
        notificationListView.setItems(notificationList);

        // Custom Cell Factory: แสดง notificationType + notificationId
        notificationListView.setCellFactory(lv -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item.toString());

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
        if (currentPilot == null) return;

        List<Notification> fetchedNotifs = notificationRepository.getNotificationsByUsername(currentPilot.getUsername());

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
            FXRouter.goTo("pilot-main-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleScheduleButton() {
        try {
            FXRouter.goTo("pilot-schedule-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReportButton() {
        try {
            FXRouter.goTo("pilot-report-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleResignButton() {
        try {
            FXRouter.goTo("pilot-resign-page", currentPilot);
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