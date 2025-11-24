// File: ku/cs/models/notification/Notification.java

package ku.cs.models.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {

    // Primary Key
    private String notificationId;

    // ผู้รับ
    private String recipientId;

    // ผู้ส่ง (Username ของผู้ส่ง)
    private String senderId;

    // หัวข้อการแจ้งเตือน
    private String notificationSubject;

    // เนื้อหา/รายละเอียด
    private String notificationContent;

    // ประเภทการแจ้งเตือน
    private String notificationType;

    // สถานะการอ่าน (ยังคงใช้ isRead เพื่อความสอดคล้องกับ Controller/DB)
    private boolean isRead;

    // เวลาที่สร้างการแจ้งเตือน
    private LocalDateTime notificationTimestamp;

    // --- Constructors ---

    /**
     * Constructor สำหรับการโหลดข้อมูลจาก Database
     */
    public Notification(String notificationId, String recipientId, String senderId, String notificationSubject,
                        String notificationContent, String notificationType, boolean isRead, LocalDateTime notificationTimestamp) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.notificationSubject = notificationSubject;
        this.notificationContent = notificationContent;
        this.notificationType = notificationType;
        this.isRead = isRead;
        this.notificationTimestamp = notificationTimestamp;
    }

    /**
     * Constructor สำหรับการสร้าง Notification ใหม่ (ก่อนบันทึกเข้า Database)
     */
    public Notification(String recipientId, String senderId, String notificationSubject, String notificationContent, String notificationType) {
        this.notificationId = "NOTIF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.notificationSubject = notificationSubject;
        this.notificationContent = notificationContent;
        this.notificationType = notificationType;
        this.isRead = false;
        this.notificationTimestamp = LocalDateTime.now();
    }

    // --- Getters ---
    public String getNotificationId() { return notificationId; }
    public String getRecipientId() { return recipientId; }
    public String getSenderId() { return senderId; }
    public String getNotificationSubject() { return notificationSubject; }
    public String getNotificationContent() { return notificationContent; }
    public String getNotificationType() { return notificationType; }
    public boolean isRead() { return isRead; }
    public LocalDateTime getNotificationTimestamp() { return notificationTimestamp; }

    // --- Setters ---
    public void setRead(boolean read) { isRead = read; }

    // --- Override toString() สำหรับใช้แสดงใน ListView (PilotNotificationPageController) ---
    @Override
    public String toString() {
        // ใช้ notificationType + notificationId ตามที่คุณต้องการ
        String statusIcon = isRead ? " " : "⚫ ";
        return statusIcon + this.notificationType + " - " + this.notificationId;
    }
}