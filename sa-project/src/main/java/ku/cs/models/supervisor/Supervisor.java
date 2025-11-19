package ku.cs.models.supervisor;

import ku.cs.models.user.User;

public class Supervisor extends User {
    // 1. มีเฉพาะฟิลด์ที่ User "ไม่มี"
    private String supervisorID;

    public Supervisor() {
        super(); // เรียก constructor ของ User
    }

    // 2. Getters/Setters สำหรับฟิลด์ของตัวเอง
    public String getSupervisorID() {
        return supervisorID;
    }

    public void setSupervisorID(String supervisorID) {
        this.supervisorID = supervisorID;
    }
}