package point.dot.carpoint;

//additional class for users. We used this class to add users data on Firebase database
public class User {

    public String email, username, phone;

    public User() {

    }

    public User(String email, String username, String phone) {
        this.email = email;
        this.username = username;
        this.phone = phone;
    }
}