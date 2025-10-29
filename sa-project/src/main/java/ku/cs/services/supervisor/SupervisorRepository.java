package ku.cs.services.supervisor;

import ku.cs.database.DbConnect;
import ku.cs.models.supervisor.Supervisor; // ตรวจสอบว่า import model ถูกต้อง

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp; // ต้อง import Timestamp

public class SupervisorRepository {

    /**
     * ค้นหา Supervisor (พร้อมข้อมูล User) จาก username
     * โดยใช้ SQL JOIN
     */
    public Supervisor findSupervisorByUsername(String username) {
        Supervisor supervisor = null;
        Connection conn = DbConnect.getConnection();

        // 1. SQL JOIN ระหว่าง 'users' (u) และ 'supervisors' (s)
        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN supervisors s ON u.username = s.username " +
                "WHERE u.username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 2. สร้างอ็อบเจกต์เปล่า
                supervisor = new Supervisor();

                // 3. ตั้งค่าข้อมูลพื้นฐานจากตาราง 'users'
                supervisor.setUsername(rs.getString("username"));
                supervisor.setPassword(rs.getString("password"));
                supervisor.setName(rs.getString("name"));
                supervisor.setEmail(rs.getString("email"));
                supervisor.setPhone(rs.getString("phone"));

            }

        } catch (SQLException e) {
            System.err.println("SupervisorRepository (findSupervisorByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
        return supervisor;
    }

    /**
     * อัปเดตสถานะ firstTimeLogin และรหัสผ่าน (ถ้ามี) ในตาราง 'supervisors'
     */
    public void updatePasswordAndStatus(String username, String newPassword) {
        Connection conn = DbConnect.getConnection();
        // ถ้าตาราง supervisors ไม่เก็บ password ให้ลบ 'password = ?,' ออก
        String sql = "UPDATE supervisors SET password = ?, firstTimeLogin = ? WHERE username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setBoolean(2, false);
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SupervisorRepository (updatePasswordAndStatus) Error: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}