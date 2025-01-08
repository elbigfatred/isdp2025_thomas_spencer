package main.java.com.frontend_desktop.swingapp.utils;

public class SessionManager {

    private static SessionManager instance; // Singleton instance

    //Constants
    private static final long MAX_SESSION_TIME = 10000; //10 sec in milliseconds

    private String username; // Store the current username
    private String location; // Example: user's current location
    private String[] roles;  // User roles or permissions
    private long lastActivityTime; // Tracks the last activity timestamp

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
    public void login(String username, String location, String[] roles) {
        this.username = username;
        this.location = location;
        this.roles = roles;
        this.lastActivityTime = System.currentTimeMillis();
    }

    // Reset session to clear user data (e.g., on logout)
    public void resetSession() {
        username = null;
        location = null;
        roles = null;
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

    public String getLocation() {
        return location;
    }

    public String[] getRoles() {
        return roles;
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
            System.out.println("Location: " + session.getLocation());
            System.out.println("Roles: " + String.join(", ", session.getRoles()));
            System.out.println("Last Activity: " + session.lastActivityTime);
        } else {
            System.out.println("No user is currently logged in.");
        }
    }
}
