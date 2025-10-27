package ku.cs.services.user;

import ku.cs.models.user.User;
import ku.cs.services.Filterer;

public class UserListByRoleFilterer implements Filterer<User> {
    String role;
    public UserListByRoleFilterer(String role) {
        this.role = role;
    }


    @Override
    public boolean filter(User user) {
        return user.getRole().equals(role);
    }

}
