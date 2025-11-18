package ku.cs.models.schedule;

// (ลบ) import java.time.LocalDateTime;
import java.util.UUID;

public class Schedule {

    private String scheduleId; // ID ของตารางฝึกนี้ (เช่น UUID)
    private String supervisorId; // ID (username) ของ Supervisor ผู้สร้าง
    private String instructorId; // ID (username) ของ Instructor
    private String pilotId1; // ID (username) ของ Pilot คนที่ 1
    private String pilotId2; // ID (username) ของ Pilot คนที่ 2
    private String scheduleStatus;

    // --- (เปลี่ยนแปลง) ---
    private String practiceProgram; // (แทน programName)
    private String scheduleDate;      // (แทน trainingTimestamp)
    private String scheduleTime;      // (แทน trainingTimestamp)
    private String simulator;         // (เพิ่มใหม่)
    // --- (สิ้นสุดการเปลี่ยนแปลง) ---


    /**
     * Constructor ว่าง (สำหรับ Repository ใช้ตอนดึงข้อมูล)
     */
    public Schedule() { }

    /**
     * Constructor เต็ม (สำหรับ Controller/Seeder ใช้ตอนสร้างใหม่)
     * (อัปเดต Constructor นี้ให้รับ Parameters ใหม่)
     */
    public Schedule(String supervisorId, String instructorId, String pilotId1, String pilotId2,
                    String scheduleStatus, String practiceProgram, String scheduleDate, String scheduleTime, String simulator) {

        this.scheduleId = UUID.randomUUID().toString();
        this.supervisorId = supervisorId;
        this.instructorId = instructorId;
        this.pilotId1 = pilotId1;
        this.pilotId2 = pilotId2;

        // --- (เปลี่ยนแปลง) ---
        this.practiceProgram = practiceProgram;
        this.scheduleDate = scheduleDate;
        this.scheduleTime = scheduleTime;
        this.simulator = simulator;
        // --- (สิ้นสุดการเปลี่ยนแปลง) ---
    }

    // --- Getters ---

    public String getScheduleId() {
        return scheduleId;
    }
    public String getSupervisorId() {
        return supervisorId;
    }
    public String getInstructorId() {
        return instructorId;
    }
    public String getPilotId1() {
        return pilotId1;
    }
    public String getPilotId2() {
        return pilotId2;
    }

    // --- (Getters ใหม่) ---
    public String getPracticeProgram() {
        return practiceProgram;
    }
    public String getScheduleDate() {
        return scheduleDate;
    }
    public String getScheduleTime() {
        return scheduleTime;
    }
    public String getSimulator() {
        return simulator;
    }
    // --- (สิ้นสุด) ---


    // --- Setters ---
    // (สำคัญมากสำหรับ Repository)

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }
    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }
    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }
    public void setPilotId1(String pilotId1) {
        this.pilotId1 = pilotId1;
    }
    public void setPilotId2(String pilotId2) {
        this.pilotId2 = pilotId2;
    }

    // --- (Setters ใหม่) ---
    public void setPracticeProgram(String practiceProgram) {
        this.practiceProgram = practiceProgram;
    }
    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }
    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }
    public void setSimulator(String simulator) {
        this.simulator = simulator;
    }

    public String getScheduleStatus() {
        return scheduleStatus;
    }

    public void setScheduleStatus(String scheduleStatus) {
        this.scheduleStatus = scheduleStatus;
    }
    // --- (สิ้นสุด) ---

    // (ลบ Getters/Setters ของ programName และ trainingTimestamp ที่เคยมีอยู่ออก)
}