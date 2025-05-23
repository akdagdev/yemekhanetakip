package yemekhanetakip.user;

public interface UserActions {
    void login(int id, String fullName, String email, String username);
    void logout();
}