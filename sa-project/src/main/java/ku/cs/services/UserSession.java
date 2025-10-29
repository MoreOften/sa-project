package ku.cs.services;

import ku.cs.models.user.User; // <-- ต้อง import User model ของคุณ

/**
 * คลาสนี้ทำหน้าที่เก็บข้อมูล User ที่กำลังล็อกอินอยู่
 * (Singleton Pattern)
 */
public class UserSession {

    private static UserSession instance;
    private User currentUser;

    // ทำให้ Constructor เป็น private เพื่อไม่ให้คนอื่นสร้างใหม่
    private UserSession() { }

    /**
     * เมธอดสำหรับเรียกใช้ instance เดียวของคลาสนี้
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * เก็บข้อมูล User ตอนล็อกอินสำเร็จ
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * ดึงข้อมูล User ที่ล็อกอินอยู่
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * ล้างข้อมูล (ตอน Logout)
     */
    public void clearSession() {
        this.currentUser = null;
    }
}