package ku.cs.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.report.ReportStatus;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.models.report.Report;
import ku.cs.models.user.User;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.pilot.PilotRepository;
import ku.cs.services.report.ReportRepository;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.supervisor.SupervisorRepository;
import ku.cs.services.user.UserRepository;

public class DbConnect {

    // กำหนด URL สำหรับเชื่อมต่อ SQLite
// ไฟล์ฐานข้อมูล "mydatabase.db" จะถูกสร้างขึ้นที่โฟลเดอร์ราก (root) ของโปรเจกต์
    private static final String URL = "jdbc:sqlite:mydatabase.db";

    /**
     * เมธอดสำหรับเชื่อมต่อฐานข้อมูล SQLite
     * @return อ็อบเจกต์ Connection
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
// โหลด JDBC Driver (สำหรับ Java เก่าๆ อาจจำเป็น แต่เวอร์ชันใหม่ๆ มักจะไม่ต้อง)
// Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("การเชื่อมต่อฐานข้อมูลล้มเหลว: " + e.getMessage());
        }
        return conn;
    }

    /**
     * (แนะนำ) เมธอดสำหรับสร้างตารางเริ่มต้น หากยังไม่มี
     * ควรเรียกใช้เมธอดนี้แค่ครั้งเดียวตอนเริ่มโปรแกรม (เช่น ในคลาส MainApp)
     */
    public static void initializeDatabase() {
        // --- 1. ตาราง User (ไม่เปลี่ยนแปลง) ---
        String userSql = "CREATE TABLE IF NOT EXISTS users ("
                + " username TEXT PRIMARY KEY,"
                + " password TEXT NOT NULL,"
                + " name TEXT NOT NULL,"
                + " role TEXT,"
                + " phone TEXT,"
                + " email TEXT UNIQUE,"
                + " profilePicture TEXT,"
                + " lastLogin TEXT,"
                + " hasAccess INTEGER NOT NULL DEFAULT 1"
                + ");";

        // --- 2. ตาราง Instructor (อัปเดตตาม ERD) ---
        String instructorSql = "CREATE TABLE IF NOT EXISTS instructors ("
                + " username TEXT PRIMARY KEY,"
                + " instructor_id TEXT NOT NULL UNIQUE,"
                + " instructor_is_available INTEGER DEFAULT 1," // Field ใหม่
                + " FOREIGN KEY (username) REFERENCES users (username)"
                + ");";

        // --- 3. ตาราง Supervisor (ไม่เปลี่ยนแปลง) ---
        String supervisorSql = "CREATE TABLE IF NOT EXISTS supervisors ("
                + " username TEXT PRIMARY KEY,"
                + " supervisor_id TEXT NOT NULL UNIQUE,"
                + " FOREIGN KEY (username) REFERENCES users (username)"
                + ");";

        // --- 4. ตาราง Pilot (อัปเดตตาม ERD) ---
        String pilotSql = "CREATE TABLE IF NOT EXISTS pilots ("
                + " username TEXT PRIMARY KEY,"
                + " pilot_id TEXT NOT NULL UNIQUE,"
                + " pilot_type TEXT," // Field ใหม่
                + " pilot_is_failed INTEGER DEFAULT 0," // Field ใหม่
                + " pilot_fail_count INTEGER DEFAULT 0," // Field ใหม่
                + " pilot_status TEXT," // Field ใหม่
                + " pilot_progress TEXT," // Field ใหม่
                + " pilot_is_available INTEGER DEFAULT 1," // Field ใหม่
                + " FOREIGN KEY (username) REFERENCES users (username)"
                + ");";

        // --- 5. ตาราง Schedule (***แก้ไข***) ---
        String scheduleSql = "CREATE TABLE IF NOT EXISTS schedules ("
                + " schedule_id TEXT PRIMARY KEY,"
                + " supervisor_id TEXT NOT NULL,"
                + " instructor_id TEXT NOT NULL,"
                // (คงไว้ตามที่คุณขอ) ยังคงเก็บ 2 Pilots
                + " pilot_id_1 TEXT NOT NULL,"
                + " pilot_id_2 TEXT NOT NULL,"
                // (อัปเดต) Field ใหม่จาก ERD
                + " practice_program TEXT NOT NULL," // (แทน program_name)
                + " schedule_date TEXT NOT NULL,"    // (แทน training_timestamp)
                + " schedule_time TEXT NOT NULL,"    // (แทน training_timestamp)
                + " simulator TEXT,"                 // Field ใหม่
                + " schedule_status TEXT DEFAULT 'Scheduled'," // <-- เพิ่มคอลัมน์นี้
                + " FOREIGN KEY (supervisor_id) REFERENCES supervisors (supervisor_id),"
                + " FOREIGN KEY (instructor_id) REFERENCES instructors (instructor_id),"
                // (คงไว้ตามที่คุณขอ) Foreign Key สำหรับ 2 Pilots
                + " FOREIGN KEY (pilot_id_1) REFERENCES pilots (pilot_id),"
                + " FOREIGN KEY (pilot_id_2) REFERENCES pilots (pilot_id)"
                + ");";

        // --- 6. ตาราง Report (อัปเดตตาม ERD) ---
        String reportSql = "CREATE TABLE IF NOT EXISTS reports ("
                + " report_id TEXT PRIMARY KEY,"
                + " schedule_id TEXT NOT NULL,"
                + " pilot_id TEXT NOT NULL,"
                + " instructor_id TEXT NOT NULL,"
                + " approval_status TEXT NOT NULL," // (แทน status)
                + " report_result TEXT,"            // Field ใหม่
                + " report_notes TEXT,"             // (แทน report_details)
                + " created_at TEXT NOT NULL,"
                + " updated_at TEXT NOT NULL,"
                + " FOREIGN KEY (schedule_id) REFERENCES schedules (schedule_id),"
                + " FOREIGN KEY (pilot_id) REFERENCES pilots (pilot_id),"
                + " FOREIGN KEY (instructor_id) REFERENCES instructors (instructor_id)"
                + ");";
        // File: ku/cs/database/DbConnect.java (ส่วน initializeDatabase)

// ... (existing code)

        // --- 7. ตาราง Notification (***แก้ไข***) ---
        String notificationSql = "CREATE TABLE IF NOT EXISTS notifications ("
                + " notification_id TEXT PRIMARY KEY,"
                + " recipient_id TEXT NOT NULL,"
                + " sender_id TEXT NOT NULL,"
                + " notification_subject TEXT,"
                + " notification_content TEXT,"
                + " notification_type TEXT NOT NULL,"
                + " is_read INTEGER NOT NULL DEFAULT 0,"
                + " notification_timestamp TEXT NOT NULL,"
                + " FOREIGN KEY (recipient_id) REFERENCES users (username),"
                + " FOREIGN KEY (sender_id) REFERENCES users (username)"
                + ");";

        // --- (ส่วน Execute ไม่เปลี่ยนแปลง) ---
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(userSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Users สำเร็จ");

            stmt.execute(instructorSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Instructor สำเร็จ");

            stmt.execute(supervisorSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Supervisor สำเร็จ");

            stmt.execute(pilotSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Pilot สำเร็จ");

            stmt.execute(scheduleSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Schedules สำเร็จ");

            stmt.execute(reportSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Reports สำเร็จ");

            stmt.execute(notificationSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Notifications สำเร็จ");

        } catch (SQLException e) {
            System.err.println("เกิดข้อผิดพลาดในการสร้างตาราง: " + e.getMessage());
        }
    }

    public static void seedInitialData() {
        // ... (Code for seedInitialData remains the same, ensuring the Supervisor and Pilots are properly seeded before the Schedule is added) ...

        UserRepository userRepository = new UserRepository();
        PilotRepository pilotRepository = new PilotRepository();
        InstructorRepository instructorRepository = new InstructorRepository();
        SupervisorRepository supervisorRepository = new SupervisorRepository();
        ScheduleRepository scheduleRepository = new ScheduleRepository();
        ReportRepository reportRepository = new ReportRepository();

        try {

            if (userRepository.findUserByUsername("supervisor_admin") != null) {
                System.out.println("ข้อมูลเริ่มต้น (Seed) มีอยู่แล้ว ไม่ต้องสร้างซ้ำ");
                return;
            }

            User supervisorUser = new User();
            supervisorUser.setUsername("supervisor_admin");
            supervisorUser.setPassword("pass123");
            supervisorUser.setName("Admin Supervisor");
            supervisorUser.setRole("supervisor");
            supervisorUser.setEmail("supervisor@test.com");
            supervisorUser.setHasAccess(true);
            supervisorUser.setLastLogin();
            supervisorUser.setProfilePicture("default-user-photo.png");
            userRepository.registerUser(supervisorUser);

            Supervisor supervisorProfile = new Supervisor();
            supervisorProfile.setUsername("supervisor_admin");
            supervisorProfile.setSupervisorID("S001");
            supervisorRepository.addSupervisor(supervisorProfile);

            User instructorUser = new User(); // <-- ประกาศตัวแปร
            instructorUser.setUsername("instructor_test");
            instructorUser.setPassword("pass123");
            instructorUser.setName("Test Instructor");
            instructorUser.setRole("instructor");
            instructorUser.setEmail("instructor@test.com");
            instructorUser.setHasAccess(true);
            instructorUser.setLastLogin();
            instructorUser.setProfilePicture("default-user-photo.png");
            userRepository.registerUser(instructorUser);

            Instructor instructorProfile = new Instructor();
            instructorProfile.setUsername("instructor_test");
            instructorProfile.setInstructorID("I001");
            instructorRepository.addInstructor(instructorProfile);

            User pilotUser = new User(); // <-- ประกาศตัวแปร
            pilotUser.setUsername("pilot_test");
            pilotUser.setPassword("pass123");
            pilotUser.setName("Test Pilot");
            pilotUser.setRole("pilot");
            pilotUser.setEmail("pilot@test.com");
            pilotUser.setHasAccess(true);
            pilotUser.setLastLogin();
            pilotUser.setProfilePicture("default-user-photo.png");

            User pilotUser2 = new User(); // <-- ประกาศตัวแปร
            pilotUser2.setUsername("pilot_test_two");
            pilotUser2.setPassword("pass123");
            pilotUser2.setName("Test Pilot 2");
            pilotUser2.setRole("pilot");
            pilotUser2.setEmail("pilot2@test.com");
            pilotUser2.setHasAccess(true);
            pilotUser2.setLastLogin();
            pilotUser2.setProfilePicture("default-user-photo.png");

            userRepository.registerUser(pilotUser);

            Pilot pilotProfile = new Pilot();
            pilotProfile.setUsername("pilot_test");
            pilotProfile.setPilotID("PL001");
            pilotRepository.addPilot(pilotProfile);

            userRepository.registerUser(pilotUser2);

            Pilot pilotProfile2 = new Pilot();
            pilotProfile2.setUsername("pilot_test_two");
            pilotProfile2.setPilotID("PL002");
            pilotRepository.addPilot(pilotProfile2);

            System.out.println("Seeding mock schedule...");

            String FIXED_SCHEDULE_ID = "SC-001";
            Schedule schedule1 = new Schedule(
                    FIXED_SCHEDULE_ID,
                    "S001", // supervisorId
                    "I001", // instructorId
                    "PL001", // pilotId1
                    "PL002", // pilotId2
                    "Scheduled",
                    "Introduction to Flight", // practice_program
                    java.time.LocalDate.now().plusDays(3).toString(), // schedule_date (TEXT)
                    "14:00", // schedule_time (TEXT)
                    "SIM-A380" // simulator
            );
            scheduleRepository.addSchedule(schedule1);

            System.out.println("สร้างข้อมูลเริ่มต้น (Seeding) สำเร็จ!");

            System.out.println("Seeding mock reports...");
            // Assumes Schedule ID SC-001 is the one created earlier.
            String mockScheduleId = "SC-001";

            Report report1 = new Report(
                    "R001", // reportId
                    mockScheduleId, // scheduleId (Reference to the seeded schedule)
                    "PL001", // pilotId (The pilot associated with the report)
                    "I001", // instructorId (The instructor associated with the report)
                    "Simulator Malfunction: The autopilot system failed to disengage during approach to runway 30.", // reportNotes (Combined Subject + Detail)
                    "test", // reportResult (Empty, as status is PENDING_REVIEW)
                    ReportStatus.PENDING_REVIEW, // approvalStatus (Mapped from old "Pending")
                    LocalDateTime.now().minusDays(1), // createdAt
                    LocalDateTime.now().minusDays(1) // updatedAt
            );

            Report report2 = new Report(
                    "R002", // reportId
                    mockScheduleId, // scheduleId
                    "PL002", // pilotId (The subject of the report)
                    "I001", // instructorId (The reporter)
                    "Pilot PL002 was 15 minutes late for the 14:00 session.", // reportNotes
                    "Pilot was counselled and documented. Approved by supervisor.", // reportResult (Mocked result for a resolved issue)
                    ReportStatus.APPROVED, // approvalStatus (Mapped from old "Resolved")
                    LocalDateTime.now().minusDays(5), // createdAt
                    LocalDateTime.now().minusDays(4) // updatedAt (Updated when resolved)
            );

            reportRepository.save(report1);
            reportRepository.save(report2);

            System.out.println("สร้างข้อมูลเริ่มต้น (Seeding) สำเร็จ!");

        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดระหว่างการ Seeding ข้อมูล: " + e.getMessage());
            e.printStackTrace();
        }
    }
}