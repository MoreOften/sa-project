package ku.cs.models.user;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    private String username;
    private String password;
    private String name;
    private String role;
    private String phone;
    private String email;
    private String profilePicture;
    private LocalDateTime lastLogin;
    private boolean hasAccess;

    //----------Constructor----------

    public User(String name) {
        this.name = name;
        this.profilePicture = "default-user-photo.png";
        this.hasAccess = true;
    }


    public User(String username, String password) {
        this.username = username;
        setPassword(password);
        this.profilePicture = "default-user-photo.png";
        this.hasAccess = true;
    }


//    public User(String username, String password, String role, String name) {
//        this.username = username;
//        setPassword(password);
//        this.role = role;
//        this.name = name;
//        this.profilePicture = "default-user-photo.png";
//        this.hasAccess = true;
//    }

    public User(String name, String email, String username, String password) {
        this.username = username;
        setPassword(password);
        this.name = name;
        this.email = email;
        this.profilePicture = "default-user-photo.png";
        this.hasAccess = true;
    }


    public User(String username, String password, String role, String name, boolean isPasswordHashed) {
        this.username = username;
        if (isPasswordHashed) {
            this.password = password;
        } else {
            setPassword(password);
        }
        this.role = role;
        this.name = name;
        this.profilePicture = "default-user-photo.png";
        this.lastLogin = LocalDateTime.now();
        this.hasAccess = true;
    }


    public User(String username, String password, String role, String name, String profilePicture, LocalDateTime lastLogin, boolean isPasswordHashed, boolean hasAccess) {
        this.username = username;
        if (isPasswordHashed) {
            this.password = password;
        } else {
            setPassword(password);
        }
        this.role = role;
        this.name = name;
        this.profilePicture = profilePicture;
        this.lastLogin = lastLogin;
        this.hasAccess = hasAccess;
    }


    public User() {
        // ตั้งค่าเริ่มต้นสำหรับ object ที่สร้างแบบว่างๆ
        this.profilePicture = "default-user-photo.png";
        this.hasAccess = true;
    }


    //----------Method----------

    public boolean isUsername(String username) {
        return this.username.equals(username);
    }

    public boolean login(String username, String password) {
        if (this.validatePassword(password) && this.getUsername().equals(username)) {
            return true;
        }
        return false;
    }

    public boolean validatePassword(String password) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), this.password);
        return result.verified;
    }

    public boolean changePassword(String oldPassword, String newPassword, String confirmPassword) {
        if (this.validatePassword(oldPassword) && newPassword.equals(confirmPassword)) {
            return true;
        }
        return false;
    }


    //----------Setter----------

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public void setHashedPassword(String hashedPassword) {
        this.password = hashedPassword;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setLastLogin(LocalDateTime parse) {
        this.lastLogin = LocalDateTime.now();
    }

    public void setLastLogin() { this.lastLogin = LocalDateTime.now(); }

    public void setHasAccess(boolean hasAccess) {
        this.hasAccess = hasAccess;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    //----------Getter----------

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public String getLastLoginFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return lastLogin.format(formatter);
    }

    public String getRole() {
        return role;
    }

    public boolean getHasAccess() {
        return hasAccess;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

}
