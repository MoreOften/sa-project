package ku.cs.models.report;

import java.time.LocalDateTime;
import java.util.UUID;

public class Report {
    private String reportId;
    private String scheduleId;
    private String pilotId;
    private String instructorId;

    // --- (1. Fields Renamed/Added) ---
    private String reportNotes;     // (Renamed from reportDetails)
    private String reportResult;    // (Added)
    private ReportStatus approvalStatus;  // (Renamed from status)
    // --- (End Change) ---

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new Report (e.g., from a form)
     * (2. Update Constructor)
     */
    public Report(String scheduleId, String pilotId, String instructorId, String reportNotes) {
        this.reportId = "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.scheduleId = scheduleId;
        this.pilotId = pilotId;
        this.instructorId = instructorId;
        this.reportNotes = reportNotes;
        this.reportResult = ""; // Default to empty string
        this.approvalStatus = ReportStatus.DRAFT; // Default status

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor for loading an existing Report from the repository
     * (3. Update Constructor)
     */
    public Report(String reportId, String scheduleId, String pilotId, String instructorId,
                  String reportNotes, String reportResult,
                  ReportStatus approvalStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reportId = reportId;
        this.scheduleId = scheduleId;
        this.pilotId = pilotId;
        this.instructorId = instructorId;
        this.reportNotes = reportNotes;
        this.reportResult = reportResult;
        this.approvalStatus = approvalStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * (4. Update Method)
     */
    public void sendToSupervisor() {
        if (this.approvalStatus == ReportStatus.DRAFT) {
            this.approvalStatus = ReportStatus.PENDING_REVIEW;
            this.updatedAt = LocalDateTime.now();
        } else {
            System.err.println("Report: Cannot send report " + reportId + ". Status is not DRAFT.");
        }
    }

    // --- Getters (Updated) ---

    public String getReportId() { return reportId; }
    public String getScheduleId() { return scheduleId; }
    public String getPilotId() { return pilotId; }
    public String getInstructorId() { return instructorId; }

    public String getReportNotes() { return reportNotes; }
    public String getReportResult() { return reportResult; }
    public ReportStatus getApprovalStatus() { return approvalStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // --- Setters (Updated) ---

    public void setReportNotes(String reportNotes) {
        this.reportNotes = reportNotes;
        this.updatedAt = LocalDateTime.now();
    }

    public void setReportResult(String reportResult) {
        this.reportResult = reportResult;
        this.updatedAt = LocalDateTime.now();
    }

    public void setApprovalStatus(ReportStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
        this.updatedAt = LocalDateTime.now();
    }

    // (toCsvString() method removed for brevity as it's not used by DB)
}