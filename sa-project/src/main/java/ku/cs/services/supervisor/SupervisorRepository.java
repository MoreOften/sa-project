package ku.cs.services.supervisor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ku.cs.database.DbConnect;
import ku.cs.models.supervisor.Supervisor;

public class SupervisorRepository {

    public Supervisor findSupervisorByUsername(String username) {
        Supervisor supervisor = null;
        Connection conn = DbConnect.getConnection();

        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN supervisors s ON u.username = s.username " +
                "WHERE u.username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                supervisor = new Supervisor();
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
     * 🔧 FIXED: แก้ไขให้โหลด Username ด้วย
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
                    supervisor.setName(rs.getString("name"));
                    supervisor.setUsername(rs.getString("username")); // ✅ โหลด Username
                    supervisor.setSupervisorID(rs.getString("supervisor_id")); // ✅ โหลด ID ด้วย
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

    public String getEmailById(String supervisorID) {
        if (supervisorID.equals("S001")) {
            return "heartofficial16@gmail.com";
        }
        return null;
    }
}