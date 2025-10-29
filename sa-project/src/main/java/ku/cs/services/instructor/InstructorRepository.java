// ในคลาส InstructorRepository.java
package ku.cs.services.instructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp; // อย่าลืม import Timestamp
import ku.cs.database.DbConnect;
import ku.cs.models.instructor.Instructor;

public class InstructorRepository {

    public Instructor findInstructorByUsername(String username) {
        Instructor instructor = null;
        Connection conn = DbConnect.getConnection();

        // 1. แก้ไข SQL Query ให้ JOIN สองตาราง
        // (u = users, i = instructors)
        // ผมสมมติว่า name, email, phone อยู่ในตาราง users
        // และ instructorID อยู่ในตาราง instructors
        String sql = "SELECT * " +
                "FROM users u " +
                "JOIN instructors i ON u.username = i.username " +
                "WHERE u.username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

                // 2. สร้างอ็อบเจกต์ Instructor โดยเรียก Constructor ให้ถูกต้อง
                // (เรียงลำดับพารามิเตอร์ให้ตรงกับที่คุณให้มา)
                instructor = new Instructor(
                        rs.getString("name"),         // 1. name (จากตาราง users)
                        rs.getString("email"),        // 2. email (จากตาราง users)
                        rs.getString("username"),     // 3. username (จากตาราง users/instructors)
                        rs.getString("password"),     // 4. password (จากตาราง users)
                        rs.getString("instructorID"), // 5. instructorID (จากตาราง instructors)
                        rs.getString("phone")         // 6. phone (จากตาราง users)
                );

                // 3. ตั้งค่าอื่นๆ ที่เหลือ (ที่ไม่ได้อยู่ใน Constructor)
                // *** คลาส Instructor ของคุณต้องมี Setter Methods เหล่านี้ด้วยนะครับ ***
                // (เช่น setRole, setHasAccess, setFirstTimeLogin)

                // ดึงจากตาราง users
//                instructor.setRole(rs.getString("role"));
//                instructor.setHasAccess(rs.getBoolean("hasAccess"));
//                instructor.setLastLogin(new Timestamp(rs.getLong("lastLogin")));
//                // instructor.setImagePath(rs.getString("imagePath")); // ถ้ามี
//
//                // ดึงจากตาราง instructors
//                instructor.setFirstTimeLogin(rs.getBoolean("firstTimeLogin"));
            }

        } catch (SQLException e) {
            System.err.println("InstructorRepository (findInstructorByUsername) Error: " + e.getMessage());
            e.printStackTrace(); // พิมพ์ stack trace เพื่อดูข้อผิดพลาด
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
        return instructor;
    }

    // ... (เมธอด updatePasswordAndStatus อยู่ที่นี่) ...
}