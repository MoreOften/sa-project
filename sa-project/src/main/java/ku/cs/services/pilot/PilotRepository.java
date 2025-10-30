package ku.cs.services.pilot;

import java.sql.Connection;
import java.sql.PreparedStatement; // ตรวจสอบว่า import model ถูกต้อง
import java.sql.ResultSet;
import java.sql.SQLException;

import ku.cs.database.DbConnect1;
import ku.cs.models.pilot.Pilot;

public class PilotRepository {

    /**
     * ค้นหา Pilot (พร้อมข้อมูล User) จาก username
     * โดยใช้ SQL JOIN
     */
    public Pilot findPilotByUsername(String username) {
        Pilot pilot = null;
        Connection conn = DbConnect1.getConnection();

        // 1. SQL JOIN ระหว่าง 'users' (u) และ 'pilots' (p)
        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN pilots p ON u.username = p.username " +
                "WHERE u.username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 2. สร้างอ็อบเจกต์เปล่า
                pilot = new Pilot();

                // 3. ตั้งค่าข้อมูลพื้นฐานจากตาราง 'users'
                // (สมมติว่า Pilot model มี setters เหล่านี้ทั้งหมด)
                pilot.setUsername(rs.getString("username"));
                pilot.setPassword(rs.getString("password"));
                pilot.setName(rs.getString("name"));           // สมมติว่ามี
                pilot.setEmail(rs.getString("email"));         // สมมติว่ามี
                pilot.setPhone(rs.getString("phone"));         // สมมติว่ามี

            }

        } catch (SQLException e) {
            System.err.println("PilotRepository (findPilotByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
        return pilot;
    }

    /**
     * อัปเดตสถานะ firstTimeLogin และรหัสผ่าน (ถ้ามี) ในตาราง 'pilots'
     */
    public void updateStatusAfterFirstLogin(String username) {
        // สมมติว่าตาราง pilot มีคอลัมน์ first_time_login (INTEGER 1=true, 0=false)
        String sql = "UPDATE pilot_profiles SET first_time_login = 0 WHERE username = ?";
        try (Connection conn = DbConnect1.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Update pilot status failed: " + e.getMessage(), e);
        }
    }

    public void addPilot(Pilot pilot) {
    String sql = "INSERT INTO pilots (username, pilot_id) VALUES (?, ?)";
    try (Connection conn = DbConnect1.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, pilot.getUsername());
        pstmt.setString(2, pilot.getPilotID());
        
        pstmt.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException("addPilot failed: " + e.getMessage(), e);
    }
}
}