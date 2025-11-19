package ku.cs.services.report; // (สร้าง package ใหม่)

import ku.cs.database.DbConnect;
import ku.cs.models.report.Report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    /**
     * (สำหรับ Supervisor) ดึง Report ทั้งหมดที่มีในระบบ
     */
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports ORDER BY create_date DESC";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reports.add(createReportFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("ReportRepository (getAllReports) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return reports;
    }

    /**
     * (สำหรับ Instructor) เพิ่ม Report ใหม่ลงในตาราง
     */
    public void addReport(Report report) {
        String sql = "INSERT INTO reports (report_id, instructor_id, schedule_id, create_date, status, feedback, grade) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, report.getReportId());
            pstmt.setString(2, report.getInstructorId());
            pstmt.setString(3, report.getScheduleId());
            pstmt.setString(4, report.getCreateDate().toString());
            pstmt.setString(5, report.getStatus());
            pstmt.setString(6, report.getFeedback());
            pstmt.setString(7, report.getGrade());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("ReportRepository (addReport) Error: " + e.getMessage());
            throw new RuntimeException("Could not add report to DB", e);
        }
    }

    // (สามารถเพิ่มเมธอด updateStatus(String reportId, String newStatus) ที่นี่ได้ในอนาคต)

    /**
     * Helper method สำหรับแปลง ResultSet เป็น Report object
     */
    private Report createReportFromResultSet(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getString("report_id"));
        report.setInstructorId(rs.getString("instructor_id"));
        report.setScheduleId(rs.getString("schedule_id"));
        report.setCreateDate(LocalDateTime.parse(rs.getString("create_date")));
        report.setStatus(rs.getString("status"));
        report.setFeedback(rs.getString("feedback"));
        report.setGrade(rs.getString("grade"));
        return report;
    }
}