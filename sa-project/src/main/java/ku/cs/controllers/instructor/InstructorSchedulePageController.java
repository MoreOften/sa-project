package ku.cs.controllers.instructor;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import ku.cs.models.instructor.Instructor;
import ku.cs.models.schedule.Schedule;
import ku.cs.services.FXRouter;

import java.io.IOException;

public class InstructorSchedulePageController {
    @FXML
    TableView<Schedule> scheduleTableView;
    @FXML ImageView logoImageView;

    public void initialize() {
        Instructor instructor = new Instructor("A", "a@gmail.com", "12345678", "I0001", "0123456789", "08888888");
    }

    public void onHomepageButtonClick() {
        try {
            FXRouter.goTo("instructor-main-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onScheduleButtonClick() {
        try {
            FXRouter.goTo("instructor-schedule-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onReportButtonClick() {
        try {
            FXRouter.goTo("instructor-report-page");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onLogoutButtonClick() {

    }
}
