package ku.cs.services.user;

import ku.cs.database.DbConnect;
import ku.cs.models.user.User; // ตรวจสอบว่า import model ถูกต้อง

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp; // ต้อง import Timestamp

public class UserRepository {

    /**
     * ตรวจสอบการเข้าสู่ระบบ และอัปเดต lastLogin
     * @return User object ถ้าสำเร็จ, null ถ้าล้มเหลว
     */
    public User login(String username, String password) {
        User user = null;
        Connection conn = DbConnect.getConnection();
        // 1. ค้นหา user และ password ที่ตรงกัน
        // (ควรใช้ Hashing ในอนาคต)
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 2. ถ้าพบ, สร้าง User object ด้วย Setters
                user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));


                // 3. อัปเดตเวลา login ล่าสุด
                updateLastLogin(username, conn);
            }

        } catch (SQLException e) {
            System.err.println("UserRepository (login) Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
        return user; // คืนค่า user (หรือ null ถ้าไม่พบ)
    }

    /**
     * ค้นหา User จาก username (ไม่ตรวจสอบรหัสผ่าน)
     * @return User object หรือ null ถ้าไม่พบ
     */
    public User findUserByUsername(String username) {
        User user = null;
        Connection conn = DbConnect.getConnection();
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));

            }
        } catch (SQLException e) {
            System.err.println("UserRepository (findUserByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
        return user;
    }

    /**
     * อัปเดตคอลัมน์ lastLogin สำหรับ user ที่กำหนด
     */
    private void updateLastLogin(String username, Connection conn) throws SQLException {
        String sql = "UPDATE users SET lastLogin = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    /**
     * อัปเดตรหัสผ่านในตาราง 'users'
     */
    public void updatePassword(String username, String newPassword) {
        Connection conn = DbConnect.getConnection();
        String sql = "UPDATE users SET password = ? WHERE username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("UserRepository (updatePassword) Error: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}