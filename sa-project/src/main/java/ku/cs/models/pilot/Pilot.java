package ku.cs.models.pilot;

import ku.cs.models.user.User;

public class Pilot extends User {

    // 1. มีเฉพาะฟิลด์ที่ User "ไม่มี"
    private String pilotID;

    public Pilot() {
        super(); // เรียก constructor ของ User
    }

    public Pilot(String username, String password, String name) {
        super(username, password, "pilot", name, false);
    }

    public String getPilotID() {
        return pilotID;
    }

    public void setPilotID(String pilotID) {
        this.pilotID = pilotID;
    }
}