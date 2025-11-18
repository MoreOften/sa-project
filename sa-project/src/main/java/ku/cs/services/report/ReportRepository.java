package ku.cs.services.report;

import ku.cs.database.DbConnect; // (1) Import DbConnect
import ku.cs.models.report.Report;
import ku.cs.models.report.ReportStatus;

import java.sql.Connection; // (2) Import SQL classes
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {
    private List<Report> reports;

    public ReportRepository() {
        this.reports = new ArrayList<>();
        load(); // (3) Load data from Database
    }

    /**
     * (FIX 1: Update load() method)
     */
    private void load() {
        this.reports.clear();
        String sql = "SELECT * FROM reports";

        try (Connection conn = DbConnect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String reportId = rs.getString("report_id");
                String scheduleId = rs.getString("schedule_id");
                String pilotId = rs.getString("pilot_id");
                String instructorId = rs.getString("instructor_id");

                // --- (New Columns) ---
                String reportNotes = rs.getString("report_notes");     // (Was report_details)
                String reportResult = rs.getString("report_result");
                ReportStatus approvalStatus = ReportStatus.valueOf(rs.getString("approval_status")); // (Was status)
                // --- (End Change) ---

                LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
                LocalDateTime updatedAt = LocalDateTime.parse(rs.getString("updated_at"));

                // --- (Call New Constructor) ---
                Report report = new Report(reportId, scheduleId, pilotId, instructorId,
                        reportNotes, reportResult,
                        approvalStatus, createdAt, updatedAt);
                this.reports.add(report);
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("ReportRepo (SQL): Error loading report data from database.");
            e.printStackTrace();
        }
    }

    /**
     * (FIX 2: Update save() method)
     */
    public void save(Report report) {
        Report existingReport = findReportById(report.getReportId());

        if (existingReport == null) {
            // (4) --- INSERT new report (Updated SQL) ---
            String sql = "INSERT INTO reports (report_id, schedule_id, pilot_id, instructor_id, " +
                    "report_notes, report_result, approval_status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"; // (10 params)

            try (Connection conn = DbConnect.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, report.getReportId());
                ps.setString(2, report.getScheduleId());
                ps.setString(3, report.getPilotId());
                ps.setString(4, report.getInstructorId());

                // --- (Updated Setters) ---
                ps.setString(5, report.getReportNotes());
                ps.setString(6, report.getReportResult());
                ps.setString(7, report.getApprovalStatus().name()); // (Was getStatus)
                // --- (End Change) ---

                ps.setString(8, report.getCreatedAt().toString());
                ps.setString(9, report.getUpdatedAt().toString());

                ps.executeUpdate();
                this.reports.add(report);

            } catch (SQLException e) {
                System.err.println("ReportRepo (SQL): Error INSERTING report: " + report.getReportId());
                e.printStackTrace();
            }
        } else {
            // (5) --- UPDATE existing report (Updated SQL) ---
            String sql = "UPDATE reports SET " +
                    "schedule_id = ?, " +
                    "pilot_id = ?, " +
                    "instructor_id = ?, " +
                    "practice_program = ?, " +
                    "report_notes = ?, " +
                    "report_result = ?, " +
                    "approval_status = ?, " +
                    "updated_at = ? " +
                    "WHERE report_id = ?";

            try (Connection conn = DbConnect.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, report.getScheduleId());
                ps.setString(2, report.getPilotId());
                ps.setString(3, report.getInstructorId());

                // --- (Updated Setters) ---
                ps.setString(4, report.getReportNotes());
                ps.setString(5, report.getReportResult());
                ps.setString(6, report.getApprovalStatus().name());
                // --- (End Change) ---

                ps.setString(7, report.getUpdatedAt().toString());
                ps.setString(8, report.getReportId()); // WHERE clause

                ps.executeUpdate();

                this.reports.remove(existingReport);
                this.reports.add(report);

            } catch (SQLException e) {
                System.err.println("ReportRepo (SQL): Error UPDATING report: " + report.getReportId());
                e.printStackTrace();
            }
        }
    }

    // ... (delete, flushToCsv, findReportById, findReportsByInstructor, findAll methods are fine) ...
    // [REST OF THE FILE IS OMITTED FOR BREVITY, NO CHANGES NEEDED BELOW THIS LINE]

    public void delete(String reportId) {
        Report reportToRemove = findReportById(reportId);
        if (reportToRemove == null) {
            System.err.println("ReportRepo (SQL): No report found with ID " + reportId + " to delete.");
            return;
        }

        String sql = "DELETE FROM reports WHERE report_id = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reportId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                this.reports.remove(reportToRemove);
            }

        } catch (SQLException e) {
            System.err.println("ReportRepo (SQL): Error DELETING report: " + reportId);
            e.printStackTrace();
        }
    }

    private void flushToCsv() {
        // Obsolete
    }

    public Report findReportById(String reportId) {
        for (Report report : this.reports) {
            if (report.getReportId().equals(reportId)) {
                return report;
            }
        }
        return null;
    }

    public List<Report> findReportsByInstructor(String instructorId) {
        List<Report> found = new ArrayList<>();
        for (Report report : this.reports) {
            if (report.getInstructorId().equals(instructorId)) {
                found.add(report);
            }
        }
        return found;
    }

    public List<Report> findAll() {
        return new ArrayList<>(this.reports);
    }
}