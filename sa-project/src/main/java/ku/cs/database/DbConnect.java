package ku.cs.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.models.user.User;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.supervisor.SupervisorRepository;
import ku.cs.services.user.UserRepository;

public class DbConnect {

    private static final String URL = "jdbc:sqlite:mydatabase.db";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("การเชื่อมต่อฐานข้อมูลล้มเหลว: " + e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        // ... (userSql, instructorSql, supervisorSql, pilotSql เหมือนเดิม) ...
        String userSql = "CREATE TABLE IF NOT EXISTS users (...)"; // (ย่อโค้ดเดิม)
        String instructorSql = "CREATE TABLE IF NOT EXISTS instructors (...)"; // (ย่อโค้ดเดิม)
        String supervisorSql = "CREATE TABLE IF NOT EXISTS supervisors (...)"; // (ย่อโค้dเดิม)
        String pilotSql = "CREATE TABLE IF NOT EXISTS pilots (...)"; // (ย่อโค้ดเดิม)

        // [MERGE] นี่คือ scheduleSql ที่รวมทั้ง 2 เวอร์ชัน
        String scheduleSql = "CREATE TABLE IF NOT EXISTS schedules ("
                + " schedule_id TEXT PRIMARY KEY,"
                + " supervisor_id TEXT NOT NULL,"      // Foreign Key (username)
                + " instructor_id TEXT NOT NULL,"      // Foreign Key (username)
                + " pilot_id_1 TEXT NOT NULL,"         // Foreign Key (username)
                + " pilot_id_2 TEXT,"                  // [FIX] เปลี่ยนเป็น Nullable (อาจมีนักบินคนเดียว)
                + " program_name TEXT NOT NULL,"
                + " training_timestamp TEXT NOT NULL," // เก็บ LocalDateTime.toString()

                // [ADD] เพิ่ม 3 คอลัมน์ที่จำเป็นสำหรับหน้า Report
                + " status TEXT NOT NULL,"
                + " grade TEXT,"                       // Nullable (ยังไม่ให้เกรด)
                + " instructor_feedback TEXT,"         // Nullable (ยังไม่ให้ feedback)

                + " FOREIGN KEY (supervisor_id) REFERENCES users (username),"
                + " FOREIGN KEY (instructor_id) REFERENCES users (username),"
                + " FOREIGN KEY (pilot_id_1) REFERENCES users (username),"
                + " FOREIGN KEY (pilot_id_2) REFERENCES users (username)"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // ... (execute user, instructor, supervisor, pilot เหมือนเดิม) ...
            stmt.execute(userSql);
            stmt.execute(instructorSql);
            stmt.execute(supervisorSql);
            stmt.execute(pilotSql);

            // Execute ตารางที่ Merged แล้ว
            stmt.execute(scheduleSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Schedules (Merged) สำเร็จ");

        } catch (SQLException e) {
            System.err.println("เกิดข้อผิดพลาดในการสร้างตาราง: " + e.getMessage());
        }
    }

    public static void seedInitialData() {
        // ... (Repositories เหมือนเดิม) ...
        UserRepository userRepository = new UserRepository();
        PilotRepository pilotRepository = new PilotRepository();
        InstructorRepository instructorRepository = new InstructorRepository();
        SupervisorRepository supervisorRepository = new SupervisorRepository();
        ScheduleRepository scheduleRepository = new ScheduleRepository();
        try {
            if (userRepository.findUserByUsername("supervisor_admin") != null) {
                System.out.println("ข้อมูลเริ่มต้น (Seed) มีอยู่แล้ว ไม่ต้องสร้างซ้ำ");
                return;
            }

            // ... (สร้าง supervisorUser, instructorUser, pilotUser, pilotUser2 เหมือนเดิม) ...

            // (ย่อโค้ดเดิม)
            User supervisorUser = new User("supervisor_admin", "pass123", "Admin Supervisor", "supervisor", ...);
            userRepository.registerUser(supervisorUser);
            Supervisor supervisorProfile = new Supervisor("supervisor_admin", "S001");
            supervisorRepository.addSupervisor(supervisorProfile);

            User instructorUser = new User("instructor_test", "pass123", "Test Instructor", "instructor", ...);
            userRepository.registerUser(instructorUser);
            Instructor instructorProfile = new Instructor("instructor_test", "I001");
            instructorRepository.addInstructor(instructorProfile);

            User pilotUser = new User("pilot_test", "pass123", "Test Pilot", "pilot", ...);
            userRepository.registerUser(pilotUser);
            Pilot pilotProfile = new Pilot("pilot_test", "PL001");
            pilotRepository.addPilot(pilotProfile);

            User pilotUser2 = new User("pilot_test_two", "pass123", "Test Pilot 2", "pilot", ...);
            userRepository.registerUser(pilotUser2);
            Pilot pilotProfile2 = new Pilot("pilot_test_two", "PL002");
            pilotRepository.addPilot(pilotProfile2);


            System.out.println("Seeding mock schedule...");

            // [MERGE] สร้าง Schedule 1 (Booked)
            // [FIX] เราต้องใส่ Username (เช่น "supervisor_admin") ไม่ใช่ ID (เช่น "S001")
            // เพราะ Foreign Key อ้างอิงถึง `users(username)`
            Schedule schedule1 = new Schedule(
                    "supervisor_admin",       // supervisorId (Username)
                    "instructor_test",        // instructorId (Username)
                    "pilot_test",             // pilotId1 (Username)
                    "pilot_test_two",         // pilotId2 (Username)
                    "Introduction to Flight", // programName
                    LocalDateTime.now().plusDays(3).withHour(14).withMinute(0)
            );
            // (Constructor ที่ Merged แล้ว จะตั้ง status="Booked" ให้อัตโนมัติ)
            scheduleRepository.addSchedule(schedule1);


            // [ADD] สร้าง Schedule 2 (Completed) เพื่อให้หน้า Report ของคุณมีข้อมูลโชว์
            Schedule schedule2 = new Schedule(
                    "supervisor_admin",
                    "instructor_test",
                    "pilot_test",
                    null, // ไฟลท์นี้บินเดี่ยว (ทดสอบ pilot_id_2 = null)
                    "B787 Simulator Checkride",
                    LocalDateTime.now().minusDays(5).withHour(10).withMinute(0) // 5 วันที่แล้ว
            );
            // ตั้งค่าข้อมูลสำหรับ Report
            schedule2.setStatus("Completed");
            schedule2.setGrade("Pass");
            schedule2.setInstructorFeedback("Pilot demonstrated excellent handling of the B787 during the engine-out scenario. Good CRM. Cleared for line training.");
            scheduleRepository.addSchedule(schedule2);

            System.out.println("สร้างข้อมูลเริ่มต้น (Seeding) สำเร็จ!");

        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดระหว่างการ Seeding ข้อมูล: " + e.getMessage());
            e.printStackTrace();
        }
    }
}