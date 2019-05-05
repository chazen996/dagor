public class User {
    private String username;
    private int userId;

    public int getUserId() {
        return userId;
    }

    public User(String username, int userId) {
        this.username = username;
        this.userId = userId;
    }
}
