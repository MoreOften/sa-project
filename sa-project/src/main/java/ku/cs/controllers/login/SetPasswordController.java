package ku.cs.controllers.login;

import javafx.fxml.FXML;
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
import ku.cs.services.department.DepartmentStaffListFileDatasource;
import ku.cs.services.faculty.FacultyStaffListFileDatasource;
import ku.cs.services.professor.ProfessorListFileDatasource;
import ku.cs.services.user.UserListFileDatasource;

import java.io.IOException;

public class SetPasswordController {
    @FXML private TextField givePasswordTextField;
    @FXML private TextField giveConfirmPasswordTextField;
    @FXML private Label errorLabel;

    User user = (User) FXRouter.getData();
    String role = user.getRole();
    private User realUser;

    private UserList userlist;
    private Datasource<UserList> userlistDatasource;

    private PilotList pilotList;
    private Datasource<PilotList> pilotListDatasource;
    private Pilot pilot;

    private InstructorList instructorList;
    private Datasource<InstructorList> instructorListDatasource;
    private Instructor instructor;

    private SupervisorList supervisorList;
    private Datasource<SupervisorList> supervisorListDatasource;
    private Supervisor supervisor;


    @FXML
    public void initialize() {
        errorLabel.setText("");
        userlistDatasource = new UserListFileDatasource("data", "user-list.csv");
        userlist = userlistDatasource.readData();
        realUser = userlist.findUserByUsername(user.getUsername());

        if (role.equals("pilot")) {
            pilotListDatasource = new ProfessorListFileDatasource("data", "pilot-list.csv");
            pilotList = pilotListDatasource.readData();
            pilot = pilotList.findPilotByUsername(realUser.getUsername());
        }
        else if (role.equals("instructor")) {
            instructorListDatasource = new InstructorListFileDatasource("data", "Instructor-list.csv");
            instructorList = instructorListDatasource.readData();
            instructor = instructorList.findInstructorByUsername(realUser.getUsername());
        }
        else if (role.equals("supervisor")) {
            supervisorListDatasource = new SupervisorListFileDatasource("data", "supervisor-list.csv");
            supervisorList = supervisorListDatasource.readData();
            supervisor = supervisorList.findSupervisorByUsername(realUser.getUsername());
        }

    }


    @FXML
    public void onConfirmButtonClick() {
        try {

            String passwordText = givePasswordTextField.getText();
            String confirmPasswordText = giveConfirmPasswordTextField.getText();
            if (passwordText.equals(confirmPasswordText)) {
                realUser.setPassword(passwordText);
                userlistDatasource.writeData(userlist);

                if (role.equals("pilot")) {
                    pilq.setPassword(passwordText);
                    pilot.setFirstTimeLogin(false);
                    pilotListDatasource.writeData(pilotList);

                    FXRouter.goTo("home-pilot", realUser);
                }
                else if (role.equals("instructor")) {
                    instructor.setPassword(passwordText);
                    instructor.setFirstTimeLogin(false);
                    instructorListDatasource.writeData(instructorList);
                    FXRouter.goTo("home-instructor", realUser);
                }
                else if (role.equals("supervisor")) {
                    supervisor.setPassword(passwordText);
                    supervisor.setFirstTimeLogin(false);
                    supervisorListDatasource.writeData(supervisorList);
                    FXRouter.goTo("home-supervisor", realUser);
                }

            }

            else {
                errorLabel.setText("Passwords do not match");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    public void onBackButtonClick() {
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
