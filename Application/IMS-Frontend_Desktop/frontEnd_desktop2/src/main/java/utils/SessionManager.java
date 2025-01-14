package utils;

public class SessionManager {

    private static SessionManager instance; // Singleton instance

    //Constants
    private static final long MAX_SESSION_TIME = 30000; //10 sec in milliseconds

    private String username; // Store the current username
    private String location; // Example: user's current location
    private String[] roles;  // User roles or permissions
    private long lastActivityTime; // Tracks the last activity timestamp
    private String firstname;
    private String lastname;
    private String email;
    private String permissionLevel;
    private String siteName;

    // Private constructor to enforce singleton
    private SessionManager() {
        resetSession();
    }

    // Get the singleton instance
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Initialize session with user data
    // Initialize session with user data
    public void login(String username, String firstname, String lastname, String email, String permissionLevel, String siteName) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.permissionLevel = permissionLevel;
        this.siteName = siteName;
        this.lastActivityTime = System.currentTimeMillis();
    }

    // Reset session to clear user data (e.g., on logout)
    public void resetSession() {
        username = null;
        firstname = null;
        lastname = null;
        email = null;
        permissionLevel = null;
        siteName = null;
        lastActivityTime = 0;
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return username != null;
    }

    // Update activity time
    public void updateLastActivityTime() {
        lastActivityTime = System.currentTimeMillis();
    }

    // Check if session has timed out
    public boolean isSessionTimedOut() {
        return System.currentTimeMillis() - lastActivityTime > MAX_SESSION_TIME;
    }

    // Getters for user info
    public String getUsername() {
        return username;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getPermissionLevel() {
        return permissionLevel;
    }

    public String getSiteName() {
        return siteName;
    }

    // Get the max session time (if needed elsewhere)
    public static long getMaxSessionTime() {
        return MAX_SESSION_TIME;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public static synchronized void getInfo() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            System.out.println("User Info:");
            System.out.println("Username: " + session.getUsername());
            System.out.println("First Name: " + session.getFirstname());
            System.out.println("Last Name: " + session.getLastname());
            System.out.println("Email: " + session.getEmail());
            System.out.println("Permission Level: " + session.getPermissionLevel());
            System.out.println("Site Name: " + session.getSiteName());
            System.out.println("Last Activity: " + session.getLastActivityTime());
        } else {
            System.out.println("No user is currently logged in.");
        }
    }
}
