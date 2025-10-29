package ku.cs.models.user;

//import ku.cs.services.Filterer;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class UserList {
    private ArrayList<User> users;

    public UserList() {
        users = new ArrayList<>();
    }

    public User findUserByUsername(String username) {
        for (User user : users) {
            if (user.isUsername(username)) {
                return user;
            }
        }
        return null;
    }

    public void addUser(String username, String password, String role, String name, boolean isPasswordHashed) {
        User exist = findUserByUsername(username);
        if (exist == null) {
            users.add(new User(username, password, role, name, isPasswordHashed));
        }
    }


    public void addUser(String username, String password, String role, String name, String profilePicture, LocalDateTime lastLogin, boolean isPasswordHashed, boolean hasAccess) {
        User exist = findUserByUsername(username);
        if (exist == null) {
            users.add(new User(username, password, role, name, profilePicture ,lastLogin, isPasswordHashed, hasAccess));
        }
    }


    public User login(String username, String password) {
        User user = findUserByUsername(username);
        if (user != null) {
            if (user.validatePassword(password) && user.getUsername().equals(username)) {
                user.setLastLogin();
                return user;
            }
        }
        return null;
    }

//    public UserList filterUsers(Filterer<User> filterer) {
//        UserList filteredUserList = new UserList();
//        for (User user : users) {
//            if (filterer.filter(user)) {
//                filteredUserList.addUser(user.getUsername(), user.getPassword(), user.getRole(), user.getName(), user.getProfilePicture(), user.getLastLogin(),false, true);
//            }
//        }
//        return filteredUserList;
//    }

    public ArrayList<User> getUsers() { return users; }

}
