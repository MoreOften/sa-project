package ku.cs.controllers.login;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ku.cs.models.department.DepartmentStaff;
import ku.cs.models.department.DepartmentStaffList;
import ku.cs.models.faculty.FacultyStaff;
import ku.cs.models.faculty.FacultyStaffList;
import ku.cs.models.professor.Professor;
import ku.cs.models.professor.ProfessorList;
import ku.cs.models.user.User;
import ku.cs.models.user.UserList;
import ku.cs.services.Datasource;
import ku.cs.services.FXRouter;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ku.cs.services.department.DepartmentStaffListFileDatasource;
import ku.cs.services.faculty.FacultyStaffListFileDatasource;
import ku.cs.services.professor.ProfessorListFileDatasource;
import ku.cs.services.user.UserListFileDatasource;

import java.io.IOException;


public class LoginController {
    @FXML private TextField giveUsernameTextField;
    @FXML private TextField givePasswordTextField;
    @FXML private Label errorLabel;

    @FXML private ImageView loginLogoImageView;
    private UserList userList;
    private Datasource<UserList> userListDatasource;

    private PilotList pilotList;
    private Datasource<PilotList> pilotListDatasource;

    private InstructorList instructorList;
    private Datasource<InstructorList> instructorListDatasource;

    private SupervisorList supervisorList;
    private Datasource<SupervisorList> supervisorListDatasource;


    @FXML private void initialize() {
        errorLabel.setText("");
        Image image = new Image(getClass().getResource("/images/login-logo.png").toString());
        kuLogoImageView.setImage(image);

        userListDatasource = new UserListFileDatasource("data", "user-list.csv");
        userList = userListDatasource.readData();

        pilotListDatasource = new PilotListFileDatasource("data", "pilot-list.csv");
        pilotList = pilotListDatasource.readData();

        instructorListDatasource = new InstructorListFileDatasource("data", "instructor-list.csv");
        instructorList = instructorListDatasource.readData();

        supervisorListDatasource = new SupervisorListFileDatasource("data", "supervisor-list.csv");
        supervisorList = supervisorListDatasource.readData();

    }

    @FXML
    public void onLoginButtonClick() {
        try {
            String usernameText = giveUsernameTextField.getText();
            String passwordText = givePasswordTextField.getText();

            User user = userList.login(usernameText, passwordText);
            if (user != null && user.getHasAccess()) {
                handleLoginBasedOnRole(user);
            }
            else if (user != null && user.getHasAccess() == false) {
                errorLabel.setText("You are banned.");
            }
            else {
                errorLabel.setText("Wrong username or password");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void handleLoginBasedOnRole(User user) throws IOException {
        switch (user.getRole()) {
            case "pilot":
                handlePilotLogin(user);
                break;
            case "instructor":
                handleInstructorLogin(user);
                break;
            case "supervisor":
                handleSupervisorLogin(user);
                break;
            default:
                System.out.println(user.getRole());
                errorLabel.setText("Unknown role");
                break;
        }
    }


    private void handlePilotLogin(User user) throws IOException {
        Pilot pilot = pilotList.findPilotByUsername(user.getUsername());
        if (pilot.getFirstTimeLogin()) {
            pilotListDatasource.writeData(pilotList);
            FXRouter.goTo("set-password", user);
        } else {
            FXRouter.goTo("home-pilot", user);
        }
        userListDatasource.writeData(userList);
    }


    private void handleInstructorLogin(User user) throws IOException {
        Instructor instructor = instructorList.findInstructorByUsername(user.getUsername());
        if (instructor.getFirstTimeLogin()) {
            instructorListDatasource.writeData(instructorList);
            FXRouter.goTo("set-password", user);
        } else {
            FXRouter.goTo("home-instructor", user);
        }
        userListDatasource.writeData(userList);
    }


    private void handleSupervisorLogin(User user) throws IOException {
        Supervisor supervisor = supervisorList.findSupervisorByUsername(user.getUsername());
        if (supervisor.getFirstTimeLogin()) {
            supervisorListDatasource.writeData(supervisorList);
            FXRouter.goTo("set-password", user);
        } else {
            FXRouter.goTo("home-supervisor", user);
        }
        userListDatasource.writeData(userList);
    }


    @FXML
    public void onRegisterButtonClick() {
        try {
            FXRouter.goTo("register");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
