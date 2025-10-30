package ku.cs.services.user;

import ku.cs.database.DbConnect;
import ku.cs.models.user.User; // ตรวจสอบว่า import model ถูกต้อง

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

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

    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        User user = null;

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 1. ดึงข้อมูลดิบจาก DB
                String dbUsername = rs.getString("username");
                String dbHashedPassword = rs.getString("password"); // นี่คือ Hash
                String dbRole = rs.getString("role");
                String dbName = rs.getString("name");
                String dbProfilePic = rs.getString("profilePicture");
                String dbLastLogin = rs.getString("lastLogin"); // นี่คือ TEXT
                boolean dbHasAccess = rs.getInt("hasAccess") == 1; // นี่คือ 0 หรือ 1

                // 2. แปลงค่าที่ต้องแปลง
                LocalDateTime lastLoginTime = null;
                if (dbLastLogin != null) {
                    lastLoginTime = LocalDateTime.parse(dbLastLogin);
                }

                // 3. (สำคัญมาก) เรียก Constructor ที่ "ไม่ Hash ซ้ำ"
                // เราใช้: User(String username, String password, String role, String name, String profilePicture, LocalDateTime lastLogin, boolean isPasswordHashed, boolean hasAccess)
                user = new User(dbUsername, dbHashedPassword, dbRole, dbName,
                        dbProfilePic, lastLoginTime,
                        true, // <-- บอก Model ว่านี่คือ Hash แล้ว (isPasswordHashed = true)
                        dbHasAccess);

                // 4. (Optional) เติมส่วนที่เหลือ (ถ้า Constructor ไม่มี)
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
            }
        } catch (SQLException e) {
            System.err.println("UserRepository (findUserByUsername) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }

    public void registerUser(User user) {
        // 1. SQL ต้องมีทุกคอลัมน์
        String sql = "INSERT INTO users (username, password, name, role, phone, email, profilePicture, lastLogin, hasAccess) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());

            // 2. เราดึง Hashed Password จาก Model ได้เลย (เพราะ setPassword มัน Hash ให้แล้ว)
            pstmt.setString(2, user.getPassword());

            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getRole());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getEmail());
            pstmt.setString(7, user.getProfilePicture());

            // 3. แปลง LocalDateTime เป็น TEXT (ISO-8601)
            pstmt.setString(8, user.getLastLogin() != null ? user.getLastLogin().toString() : null);

            // 4. แปลง boolean เป็น INTEGER
            pstmt.setInt(9, user.getHasAccess() ? 1 : 0);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("UserRepository (registerUser) Error: " + e.getMessage());
            throw new RuntimeException("Database error: Could not register user", e);
        }
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

    public void updateUser(User user) {
        // 1. SQL ต้องอัปเดตทุกคอลัมน์ที่ "อาจจะ" เปลี่ยนแปลง
        String sql = "UPDATE users SET "
                + " password = ?, "       // 1
                + " name = ?, "           // 2
                + " role = ?, "           // 3
                + " phone = ?, "          // 4
                + " email = ?, "          // 5
                + " profilePicture = ?, " // 6
                + " lastLogin = ?, "      // 7
                + " hasAccess = ? "        // 8
                + " WHERE username = ?";  // 9

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 2. ตั้งค่า PreparedStatement
            // (เราดึง Hashed Password จาก user.getPassword() ได้เลย)
            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getProfilePicture());

            // 3. แปลง LocalDateTime เป็น TEXT (ต้องเช็ค null)
            if (user.getLastLogin() != null) {
                pstmt.setString(7, user.getLastLogin().toString());
            } else {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            }

            // 4. แปลง boolean เป็น INTEGER
            pstmt.setInt(8, user.getHasAccess() ? 1 : 0);

            // 5. WHERE clause
            pstmt.setString(9, user.getUsername());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("UserRepository (updateUser) Error: " + e.getMessage());
            throw new RuntimeException("Database update user failed: " + e.getMessage(), e);
        }
    }
}