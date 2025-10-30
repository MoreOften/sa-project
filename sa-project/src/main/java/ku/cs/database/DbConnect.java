package ku.cs.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.schedule.Schedule; // [ADD] 1. Import model
import ku.cs.models.supervisor.Supervisor;
import ku.cs.models.user.User;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.schedule.ScheduleRepository; // [ADD] 2. Import repository
import ku.cs.services.supervisor.SupervisorRepository;
import ku.cs.services.user.UserRepository;
import java.time.LocalDate; // [ADD] 3. Import LocalDate

public class DbConnect {

    private static final String URL = "jdbc:sqlite:mydatabase.db";

    public static Connection getConnection() {
        // ... (โค้ดส่วนนี้เหมือนเดิม) ...
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
        String supervisorSql = "CREATE TABLE IF NOT EXISTS supervisors (...)"; // (ย่อโค้ดเดิม)
        String pilotSql = "CREATE TABLE IF NOT EXISTS pilots ("
                + " username TEXT PRIMARY KEY,"
                + " pilot_id TEXT NOT NULL UNIQUE,"
                + " FOREIGN KEY (username) REFERENCES users (username)"
                + ");";

        // [ADD] 4. เพิ่ม SQL สำหรับสร้างตาราง schedules
        String scheduleSql = "CREATE TABLE IF NOT EXISTS schedules ("
                + " schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " pilot_username TEXT NOT NULL,"
                + " instructor_username TEXT NOT NULL,"
                + " date TEXT NOT NULL," // 'YYYY-MM-DD'
                + " time TEXT NOT NULL," // 'HH:MM - HH:MM'
                + " program_name TEXT NOT NULL,"
                + " status TEXT NOT NULL DEFAULT 'Booked',"
                + " FOREIGN KEY (pilot_username) REFERENCES users (username),"
                + " FOREIGN KEY (instructor_username) REFERENCES users (username)"
                + ");";
        // -------------------------------------------------

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // ... (execute userSql, instructorSql, supervisorSql, pilotSql เหมือนเดิม) ...
            stmt.execute(userSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Users สำเร็จ");
            stmt.execute(instructorSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Instructor สำเร็จ");
            stmt.execute(supervisorSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Supervisor สำเร็จ");
            stmt.execute(pilotSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Pilot สำเร็จ");

            // [ADD] 5. สั่ง execute SQL ของตาราง schedule
            stmt.execute(scheduleSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Schedule สำเร็จ");
            // -------------------------------------------------

        } catch (SQLException e) {
            System.err.println("เกิดข้อผิดพลาดในการสร้างตาราง: " + e.getMessage());
        }
    }

    public static void seedInitialData() {
        UserRepository userRepository = new UserRepository();
        PilotRepository pilotRepository = new PilotRepository();
        InstructorRepository instructorRepository = new InstructorRepository();
        SupervisorRepository supervisorRepository = new SupervisorRepository();

        // [ADD] 6. สร้าง ScheduleRepository
        ScheduleRepository scheduleRepository = new ScheduleRepository();
        // -------------------------------------------------

        try {
            if (userRepository.findUserByUsername("supervisor_admin") != null) {
                System.out.println("ข้อมูลเริ่มต้น (Seed) มีอยู่แล้ว ไม่ต้องสร้างซ้ำ");
                return;
            }
            // ... (โค้ดสร้าง supervisorUser, supervisorProfile เหมือนเดิม) ...
            User supervisorUser = new User();
            // ...
            userRepository.registerUser(supervisorUser);
            Supervisor supervisorProfile = new Supervisor();
            // ...
            supervisorRepository.addSupervisor(supervisorProfile);

            // ... (โค้ดสร้าง instructorUser, instructorProfile เหมือนเดิม) ...
            User instructorUser = new User();
            // ...
            userRepository.registerUser(instructorUser);
            Instructor instructorProfile = new Instructor();
            // ...
            instructorRepository.addInstructor(instructorProfile);

            // ... (โค้ดสร้าง pilotUser) ...
            User pilotUser = new User();
            pilotUser.setUsername("pilot_test");
            pilotUser.setPassword("pass123");
            pilotUser.setName("Test Pilot");
            pilotUser.setRole("pilot");
            pilotUser.setEmail("pilot@test.com");
            pilotUser.setHasAccess(true);
            pilotUser.setLastLogin();
            pilotUser.setProfilePicture("default-user-photo.png");

            userRepository.registerUser(pilotUser); // <-- [FIX] 7. ลบการเรียกซ้ำบรรทัดล่างออก
            // userRepository.registerUser(pilotUser); // <-- บรรทัดนี้ซ้ำ

            Pilot pilotProfile = new Pilot();
            pilotProfile.setUsername("pilot_test");
            pilotProfile.setPilotID("PL001");
            pilotRepository.addPilot(pilotProfile);

            System.out.println("สร้างข้อมูลเริ่มต้น (Seeding) สำเร็จ!");

            // [ADD] 8. สร้างข้อมูลตารางเรียนตัวอย่าง
            Schedule sched1 = new Schedule(
                    "pilot_test",
                    "instructor_test",
                    LocalDate.of(2025, 11, 20),
                    "09:00 - 12:00",
                    "B787 Simulator Session",
                    "Booked"
            );
            Schedule sched2 = new Schedule(
                    "pilot_test",
                    "instructor_test",
                    LocalDate.of(2025, 11, 22),
                    "13:00 - 15:00",
                    "Emergency Procedures Review",
                    "Booked"
            );

            scheduleRepository.addSchedule(sched1);
            scheduleRepository.addSchedule(sched2);

            System.out.println("สร้างข้อมูลตารางเรียน (Seeding) สำเร็จ!");
            // -------------------------------------------------

        } catch (Exception e) {
            e.printStackTrace(); // [EDIT] ควรพิมพ์ e.printStackTrace() เพื่อดูข้อผิดพลาด
        }
    }
}