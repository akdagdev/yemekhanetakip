package yemekhanetakip.user;

public abstract class UserBase {
    private int id;
    private String fullName;
    private String email;
    private String username;


    public UserBase() { }

    // Constructor
    public UserBase(int id, String fullName, String email, String username) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public abstract void DisplayInfo();
}
