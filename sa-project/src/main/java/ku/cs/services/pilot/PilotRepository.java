package ku.cs.services.pilot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import ku.cs.database.DbConnect;
import ku.cs.models.pilot.Pilot;

public class PilotRepository {

    /**
     * ค้นหา Pilot (พร้อมข้อมูล User) จาก username
     */
    public Pilot findPilotByUsername(String username) {
        Pilot pilot = null;

        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN pilots p ON u.username = p.username " +
                "WHERE u.username = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pilot = new Pilot();

                    // ตั้งค่าข้อมูล "User" (Parent)
                    pilot.setUsername(rs.getString("username"));
                    pilot.setName(rs.getString("name"));
                    pilot.setEmail(rs.getString("email"));
                    pilot.setPhone(rs.getString("phone"));
                    pilot.setRole(rs.getString("role"));
                    pilot.setProfilePicture(rs.getString("profilePicture"));
                    pilot.setHasAccess(rs.getInt("hasAccess") == 1);

                    // ตั้งค่า Hashed Password (ต้องมี setHashedPassword ใน User.java)
                    pilot.setHashedPassword(rs.getString("password"));

                    // ตั้งค่าเวลา Login
                    String dbLastLogin = rs.getString("lastLogin");
                    if (dbLastLogin != null) {
                        pilot.setLastLogin(LocalDateTime.parse(dbLastLogin));
                    }

                    // *** FIXED: ตั้งค่าข้อมูล "Pilot" (Child) ทั้งหมด ***
                    pilot.setPilotID(rs.getString("pilot_id"));
                    pilot.setPilotType(rs.getString("pilot_type"));
                    // Note: Assuming '0' or '1' from DB maps to String in Pilot model
                    pilot.setPilotIsFailed(rs.getString("pilot_is_failed"));
                    pilot.setPilotFailCount(rs.getString("pilot_fail_count"));
                    pilot.setPilotStatus(rs.getString("pilot_status"));
                    pilot.setPilotProgress(rs.getString("pilot_progress"));
                    pilot.setPilotIsAvailable(rs.getString("pilot_is_available"));
                    // *** END FIXED ***
                }
            }

        } catch (SQLException e) {
            System.err.println("PilotRepository (findPilotByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        }

        return pilot;
    }

    /**
     * *** ADDED: ค้นหา Pilot (พร้อมข้อมูล User) จาก pilot ID ***
     * (จำเป็นสำหรับ InstructorSchedulePageController)
     */
    public Pilot findPilotById(String pilotId) {
        Pilot pilot = null;

        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN pilots p ON u.username = p.username " +
                "WHERE p.pilot_id = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pilotId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pilot = new Pilot();

                    // ตั้งค่าข้อมูล "User" (Parent)
                    pilot.setUsername(rs.getString("username"));
                    pilot.setName(rs.getString("name"));
                    pilot.setEmail(rs.getString("email"));
                    pilot.setPhone(rs.getString("phone"));
                    pilot.setRole(rs.getString("role"));
                    pilot.setProfilePicture(rs.getString("profilePicture"));
                    pilot.setHasAccess(rs.getInt("hasAccess") == 1);

                    // ตั้งค่า Hashed Password
                    pilot.setHashedPassword(rs.getString("password"));

                    // ตั้งค่าเวลา Login
                    String dbLastLogin = rs.getString("lastLogin");
                    if (dbLastLogin != null) {
                        pilot.setLastLogin(LocalDateTime.parse(dbLastLogin));
                    }

                    // ตั้งค่าข้อมูล "Pilot" (Child) ทั้งหมด
                    pilot.setPilotID(rs.getString("pilot_id"));
                    pilot.setPilotType(rs.getString("pilot_type"));
                    pilot.setPilotIsFailed(rs.getString("pilot_is_failed"));
                    pilot.setPilotFailCount(rs.getString("pilot_fail_count"));
                    pilot.setPilotStatus(rs.getString("pilot_status"));
                    pilot.setPilotProgress(rs.getString("pilot_progress"));
                    pilot.setPilotIsAvailable(rs.getString("pilot_is_available"));
                }
            }

        } catch (SQLException e) {
            System.err.println("PilotRepository (findPilotById) Error: " + e.getMessage());
            e.printStackTrace();
        }

        return pilot;
    }


    /**
     * อัปเดตสถานะ firstTimeLogin
     */
    public void updateStatusAfterFirstLogin(String username) {
        // SQL assuming you have 'first_time_login' column in 'pilots' table
        String sql = "UPDATE pilots SET first_time_login = 0 WHERE username = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("PilotRepository (updateStatus) Error: " + e.getMessage());
            throw new RuntimeException("Update pilot status failed: " + e.getMessage(), e);
        }
    }

    /**
     * เพิ่ม "โปรไฟล์" Pilot
     */
    public void addPilot(Pilot pilot) {
        String sql = "INSERT INTO pilots (username, pilot_id) VALUES (?, ?)";
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pilot.getUsername());
            pstmt.setString(2, pilot.getPilotID());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("addPilot failed: " + e.getMessage(), e);
        }
    }
}