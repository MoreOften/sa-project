package ku.cs.controllers.pilot;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
// --- เพิ่ม import ที่จำเป็น ---
import ku.cs.models.pilot.Pilot;
import ku.cs.models.user.User;
import ku.cs.services.FXRouter;
import ku.cs.services.pilot.PilotRepository;

import java.io.IOException;

public class PilotMainPageController {
    @FXML Label nameLabel;
    @FXML Label emailLabel;
    @FXML Label idLabel;
    @FXML Label roleLabel;
    @FXML Label typeLabel;

    @FXML ImageView instructorImageView;
    @FXML ImageView logoImageView;

    private Pilot currentPilot;
    private PilotRepository pilotRepository;

    @FXML
    public void initialize() {
        pilotRepository = new PilotRepository();

        // **UNIFIED DATA LOGIC FIX:** Prioritize checking for Pilot object
        Object data = FXRouter.getData();

        if (data instanceof Pilot) {
            // Case 1: Pilot object passed directly (from another sidebar button)
            this.currentPilot = (Pilot) data;
        } else if (data instanceof User) {
            // Case 2: Base User object passed (likely from initial login) - fetch full profile
            User user = (User) data;
            this.currentPilot = pilotRepository.findPilotByUsername(user.getUsername());
        }

        if (this.currentPilot != null) {
            showPilotData();
        } else {
            clearLabel();
            // Since this is the main page, we use nameLabel (assuming fx:id="nameLabel" in its FXML)
            nameLabel.setText("Error: Cannot get user data.");
        }
    }

    private void showPilotData() {
        nameLabel.setText(currentPilot.getName());
        emailLabel.setText(currentPilot.getEmail());
        idLabel.setText(currentPilot.getPilotID());
        roleLabel.setText(currentPilot.getRole());
        typeLabel.setText(currentPilot.getPilotType());
    }

    public void clearLabel() {
        nameLabel.setText("");
        emailLabel.setText("");
        idLabel.setText("");
        roleLabel.setText("");
        typeLabel.setText("");
    }

    // --- Sidebar Handlers ---

    @FXML
    public void handleHomepageButton() {
        // On the main page, just refresh data
        showPilotData();
    }

    @FXML
    public void handleScheduleButton() {
        try {
            // **Crucial:** Always pass the currentPilot object
            FXRouter.goTo("pilot-schedule-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReportButton() {
        try {
            // **Crucial:** Always pass the currentPilot object
            FXRouter.goTo("pilot-report-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleResignButton() {
        try {
            // **Crucial:** Always pass the currentPilot object
            FXRouter.goTo("pilot-resign-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleNotificationButton() {
        try {
            // **Crucial:** Always pass the currentPilot object
            FXRouter.goTo("pilot-notification-page", currentPilot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleLogoutButton() {
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}