package ku.cs.models.report;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Model สำหรับจัดเก็บข้อมูล Report
 * (สร้างโดย Instructor, ตรวจสอบโดย Supervisor)
 */
public class Report {
    private String reportId;
    private String instructorId; // ID (username) ของผู้สร้าง
    private String scheduleId;   // ID ของ Schedule ที่อ้างอิง
    private LocalDateTime createDate; // วันที่สร้าง
    private String status;       // สถานะ (เช่น Pending, Reviewed)
    private String feedback;     // (Optional) เนื้อหา/ข้อความ
    private String grade;        // (Optional) เกรด

    /**
     * Constructor สำหรับสร้าง Report ใหม่ (ใช้โดย Instructor)
     */
    public Report(String instructorId, String scheduleId, String feedback, String grade) {
        this.reportId = UUID.randomUUID().toString();
        this.instructorId = instructorId;
        this.scheduleId = scheduleId;
        this.feedback = feedback;
        this.grade = grade;
        this.createDate = LocalDateTime.now();
        this.status = "Pending"; // สถานะเริ่มต้น
    }

    /**
     * Constructor ว่าง (สำหรับ Repository ใช้ตอนดึงจาก DB)
     */
    public Report() { }

    // --- Getters and Setters ---

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}