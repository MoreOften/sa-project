package ku.cs.services.supervisor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ku.cs.database.DbConnect;
import ku.cs.models.supervisor.Supervisor;

public class SupervisorRepository {

    /**
     * ค้นหา Supervisor (พร้อมข้อมูล User) จาก username
     * โดยใช้ SQL JOIN
     */
    public Supervisor findSupervisorByUsername(String username) {
        Supervisor supervisor = null;

        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN supervisors s ON u.username = s.username " +
                "WHERE u.username = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                supervisor = new Supervisor();
                supervisor.setUsername(rs.getString("username"));
                supervisor.setPassword(rs.getString("password"));
                supervisor.setName(rs.getString("name"));
                supervisor.setEmail(rs.getString("email"));
                supervisor.setPhone(rs.getString("phone"));
                supervisor.setSupervisorID(rs.getString("supervisor_id"));
            }

        } catch (SQLException e) {
            System.err.println("SupervisorRepository (findSupervisorByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        }

        return supervisor;
    }

    /**
     * *** แก้ไข: ค้นหา Supervisor จาก supervisor_id ***
     * เดิมใช้ WHERE s.supervisor_id = ? ซึ่งถูกต้องแล้ว
     * แต่ต้องแน่ใจว่า setSupervisorID() ถูกเรียกด้วย
     */
    public Supervisor findSupervisorById(String supervisorId) {
        String sql = "SELECT * FROM users u " +
                "JOIN supervisors s ON u.username = s.username " +
                "WHERE s.supervisor_id = ?";

        Supervisor supervisor = null;

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supervisorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    supervisor = new Supervisor();
                    // *** สำคัญ: ต้อง set ข้อมูลทั้งหมดจาก ResultSet ***
                    supervisor.setUsername(rs.getString("username"));
                    supervisor.setName(rs.getString("name"));
                    supervisor.setEmail(rs.getString("email"));
                    supervisor.setPhone(rs.getString("phone"));
                    supervisor.setSupervisorID(rs.getString("supervisor_id"));

                    System.out.println("-> [SupervisorRepo] พบ Supervisor ID: " + supervisorId +
                            ", Name: " + supervisor.getName());
                }
            }
        } catch (SQLException e) {
            System.err.println("SupervisorRepository (findSupervisorById) Error: " + e.getMessage());
            e.printStackTrace();
        }

        return supervisor;
    }

    /**
     * อัปเดตสถานะ firstTimeLogin และรหัสผ่าน (ถ้ามี) ในตาราง 'supervisors'
     */
    public void updateStatusAfterFirstLogin(String username) {
        String sql = "UPDATE supervisors SET first_time_login = 0 WHERE username = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SupervisorRepository (updateStatus) Error: " + e.getMessage());
            throw new RuntimeException("Database update supervisor status failed: " + e.getMessage(), e);
        }
    }

    /**
     * เพิ่ม Supervisor ใหม่
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

    /**
     * *** เมธอดใหม่: ดึง Email จาก Supervisor ID ***
     * (ใช้สำหรับ EmailService)
     */
    public String getEmailById(String supervisorID) {
        String sql = "SELECT u.email FROM users u " +
                "JOIN supervisors s ON u.username = s.username " +
                "WHERE s.supervisor_id = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supervisorID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            System.err.println("SupervisorRepository (getEmailById) Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}