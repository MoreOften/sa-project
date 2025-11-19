package ku.cs.services.supervisor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime; // (1) เพิ่ม Import

import ku.cs.database.DbConnect;
import ku.cs.models.supervisor.Supervisor;

public class SupervisorRepository {

    /**
     * ค้นหา Supervisor (พร้อมข้อมูล User) จาก username
     * โดยใช้ SQL JOIN (ฉบับแก้ไข)
     */
    public Supervisor findSupervisorByUsername(String username) {
        Supervisor supervisor = null;

        // 1. SQL JOIN ระหว่าง 'users' (u) และ 'supervisors' (s)
        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN supervisors s ON u.username = s.username " +
                "WHERE u.username = ?";

        // (2) ใช้ try-with-resources ที่ครอบคลุม
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // (3) สร้างอ็อบเจกต์เปล่า
                    supervisor = new Supervisor();

                    // (4) ตั้งค่าข้อมูล "User" (Parent)
                    supervisor.setUsername(rs.getString("username"));
                    supervisor.setName(rs.getString("name"));
                    supervisor.setEmail(rs.getString("email"));
                    supervisor.setPhone(rs.getString("phone"));
                    supervisor.setRole(rs.getString("role"));
                    supervisor.setProfilePicture(rs.getString("profilePicture"));
                    supervisor.setHasAccess(rs.getInt("hasAccess") == 1);

                    // (5) ตั้งค่า Hashed Password (ต้องมี setHashedPassword ใน User.java)
                    supervisor.setHashedPassword(rs.getString("password"));

                    // (6) ตั้งค่าเวลา Login
                    String dbLastLogin = rs.getString("lastLogin");
                    if (dbLastLogin != null) {
                        supervisor.setLastLogin(LocalDateTime.parse(dbLastLogin));
                    }

                    // (7) ตั้งค่าข้อมูล "Supervisor" (Child)
                    supervisor.setSupervisorID(rs.getString("supervisor_id"));
                }
            }

        } catch (SQLException e) {
            System.err.println("SupervisorRepository (findSupervisorByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        }
        // (8) ไม่ต้องใช้ finally { conn.close(); }

        return supervisor;
    }

    /**
     * อัปเดตสถานะ firstTimeLogin และรหัสผ่าน (ถ้ามี) ในตาราง 'supervisors'
     * (หมายเหตุ: เมธอดนี้จะทำงานได้ ต่อเมื่อคุณเพิ่มคอลัมน์ first_time_login ในตาราง supervisors)
     */
    public void updateStatusAfterFirstLogin(String username) {
        // สมมติว่าตาราง supervisors มีคอลัมน์ first_time_login (INTEGER 1=true, 0=false)
        String sql = "UPDATE supervisors SET first_time_login = 0 WHERE username = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SupervisorRepository (updateStatus) Error: " + e.getMessage());
            // หากคุณยังไม่ได้เพิ่มคอลัมน์ first_time_login ในตาราง supervisors
            // SQL error "no such column: first_time_login" จะเกิดขึ้นที่นี่
            throw new RuntimeException("Database update supervisor status failed: " + e.getMessage(), e);
        }
    }

    /**
     * เพิ่ม "โปรไฟล์" Supervisor (เมธอดนี้ถูกต้องแล้ว)
     * ใช้สำหรับเชื่อมโยง username (จากตาราง users) กับ supervisor_id
     */
    public void addSupervisor(Supervisor supervisor) {
        String sql = "INSERT INTO supervisors (username, supervisor_id) VALUES (?, ?)";
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supervisor.getUsername());
            pstmt.setString(2, supervisor.getSupervisorID());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("addSupervisor failed: " + e.getMessage(), e);
        }
    }
}