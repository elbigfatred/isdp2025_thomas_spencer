package utils;

/**
 * SessionManager manages user session data and authentication state.
 *
 * Features:
 * - Implements a singleton pattern to maintain a single session instance.
 * - Stores user information including username, roles, and last activity time.
 * - Supports session timeout management.
 * - Provides utility methods to reset, update, and retrieve session details.
 */
public class SessionManager {

    private static SessionManager instance; // Singleton instance

    //Constants
    private static final long MAX_SESSION_TIME = 8000; //20 sec in milliseconds

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

    /**
     * Retrieves the singleton instance of SessionManager.
     * Ensures only one instance exists throughout the application.
     *
     * @return The single instance of SessionManager.
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Initializes the session with user data on login.
     * Stores user credentials and activity timestamp.
     *
     * @param username        The username of the logged-in user.
     * @param firstname       The first name of the user.
     * @param lastname        The last name of the user.
     * @param email           The email of the user.
     * @param permissionLevel The user's access level or role.
     * @param siteName        The site or location associated with the user.
     */
    public void login(String username, String firstname, String lastname, String email, String permissionLevel, String siteName) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.permissionLevel = permissionLevel;
        this.siteName = siteName;
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Resets the session data, clearing all stored user information.
     * Typically called on logout.
     */
    public void resetSession() {
        username = null;
        firstname = null;
        lastname = null;
        email = null;
        permissionLevel = null;
        siteName = null;
        lastActivityTime = 0;
    }

    /**
     * Checks whether a user is currently logged in.
     *
     * @return true if a user session is active, false otherwise.
     */
    public boolean isLoggedIn() {
        return username != null;
    }

    /**
     * Updates the last activity timestamp of the session.
     * Used to track inactivity for session timeout purposes.
     */
    public void updateLastActivityTime() {
        lastActivityTime = System.currentTimeMillis();
    }

    // Check if session has timed out
    public boolean isSessionTimedOut() {
        return System.currentTimeMillis() - lastActivityTime > MAX_SESSION_TIME;
    }

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
