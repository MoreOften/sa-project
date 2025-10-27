package ku.cs.services.user;

import ku.cs.models.user.User;

import java.time.LocalDateTime;
import java.util.Comparator;

public class UserListDescendingDateComparator implements Comparator<User> {

    @Override
    public int compare(User user1, User user2) {
        LocalDateTime date1 = user1.getLastLogin();
        LocalDateTime date2 = user2.getLastLogin();


        return date2.compareTo(date1);
    }

}
