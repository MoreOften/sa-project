package ku.cs.controllers.instructor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import ku.cs.models.instructor.Instructor;

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
        Instructor instructor = new Instructor("A", "a@gmail.com", "12345678", "I0001", "0123456789");
        nameLabel.setText(instructor.getName());
        emailLabel.setText(instructor.getEmail());
        idLabel.setText(instructor.getInstructorID());
        roleLabel.setText("Instructor");
    }

    public void clearLabel() {
        nameLabel.setText("");
        emailLabel.setText("");
        idLabel.setText("");
        roleLabel.setText("");
    }

    public void onHomepageButtonClick() {

    }

    public void onScheduleButtonClick() {

    }

    public void onReportButtonClick() {

    }

    public void onLogoutButtonClick() {

    }
}
