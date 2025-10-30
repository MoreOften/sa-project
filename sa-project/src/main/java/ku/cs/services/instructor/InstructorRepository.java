// ในคลาส InstructorRepository.java
package ku.cs.services.instructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime; // อย่าลืม import Timestamp

import ku.cs.database.DbConnect1;
import ku.cs.models.instructor.Instructor;

public class InstructorRepository {

    public Instructor findInstructorByUsername(String username) {
        Instructor instructor = null;

        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN instructors i ON u.username = i.username " +
                "WHERE u.username = ?";

        // 1. (แก้ไข) ใช้ try-with-resources กับ Connection และ PreparedStatement
        try (Connection conn = DbConnect1.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 2. (แก้ไข) สร้างอ็อบเจกต์ว่าง
                instructor = new Instructor();

                // 3. (แก้ไข) เติมข้อมูล "User" (Parent) โดยใช้ Setters
                instructor.setUsername(rs.getString("username"));
                instructor.setName(rs.getString("name"));
                instructor.setEmail(rs.getString("email"));
                instructor.setPhone(rs.getString("phone"));
                instructor.setRole(rs.getString("role"));
                instructor.setProfilePicture(rs.getString("profilePicture"));
                instructor.setHasAccess(rs.getInt("hasAccess") == 1);

                // แปลง String (จาก DB) กลับเป็น LocalDateTime
                String dbLastLogin = rs.getString("lastLogin");
                if (dbLastLogin != null) {
                    instructor.setLastLogin(LocalDateTime.parse(dbLastLogin));
                }

                // 4. (สำคัญ!) ใช้เมธอดใหม่เพื่อตั้งค่า Hashed Password
                instructor.setHashedPassword(rs.getString("password"));

                // 5. (แก้ไข) เติมข้อมูล "Instructor" (Child)
                // (คุณต้องสร้าง Setter นี้ในคลาส Instructor)
                instructor.setInstructorID(rs.getString("instructor_id"));
                // (สมมติว่าคอลัมน์ชื่อ first_time_login)
                // instructor.setFirstTimeLogin(rs.getInt("first_time_login") == 1);
            }

        } catch (SQLException e) {
            System.err.println("InstructorRepository (findInstructorByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        }
        // 6. (แก้ไข) ไม่ต้องใช้ finally { conn.close(); } แล้ว

        return instructor;
    }

    public void addInstructor(Instructor instructor) {
    String sql = "INSERT INTO instructors (username, instructor_id) VALUES (?, ?)";
    try (Connection conn = DbConnect1.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, instructor.getUsername());
        pstmt.setString(2, instructor.getInstructorID());
        
        pstmt.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException("addInstructor failed: " + e.getMessage(), e);
    }
}

    public void updateStatusAfterFirstLogin(String username) {
        // สมมติว่าตาราง instructors มีคอลัมน์ first_time_login (INTEGER 1=true, 0=false)
        String sql = "UPDATE instructors SET first_time_login = 0 WHERE username = ?";

        try (Connection conn = DbConnect1.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("InstructorRepository (updateStatus) Error: " + e.getMessage());
            throw new RuntimeException("Database update instructor status failed: " + e.getMessage(), e);
        }
    }

    // ... (เมธอด updatePasswordAndStatus อยู่ที่นี่) ...
}