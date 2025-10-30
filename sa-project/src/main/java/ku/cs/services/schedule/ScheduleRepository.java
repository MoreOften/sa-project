package ku.cs.services.schedule;

import ku.cs.database.DbConnect1;
import ku.cs.models.schedule.Schedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

    /**
     * ดึงข้อมูลตารางเรียนของ Pilot โดยระบุ username
     * และ JOIN ตาราง users เพื่อดึงชื่อของ Instructor
     */
    public List<Schedule> getSchedulesForPilot(String pilotUsername) {
        List<Schedule> schedules = new ArrayList<>();
        Connection conn = DbConnect1.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        /*
         * SQL นี้จะ:
         * 1. เลือกข้อมูลจากตาราง schedules (ตั้งชื่อย่อ 's')
         * 2. JOIN ตาราง users (ตั้งชื่อย่อ 'u') โดยใช้ instructor_username
         * 3. ดึง 'u.name' (ชื่อเต็มของ instructor) และตั้งชื่อคอลัมน์ใหม่ว่า 'instructor_name'
         * 4. กรองเฉพาะ pilot_username ที่ต้องการ
         */
        String sql = "SELECT s.date, s.time, s.program_name, u.name AS instructor_name " +
                "FROM schedules s " +
                "JOIN users u ON s.instructor_username = u.username " +
                "WHERE s.pilot_username = ? " +
                "ORDER BY s.date, s.time";

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, pilotUsername);
            rs = stmt.executeQuery();

            while (rs.next()) {
                // [สำคัญ] เราสร้าง Schedule object ด้วย Constructor พิเศษ
                // ที่รับ instructor_name (จาก JOIN) ไม่ใช่ instructor_username
                Schedule schedule = new Schedule(
                        rs.getDate("date").toLocalDate(),
                        rs.getString("time"),
                        rs.getString("program_name"),
                        rs.getString("instructor_name") // <-- นี่คือ u.name
                );
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return schedules;
    }

    /**
     * เพิ่มตารางเรียนใหม่ (สำหรับ Seeding)
     */
    public void addSchedule(Schedule schedule) {
        Connection conn = DbConnect1.getConnection();
        PreparedStatement stmt = null;
        String sql = "INSERT INTO schedules (pilot_username, instructor_username, date, time, program_name, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, schedule.getPilotUsername());
            stmt.setString(2, schedule.getInstructorUsername());
            stmt.setString(3, schedule.getDate().toString()); // 'YYYY-MM-DD'
            stmt.setString(4, schedule.getTime());
            stmt.setString(5, schedule.getProgramName());
            stmt.setString(6, schedule.getStatus());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}