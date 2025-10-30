package ku.cs.services.schedule;



import ku.cs.database.DbConnect;

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

     * เพิ่ม Schedule ใหม่ลงในฐานข้อมูล

     * @param schedule อ็อบเจกต์ Schedule ที่สร้างจาก Controller

     */

    public void addSchedule(Schedule schedule) {

        String sql = "INSERT INTO schedules (schedule_id, supervisor_id, instructor_id, pilot_id_1, pilot_id_2, program_name, training_timestamp) "

                + "VALUES (?, ?, ?, ?, ?, ?, ?)";



        try (Connection conn = DbConnect.getConnection();

             PreparedStatement pstmt = conn.prepareStatement(sql)) {



            pstmt.setString(1, schedule.getScheduleId());

            pstmt.setString(2, schedule.getSupervisorId());

            pstmt.setString(3, schedule.getInstructorId());

            pstmt.setString(4, schedule.getPilotId1());

            pstmt.setString(5, schedule.getPilotId2());

            pstmt.setString(6, schedule.getProgramName());



// แปลง LocalDateTime เป็น TEXT (ISO String) เพื่อเก็บใน DB

            pstmt.setString(7, schedule.getTrainingTimestamp().toString());



            pstmt.executeUpdate();



        } catch (SQLException e) {

            System.err.println("ScheduleRepository (addSchedule) Error: " + e.getMessage());

            throw new RuntimeException("Database error: Could not add schedule", e);

        }

    }



    public List<Schedule> findSchedulesByInstructor(String instructorId) {

        List<Schedule> schedules = new ArrayList<>();

        String sql = "SELECT * FROM schedules WHERE instructor_id = ?";



        try (Connection conn = DbConnect.getConnection();

             PreparedStatement pstmt = conn.prepareStatement(sql)) {



            pstmt.setString(1, instructorId);



            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

// เรียกใช้ helper method เพื่อสร้าง object

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



// ค้นหาโดยเช็คว่า pilotId อยู่ในช่อง 1 "หรือ" ช่อง 2

        String sql = "SELECT * FROM schedules WHERE pilot_id_1 = ? OR pilot_id_2 = ?";



        try (Connection conn = DbConnect.getConnection();

             PreparedStatement pstmt = conn.prepareStatement(sql)) {



// ใส่ค่า pilotId ให้กับ ? ทั้งสองตัว

            pstmt.setString(1, pilotId);

            pstmt.setString(2, pilotId);



            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

// เรียกใช้ helper method เพื่อสร้าง object

                    schedules.add(createScheduleFromResultSet(rs));

                }

            }

        } catch (SQLException e) {

            System.err.println("ScheduleRepository (findSchedulesByPilot) Error: " + e.getMessage());

            e.printStackTrace();

        }

        return schedules;

    }



// (คุณต้องเพิ่มเมธอดอื่นๆ ที่นี่ เช่น...)

// public Schedule findScheduleById(String scheduleId) { ... }



    private Schedule createScheduleFromResultSet(ResultSet rs) throws SQLException {

        Schedule schedule = new Schedule();



        schedule.setScheduleId(rs.getString("schedule_id"));

        schedule.setSupervisorId(rs.getString("supervisor_id"));

        schedule.setInstructorId(rs.getString("instructor_id"));

        schedule.setPilotId1(rs.getString("pilot_id_1"));

        schedule.setPilotId2(rs.getString("pilot_id_2"));

        schedule.setProgramName(rs.getString("program_name"));



// แปลง TEXT (ISO String) จาก DB กลับเป็น LocalDateTime

        String timestampString = rs.getString("training_timestamp");

        if (timestampString != null) {

            schedule.setTrainingTimestamp(LocalDateTime.parse(timestampString));

        }



        return schedule;

    }

}