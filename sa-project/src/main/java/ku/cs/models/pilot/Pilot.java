package ku.cs.models.pilot;

import ku.cs.models.user.User;

public class Pilot extends User {

    // 1. มีเฉพาะฟิลด์ที่ User "ไม่มี"
    private String pilotID;
    private String pilotType;
    private String pilotIsFailed;
    private String pilotFailCount;
    private String pilotStatus;
    private String pilotIsAvailable;
    private String pilotProgress;


    public Pilot() {
        super(); // เรียก constructor ของ User
    }

    public Pilot(String username, String password, String name) {
        super(username, password, "pilot", name, false);
    }

    public String getPilotID() {
        return pilotID;
    }
    public String getPilotType() {
        return pilotType;
    }
    public String getPilotIsFailed() {
        return pilotIsFailed;
    }
    public String getPilotFailCount() { return pilotFailCount; }
    public String getPilotStatus() {
        return pilotStatus;
    }
    public String getPilotIsAvailable() {
        return pilotIsAvailable;
    }
    public String getPilotProgress() {
        return pilotProgress;
    }



    public void setPilotID(String pilotID) {
        this.pilotID = pilotID;
    }
    public void setPilotType(String pilotType) {
        this.pilotType = pilotType;
    }

    public void setPilotIsFailed(String pilotIsFailed) {
        this.pilotIsFailed = pilotIsFailed;
    }

    public void setPilotFailCount(String pilotFailCount) {
        this.pilotFailCount = pilotFailCount;
    }

    public void setPilotStatus(String pilotStatus) {
        this.pilotStatus = pilotStatus;
    }

    public void setPilotIsAvailable(String pilotIsAvailable) {
        this.pilotIsAvailable = pilotIsAvailable;
    }

    public void setPilotProgress(String pilotProgress) {
        this.pilotProgress = pilotProgress;
    }
}