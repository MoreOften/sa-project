package ku.cs.models.report;

import java.time.LocalDateTime;
import java.util.UUID;

public class Report {
    private String reportId;
    private String scheduleId;
    private String pilotId;
    private String instructorId;
    private String trainingProgram;
    private String reportDetails;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new Report (e.g., from a form)
     */
    public Report(String scheduleId, String pilotId, String instructorId, String trainingProgram, String reportDetails) {
        // Generate a unique ID for the new report
        this.reportId = "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.scheduleId = scheduleId;
        this.pilotId = pilotId;
        this.instructorId = instructorId;
        this.trainingProgram = trainingProgram;
        this.reportDetails = reportDetails;
        this.status = ReportStatus.DRAFT; // Default status
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor for loading an existing Report from the repository (CSV)
     * This constructor trusts the data being passed in.
     */
    public Report(String reportId, String scheduleId, String pilotId, String instructorId, String trainingProgram, String reportDetails, ReportStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reportId = reportId;
        this.scheduleId = scheduleId;
        this.pilotId = pilotId;
        this.instructorId = instructorId;
        this.trainingProgram = trainingProgram;
        this.reportDetails = reportDetails;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Changes the report's status from DRAFT to PENDING_REVIEW.
     * Called from the controller before saving.
     */
    public void sendToSupervisor() {
        if (this.status == ReportStatus.DRAFT) {
            this.status = ReportStatus.PENDING_REVIEW;
            this.updatedAt = LocalDateTime.now();
        } else {
            // Optional: Log a warning if trying to send a non-draft report
            System.err.println("Report: Cannot send report " + reportId + ". Status is not DRAFT.");
        }
    }

    // --- Getters ---
    // (These are used by the Repository to write to CSV and by Controllers)

    public String getReportId() { return reportId; }
    public String getScheduleId() { return scheduleId; }
    public String getPilotId() { return pilotId; }
    public String getInstructorId() { return instructorId; }
    public String getTrainingProgram() { return trainingProgram; }
    public String getReportDetails() { return reportDetails; }
    public ReportStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // --- Setters ---
    // (Used for updating the report)

    public void setReportDetails(String reportDetails) {
        this.reportDetails = reportDetails;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStatus(ReportStatus status) {
        // This allows a Supervisor to approve/reject
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Converts the Report object to a CSV-formatted string.
     * @return A single line formatted for the CSV file.
     */
    public String toCsvString() {
        // Using a simple comma delimiter.
        // Note: This simple implementation will break if details contain commas.
        // A more robust solution would use a CSV library to handle escaping.
        return String.join(",",
                reportId,
                scheduleId,
                pilotId,
                instructorId,
                trainingProgram,
                reportDetails.replace("\n", "[NL]"), // Simple newline encoding
                status.name(), // "DRAFT", "PENDING_REVIEW", etc.
                createdAt.toString(),
                updatedAt.toString()
        );
    }
}
