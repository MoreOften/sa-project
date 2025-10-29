package ku.cs.models.instructor;

import ku.cs.models.user.User;

import java.time.LocalDateTime;

public class Instructor extends User {
    private String instructorID;

    public Instructor() {
        super(); // เรียก Constructor ของ User
    }


    public String getInstructorID() { return instructorID; }

    public void setInstructorID(String instructorID) { this.instructorID = instructorID; }
}
