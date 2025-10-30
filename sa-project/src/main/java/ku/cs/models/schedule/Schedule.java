package ku.cs.models.schedule;

import java.time.LocalDate;

public class Schedule {

    // สำหรับตาราง schedules
    private String pilotUsername;
    private String instructorUsername;
    private LocalDate date;
    private String time;
    private String programName; // เปลี่ยนจาก 'program'
    private String status;

    // สำหรับแสดงผลใน TableView (ได้มาจาก JOIN)
    private String instructorName;

    // Constructor 1: สำหรับ Seeding (ข้อมูลครบถ้วน)
    public Schedule(String pilotUsername, String instructorUsername, LocalDate date, String time, String programName, String status) {
        this.pilotUsername = pilotUsername;
        this.instructorUsername = instructorUsername;
        this.date = date;
        this.time = time;
        this.programName = programName;
        this.status = status;
    }

    // Constructor 2: สำหรับ Repository (รับข้อมูลที่ JOIN แล้ว)
    public Schedule(LocalDate date, String time, String programName, String instructorName) {
        this.date = date;
        this.time = time;
        this.programName = programName;
        this.instructorName = instructorName; // <-- เก็บชื่อที่ JOIN มา
    }

    // --- Getters (สำคัญมากสำหรับ TableView และ Repository) ---

    // PropertyValueFactory ใน FXML จะเรียก 4 เมธอดนี้:
    public LocalDate getDate() { return date; }
    public String getTime() { return time; }
    public String getProgramName() { return programName; } // <-- ต้องแก้ FXML
    public String getInstructorName() { return instructorName; } // <-- ต้องแก้ FXML

    // เมธอดเหล่านี้ใช้สำหรับ Repository (addSchedule)
    public String getPilotUsername() { return pilotUsername; }
    public String getInstructorUsername() { return instructorUsername; }
    public String getStatus() { return status; }

    // (เพิ่ม Setters ได้ตามต้องการ)
}