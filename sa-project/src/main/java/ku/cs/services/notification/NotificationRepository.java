package ku.cs.services.notification;

import ku.cs.database.DbConnect;
import ku.cs.models.notification.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime; // เพิ่ม Import

public class NotificationRepository {

    // 1. เมธอดสำหรับดึงรายการแจ้งเตือนทั้งหมดของผู้ใช้ (แก้ไขให้ใช้ Field ใหม่)
    public List<Notification> getNotificationsByUsername(String username) {
        List<Notification> notifications = new ArrayList<>();
        // ใช้ recipient_id
        String sql = "SELECT * FROM notifications WHERE recipient_id = ? ORDER BY notification_timestamp DESC";

        try (Connection con = DbConnect.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    // Mapping ตาม Field ใหม่
                    String id = rs.getString("notification_id");
                    String recipientId = rs.getString("recipient_id");
                    String senderId = rs.getString("sender_id");
                    String subject = rs.getString("notification_subject");
                    String content = rs.getString("notification_content");
                    String type = rs.getString("notification_type");
                    boolean isRead = rs.getInt("is_read") == 1;
                    LocalDateTime timestamp = LocalDateTime.parse(rs.getString("notification_timestamp"));

                    Notification notification = new Notification(id, recipientId, senderId, subject,
                            content, type, isRead, timestamp);
                    notifications.add(notification);
                }
            }
        } catch (Exception e) {
            System.err.println("NotificationRepository (getNotificationsByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return notifications;
    }

    // 2. เมธอดสำหรับทำเครื่องหมายว่าอ่านแล้ว (Mark As Read)
    public void markAsRead(String notificationId) {
        // ใช้ notification_id
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        try (Connection con = DbConnect.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, notificationId);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3. เมธอดสำหรับทำเครื่องหมายว่าอ่านทั้งหมด
    public void markAllAsRead(String username) {
        // ใช้ recipient_id
        String sql = "UPDATE notifications SET is_read = 1 WHERE recipient_id = ?";
        try (Connection con = DbConnect.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 4. เมธอดสำหรับบันทึกการแจ้งเตือนใหม่ (Insert New Notification) (แก้ไขให้ใช้ Field ใหม่)
    public void save(Notification notification) {
        String sql = "INSERT INTO notifications (notification_id, recipient_id, sender_id, notification_subject, notification_content, notification_type, is_read, notification_timestamp) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DbConnect.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            // Mapping ตาม Field ใหม่
            statement.setString(1, notification.getNotificationId());
            statement.setString(2, notification.getRecipientId());
            statement.setString(3, notification.getSenderId());
            statement.setString(4, notification.getNotificationSubject());
            statement.setString(5, notification.getNotificationContent());
            statement.setString(6, notification.getNotificationType());
            statement.setInt(7, notification.isRead() ? 1 : 0);
            statement.setString(8, notification.getNotificationTimestamp().toString());

            statement.executeUpdate();
            System.out.println("Notification saved for recipient: " + notification.getRecipientId());

        } catch (Exception e) {
            System.err.println("NotificationRepository (save) Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}