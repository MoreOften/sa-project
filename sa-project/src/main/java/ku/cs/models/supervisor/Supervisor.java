package ku.cs.models.supervisor;

import ku.cs.models.user.User;

public class Supervisor extends User {
    private String supervisorID;

    // *** Constructor ว่าง (สำคัญมาก!) ***
    public Supervisor() {
        super(); // เรียก Constructor ของ User
    }

    // Constructor แบบมี parameters (ถ้าต้องการใช้)
    public Supervisor(String name, String email, String username, String password, String supervisorID, String phone) {
        super(name, email, username, password);
        this.supervisorID = supervisorID;
        super.setPhone(phone);
    }

    // --- Getters ---
    public String getSupervisorID() {
        return supervisorID;
    }

    // --- Setters ---
    public void setSupervisorID(String supervisorID) {
        this.supervisorID = supervisorID;
    }

    // *** แก้ไข setUsername() - ต้อง set ให้ parent class (User) ***
    @Override
    public void setUsername(String username) {
        super.setUsername(username); // เรียก method ของ User
    }
}