package ku.cs.models.notification;

import java.time.LocalDateTime;

public class Notification {

    // Primary Key (สำหรับใช้ใน Database)
    private String id;

    // ผู้รับ (Pilot, Instructor, Supervisor)
    private String recipientUsername;

    // หัวข้อการแจ้งเตือน
    private String subject;

    // เนื้อหา/รายละเอียด
    private String content;

    // ประเภทการแจ้งเตือน (เช่น "Resignation", "Schedule_Change", "Report_Approved")
    private String type;

    // สถานะการอ่าน
    private boolean isRead;

    // เวลาที่สร้างการแจ้งเตือน
    private LocalDateTime timestamp;

    /**
     * Constructor สำหรับการโหลดข้อมูลจาก Database
     */
    public Notification(String id, String recipientUsername, String subject,
                        String content, String type, boolean isRead, LocalDateTime timestamp) {
        this.id = id;
        this.recipientUsername = recipientUsername;
        this.subject = subject;
        this.content = content;
        this.type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    /**
     * Constructor สำหรับการสร้าง Notification ใหม่ ก่อนบันทึกเข้า Database (ไม่ต้องมี ID)
     */
    public Notification(String recipientUsername, String subject, String content, String type) {
        // ID จะถูกกำหนดโดย Database
        this.recipientUsername = recipientUsername;
        this.subject = subject;
        this.content = content;
        this.type = type;
        this.isRead = false; // การแจ้งเตือนใหม่ สถานะเริ่มต้นคือยังไม่ได้อ่าน
        this.timestamp = LocalDateTime.now(); // กำหนดเวลาปัจจุบัน
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // --- Setters (เฉพาะที่จำเป็นต้องเปลี่ยนใน UI หรือ Service) ---

    public void setRead(boolean read) {
        isRead = read;
    }

    // --- Override toString() สำหรับใช้แสดงใน ListView (PilotNotificationPageController) ---
    @Override
    public String toString() {
        // แสดงสัญลักษณ์ที่บ่งบอกสถานะการอ่าน
        String statusIcon = isRead ? " " : "⚫ ";
        return statusIcon + this.subject;
    }
}