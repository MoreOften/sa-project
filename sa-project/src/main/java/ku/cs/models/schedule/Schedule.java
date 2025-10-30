package ku.cs.models.schedule;

import java.time.LocalDateTime;
import java.util.UUID;

public class Schedule {

    private String scheduleId;       // ID ของตารางฝึกนี้ (เช่น UUID)
    private String supervisorId;     // ID (username) ของ Supervisor ผู้สร้าง
    private String instructorId;     // ID (username) ของ Instructor
    private String pilotId1;         // ID (username) ของ Pilot คนที่ 1
    private String pilotId2;         // ID (username) ของ Pilot คนที่ 2
    private String programName;      // ชื่อโปรแกรมการฝึก
    private LocalDateTime trainingTimestamp; // วันที่และเวลาฝึก

    /**
     * Constructor ว่าง (สำหรับ Repository ใช้ตอนดึงข้อมูล)
     */
    public Schedule() { }

    /**
     * Constructor เต็ม (สำหรับ Controller ใช้ตอนสร้างใหม่)
     */
    public Schedule(String supervisorId, String instructorId, String pilotId1, String pilotId2, String programName, LocalDateTime trainingTimestamp) {
        // สร้าง ID ที่ไม่ซ้ำกันอัตโนมัติ
        this.scheduleId = UUID.randomUUID().toString();

        this.supervisorId = supervisorId;
        this.instructorId = instructorId;
        this.pilotId1 = pilotId1;
        this.pilotId2 = pilotId2;
        this.programName = programName;
        this.trainingTimestamp = trainingTimestamp;
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

    public String getProgramName() {
        return programName;
    }

    public LocalDateTime getTrainingTimestamp() {
        return trainingTimestamp;
    }

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

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public void setTrainingTimestamp(LocalDateTime trainingTimestamp) {
        this.trainingTimestamp = trainingTimestamp;
    }
}