package ku.cs.controllers.instructor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.notification.Notification;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.UserSession;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.notification.NotificationRepository;
import ku.cs.services.user.UserRepository;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InstructorNotificationPageController {

    @FXML private ListView<Notification> notificationListView;
    @FXML private Label detailSenderNameLabel;
    @FXML private Label detailSenderRoleLabel;
    @FXML private Label detailSubjectLabel;
    @FXML private Label detaildateLabel;      // ✅ แยกวันที่
    @FXML private Label detailtimeLabel;      // ✅ แยกเวลา
    @FXML private TextArea detailContentTextArea;

    private Instructor currentInstructor;
    private InstructorRepository instructorRepository;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private ObservableList<Notification> notificationList;

    @FXML
    public void initialize() {
        instructorRepository = new InstructorRepository();
        notificationRepository = new NotificationRepository();
        userRepository = new UserRepository();

        loadInstructorData();

        if (this.currentInstructor != null) {
            setupNotificationList();
            loadNotifications();

            notificationListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            showNotificationDetail(newValue);
                            if (!newValue.isRead()) {
                                newValue.setRead(true);
                                notificationListView.refresh();
                                notificationRepository.markAsRead(newValue.getNotificationId());
                            }
                        } else {
                            clearNotificationDetail();
                        }
                    });

            if (notificationList.isEmpty()) {
                notificationListView.setPlaceholder(new Label("ไม่พบการแจ้งเตือน"));
            }
        }
        clearNotificationDetail();
    }

    private void loadInstructorData() {
        User loggedInUser = UserSession.getInstance().getCurrentUser();

        if (loggedInUser instanceof Instructor) {
            this.currentInstructor = (Instructor) loggedInUser;
        } else {
            Object data = FXRouter.getData();
            if (data instanceof Instructor) {
                this.currentInstructor = (Instructor) data;
            } else if (data instanceof User user) {
                this.currentInstructor = instructorRepository.findInstructorByUsername(user.getUsername());
            }
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
        notificationListView.refresh();
    }

    /**
     * ✅ แสดงรายละเอียดพร้อมแยกวันที่และเวลา
     */
    private void showNotificationDetail(Notification notification) {
        // ✅ 1. ตรวจสอบว่าเป็น System_admin หรือไม่
        String senderId = notification.getSenderId();

        if ("System_admin".equalsIgnoreCase(senderId)) {
            // กรณีที่เป็นระบบ
            detailSenderNameLabel.setText("ผู้ส่ง: ระบบ");
            detailSenderRoleLabel.setText("ตำแหน่ง: -");
        } else {
            // กรณีที่เป็นผู้ใช้ทั่วไป
            User sender = userRepository.findUserByUsername(senderId);
            String senderName = (sender != null) ? sender.getName() : "ไม่พบผู้ส่ง";
            String senderRole = (sender != null) ? sender.getRole() : "N/A";

            detailSenderNameLabel.setText("ผู้ส่ง: " + senderName);
            detailSenderRoleLabel.setText("ตำแหน่ง: " + senderRole);
        }

        // ✅ 2. แสดงหัวข้อ
        detailSubjectLabel.setText("เรื่อง: " + notification.getNotificationSubject());

        // ✅ 3. แยกวันที่และเวลา
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String date = notification.getNotificationTimestamp().format(dateFormatter);
        String time = notification.getNotificationTimestamp().format(timeFormatter);

        detaildateLabel.setText("วันที่: " + date);
        detailtimeLabel.setText("เวลา: " + time);

        // ✅ 4. แสดงเนื้อหา
        detailContentTextArea.setText(notification.getNotificationContent());

        // ✅ 5. ทำเครื่องหมายว่าอ่านแล้ว
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationListView.refresh();
            notificationRepository.markAsRead(notification.getNotificationId());
        }
    }

    private void clearNotificationDetail() {
        detailSenderNameLabel.setText("ผู้ส่ง:");
        detailSenderRoleLabel.setText("ตำแหน่ง:");
        detailSubjectLabel.setText("เรื่อง:");
        detaildateLabel.setText("วันที่:");
        detailtimeLabel.setText("เวลา:");
        detailContentTextArea.setText("");
    }

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