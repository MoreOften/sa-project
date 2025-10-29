package ku.cs.models.supervisor;

import ku.cs.models.user.User;

public class Supervisor extends User {
    private String name;
    private String username;
    private String email;
    private String password;
    private String supervisorID;
    private String phone;

    public Supervisor(String name, String email, String username, String password, String supervisorID, String phone) {
        super(name, email, username, password);
        this.supervisorID = supervisorID;
        this.phone = phone;
    }

    public Supervisor() {
        this.name = "";
        this.email = "";
        this.username = "";
        this.password = "";
        this.supervisorID = "";
        this.phone = "";
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getSupervisorID() { return supervisorID; }
    public String getPhone() { return phone; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.name = username; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setSupervisorID(String pilotID) { this.supervisorID = supervisorID; }
    public void setPhone(String phone) { this.phone = phone; }


    public void setEmployeeId(String s001) {
    }
}
