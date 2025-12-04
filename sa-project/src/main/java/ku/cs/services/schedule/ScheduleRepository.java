package ku.cs.services.schedule;

import ku.cs.database.DbConnect;
import ku.cs.models.schedule.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

    /**
     * อัปเดตสถานะของตารางฝึก
     */
    public boolean updateScheduleStatus(String scheduleId, String newStatus) {
        String sql = "UPDATE schedules SET schedule_status = ? WHERE schedule_id = ?";
        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, scheduleId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("ScheduleRepository (updateScheduleStatus) Database Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ค้นหาตารางที่ยังไม่เสร็จสิ้น (Scheduled, Pending) สำหรับ Pilot (ใช้ใน ResignationService)
     */
    public List<Schedule> findActiveSchedulesForPilot(String pilotID) {
        List<Schedule> activeSchedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE (pilot_id_1 = ? OR pilot_id_2 = ?) AND schedule_status IN ('Scheduled', 'Pending')";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pilotID);
            pstmt.setString(2, pilotID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activeSchedules.add(createScheduleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ScheduleRepository (findActiveSchedulesForPilot) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return activeSchedules;
    }

    /**
     * (Legacy) เมธอดสำหรับค้นหาและยกเลิกตารางฝึกที่ยังไม่เสร็จสิ้น (รองรับโค้ดเก่า)
     */
    public List<Schedule> cancelIncompleteSchedulesForPilot(String pilotID) {
        // ใช้ logic เดียวกับ findActiveSchedulesForPilot แล้วอัปเดตสถานะ
        List<Schedule> schedules = findActiveSchedulesForPilot(pilotID);
        for (Schedule s : schedules) {
            updateScheduleStatus(s.getScheduleId(), "cancelled_resignation");
            s.setScheduleStatus("cancelled_resignation");
        }
        return schedules;
    }

    public void addSchedule(Schedule schedule) {
        String sql = "INSERT INTO schedules (schedule_id, supervisor_id, instructor_id, pilot_id_1, pilot_id_2, " +
                "practice_program, schedule_date, schedule_time, simulator, schedule_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getScheduleId());
            pstmt.setString(2, schedule.getSupervisorId());
            pstmt.setString(3, schedule.getInstructorId());
            pstmt.setString(4, schedule.getPilotId1());
            pstmt.setString(5, schedule.getPilotId2());
            pstmt.setString(6, schedule.getPracticeProgram());
            pstmt.setString(7, schedule.getScheduleDate());
            pstmt.setString(8, schedule.getScheduleTime());
            pstmt.setString(9, schedule.getSimulator());
            pstmt.setString(10, schedule.getScheduleStatus());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("ScheduleRepository (addSchedule) Error: " + e.getMessage());
            throw new RuntimeException("Database error: Could not add schedule", e);
        }
    }

    public List<Schedule> findSchedulesByInstructor(String instructorId) {
        List<Schedule> schedules = new ArrayList<>();
        // ดึงข้อมูลทั้งหมดรวมถึงที่ถูกยกเลิก เพื่อให้ Instructor เห็นประวัติ
        String sql = "SELECT * FROM schedules WHERE instructor_id = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, instructorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(createScheduleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ScheduleRepository (findSchedulesByInstructor) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return schedules;
    }

    public List<Schedule> findSchedulesByPilot(String pilotId) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE (pilot_id_1 = ? OR pilot_id_2 = ?)";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pilotId);
            pstmt.setString(2, pilotId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(createScheduleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ScheduleRepository (findSchedulesByPilot) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return schedules;
    }

    public Schedule findScheduleById(String scheduleId) {
        String sql = "SELECT * FROM schedules WHERE schedule_id = ?";
        Schedule schedule = null;

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, scheduleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    schedule = createScheduleFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("ScheduleRepository (findScheduleById) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return schedule;
    }

    /**
     * ค้นหาตารางฝึกทั้งหมดที่สร้างโดย Supervisor คนนี้ (เพิ่มใหม่จาก Supervisor Project)
     */
    public List<Schedule> findSchedulesBySupervisor(String supervisorId) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE supervisor_id = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supervisorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(createScheduleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ScheduleRepository (findSchedulesBySupervisor) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return schedules;
    }

    /**
     * ตรวจสอบความพร้อม (Availability) ของ Instructor หรือ Pilot (เพิ่มใหม่จาก Supervisor Project)
     * @return true ถ้าว่าง (ไม่ชน), false ถ้าไม่ว่าง
     */
    public boolean checkAvailability(String date, String time, String instructorId, String pilotId) {
        String sql = "SELECT COUNT(*) FROM schedules " +
                "WHERE schedule_date = ? AND schedule_time = ? " +
                "AND schedule_status != 'Cancelled' " +
                "AND (instructor_id = ? OR pilot_id_1 = ? OR pilot_id_2 = ?)";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date);
            pstmt.setString(2, time);
            pstmt.setString(3, instructorId);
            pstmt.setString(4, pilotId);
            pstmt.setString(5, pilotId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("ScheduleRepository (checkAvailability) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Helper Method
    private Schedule createScheduleFromResultSet(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(rs.getString("schedule_id"));
        schedule.setSupervisorId(rs.getString("supervisor_id"));
        schedule.setInstructorId(rs.getString("instructor_id"));
        schedule.setPilotId1(rs.getString("pilot_id_1"));
        schedule.setPilotId2(rs.getString("pilot_id_2"));
        schedule.setPracticeProgram(rs.getString("practice_program"));
        schedule.setScheduleDate(rs.getString("schedule_date"));
        schedule.setScheduleTime(rs.getString("schedule_time"));
        schedule.setSimulator(rs.getString("simulator"));
        schedule.setScheduleStatus(rs.getString("schedule_status"));
        return schedule;
    }
}