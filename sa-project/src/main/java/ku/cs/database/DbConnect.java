package ku.cs.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbConnect {

    // กำหนด URL สำหรับเชื่อมต่อ SQLite
    // ไฟล์ฐานข้อมูล "mydatabase.db" จะถูกสร้างขึ้นที่โฟลเดอร์ราก (root) ของโปรเจกต์
    private static final String URL = "jdbc:sqlite:mydatabase.db";

    /**
     * เมธอดสำหรับเชื่อมต่อฐานข้อมูล SQLite
     * @return อ็อบเจกต์ Connection
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // โหลด JDBC Driver (สำหรับ Java เก่าๆ อาจจำเป็น แต่เวอร์ชันใหม่ๆ มักจะไม่ต้อง)
            // Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("การเชื่อมต่อฐานข้อมูลล้มเหลว: " + e.getMessage());
        }
        return conn;
    }

    /**
     * (แนะนำ) เมธอดสำหรับสร้างตารางเริ่มต้น หากยังไม่มี
     * ควรเรียกใช้เมธอดนี้แค่ครั้งเดียวตอนเริ่มโปรแกรม (เช่น ในคลาส MainApp)
     */
    public static void initializeDatabase() {
        // SQL สำหรับสร้างตาราง (ตัวอย่างคือตาราง "users")
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL UNIQUE,"
                + " password TEXT NOT NULL"
                + ");";

        // ใช้ try-with-resources เพื่อให้แน่ใจว่า Connection และ Statement ถูกปิดเสมอ
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // สั่งให้ SQL ทำงาน
            stmt.execute(sql);
            System.out.println("ตรวจสอบ/สร้างตาราง Users สำเร็จ");

        } catch (SQLException e) {
            System.err.println("เกิดข้อผิดพลาดในการสร้างตาราง: " + e.getMessage());
        }
    }
}
