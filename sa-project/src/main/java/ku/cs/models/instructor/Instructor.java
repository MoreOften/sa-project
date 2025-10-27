package ku.cs.models.instructor;

public class Instructor {
    private String name;
    private String email;
    private String password;
    private String instructorID;
    private String phone;

    public Instructor(String name, String email, String password, String instructorID, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.instructorID = instructorID;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getInstructorID() { return instructorID; }
    public String getPhone() { return phone; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setInstructorID(String instructorID) { this.instructorID = instructorID; }
    public void setPhone(String phone) { this.phone = phone; }
}
