package ku.cs.models.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Schedule {

    // --- 1. Fields from 'develop' (Friend's) ---
    private String scheduleId;       // ID ของตารางฝึกนี้ (เช่น UUID)
    private String supervisorId;     // ID (username) ของ Supervisor ผู้สร้าง
    private String instructorId;     // ID (username) ของ Instructor
    private String pilotId1;         // ID (username) ของ Pilot คนที่ 1
    private String pilotId2;         // ID (username) ของ Pilot คนที่ 2 (ถ้ามี)
    private String programName;      // ชื่อโปรแกรมการฝึก
    private LocalDateTime trainingTimestamp; // วันที่และเวลาฝึก

    // --- 2. Fields from 'your' (Gemini's) version ---
    // (Fields ที่ต้องเก็บลง Database เหมือนกัน)
    private String status;           // เช่น "Booked", "Completed", "Cancelled"
    private String grade;            // เช่น "Pass", "Fail"
    private String instructorFeedback; // Feedback/Comment จากครูฝึก

    // (Field สำหรับ View/Join เท่านั้น - ไม่ใช่คอลัมน์ในตาราง schedules)
    private String instructorName;   // ชื่อเต็มของครูฝึก (ได้มาจาก JOIN)

    /**
     * Constructor ว่าง (สำหรับ Repository ใช้ตอนดึงข้อมูล)
     */
    public Schedule() { }

    /**
     * Constructor เต็ม (สำหรับ Controller ใช้ตอนสร้างใหม่)
     */
    public Schedule(String supervisorId, String instructorId, String pilotId1, String pilotId2, String programName, LocalDateTime trainingTimestamp) {
        this.scheduleId = UUID.randomUUID().toString();
        this.supervisorId = supervisorId;
        this.instructorId = instructorId;
        this.pilotId1 = pilotId1;
        this.pilotId2 = pilotId2;
        this.programName = programName;
        this.trainingTimestamp = trainingTimestamp;

        // [ADD] ตั้งค่าเริ่มต้น
        this.status = "Booked";
    }

    // --- 3. Getters & Setters (สำหรับทุก Field) ---

    // Getters/Setters จาก 'develop'
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }
    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }
    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
    public String getPilotId1() { return pilotId1; }
    public void setPilotId1(String pilotId1) { this.pilotId1 = pilotId1; }
    public String getPilotId2() { return pilotId2; }
    public void setPilotId2(String pilotId2) { this.pilotId2 = pilotId2; }
    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }
    public LocalDateTime getTrainingTimestamp() { return trainingTimestamp; }
    public void setTrainingTimestamp(LocalDateTime trainingTimestamp) { this.trainingTimestamp = trainingTimestamp; }

    // Getters/Setters ที่เพิ่มใหม่ (จากโค้ดของเรา)
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getInstructorFeedback() { return instructorFeedback; }
    public void setInstructorFeedback(String instructorFeedback) { this.instructorFeedback = instructorFeedback; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    // --- 4. Helper Getters (สำหรับ TableView ที่เราสร้างไว้) ---
    // (โค้ด FXML ของเราต้องการ 'date' และ 'time' แบบแยกส่วน)

    /**
     * @return วันที่ (LocalDate) ที่ดึงมาจาก trainingTimestamp
     */
    public LocalDate getDate() {
        if (this.trainingTimestamp == null) {
            return null;
        }
        return this.trainingTimestamp.toLocalDate();
    }

    /**
     * @return เวลา (String) ที่ดึงมาจาก trainingTimestamp (เช่น "09:00")
     */
    public String getTime() {
        if (this.trainingTimestamp == null) {
            return null;
        }
        // ดึงเวลา 4 หลักแรก (HH:mm)
        return this.trainingTimestamp.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}