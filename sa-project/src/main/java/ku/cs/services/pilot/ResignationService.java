package ku.cs.services.pilot;

import ku.cs.models.notification.Notification;
import ku.cs.models.schedule.Schedule;
import ku.cs.services.email.EmailService;
import ku.cs.services.notification.NotificationRepository;
import ku.cs.services.schedule.ScheduleRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ResignationService {

    private final PilotRepository pilotRepository;
    private final ScheduleRepository scheduleRepository;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    // ผู้ส่ง Notification คือ Supervisor Admin
    private static final String SYSTEM_SENDER_ID = "supervisor_admin";

    public ResignationService(PilotRepository pilotRepository, ScheduleRepository scheduleRepository,
                              EmailService emailService, NotificationRepository notificationRepository) {
        this.pilotRepository = pilotRepository;
        this.scheduleRepository = scheduleRepository;
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
    }

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
     * เมธอดสำหรับสร้างและบันทึก In-App Notification
     */
    private void notifyViaInAppNotification(String pilotID, List<Schedule> cancelledSchedules) {

        String subject = String.format("[ด่วน!] นักบิน ID: %s ลาออก", pilotID);
        String content = createNotificationContent(pilotID, cancelledSchedules);
        String type = "Resignation";

        // รวบรวมผู้รับ (Instructor และ Supervisor)
        List<String> recipients = cancelledSchedules.stream()
                .flatMap(s -> List.of(s.getInstructorId(), s.getSupervisorId()).stream())
                .distinct()
                .collect(Collectors.toList());

        for (String recipientUsername : recipients) {
            // Notification(recipientId, senderId, subject, content, type)
            Notification notification = new Notification(
                    recipientUsername,
                    SYSTEM_SENDER_ID,
                    subject,
                    content,
                    type
            );
            notificationRepository.save(notification);
        }

        System.out.printf("-> [NotificationService] ส่ง In-App Notification ไปยังผู้เกี่ยวข้อง %d คนแล้ว%n", recipients.size());
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