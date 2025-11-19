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

import java.io.IOException;
import java.util.List;

public class PilotNotificationPageController {

    // FXML Elements ที่อ้างอิงตาม FXML ใหม่

    // Label แสดงชื่อนักบินถูกลบออกจาก FXML แล้ว จึงต้องลบคอมเมนต์ส่วนนี้
    // @FXML private Label pilotNameLabel;

    // Label และ Button สำหรับสถานะการอ่านถูกลบออกจาก FXML แล้ว
    // @FXML private Label unreadCountLabel;
    // @FXML private Button markAsReadButton;

    @FXML private ListView<Notification> notificationListView;

    // FXML ใหม่ (เปลี่ยนตามวัตถุประสงค์ที่กำหนดใน FXML)
    @FXML private Label detailTimestampLabel1;  // สำหรับ "ผู้ส่ง:"
    @FXML private Label detailTimestampLabel11; // สำหรับ "ตำแหน่ง:"
    @FXML private Label detailTypeLabel;        // สำหรับ "เรื่อง:" (เดิมคือ Subject)
    @FXML private Label detailTimestampLabel;   // สำหรับ "วันที่:"
    @FXML private TextArea detailContentTextArea;


    // Sidebar Buttons (ยังคงเดิม)
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
    private ObservableList<Notification> notificationList;

    @FXML
    public void initialize() {
        // 1. Initialize Services
        pilotRepository = new PilotRepository();
        notificationRepository = new NotificationRepository();

        // 2. Load Pilot Data
        loadPilotData();

        if (this.currentPilot != null) {
            // showPilotData(); // Label แสดงชื่อนักบินถูกลบแล้ว

            // 3. Setup Notification List View
            setupNotificationList();

            // 4. Load Notifications from Repository
            loadNotifications();

            // 5. Setup Listener for item selection
            notificationListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            showNotificationDetail(newValue);
                            // ตรรกะการทำเครื่องหมายว่าอ่านแล้วถูกเอาออก เพราะปุ่มถูกลบ
                        } else {
                            clearNotificationDetail();
                        }
                    });
        }
        clearNotificationDetail();
    }

    // --- Data Loading and Display ---

    private void loadPilotData() {
        // ... (ตรรกะเดิมในการโหลด currentPilot) ...
        Object data = FXRouter.getData();
        if (data instanceof Pilot) {
            this.currentPilot = (Pilot) data;
        } else if (data instanceof User) {
            User user = (User) data;
            this.currentPilot = pilotRepository.findPilotByUsername(user.getUsername());
        }
    }

    // private void showPilotData() {
    //     // pilotNameLabel ถูกลบออกจาก FXML แล้ว
    //     // pilotNameLabel.setText(currentPilot.getName());
    // }

    private void setupNotificationList() {
        notificationList = FXCollections.observableArrayList();
        notificationListView.setItems(notificationList);

        // Custom Cell Factory: เน้นข้อความที่ยังไม่ได้อ่านด้วยตัวหนา (ยังคงไว้)
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
        if (currentPilot == null) return;

        // ดึงการแจ้งเตือนทั้งหมด
        List<Notification> fetchedNotifs = notificationRepository.getNotificationsByUsername(currentPilot.getUsername());

        notificationList.clear();
        notificationList.addAll(fetchedNotifs);

        // updateUnreadCount(); // ถูกลบเพราะ Label ถูกลบแล้ว
    }

    // private void updateUnreadCount() {
    //     // ถูกลบเพราะ Label ถูกลบแล้ว
    //     long unreadCount = notificationList.stream().filter(n -> !n.isRead()).count();
    //     unreadCountLabel.setText("✉️ ยังไม่ได้อ่าน: " + unreadCount + " รายการ");
    // }

    private void showNotificationDetail(Notification notification) {
        // FXML ใหม่
        // ผู้ส่งและตำแหน่งไม่ได้อยู่ใน Model (Notification) โดยตรง, ใช้ placeholder หรือสมมติว่า Type คือผู้ส่ง
        detailTimestampLabel1.setText("ผู้ส่ง: " + notification.getType());
        detailTimestampLabel11.setText("ตำแหน่ง: " + (notification.getType().equals("Resignation") ? "ระบบ" : "ไม่ทราบ")); // Placeholder

        // detailTypeLabel ถูกเปลี่ยนให้แสดง "เรื่อง:"
        detailTypeLabel.setText("เรื่อง: " + notification.getSubject());

        // detailTimestampLabel ยังคงแสดง "วันที่:"
        detailTimestampLabel.setText("วันที่: " + notification.getTimestamp().toString());

        detailContentTextArea.setText(notification.getContent());

        // markAsReadButton ถูกลบ
    }

    private void clearNotificationDetail() {
        detailTimestampLabel1.setText("ผู้ส่ง:");
        detailTimestampLabel11.setText("ตำแหน่ง:");
        detailTypeLabel.setText("เรื่อง:");
        detailTimestampLabel.setText("วันที่:");
        detailContentTextArea.setText("");
        // markAsReadButton ถูกลบ
    }

    // --- Action Handlers ---

    // handleMarkAsRead และ handleMarkAllAsRead ถูกลบเพราะปุ่มถูกลบออกจาก FXML แล้ว

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