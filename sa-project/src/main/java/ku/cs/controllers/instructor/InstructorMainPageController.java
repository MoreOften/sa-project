package ku.cs.controllers.instructor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import ku.cs.models.instructor.Instructor;
import ku.cs.services.FXRouter;

import java.io.IOException;

public class InstructorMainPageController {
    @FXML Label nameLabel;
    @FXML Label emailLabel;
    @FXML Label idLabel;
    @FXML Label roleLabel;
    @FXML
    ImageView instructorImageView;
    @FXML ImageView logoImageView;

    public void initialize() {
        clearLabel();
        nameLabel.setText("A");
        emailLabel.setText("a@email.com");
        idLabel.setText("0123456");
        roleLabel.setText("Instructor");
    }

    public void clearLabel() {
        nameLabel.setText("");
        emailLabel.setText("");
        idLabel.setText("");
        roleLabel.setText("");
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
