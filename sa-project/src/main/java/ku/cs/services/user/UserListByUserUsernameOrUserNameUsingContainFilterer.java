package ku.cs.services.user;

import ku.cs.models.user.User;
import ku.cs.services.Filterer;

public class UserListByUserUsernameOrUserNameUsingContainFilterer implements Filterer<User> {
    private String text;
    public UserListByUserUsernameOrUserNameUsingContainFilterer(String text) {
        this.text = text;
    }

    @Override
    public boolean filter(User user) {
        return user.getUsername().contains(text) || user.getName().contains(text);
    }
}
