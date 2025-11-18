package ku.cs.services.schedule;

import ku.cs.database.DbConnect;
import ku.cs.models.schedule.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// (ลบ) import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleRepository {
    private static final List<Schedule> schedules = List.of();

    // ... (Existing methods) ...

    /**
     * Use Case Step 4 & 5: ค้นหาและยกเลิกตารางฝึกที่ยังไม่เสร็จสิ้น
     */
    public List<Schedule> cancelIncompleteSchedulesForPilot(String pilotID) {
        // สถานะที่ต้องยกเลิกตาม Use Case: status IN ('scheduled', 'pending')
        List<String> statusesToCancel = Arrays.asList("scheduled", "pending");

        // Step 4: SELECT (หาตารางที่เกี่ยวข้อง)
        List<Schedule> schedulesToCancel = schedules.stream()
                // ตรวจสอบสถานะและว่านักบินเกี่ยวข้องกับตารางนั้นๆ
                .filter(s -> statusesToCancel.contains(s.getScheduleStatus().toLowerCase()))
                .filter(s -> s.getPilotId1().equals(pilotID) || s.getPilotId2().equals(pilotID))
                .collect(Collectors.toList());

        // Step 5: DELETE/UPDATE (จำลองการยกเลิกโดยเปลี่ยนสถานะ)
        List<Schedule> cancelledSchedules = new ArrayList<>();

        for (Schedule schedule : schedulesToCancel) {
            schedule.setScheduleStatus("cancelled_resignation"); // เปลี่ยนสถานะเป็นยกเลิก
            cancelledSchedules.add(schedule);
            System.out.printf("-> [ScheduleRepo] ยกเลิกตาราง %s (Program: %s) แล้ว%n",
                    schedule.getScheduleId(), schedule.getPracticeProgram());
        }

        return cancelledSchedules;
    }

    /**
     * เพิ่ม Schedule ใหม่ลงในฐานข้อมูล
     * @param schedule อ็อบเจกต์ Schedule ที่สร้างจาก Controller
     */
    public void addSchedule(Schedule schedule) {

        // (1. แก้ไข SQL INSERT)
        String sql = "INSERT INTO schedules (schedule_id, supervisor_id, instructor_id, pilot_id_1, pilot_id_2, " +
                "practice_program, schedule_date, schedule_time, simulator) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getScheduleId());
            pstmt.setString(2, schedule.getSupervisorId());
            pstmt.setString(3, schedule.getInstructorId());
            pstmt.setString(4, schedule.getPilotId1());
            pstmt.setString(5, schedule.getPilotId2());

            // (2. แก้ไข PreparedStatement setters)
            pstmt.setString(6, schedule.getPracticeProgram());
            pstmt.setString(7, schedule.getScheduleDate());
            pstmt.setString(8, schedule.getScheduleTime());
            pstmt.setString(9, schedule.getSimulator());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("ScheduleRepository (addSchedule) Error: " + e.getMessage());
            throw new RuntimeException("Database error: Could not add schedule", e);
        }
    }

    public List<Schedule> findSchedulesByInstructor(String instructorId) {
        List<Schedule> schedules = new ArrayList<>();
        // (ไม่ต้องแก้ไข SQL นี้ เพราะ SELECT * จะดึงมาทุกคอลัมน์)
        String sql = "SELECT * FROM schedules WHERE instructor_id = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, instructorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // (เรียก helper method ที่เราจะแก้ไขด้านล่าง)
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
        // (ไม่ต้องแก้ไข SQL นี้)
        String sql = "SELECT * FROM schedules WHERE pilot_id_1 = ? OR pilot_id_2 = ?";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pilotId);
            pstmt.setString(2, pilotId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // (เรียก helper method ที่เราจะแก้ไขด้านล่าง)
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
                    // เราใช้ helper method ที่มีอยู่แล้วได้เลย
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
     * Helper method สำหรับสร้าง Schedule object จาก ResultSet
     * (3. แก้ไขเมธอดนี้)
     */
    private Schedule createScheduleFromResultSet(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();

        schedule.setScheduleId(rs.getString("schedule_id"));
        schedule.setSupervisorId(rs.getString("supervisor_id"));
        schedule.setInstructorId(rs.getString("instructor_id"));
        schedule.setPilotId1(rs.getString("pilot_id_1"));
        schedule.setPilotId2(rs.getString("pilot_id_2"));

        // (แก้ไขส่วนนี้ให้ตรงกับ Model และ DB)
        schedule.setPracticeProgram(rs.getString("practice_program"));
        schedule.setScheduleDate(rs.getString("schedule_date"));
        schedule.setScheduleTime(rs.getString("schedule_time"));
        schedule.setSimulator(rs.getString("simulator")); // (getString() จะคืนค่า null ถ้าใน DB เป็น null)

        return schedule;
    }
}