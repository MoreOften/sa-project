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

// SQL สำหรับสร้างตาราง (ตัวอย่างคือตาราง "users")

        String userSql = "CREATE TABLE IF NOT EXISTS users ("

                + " username TEXT PRIMARY KEY," // ใช้ username เป็น PK ไปเลยง่ายกว่า

                + " password TEXT NOT NULL,"

                + " name TEXT NOT NULL,"

                + " role TEXT,"

                + " phone TEXT,"

                + " email TEXT UNIQUE,"

                + " profilePicture TEXT,"

                + " lastLogin TEXT," // 1. เปลี่ยนเป็น TEXT เพื่อเก็บ LocalDateTime.toString()

                + " hasAccess INTEGER NOT NULL DEFAULT 1" // 2. เปลี่ยนเป็น INTEGER (1=true, 0=false)

                + ");";



        String instructorSql = "CREATE TABLE IF NOT EXISTS instructors ("

// 1. ใช้ username เป็น "กุญแจหลัก" และ "กุญแจต่างประเทศ" (Foreign Key)

// เพื่อเชื่อมโยงไปยังตาราง users

                + " username TEXT PRIMARY KEY,"



// 2. เก็บเฉพาะข้อมูลของ Instructor ที่ User ไม่มี

                + " instructor_id TEXT NOT NULL UNIQUE,"



// 5. สร้าง Foreign Key constraint

                + " FOREIGN KEY (username) REFERENCES users (username)"

                + ");";



        String supervisorSql = "CREATE TABLE IF NOT EXISTS supervisors ("

                + " username TEXT PRIMARY KEY,"

                + " supervisor_id TEXT NOT NULL UNIQUE,"

// + " name TEXT NOT NULL," // <--- (1) ลบบรรทัดนี้

                + " FOREIGN KEY (username) REFERENCES users (username)"

                + ");";



        String pilotSql = "CREATE TABLE IF NOT EXISTS pilots ("

                + " username TEXT PRIMARY KEY,"

                + " pilot_id TEXT NOT NULL UNIQUE,"

// + " name TEXT NOT NULL," // <--- (2) ลบบรรทัดนี้

                + " FOREIGN KEY (username) REFERENCES users (username)"

                + ");";



        String scheduleSql = "CREATE TABLE IF NOT EXISTS schedules ("

                + " schedule_id TEXT PRIMARY KEY,"

                + " supervisor_id TEXT NOT NULL,"

                + " instructor_id TEXT NOT NULL,"

                + " pilot_id_1 TEXT NOT NULL,"

                + " pilot_id_2 TEXT NOT NULL,"

                + " program_name TEXT NOT NULL,"

                + " training_timestamp TEXT NOT NULL,"

                + " FOREIGN KEY (supervisor_id) REFERENCES users (username),"

                + " FOREIGN KEY (instructor_id) REFERENCES users (username),"

                + " FOREIGN KEY (pilot_id_1) REFERENCES users (username),"

                + " FOREIGN KEY (pilot_id_2) REFERENCES users (username)"

                + ");";

        // ... (ต่อจาก scheduleSql) ...

        String reportSql = "CREATE TABLE IF NOT EXISTS reports ("
                + " report_id TEXT PRIMARY KEY,"
                + " instructor_id TEXT NOT NULL,"
                + " schedule_id TEXT,"
                + " create_date TEXT NOT NULL,"
                + " status TEXT NOT NULL,"
                + " feedback TEXT,"
                + " grade TEXT,"
                + " FOREIGN KEY (instructor_id) REFERENCES users (username)"
                + ");";

// ... (ใน try-with-resources) ...
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // ... (stmt.execute ของตารางอื่นๆ) ...
            stmt.execute(scheduleSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Schedules สำเร็จ");

            // (เพิ่มบรรทัดนี้)
            stmt.execute(reportSql);
            System.out.println("ตรวจสอบ/สร้างตาราง Reports สำเร็จ");

        } catch (SQLException e) {
            // ...
        }

// ใช้ try-with-resources เพื่อให้แน่ใจว่า Connection และ Statement ถูกปิดเสมอ

        try (Connection conn = getConnection();

             Statement stmt = conn.createStatement()) {



// สั่งให้ SQL ทำงาน

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



        } catch (SQLException e) {

            System.err.println("เกิดข้อผิดพลาดในการสร้างตาราง: " + e.getMessage());

        }

    }



    public static void seedInitialData() {

// ... (สร้าง Repositories) ...

        UserRepository userRepository = new UserRepository();

        PilotRepository pilotRepository = new PilotRepository();

        InstructorRepository instructorRepository = new InstructorRepository();

        SupervisorRepository supervisorRepository = new SupervisorRepository();

        ScheduleRepository scheduleRepository = new ScheduleRepository();

        try {

// ... (if check supervisor_admin) ...

            if (userRepository.findUserByUsername("supervisor_admin") != null) {

                System.out.println("ข้อมูลเริ่มต้น (Seed) มีอยู่แล้ว ไม่ต้องสร้างซ้ำ");

                return;

            }

// ... (สร้าง supervisorUser) ...

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

// supervisorProfile.setName("Admin Supervisor"); // <--- (1) ลบบรรทัดนี้

            supervisorRepository.addSupervisor(supervisorProfile);



// 4. --- สร้าง Instructor ---

// ... (สร้าง instructorUser) ...

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

// instructorProfile.setName("Test Instructor"); // <--- (2) ลบบรรทัดนี้

            instructorRepository.addInstructor(instructorProfile);



// 5. --- สร้าง Pilot ---

// ... (สร้าง pilotUser) ...

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

// pilotProfile.setName("Test Pilot"); // <--- (3) ลบบรรทัดนี้

            pilotRepository.addPilot(pilotProfile);



            userRepository.registerUser(pilotUser2);



            Pilot pilotProfile2 = new Pilot();

            pilotProfile2.setUsername("pilot_test_two");

            pilotProfile2.setPilotID("PL002");

            pilotRepository.addPilot(pilotProfile2);



            System.out.println("Seeding mock schedule...");

            Schedule schedule1 = new Schedule(

                    "S001", // supervisorId

                    "I001", // instructorId (คนที่คุณจะเทส)

                    "PL001", // pilotId1 (คนที่ 1)

                    "PL002", // pilotId2 (คนที่ 2)

                    "Introduction to Flight", // programName

                    LocalDateTime.now().plusDays(3).withHour(14).withMinute(0) // วันเวลา (อีก 3 วัน ตอน 14:00)

            );

            scheduleRepository.addSchedule(schedule1);



            System.out.println("สร้างข้อมูลเริ่มต้น (Seeding) สำเร็จ!");



        } catch (Exception e) {

            System.err.println("เกิดข้อผิดพลาดระหว่างการ Seeding ข้อมูล: " + e.getMessage());

            e.printStackTrace(); // <--- (เพิ่มบรรทัดนี้)

        }

    }

}