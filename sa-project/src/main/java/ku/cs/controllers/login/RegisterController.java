package ku.cs.controllers.login;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ku.cs.models.student.Student;
import ku.cs.models.student.StudentList;
import ku.cs.models.user.User;
import ku.cs.models.user.UserList;
import ku.cs.services.Datasource;
import ku.cs.services.FXRouter;
import ku.cs.services.student.StudentListFileDatasource;
import ku.cs.services.user.UserListFileDatasource;

import java.io.IOException;

public class RegisterController {
    @FXML Label errorLabel;

    @FXML TextField giveUsernameTextField;
    @FXML TextField giveNameTextField;
    @FXML TextField givePasswordTextField;
    @FXML TextField giveConfirmPasswordTextField;
    @FXML TextField giveIdTextField;
    @FXML TextField giveEmailTextField;

    private Datasource<UserList> userListDatasource;
    private UserList userList;
    private User user;

    private Datasource<StudentList> studentListDatasource;
    private StudentList studentList;
    private Student student;

    @FXML
    private void initialize() {
        errorLabel.setText("");

        userListDatasource = new UserListFileDatasource("data", "user-list.csv");
        userList = userListDatasource.readData();

        studentListDatasource = new StudentListFileDatasource("data", "student-list.csv");
        studentList = studentListDatasource.readData();


    }


    @FXML
    public void onSignUpButtonClick() {
        try {
            String username = giveUsernameTextField.getText();
            String name = giveNameTextField.getText();
            String password = givePasswordTextField.getText();
            String confirmPassword = giveConfirmPasswordTextField.getText();
            String id = giveIdTextField.getText();
            String email = giveEmailTextField.getText();

            student = studentList.register(username, password, confirmPassword, id, name, email);

            if (student != null) {
                student.setUsername(username);
                student.setPassword(password);
                student.setRegistered(true);
                studentListDatasource.writeData(studentList);

                userList.addUser(username, password, "student", name, false);
                userListDatasource.writeData(userList);
                user = userList.findUserByUsername(username);



                FXRouter.goTo("student-page", user);
            }
            else {
                errorLabel.setText("student not found");
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    public void onCancelButtonClick() {
        try {
            FXRouter.goTo("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
