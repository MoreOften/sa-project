package ku.cs.services.pilot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime; // (ต้อง import)

import ku.cs.database.DbConnect1;
import ku.cs.models.pilot.Pilot;

public class PilotRepository {

    /**
     * ค้นหา Pilot (พร้อมข้อมูล User) จาก username
     * โดยใช้ SQL JOIN (ฉบับแก้ไข)
     */
    public Pilot findPilotByUsername(String username) {
        Pilot pilot = null;
<<<<<<< HEAD
        Connection conn = DbConnect1.getConnection();
=======
>>>>>>> 809eab5c8272402505ba653313eb5a9f31e67d46

        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN pilots p ON u.username = p.username " +
                "WHERE u.username = ?";

        // (แก้ไข 1) ใช้ try-with-resources ที่ครอบคลุม
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pilot = new Pilot();

                    // (แก้ไข 2) ตั้งค่าข้อมูล "User" (Parent)
                    pilot.setUsername(rs.getString("username"));
                    pilot.setName(rs.getString("name"));
                    pilot.setEmail(rs.getString("email"));
                    pilot.setPhone(rs.getString("phone"));
                    pilot.setRole(rs.getString("role"));
                    pilot.setProfilePicture(rs.getString("profilePicture"));
                    pilot.setHasAccess(rs.getInt("hasAccess") == 1);

                    // (แก้ไข 3) ตั้งค่า Hashed Password (ต้องมี setHashedPassword ใน User.java)
                    pilot.setHashedPassword(rs.getString("password"));

                    // (แก้ไข 4) ตั้งค่าเวลา Login
                    String dbLastLogin = rs.getString("lastLogin");
                    if (dbLastLogin != null) {
                        pilot.setLastLogin(LocalDateTime.parse(dbLastLogin));
                    }

                    // (แก้ไข 5) ตั้งค่าข้อมูล "Pilot" (Child)
                    pilot.setPilotID(rs.getString("pilot_id"));
                }
            }

        } catch (SQLException e) {
            System.err.println("PilotRepository (findPilotByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        }
        // (แก้ไข 6) ไม่ต้องใช้ finally { conn.close(); }

        return pilot;
    }

    /**
     * อัปเดตสถานะ firstTimeLogin
     * (ฉบับแก้ไข)
     */
    public void updateStatusAfterFirstLogin(String username) {
<<<<<<< HEAD
        // สมมติว่าตาราง pilot มีคอลัมน์ first_time_login (INTEGER 1=true, 0=false)
        String sql = "UPDATE pilot_profiles SET first_time_login = 0 WHERE username = ?";
        try (Connection conn = DbConnect1.getConnection();
=======
        // (แก้ไข 7) ต้องอัปเดตตาราง "pilots" (ไม่ใช่ "pilot_profiles")
        // และคอลัมน์ "first_time_login" (ถ้าคุณมีใน SQL)
        String sql = "UPDATE pilots SET first_time_login = 0 WHERE username = ?";

        try (Connection conn = DbConnect.getConnection();
>>>>>>> 809eab5c8272402505ba653313eb5a9f31e67d46
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            // (ต้องเช็คก่อนว่าตาราง pilots มีคอลัมน์ first_time_login จริง)
            System.err.println("PilotRepository (updateStatus) Error: " + e.getMessage());
            throw new RuntimeException("Update pilot status failed: " + e.getMessage(), e);
        }
    }

    /**
     * เพิ่ม "โปรไฟล์" Pilot (เมธอดนี้ถูกต้องแล้ว)
     */
    public void addPilot(Pilot pilot) {
<<<<<<< HEAD
    String sql = "INSERT INTO pilots (username, pilot_id) VALUES (?, ?)";
    try (Connection conn = DbConnect1.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, pilot.getUsername());
        pstmt.setString(2, pilot.getPilotID());
        
        pstmt.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException("addPilot failed: " + e.getMessage(), e);
=======
        String sql = "INSERT INTO pilots (username, pilot_id) VALUES (?, ?)";
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pilot.getUsername());
            pstmt.setString(2, pilot.getPilotID());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Error "PRIMARY KEY" ที่คุณเจอ ถูกโยนมาจากบรรทัดนี้
            throw new RuntimeException("addPilot failed: " + e.getMessage(), e);
        }
>>>>>>> 809eab5c8272402505ba653313eb5a9f31e67d46
    }
}