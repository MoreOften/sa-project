package ku.cs.services.notification;

import ku.cs.database.DbConnect;
import ku.cs.models.notification.Notification; // ต้องสร้าง Notification Model ก่อน
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    // 1. เมธอดสำหรับดึงรายการแจ้งเตือนทั้งหมดของผู้ใช้
    public List<Notification> getNotificationsByUsername(String username) {
        List<Notification> notifications = new ArrayList<>();
        // **TODO: Implement SQL Query**
        /*
        String sql = "SELECT * FROM notifications WHERE recipient_username = ? ORDER BY timestamp DESC";
        try (Connection con = DbConnect.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    // สร้าง Notification object จากข้อมูลใน ResultSet
                    // Notification notification = new Notification(...);
                    // notifications.add(notification);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        return notifications;
    }

    // 2. เมธอดสำหรับทำเครื่องหมายว่าอ่านแล้ว (Mark As Read)
    public void markAsRead(String notificationId) {
        // **TODO: Implement SQL Update**
        /*
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection con = DbConnect.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, notificationId);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    // 3. เมธอดสำหรับทำเครื่องหมายว่าอ่านทั้งหมด
    public void markAllAsRead(String username) {
        // **TODO: Implement SQL Update**
        /*
        String sql = "UPDATE notifications SET is_read = TRUE WHERE recipient_username = ?";
        try (Connection con = DbConnect.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    // 4. (Optional) เมธอดสำหรับบันทึกการแจ้งเตือนใหม่ (Insert New Notification)
    // เมธอดนี้จะถูกเรียกใช้จาก Service อื่น ๆ เช่น ResignationService หรือ ReportService
    // public void save(Notification notification) { ... }
}