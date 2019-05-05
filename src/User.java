public class User {
    private String username;

    public int getUserId(){
        return username.hashCode();
    }

    public User(String username) {
        this.username = username;
    }
}
