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

                // [เพิ่มบรรทัดนี้] ดึง Role จากฐานข้อมูลมาใส่
                supervisor.setRole(rs.getString("role"));

                supervisor.setSupervisorID(rs.getString("supervisor_id"));
            }

        } catch (SQLException e) {
            System.err.println("SupervisorRepository (findSupervisorByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return supervisor;
    }

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
                    supervisor.setUsername(rs.getString("username"));
                    supervisor.setName(rs.getString("name"));
                    supervisor.setEmail(rs.getString("email"));
                    supervisor.setPhone(rs.getString("phone"));

                    // [เพิ่มบรรทัดนี้เช่นกัน] เพื่อให้ข้อมูลครบถ้วนเวลาค้นหาด้วย ID
                    supervisor.setRole(rs.getString("role"));

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