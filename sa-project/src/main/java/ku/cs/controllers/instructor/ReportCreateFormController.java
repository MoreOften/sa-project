package ku.cs.controllers.instructor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import ku.cs.models.instructor.Instructor;
import ku.cs.services.FXRouter;

import java.io.IOException;

public class ReportCreateFormController {
    @FXML
    Label instructorNameLabel;
    @FXML Label trainingProgramLabel;
    @FXML Label errorLabel;
    @FXML ImageView logoImageView;

    public void initialize() {
        errorLabel.setVisible(false);
        instructorNameLabel.setText("A");
        trainingProgramLabel.setText("Random ahh");
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

    public void onCreateButtonClick() {

    }

    public void onLogoutButtonClick() {
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
