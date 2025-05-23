package yemekhanetakip.user;

public class User extends UserBase implements UserActions {

    // Current user that logged in
    public static User current;

    public User(){
        super();
    }

    public User(int id, String fullName, String email, String username) {
        super(id, fullName, email, username);
    }

    @Override
    public void login(int id, String fullName, String email, String username) {
        // If there is already a user logged in throw an exception
        if (current != null) {
            throw new IllegalStateException("A user is already logged in.");
        }

        // Otherwise set the current user to the new user
        current = new User(id, fullName, email, username);
    }

    @Override
    public void logout() {
        // If there is no user logged in throw an exception
        if (current == null) {
            throw new IllegalStateException("No user is currently logged in.");
        }

        // Otherwise set the current user to null
        current = null;
    }

    @Override
    public void DisplayInfo() {
        System.out.println("User{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", username='" + getUsername() + '\'' +
                '}');
    }
}