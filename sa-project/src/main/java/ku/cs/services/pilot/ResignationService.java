package ku.cs.services.pilot;

import ku.cs.models.notification.Notification;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.instructor.Instructor; // เพิ่ม Import
import ku.cs.models.supervisor.Supervisor; // เพิ่ม Import
import ku.cs.services.email.EmailService;
import ku.cs.services.notification.NotificationRepository;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.instructor.InstructorRepository; // เพิ่ม Import
import ku.cs.services.supervisor.SupervisorRepository; // เพิ่ม Import

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResignationService {

    private final PilotRepository pilotRepository;
    private final ScheduleRepository scheduleRepository;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final InstructorRepository instructorRepository; // เพิ่ม Field
    private final SupervisorRepository supervisorRepository; // เพิ่ม Field

    // ผู้ส่ง Notification คือ Supervisor Admin
    private static final String SYSTEM_SENDER_ID = "supervisor_admin";

    public ResignationService(PilotRepository pilotRepository, ScheduleRepository scheduleRepository,
                              EmailService emailService, NotificationRepository notificationRepository,
                              InstructorRepository instructorRepository, SupervisorRepository supervisorRepository) { // แก้ไข Constructor
        this.pilotRepository = pilotRepository;
        this.scheduleRepository = scheduleRepository;
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.instructorRepository = instructorRepository; // Initialize
        this.supervisorRepository = supervisorRepository; // Initialize
    }

    // (หมายเหตุ: PilotResignPageController.java ต้องถูกแก้ไขให้ส่ง InstructorRepository และ SupervisorRepository มาด้วย)

    /**
     * Use Case 7: ลาออก (Resignation Process)
     */
    public boolean processResignation(String pilotID) {
        System.out.println("-> [ResignationService] เริ่มต้นกระบวนการลาออกสำหรับ Pilot ID: " + pilotID);

        // 4. & 5. ตรวจสอบและยกเลิกตารางที่เกี่ยวข้อง
        List<Schedule> cancelledSchedules = scheduleRepository.cancelIncompleteSchedulesForPilot(pilotID);

        // 6. ระบบแจ้งผู้เกี่ยวข้องผ่าน Email และ Notification
        if (!cancelledSchedules.isEmpty()) {
            emailService.notifyResignation(pilotID, cancelledSchedules);
            notifyViaInAppNotification(pilotID, cancelledSchedules);
        } else {
            System.out.println("-> [ResignationService] ไม่มีตารางฝึกที่ต้องยกเลิก");
        }

        // 7. ระบบ เปลี่ยนสถานะ Pilot เป็น 'ลาออก'
        boolean updateSuccess = pilotRepository.updatePilotToResigned(pilotID);

        if (updateSuccess) {
            System.out.println("-> [ResignationService] กระบวนการลาออกเสร็จสมบูรณ์");
            return true;
        } else {
            System.err.println("-> [ResignationService] ล้มเหลวในการอัปเดตสถานะ Pilot ในฐานข้อมูล");
            return false;
        }
    }

    /**
     * เมธอดสำหรับสร้างและบันทึก In-App Notification (FIXED: แก้ไขการใช้ Username แทน ID)
     */
    private void notifyViaInAppNotification(String pilotID, List<Schedule> cancelledSchedules) {

        String subject = String.format("[ด่วน!] นักบิน ID: %s ลาออก", pilotID);
        String content = createNotificationContent(pilotID, cancelledSchedules);
        String type = "Resignation";

        // รวบรวมผู้รับ (Instructor และ Supervisor) - ต้องแปลง ID (I001, S001) เป็น USERNAME
        Set<String> recipientUsernames = cancelledSchedules.stream()
                .flatMap(s -> {
                    String instructorId = s.getInstructorId();
                    String supervisorId = s.getSupervisorId();

                    // 1. Resolve Instructor ID to Username
                    Instructor instructor = instructorRepository.findInstructorById(instructorId);
                    // ใช้ Username ถ้าพบ, ถ้าไม่พบใช้ ID เดิม
                    String instructorUsername = (instructor != null && instructor.getUsername() != null) ? instructor.getUsername() : instructorId;

                    // 2. Resolve Supervisor ID to Username
                    Supervisor supervisor = supervisorRepository.findSupervisorById(supervisorId);
                    // ใช้ Username ถ้าพบ, ถ้าไม่พบใช้ ID เดิม
                    String supervisorUsername = (supervisor != null && supervisor.getUsername() != null) ? supervisor.getUsername() : supervisorId;

                    return List.of(instructorUsername, supervisorUsername).stream();
                })
                .distinct()
                .collect(Collectors.toSet());

        for (String recipientUsername : recipientUsernames) {
            // Notification(recipientId, senderId, subject, content, type)
            Notification notification = new Notification(
                    recipientUsername, // <--- ใช้ Username ที่แก้ไขแล้ว
                    SYSTEM_SENDER_ID,
                    subject,
                    content,
                    type
            );
            notificationRepository.save(notification);
        }

        System.out.printf("-> [NotificationService] ส่ง In-App Notification ไปยังผู้เกี่ยวข้อง %d คนแล้ว%n", recipientUsernames.size());
    }

    /**
     * Helper method เพื่อจัดรูปแบบข้อความสำหรับ Notification Content
     */
    private String createNotificationContent(String pilotID, List<Schedule> cancelledSchedules) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("นักบิน ID %s ได้ยื่นเรื่องขอลาออกและถูกดำเนินการเรียบร้อยแล้ว\n\n", pilotID));

        if (!cancelledSchedules.isEmpty()) {
            sb.append(String.format("ตารางฝึกที่ยังไม่เสร็จสิ้นจำนวน %d รายการ ถูกยกเลิกโดยอัตโนมัติ:\n", cancelledSchedules.size()));
            sb.append("---------------------------------------------------\n");

            for (Schedule s : cancelledSchedules) {
                sb.append(String.format("- Schedule ID: %s (Program: %s)\n", s.getScheduleId(), s.getPracticeProgram()));
                sb.append(String.format("  (Instr: %s, Sup: %s)\n", s.getInstructorId(), s.getSupervisorId()));
            }
            sb.append("---------------------------------------------------\n");
            sb.append("กรุณาดำเนินการจัดตารางฝึกใหม่โดยเร็วที่สุด");
        } else {
            sb.append("ไม่มีตารางฝึกที่ต้องยกเลิกเนื่องจากการลาออกในครั้งนี้");
        }
        return sb.toString();
    }
}