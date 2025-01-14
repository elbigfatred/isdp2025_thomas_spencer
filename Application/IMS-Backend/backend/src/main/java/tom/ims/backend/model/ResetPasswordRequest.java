package tom.ims.backend.model;

public class ResetPasswordRequest {
    private String username;
    private String password;

    // Constructors
    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Optional: Override toString for debugging
    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "username='" + username + '\'' +
                ", password='[HIDDEN]'" +
                '}';
    }
}
