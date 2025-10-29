package ku.cs.models.pilot;

import ku.cs.models.user.User;

public class Pilot extends User {
    private String name;
    private String username;
    private String email;
    private String password;
    private String pilotID;
    private String phone;

    public Pilot(String name, String email, String username, String password, String pilotID, String phone) {
        super(name, email, username, password);
        this.pilotID = pilotID;
        this.phone = phone;
    }
    public Pilot() {
        this.name = "";
        this.email = "";
        this.username = "";
        this.password = "";
        this.pilotID = "";
        this.phone = "";
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPilotID() { return pilotID; }
    public String getPhone() { return phone; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.name = username; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setPilotID(String pilotID) { this.pilotID = pilotID; }
    public void setPhone(String phone) { this.phone = phone; }


}
