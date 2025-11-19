package ku.cs.services.email;

import ku.cs.models.schedule.Schedule;
import ku.cs.services.instructor.InstructorRepository;
import ku.cs.services.supervisor.SupervisorRepository; // ยังคง import ไว้

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class EmailService {

    // --- (1. CONFIGURATION: กรุณาเปลี่ยนค่าเหล่านี้) ---
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SENDER_EMAIL = "your.system.email@example.com";
    private static final String SENDER_PASSWORD = "YOUR_APP_PASSWORD_OR_SMTP_PASSWORD";
    // ----------------------------------------------------

    private final InstructorRepository instructorRepository;
    private final SupervisorRepository supervisorRepository; // ยังคง field ไว้

    public EmailService(InstructorRepository instructorRepository, SupervisorRepository supervisorRepository) {
        this.instructorRepository = instructorRepository;
        this.supervisorRepository = supervisorRepository;
    }

    /**
     * Use Case Step 6: ระบบแจ้งผู้เกี่ยวข้อง (แจ้งเฉพาะ Instructor) ผ่าน Email
     */
    public void notifyResignation(String pilotID, List<Schedule> cancelledSchedules) {

        // 1. กำหนดผู้รับอีเมล: รวบรวมเฉพาะอีเมลของ Instructor
        Set<String> recipientEmails = cancelledSchedules.stream()
                .map(s -> instructorRepository.getEmailById(s.getInstructorId()))
                .collect(Collectors.toSet());

        // **ส่วนที่ถูกลบ/คอมเมนต์:** ไม่รวม Supervisor ในการแจ้งเตือนชั่วคราว
        /*
        cancelledSchedules.stream()
                .map(s -> supervisorRepository.getEmailById(s.getSupervisorId()))
                .forEach(recipientEmails::add);
        */

        // กรองอีเมลที่เป็น null ออก
        recipientEmails.remove(null);

        if (recipientEmails.isEmpty()) {
            System.err.println("-> [Step 6] ERROR: ไม่พบอีเมล Instructor ผู้รับที่เกี่ยวข้อง. ไม่มีการส่งอีเมล.");
            return;
        }

        // 2. เตรียมเนื้อหาอีเมล
        String subject = String.format("[ด่วน!] แจ้งเตือนการลาออกของนักบิน ID: %s", pilotID);
        String body = createEmailBody(pilotID, cancelledSchedules);

        // 3. เริ่มต้นกระบวนการส่งอีเมล
        try {
            sendActualEmail(recipientEmails, subject, body);
            System.out.println("-> [Step 6] SUCCESS: ส่งอีเมลแจ้งเตือนการลาออกไปยัง Instructor ที่เกี่ยวข้องแล้ว.");
        } catch (MessagingException e) {
            System.err.println("-> [Step 6] ERROR: การส่งอีเมลล้มเหลว. โปรดตรวจสอบการตั้งค่า SMTP: " + e.getMessage());
        }
    }

    /**
     * ฟังก์ชันสร้างเนื้อหาอีเมลเป็น HTML/Text (ไม่มีการเปลี่ยนแปลง)
     */
    private String createEmailBody(String pilotID, List<Schedule> cancelledSchedules) {
        StringBuilder sb = new StringBuilder();
        sb.append("เรียน ผู้เกี่ยวข้อง\n\n");
        sb.append(String.format("นักบิน ID **%s** ได้ยื่นเรื่องขอลาออกและถูกดำเนินการเรียบร้อยแล้ว\n\n", pilotID));

        if (!cancelledSchedules.isEmpty()) {
            sb.append(String.format("เนื่องจากการลาออก ตารางฝึกที่ยังไม่เสร็จสิ้นจำนวน **%d** รายการ ถูกยกเลิกโดยอัตโนมัติ:\n", cancelledSchedules.size()));
            sb.append("---------------------------------------------------\n");

            for (Schedule s : cancelledSchedules) {
                sb.append(String.format("- Schedule ID: %s\n", s.getScheduleId()));
                sb.append(String.format("  - Program: %s\n", s.getPracticeProgram()));
                sb.append(String.format("  - Instructor: %s\n", s.getInstructorId()));
                sb.append(String.format("  - Supervisor: %s\n", s.getSupervisorId()));
                sb.append("\n");
            }
            sb.append("---------------------------------------------------\n");
            sb.append("กรุณาดำเนินการจัดตารางฝึกใหม่สำหรับนักบินคนอื่นๆ ที่เกี่ยวข้องโดยเร็วที่สุด");
        } else {
            sb.append("ไม่มีตารางฝึกที่ต้องยกเลิกเนื่องจากการลาออกในครั้งนี้");
        }
        sb.append("\n\nขอแสดงความนับถือ,\nระบบจัดการตารางฝึก");
        return sb.toString();
    }

    /**
     * ฟังก์ชันหลักในการเชื่อมต่อและส่งอีเมลจริง (ไม่มีการเปลี่ยนแปลง)
     */
    private void sendActualEmail(Set<String> recipientEmails, String subject, String body) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // TLS
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));

        // เพิ่มผู้รับทั้งหมด
        Address[] recipients = new Address[recipientEmails.size()];
        int i = 0;
        for (String email : recipientEmails) {
            recipients[i++] = new InternetAddress(email);
        }
        message.setRecipients(Message.RecipientType.TO, recipients);

        message.setSubject(subject);
        message.setText(body); // สามารถเปลี่ยนเป็น setContent(body, "text/html") หากต้องการส่งเป็น HTML

        Transport.send(message);
    }


}