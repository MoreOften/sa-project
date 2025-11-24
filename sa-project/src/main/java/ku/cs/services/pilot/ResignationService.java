package ku.cs.services.pilot;

import ku.cs.models.notification.Notification;
import ku.cs.models.schedule.Schedule;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.pilot.Pilot;
import ku.cs.models.supervisor.Supervisor;
import ku.cs.services.email.EmailService;
import ku.cs.services.notification.NotificationRepository;
import ku.cs.services.schedule.ScheduleRepository;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.supervisor.SupervisorRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Use Case 7: Process Pilot Resignation
     * Logic:
     * 1. Update Pilot Status to 'resigned'.
     * 2. Find active schedules.
     * 3. Check the OTHER pilot in those schedules.
     * - If OTHER pilot is Active -> Keep schedule, Notify "Resignation Warning".
     * - If OTHER pilot is Resigned -> Cancel schedule, Notify "Schedule Cancelled".
     */
    public boolean processResignation(String pilotID) {
        // 1. Mark the pilot as resigned immediately in the DB
        boolean success = pilotRepository.updatePilotToResigned(pilotID);
        if (!success) return false;

        // 2. Find active schedules for this pilot (without cancelling them yet)
        List<Schedule> activeSchedules = scheduleRepository.findActiveSchedulesForPilot(pilotID);

        List<Schedule> schedulesToCancel = new ArrayList<>();
        List<Schedule> schedulesToWarn = new ArrayList<>();

        // 3. Logic: Check the OTHER pilot in the schedule
        for (Schedule schedule : activeSchedules) {
            // Determine ID of the other pilot
            String otherPilotId = schedule.getPilotId1().equals(pilotID)
                    ? schedule.getPilotId2()
                    : schedule.getPilotId1();

            // Fetch other pilot data
            Pilot otherPilot = pilotRepository.findPilotById(otherPilotId);

            // Check if the other pilot is ALSO resigned
            if (otherPilot != null && "resigned".equalsIgnoreCase(otherPilot.getPilotStatus())) {
                schedulesToCancel.add(schedule);
            } else {
                schedulesToWarn.add(schedule);
            }
        }

        // 4. Handle Schedules where BOTH pilots have resigned -> CANCEL
        if (!schedulesToCancel.isEmpty()) {
            for (Schedule s : schedulesToCancel) {
                scheduleRepository.updateScheduleStatus(s.getScheduleId(), "cancelled_resignation");
                s.setScheduleStatus("cancelled_resignation"); // Update object for display/email
            }

            // Notify about cancellation
            notifyRelevantUsers(schedulesToCancel, pilotID, "Schedule Cancelled", true);

            // (Optional) Send email via existing service
            // emailService.notifyResignation(pilotID, schedulesToCancel);
        }

        // 5. Handle Schedules where ONLY ONE pilot resigned -> NOTIFY ONLY (Do not cancel)
        if (!schedulesToWarn.isEmpty()) {
            // Notify about single resignation
            notifyRelevantUsers(schedulesToWarn, pilotID, "Pilot Resigned", false);
        }

        return true;
    }

    /**
     * Unified notification method to send in-app notifications to Instructors and Supervisors.
     * @param schedules List of affected schedules
     * @param pilotID The ID of the pilot who just resigned
     * @param title Title prefix for notification subject
     * @param isCancellation True if the schedule was cancelled, False if just a warning
     */
    private void notifyRelevantUsers(List<Schedule> schedules, String pilotID, String title, boolean isCancellation) {
        // Collect recipients (Instructors and Supervisors) from the affected schedules
        Set<String> recipients = schedules.stream()
                .flatMap(s -> {
                    Instructor instructor = instructorRepository.findInstructorById(s.getInstructorId());
                    String instructorUsername = (instructor != null) ? instructor.getUsername() : s.getInstructorId();

                    Supervisor supervisor = supervisorRepository.findSupervisorById(s.getSupervisorId());
                    String supervisorUsername = (supervisor != null) ? supervisor.getUsername() : s.getSupervisorId();

                    return Stream.of(instructorUsername, supervisorUsername);
                })
                .filter(username -> username != null)
                .collect(Collectors.toSet());

        // Create Message Content
        String subject = String.format("นักบิน ID: %s ลาออก", pilotID);
        StringBuilder contentBuilder = new StringBuilder();

        if (isCancellation) {
            contentBuilder.append(String.format("เนื่องจากนักบิน ID %s ลาออก ทำให้นักบินทั้ง 2 คนในตารางฝึกลาออกจึงทำการยกเลิกตารางฝึก\n", pilotID));
        } else {
            contentBuilder.append(String.format("นักบิน ID %s ลาออก\n", pilotID));
        }
        contentBuilder.append("---------------------------------------------------\n");

        for (Schedule s : schedules) {
            contentBuilder.append(String.format("- Schedule ID: %s \n", s.getScheduleId()));
        }

        String content = contentBuilder.toString();
        String type = isCancellation ? "Schedule_Cancelled" : "Resignation_Warning";

        // Save Notification to DB
        for (String recipient : recipients) {
            Notification notification = new Notification(
                    recipient,
                    SYSTEM_SENDER_ID,
                    subject,
                    content,
                    type
            );
            notificationRepository.save(notification);
        }
    }
}