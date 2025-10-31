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

/**
 * Manages the persistence of Report objects using an SQLite database.
 */
public class ReportRepository {
    // In-memory list of all reports (Cache)
    private List<Report> reports;

    /**
     * Constructor: Initializes the repository and loads data from the database.
     */
    public ReportRepository() {
        this.reports = new ArrayList<>();
        load(); // (3) Load data from Database
    }

    /**
     * Loads all reports from the database into the in-memory list.
     */
    private void load() {
        this.reports.clear(); // Clear cache before loading
        String sql = "SELECT * FROM reports";

        try (Connection conn = DbConnect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Parse data from database columns
                String reportId = rs.getString("report_id");
                String scheduleId = rs.getString("schedule_id");
                String pilotId = rs.getString("pilot_id");
                String instructorId = rs.getString("instructor_id");
                String trainingProgram = rs.getString("training_program");
                String reportDetails = rs.getString("report_details");
                ReportStatus status = ReportStatus.valueOf(rs.getString("status"));
                LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
                LocalDateTime updatedAt = LocalDateTime.parse(rs.getString("updated_at"));

                // Create Report object and add to list
                Report report = new Report(reportId, scheduleId, pilotId, instructorId, trainingProgram, reportDetails, status, createdAt, updatedAt);
                this.reports.add(report);
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("ReportRepo (SQL): Error loading report data from database.");
            e.printStackTrace();
        }
    }

    /**
     * Saves a new report (INSERT) or updates an existing one (UPDATE).
     *
     * @param report The Report object to save.
     */
    public void save(Report report) {
        // Check if the report already exists in the database
        Report existingReport = findReportById(report.getReportId());

        if (existingReport == null) {
            // (4) --- INSERT new report ---
            String sql = "INSERT INTO reports (report_id, schedule_id, pilot_id, instructor_id, training_program, report_details, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DbConnect.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, report.getReportId());
                ps.setString(2, report.getScheduleId());
                ps.setString(3, report.getPilotId());
                ps.setString(4, report.getInstructorId());
                ps.setString(5, report.getTrainingProgram());
                ps.setString(6, report.getReportDetails());
                ps.setString(7, report.getStatus().name()); // "DRAFT", etc.
                ps.setString(8, report.getCreatedAt().toString());
                ps.setString(9, report.getUpdatedAt().toString());

                ps.executeUpdate();

                // Add to in-memory list as well
                this.reports.add(report);

            } catch (SQLException e) {
                System.err.println("ReportRepo (SQL): Error INSERTING report: " + report.getReportId());
                e.printStackTrace();
            }
        } else {
            // (5) --- UPDATE existing report ---
            String sql = "UPDATE reports SET " +
                    "schedule_id = ?, " +
                    "pilot_id = ?, " +
                    "instructor_id = ?, " +
                    "training_program = ?, " +
                    "report_details = ?, " +
                    "status = ?, " +
                    "updated_at = ? " +
                    "WHERE report_id = ?";

            try (Connection conn = DbConnect.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, report.getScheduleId());
                ps.setString(2, report.getPilotId());
                ps.setString(3, report.getInstructorId());
                ps.setString(4, report.getTrainingProgram());
                ps.setString(5, report.getReportDetails());
                ps.setString(6, report.getStatus().name());
                ps.setString(7, report.getUpdatedAt().toString()); // Update timestamp
                ps.setString(8, report.getReportId()); // WHERE clause

                ps.executeUpdate();

                // Update in-memory list
                this.reports.remove(existingReport);
                this.reports.add(report);

            } catch (SQLException e) {
                System.err.println("ReportRepo (SQL): Error UPDATING report: " + report.getReportId());
                e.printStackTrace();
            }
        }
    }

    /**
     * Deletes a report from the database.
     *
     * @param reportId The ID of the report to delete.
     */
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
                // Also remove from in-memory list
                this.reports.remove(reportToRemove);
            }

        } catch (SQLException e) {
            System.err.println("ReportRepo (SQL): Error DELETING report: " + reportId);
            e.printStackTrace();
        }
    }

    /**
     * Writes the entire in-memory list of reports back to the CSV file.
     * (This method is no longer needed with a database)
     */
    private void flushToCsv() {
        // (6) This method is now obsolete.
        // We save data instantly (INSERT/UPDATE/DELETE)
        // instead of flushing all at once.
    }

    // --- Finder Methods (Used by Controllers) ---
    // (These methods now search the in-memory list first)

    /**
     * Finds a single report by its unique ID from the in-memory list.
     *
     * @param reportId The ID to search for.
     * @return The Report object if found, or null otherwise.
     */
    public Report findReportById(String reportId) {
        for (Report report : this.reports) {
            if (report.getReportId().equals(reportId)) {
                return report;
            }
        }
        return null;
        // (Optional: If not found in cache, could query DB again,
        // but 'load()' should be comprehensive)
    }

    /**
     * Finds all reports associated with a specific instructor from the in-memory list.
     *
     * @param instructorId The Instructor's ID.
     * @return A list of matching reports.
     */
    public List<Report> findReportsByInstructor(String instructorId) {
        List<Report> found = new ArrayList<>();
        for (Report report : this.reports) {
            // Note: Make sure the ID you store (instructor_id) matches
            // what you are searching for.
            if (report.getInstructorId().equals(instructorId)) {
                found.add(report);
            }
        }
        return found;
    }

    /**
     * (Optional) Finds all reports.
     * @return A new list containing all reports.
     */
    public List<Report> findAll() {
        // Return a copy to prevent modification of the internal list
        return new ArrayList<>(this.reports);
    }
}

