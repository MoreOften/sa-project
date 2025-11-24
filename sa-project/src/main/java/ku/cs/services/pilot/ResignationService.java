package ku.cs.services.pilot;

import ku.cs.models.notification.Notification;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.services.email.EmailService;
import ku.cs.services.notification.NotificationRepository;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.supervisor.SupervisorRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResignationService {

    private final PilotRepository pilotRepository;
    private final ScheduleRepository scheduleRepository;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final InstructorRepository instructorRepository;
    private final SupervisorRepository supervisorRepository;

    private static final String SYSTEM_SENDER_ID = "System_admin";

    public ResignationService(PilotRepository pilotRepository, ScheduleRepository scheduleRepository,
                              EmailService emailService, NotificationRepository notificationRepository,
                              InstructorRepository instructorRepository, SupervisorRepository supervisorRepository) {
        this.pilotRepository = pilotRepository;
        this.scheduleRepository = scheduleRepository;
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.instructorRepository = instructorRepository;
        this.supervisorRepository = supervisorRepository;
    }

    /**
     * Use Case 7: ประมวลผลการลาออกของนักบิน
     * @param pilotID รหัสนักบินที่ต้องการลาออก
     * @return true หากดำเนินการสำเร็จ
     */
    public boolean processResignation(String pilotID) {
        List<Schedule> cancelledSchedules = scheduleRepository.cancelIncompleteSchedulesForPilot(pilotID);

        if (!cancelledSchedules.isEmpty()) {
            emailService.notifyResignation(pilotID, cancelledSchedules);
            notifyViaInAppNotification(pilotID, cancelledSchedules);
        }

        return pilotRepository.updatePilotToResigned(pilotID);
    }

    /**
     * ส่งการแจ้งเตือนภายในระบบไปยัง Instructor และ Supervisor ที่เกี่ยวข้อง
     */
    private void notifyViaInAppNotification(String pilotID, List<Schedule> cancelledSchedules) {
        String subject = String.format("นักบิน ID: %s ลาออก", pilotID);
        String content = createNotificationContent(pilotID, cancelledSchedules);
        String type = "Resignation";

        Set<String> recipientUsernames = cancelledSchedules.stream()
                .flatMap(s -> {
                    Instructor instructor = instructorRepository.findInstructorById(s.getInstructorId());
                    String instructorUsername = (instructor != null && instructor.getUsername() != null)
                            ? instructor.getUsername() : s.getInstructorId();

                    Supervisor supervisor = supervisorRepository.findSupervisorById(s.getSupervisorId());
                    String supervisorUsername = (supervisor != null && supervisor.getUsername() != null)
                            ? supervisor.getUsername() : s.getSupervisorId();

                    return List.of(instructorUsername, supervisorUsername).stream();
                })
                .distinct()
                .collect(Collectors.toSet());

        for (String recipientUsername : recipientUsernames) {
            Notification notification = new Notification(
                    recipientUsername,
                    SYSTEM_SENDER_ID,
                    subject,
                    content,
                    type
            );
            notificationRepository.save(notification);
        }
    }

    /**
     * สร้างเนื้อหาของการแจ้งเตือน
     */
    private String createNotificationContent(String pilotID, List<Schedule> cancelledSchedules) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("นักบิน ID %s ได้ยื่นเรื่องขอลาออกและถูกดำเนินการเรียบร้อยแล้ว\n\n", pilotID));

        if (!cancelledSchedules.isEmpty()) {
            sb.append(String.format("ตารางฝึกที่ยังไม่เสร็จสิ้นจำนวน %d รายการ\n", cancelledSchedules.size()));
            sb.append("---------------------------------------------------\n");

            for (Schedule s : cancelledSchedules) {
                sb.append(String.format("- Schedule ID: %s (Program: %s)\n",
                        s.getScheduleId(), s.getPracticeProgram()));
            }
        } else {
            sb.append("ไม่มีตารางฝึกที่ยังไม่เสร็จสิ้น");
        }

        return sb.toString();
    }
}