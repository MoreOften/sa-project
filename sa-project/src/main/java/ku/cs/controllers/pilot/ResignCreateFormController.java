package ku.cs.controllers.pilot;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import ku.cs.services.FXRouter;

import java.io.IOException;
import java.time.LocalDate;

// ชื่อคลาสนี้ต้องตรงกับ fx:controller ใน FXML
public class ResignCreateFormController {

    // --- Sidebar Menu ---
    @FXML private Button homepageButton;
    @FXML private Button scheduleButton;
    @FXML private Button reportButton;
    @FXML private Button resignButton;
    @FXML private Button logoutButton;

    // --- Resignation Form Fields ---
    @FXML private DatePicker lastDayPicker;
    @FXML private TextArea reasonTextArea;
    @FXML private CheckBox confirmCheckBox;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;

    /**
     * เมธอดนี้จะถูกเรียกอัตโนมัติหลังจาก FXML โหลดเสร็จ
     * ใช้สำหรับตั้งค่าเริ่มต้นต่างๆ
     */
    @FXML
    public void initialize() {
        // เริ่มต้นโดยการปิดปุ่ม submit ไว้ก่อน
        submitButton.setDisable(true);

        // เพิ่ม Listener ให้กับ CheckBox
        // เมื่อ CheckBox ถูกติ๊ก (newVal = true) ให้เปิดปุ่ม (setDisable(false))
        // เมื่อ CheckBox ไม่ถูกติ๊ก (newVal = false) ให้ปิดปุ่ม (setDisable(true))
        confirmCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            submitButton.setDisable(!newVal);
        });

        System.out.println("PilotResignPageController initialized.");
    }

    // --- Sidebar Handlers ---

    @FXML
    void handleHomepageButton(ActionEvent event) {
        System.out.println("Navigating to Homepage...");
        // ใส่ logic การเปลี่ยนหน้าไป Homepage ที่นี่
        // เช่น ku.cs.services.FXRouter.goTo("pilot_homepage");
    }

    @FXML
    void handleScheduleButton(ActionEvent event) {
        System.out.println("Navigating to Schedule...");
        // ใส่ logic การเปลี่ยนหน้าไป Schedule
    }

    @FXML
    void handleReportButton(ActionEvent event) {
        System.out.println("Navigating to Report...");
        // ใส่ logic การเปลี่ยนหน้าไป Report
    }

    @FXML
    void handleResignButton(ActionEvent event) {
        // ไม่ต้องทำอะไร เพราะอยู่ที่หน้านี้แล้ว
        System.out.println("Already on Resign Page.");
    }

    @FXML
    void handleLogoutButton(ActionEvent event) {
        System.out.println("Logging out...");
        // ใส่ logic การ Logout และกลับไปหน้า Login
    }


    // --- Form Handlers ---

    /**
     * จัดการการกดยกเลิก
     * ล้างข้อมูลในฟอร์มทั้งหมด
     */
    @FXML
    void handleCancelButton(ActionEvent event) {
        lastDayPicker.setValue(null);
        reasonTextArea.clear();
        confirmCheckBox.setSelected(false);
        System.out.println("Form cleared.");
    }

    /**
     * จัดการการกดยื่นเรื่องลาออก
     * ตรวจสอบข้อมูลและประมวลผล
     */
    @FXML
    void handleSubmitButton(ActionEvent event) {
        // 1. ดึงข้อมูลจากฟอร์ม
        LocalDate lastDay = lastDayPicker.getValue();
        String reason = reasonTextArea.getText().trim();
        boolean isConfirmed = confirmCheckBox.isSelected(); // ควรเป็น true เสมอ เพราะปุ่มถูก disable ไว้

        // 2. ตรวจสอบความถูกต้อง (Validation)
        if (lastDay == null) {
            showAlert(AlertType.ERROR, "ข้อมูลไม่ครบถ้วน", "โปรดเลือกวันทำงานวันสุดท้าย");
            return;
        }

        if (reason.isEmpty()) {
            showAlert(AlertType.ERROR, "ข้อมูลไม่ครบถ้วน", "โปรดระบุเหตุผลในการลาออก");
            return;
        }

        // (การตรวจสอบ isConfirmed ไม่จำเป็นมากนัก เพราะปุ่มถูกคุมโดย CheckBox แล้ว)

        // 3. ประมวลผลข้อมูล
        // ณ จุดนี้ คุณสามารถส่งข้อมูล (lastDay, reason) ไปยัง Service/Datasource
        // เพื่อบันทึกลงไฟล์ หรือ Database
        System.out.println("--- ยื่นเรื่องลาออกสำเร็จ ---");
        System.out.println("วันทำงานวันสุดท้าย: " + lastDay.toString());
        System.out.println("เหตุผล: " + reason);
        System.out.println("--------------------------");

        // 4. แจ้งเตือนผู้ใช้ว่าสำเร็จ
        showAlert(AlertType.INFORMATION, "สำเร็จ", "ยื่นเรื่องลาออกของคุณเรียบร้อยแล้ว");

        // 5. (ทางเลือก) เมื่อสำเร็จ อาจจะเคลียร์ฟอร์ม หรือย้ายไปหน้าอื่น
        handleCancelButton(event); // เคลียร์ฟอร์ม
        // หรือย้ายกลับไปหน้า Homepage
        // handleHomepageButton(event);
    }


    /**
     * Helper method สำหรับแสดง Alert Box
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // ไม่แสดง Header
        alert.setContentText(message);
        alert.showAndWait();
    }

//    @FXML
//    public void handleHomepageButton() {
//        // ไม่ต้องทำอะไร เพราะนี่คือหน้า Homepage อยู่แล้ว
//        // หรือจะให้ refresh ข้อมูลก็ได้
//    }
//
//    @FXML
//    public void handleScheduleButton() { // แก้ชื่อจาก onScheduleButtonClick
//        try {
//            // แก้ปลายทางเป็นหน้าของ pilot
//            FXRouter.goTo("pilot-schedule-page", currentPilot);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @FXML
//    public void handleReportButton() { // แก้ชื่อจาก onReportButtonClick
//        try {
//            // แก้ปลายทางเป็นหน้าของ pilot
//            FXRouter.goTo("pilot-report-page", currentPilot);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    // (เพิ่มปุ่ม Resign ที่เห็นใน FXML Screenshot ของคุณ)
//    @FXML
//    public void handleResignButton() {
//        try {
//            FXRouter.goTo("pilot-resign-page", currentPilot);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    @FXML
//    public void handleLogoutButton() { // แก้ชื่อจาก onLogoutButtonClick
//        try {
//            FXRouter.goTo("login");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}