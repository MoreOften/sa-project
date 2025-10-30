package ku.cs.services.schedule;

import ku.cs.database.DbConnect; // [FIX] แก้ไขชื่อ DbConnect
import ku.cs.models.schedule.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

    /**
     * [MERGED & FIXED]
     * เพิ่ม Schedule ใหม่ลงในฐานข้อมูล
     * (อัปเดตให้รองรับ status, grade, และ feedback)
     */
    public void addSchedule(Schedule schedule) {
        // [EDIT] เพิ่มคอลัมน์ status, grade, instructor_feedback
        String sql = "INSERT INTO schedules (schedule_id, supervisor_id, instructor_id, pilot_id_1, pilot_id_2, " +
                "program_name, training_timestamp, status, grade, instructor_feedback) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // (มี 10 '?' )

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getScheduleId());
            pstmt.setString(2, schedule.getSupervisorId());
            pstmt.setString(3, schedule.getInstructorId());
            pstmt.setString(4, schedule.getPilotId1());
            pstmt.setString(5, schedule.getPilotId2());
            pstmt.setString(6, schedule.getProgramName());
            pstmt.setString(7, schedule.getTrainingTimestamp().toString());

            // [ADD] เพิ่ม 3 fields ใหม่
            pstmt.setString(8, schedule.getStatus());
            pstmt.setString(9, schedule.getGrade());
            pstmt.setString(10, schedule.getInstructorFeedback());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("ScheduleRepository (addSchedule) Error: " + e.getMessage());
            e.printStackTrace(); // โยน exception เพื่อให้รู้ตัว
        }
    }

    // --- เมธอดสำหรับหน้า Pilot's View (ที่ JOIN แล้ว) ---

    /**
     * [NEW]
     * ดึงตารางเรียนที่ "ยังไม่เสร็จ" (Booked) สำหรับ Pilot
     * (ใช้ในหน้า PilotSchedulePageController)
     */
    public List<Schedule> getSchedulesForPilot(String pilotUsername) {
        List<Schedule> schedules = new ArrayList<>();

        // [NEW SQL] SQL นี้ JOIN ตาราง users เพื่อดึง u.name มาเป็น 'instructor_name'
        String sql = "SELECT s.*, u.name AS instructor_name " +
                "FROM schedules s " +
                "JOIN users u ON s.instructor_id = u.username " +
                "WHERE (s.pilot_id_1 = ? OR s.pilot_id_2 = ?) " + // ค้นหาทั้ง 2 ช่อง
                "AND s.status = 'Booked' " + // [FILTER] เอาเฉพาะที่ยังไม่เสร็จ
                "ORDER BY s.training_timestamp ASC"; // เรียงจากเก่าไปใหม่

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pilotUsername);
            pstmt.setString(2, pilotUsername);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // เรียกใช้ helper ใหม่ที่รองรับ 'instructor_name'
                    schedules.add(createScheduleFromResultSet_withJoin(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ScheduleRepository (getSchedulesForPilot) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return schedules;
    }

    /**
     * [NEW]
     * ดึงตารางเรียนที่ "เสร็จแล้ว" (Completed) สำหรับ Pilot
     * (ใช้ในหน้า PilotReportPageController)
     */
    public List<Schedule> getCompletedSchedulesForPilot(String pilotUsername) {
        List<Schedule> schedules = new ArrayList<>();

        String sql = "SELECT s.*, u.name AS instructor_name " +
                "FROM schedules s " +
                "JOIN users u ON s.instructor_id = u.username " +
                "WHERE (s.pilot_id_1 = ? OR s.pilot_id_2 = ?) " +
                "AND s.status = 'Completed' " + // [FILTER] เอาเฉพาะที่เสร็จแล้ว
                "ORDER BY s.training_timestamp DESC"; // เรียงจากล่าสุด

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pilotUsername);
            pstmt.setString(2, pilotUsername);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(createScheduleFromResultSet_withJoin(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ScheduleRepository (getCompletedSchedulesForPilot) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return schedules;
    }

    // --- เมธอด Helper ---

    /**
     * [NEW HELPER]
     * Helper สำหรับสร้าง Schedule object จาก Query ที่ JOIN ตาราง users แล้ว
     */
    private Schedule createScheduleFromResultSet_withJoin(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();

        // 1. ดึงข้อมูลจาก 'schedules' table
        schedule.setScheduleId(rs.getString("schedule_id"));
        schedule.setSupervisorId(rs.getString("supervisor_id"));
        schedule.setInstructorId(rs.getString("instructor_id"));
        schedule.setPilotId1(rs.getString("pilot_id_1"));
        schedule.setPilotId2(rs.getString("pilot_id_2"));
        schedule.setProgramName(rs.getString("program_name"));
        schedule.setTrainingTimestamp(LocalDateTime.parse(rs.getString("training_timestamp")));

        // 2. ดึงคอลัมน์ที่ Merged (status, grade, feedback)
        schedule.setStatus(rs.getString("status"));
        schedule.setGrade(rs.getString("grade"));
        schedule.setInstructorFeedback(rs.getString("instructor_feedback"));

        // 3. ดึงคอลัมน์ที่ JOIN (instructor_name)
        schedule.setInstructorName(rs.getString("instructor_name"));

        return schedule;
    }

    // (เมธอด findSchedulesByInstructor และ createScheduleFromResultSet เดิม
    //  ยังสามารถเก็บไว้ได้ หากส่วนอื่นของโปรแกรม (เช่น หน้าของ Instructor) เรียกใช้
    //  แต่ต้องอัปเดต createScheduleFromResultSet ให้ดึง status, grade, feedback ด้วย)
}