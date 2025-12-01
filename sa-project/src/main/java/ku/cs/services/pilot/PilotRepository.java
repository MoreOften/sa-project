package ku.cs.services.pilot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import ku.cs.database.DbConnect;
import ku.cs.models.pilot.Pilot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PilotRepository {

    private static final List<Pilot> pilots = List.of();

    // ... (Existing methods) ...

    // เพิ่มเมธอดนี้ใน PilotRepository.java

    /**
     * อัปเดตสถานะของ Pilot (pilot_is_available, pilot_is_failed, pilot_fail_count)
     * @param pilot อ็อบเจกต์ Pilot ที่ต้องการอัปเดต
     * @return true ถ้าอัปเดตสำเร็จ
     */
    public boolean updatePilotStatus(Pilot pilot) {
        String sql = "UPDATE pilots SET " +
                "pilot_is_available = ?, " +
                "pilot_is_failed = ?, " +
                "pilot_fail_count = ? " +
                "WHERE pilot_id = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // ตั้งค่า Parameters
            pstmt.setString(1, pilot.getPilotIsAvailable());
            pstmt.setString(2, pilot.getPilotIsFailed());
            pstmt.setString(3, pilot.getPilotFailCount());
            pstmt.setString(4, pilot.getPilotID());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.printf("-> [PilotRepo] อัปเดตสถานะ Pilot ID %s สำเร็จ " +
                                "(Available: %s, Failed: %s, FailCount: %s)%n",
                        pilot.getPilotID(),
                        pilot.getPilotIsAvailable(),
                        pilot.getPilotIsFailed(),
                        pilot.getPilotFailCount());
                return true;
            } else {
                System.err.printf("-> [PilotRepo] ERROR: ไม่พบ Pilot ID %s ในฐานข้อมูล%n",
                        pilot.getPilotID());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("PilotRepository (updatePilotStatus) Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Use Case Step 7: เปลี่ยนสถานะ Pilot เป็น 'resigned' และ Pilot_Is_Available = False
     * @param pilotID ID ของนักบินที่ลาออก
     * @return true ถ้าอัปเดตสำเร็จ
     */
    public boolean updatePilotToResigned(String pilotID) {
        // SQL: UPDATE pilots SET pilot_status = 'resigned', pilot_is_available = 0 WHERE pilot_id = ?;
        // NOTE: pilot_is_available = 0 คือ False
        String sql = "UPDATE pilots SET pilot_status = 'resigned', pilot_is_available = 0 WHERE pilot_id = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pilotID);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.printf("-> [PilotRepo] Pilot ID %s สถานะเปลี่ยนเป็น 'ลาออก' (resigned) ใน DB แล้ว.%n", pilotID);
                return true;
            } else {
                // เพิ่มการแจ้งเตือนหากไม่พบ Pilot ID นี้ในตาราง pilots
                System.err.printf("-> [PilotRepo] ERROR: ไม่พบ Pilot ID %s ในตาราง pilots หรือ Pilot มีสถานะเป็น 'resigned' อยู่แล้ว.%n", pilotID);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("PilotRepository (updatePilotToResigned) Database Error: " + e.getMessage());
            // แสดง Error Stack Trace เพื่อช่วยในการ debug
            e.printStackTrace();
            return false;
        }
    }

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

    /**
     * ค้นหา Pilot (พร้อมข้อมูล User) จาก pilot ID
     * (***แก้ไข: เพิ่มการโหลด Pilot Status/Type/etc. ทั้งหมด***)
     */
    public Pilot findPilotById(String pilotId) {
        // 1. SQL JOIN ระหว่าง 'users' (u) และ 'pilots' (p)
        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN pilots p ON u.username = p.username " +
                "WHERE p.pilot_id = ?"; // <-- ค้นหาด้วย pilot_id

        Pilot pilot = null;

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 2. ตั้งค่า Parameter เป็น pilotId
            pstmt.setString(1, pilotId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 3. (ตรรกะประกอบร่าง) สร้างอ็อบเจกต์ว่าง
                    pilot = new Pilot();

                    // 4. ตั้งค่าข้อมูล "User" (Parent) จากตาราง 'users'
                    pilot.setUsername(rs.getString("username"));
                    pilot.setName(rs.getString("name"));
                    pilot.setEmail(rs.getString("email"));
                    pilot.setPhone(rs.getString("phone"));
                    pilot.setRole(rs.getString("role"));
                    pilot.setProfilePicture(rs.getString("profilePicture"));
                    pilot.setHasAccess(rs.getInt("hasAccess") == 1);
                    pilot.setHashedPassword(rs.getString("password"));

                    // 5. ตั้งค่าเวลา Login
                    String dbLastLogin = rs.getString("lastLogin");
                    if (dbLastLogin != null) {
                        pilot.setLastLogin(LocalDateTime.parse(dbLastLogin));
                    }

                    // 6. ***FIXED: ตั้งค่าข้อมูล "Pilot" (Child) ทั้งหมด***
                    pilot.setPilotID(rs.getString("pilot_id"));
                    pilot.setPilotType(rs.getString("pilot_type"));
                    pilot.setPilotIsFailed(rs.getString("pilot_is_failed"));
                    pilot.setPilotFailCount(rs.getString("pilot_fail_count"));
                    pilot.setPilotStatus(rs.getString("pilot_status")); // <-- THIS WAS MISSING
                    pilot.setPilotProgress(rs.getString("pilot_progress"));
                    pilot.setPilotIsAvailable(rs.getString("pilot_is_available"));
                    // *** END FIXED ***
                }
            }

        } catch (SQLException e) {
            System.err.println("PilotRepository (findPilotById) Error: " + e.getMessage());
            e.printStackTrace();
        }

        return pilot; // คืนค่า pilot (ที่มีข้อมูลครบ) หรือ null
    }

    public List<Pilot> getAllPilots() {
        List<Pilot> pilots = new ArrayList<>();
        String sql = "SELECT * FROM users u JOIN pilots p ON u.username = p.username";
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Pilot pilot = new Pilot();
                // Map ข้อมูลจาก DB เข้า Object (User fields)
                pilot.setUsername(rs.getString("username"));
                pilot.setName(rs.getString("name"));

                // [เพิ่มส่วนนี้] ดึง Email และ Phone
                pilot.setEmail(rs.getString("email"));
                pilot.setPhone(rs.getString("phone"));

                // Map ข้อมูล Pilot fields
                pilot.setPilotID(rs.getString("pilot_id"));

                // [เพิ่มส่วนนี้] ดึง Status และ IsAvailable
                pilot.setPilotStatus(rs.getString("pilot_status"));
                pilot.setPilotIsAvailable(rs.getString("pilot_is_available"));

                // (เผื่อใช้ในอนาคต)
                pilot.setPilotType(rs.getString("pilot_type"));

                pilots.add(pilot);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pilots;
    }
}