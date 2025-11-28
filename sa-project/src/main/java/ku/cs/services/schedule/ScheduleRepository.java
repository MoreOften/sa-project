package ku.cs.services.schedule;

import ku.cs.database.DbConnect;
import ku.cs.models.schedule.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * Find active schedules (Scheduled or Pending) for a specific pilot without modifying them.
     */
    public List<Schedule> cancelIncompleteSchedulesForPilot(String pilotID) {
        List<Schedule> schedulesToNotify = new ArrayList<>();

        // สถานะที่ต้องยกเลิกคือ 'Scheduled' หรือ 'Pending'
        List<String> statusesToCancel = Arrays.asList("Scheduled", "Pending");

        // SQL SELECT: ค้นหาตารางที่เกี่ยวข้อง (Pilot1 หรือ Pilot2) และมีสถานะที่ต้องยกเลิก
        String selectSql = "SELECT * FROM schedules WHERE (pilot_id_1 = ? OR pilot_id_2 = ?) AND schedule_status IN (?, ?)";

        try (Connection conn = DbConnect.getConnection()) {

            // Step 1: SELECT - หาตารางที่จะยกเลิก
            try (PreparedStatement selectPstmt = conn.prepareStatement(selectSql)) {
                selectPstmt.setString(1, pilotID);
                selectPstmt.setString(2, pilotID);
                selectPstmt.setString(3, statusesToCancel.get(0));
                selectPstmt.setString(4, statusesToCancel.get(1));

                try (ResultSet rs = selectPstmt.executeQuery()) {
                    while (rs.next()) {
                        // สร้าง Schedule object จาก ResultSet
                        Schedule s = createScheduleFromResultSet(rs);
                        schedulesToNotify.add(s);
                    }
                }
            }

            // Step 2: UPDATE - เปลี่ยนสถานะใน DB ทีละรายการโดยเรียกเมธอดใหม่
            if (!schedulesToNotify.isEmpty()) {
                System.out.printf("-> [ScheduleRepo] พบ %d ตารางที่ต้องยกเลิกเนื่องจากการลาออก.%n", schedulesToNotify.size());
                String newStatus = "cancelled_resignation";

                for (Schedule schedule : schedulesToNotify) {
                    // เรียกเมธอด Update โดยตรง
                    if (updateScheduleStatus(schedule.getScheduleId(), newStatus)) {
                        System.out.println("-> [ScheduleRepo] Schedule ID " + schedule.getScheduleId() + " updated.");
                    } else {
                        System.err.println("-> [ScheduleRepo] ERROR: Failed to update status for " + schedule.getScheduleId());
                    }
                    // อัปเดตสถานะใน object เพื่อคืนค่าที่ถูกต้อง
                    schedule.setScheduleStatus(newStatus);
                }
            } else {
                System.out.println("-> [ScheduleRepo] ไม่มีตารางฝึกที่ยังไม่เสร็จสิ้นต้องยกเลิก.");
            }

        } catch (SQLException e) {
            System.err.println("ScheduleRepository (cancelIncompleteSchedulesForPilot) Database Error: " + e.getMessage());
            e.printStackTrace();
        }

        // คืนค่าตารางที่ถูกยกเลิก
        return schedulesToNotify;
    }

    // ... (rest of the file: findSchedulesByInstructor, findSchedulesByPilot, etc. UNCHANGED) ...

    // (เมธอด createScheduleFromResultSet ต้องอยู่ที่นี่เพื่อรองรับ SELECT)
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
        // (แก้ไข SQL) นำเงื่อนไขการกรองสถานะ 'cancelled_resignation' ออก
        // เพื่อให้ตารางที่ถูกยกเลิกเนื่องจากการลาออกยังคงแสดงในหน้า Instructor
        String sql = "SELECT * FROM schedules WHERE instructor_id = ?"; // <-- MODIFIED SQL

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

// ... (เมธอดอื่นๆ) ...

    /**
     * ค้นหาตารางฝึกทั้งหมดที่สร้างโดย Supervisor คนนี้
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
     * ตรวจสอบว่า Instructor หรือ Pilot ว่างในวันและเวลาที่ระบุหรือไม่
     * @return true ถ้าว่าง (ไม่ชน), false ถ้าไม่ว่าง (มีตารางอยู่แล้ว)
     */
    public boolean checkAvailability(String date, String time, String instructorId, String pilotId) {
        // SQL: นับจำนวนตารางที่ วัน/เวลา ตรงกัน และ (เป็น Instructor คนนี้ หรือ เป็น Pilot คนนี้)
        // โดยไม่นับตารางที่ถูกยกเลิก (Cancelled)
        String sql = "SELECT COUNT(*) FROM schedules " +
                "WHERE schedule_date = ? AND schedule_time = ? " +
                "AND schedule_status != 'Cancelled' " +
                "AND (instructor_id = ? OR pilot_id_1 = ? OR pilot_id_2 = ?)";

        try (Connection conn = DbConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date);
            pstmt.setString(2, time);
            pstmt.setString(3, instructorId);
            pstmt.setString(4, pilotId); // เช็คว่า Pilot เป็น pilot_1 หรือไม่
            pstmt.setString(5, pilotId); // เช็คว่า Pilot เป็น pilot_2 หรือไม่

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count == 0; // ถ้า Count เป็น 0 แสดงว่าว่าง (True)
                }
            }
        } catch (SQLException e) {
            System.err.println("ScheduleRepository (checkAvailability) Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // กรณี Error ให้กันไว้ก่อนว่าไม่ว่าง
    }
}