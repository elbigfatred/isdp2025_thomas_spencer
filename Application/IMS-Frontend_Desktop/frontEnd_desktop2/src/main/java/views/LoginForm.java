package views;

import models.Employee;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.LoginRequests;
import utils.EmployeeRequests;
import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.ConnectException;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * LoginForm handles the user login interface for the Bullseye Inventory Management System.
 * This form manages login validation, password reset navigation, and session setup.
 */
public class LoginForm {
    // GUI components
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnExit;
    private JButton btnLogin;
    private JButton btnForgotPassword;
    private JLabel SPACER;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JLabel SPACER4;
    private JLabel LblLogin;
    private JPanel ContentPane;
    private JLabel logoLabel;
    private JLabel lblPasswordEyeball;

    // Frame and state variables
    private JFrame frame;
    private boolean passwordRevealed = true; // will be toggled on display to run functionality

    // =================== FRAME INITIALIZATION SECTION ===================
    /**
     * Initializes and displays the login form.
     *
     * @param currentLocation The location to center the form on (optional).
     */
    public void showLoginForm(Point currentLocation) {
        setupListeners();           // Add action listeners for buttons
        setupFrame(currentLocation); // Create and configure the frame
        togglePasswordRevealed();
        setLogo("/bullseye.jpg", logoLabel, 100, 100); // Set up the Bullseye logo
    }
    /**
     * Sets up the frame with the main panel and basic properties.
     *
     * @param currentLocation The location to center the frame (optional).
     */
    private void setupFrame(Point currentLocation) {
        frame = new JFrame("Bullseye Inventory Management System - Login");
        frame.setContentPane(getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(475, 325);

        // Set frame location to either the provided location or center of the screen
        if (currentLocation != null) frame.setLocation(currentLocation);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true); // Show the frame
    }


    // =================== EVENT LISTENERS SECTION ===================

    /**
     * Sets up listeners for all interactive components in the form.
     */
    private void setupListeners() {
        // Login button triggers login validation
        btnLogin.addActionListener(e -> LoginButtonEvent());

        // Exit button closes the application
        btnExit.addActionListener(e -> System.exit(0));

        // Forgot Password button navigates to the password reset form
        btnForgotPassword.addActionListener(e -> ForgotPasswordButtonEvent());

        // Toggle password visibility on eyeball click
        lblPasswordEyeball.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                togglePasswordRevealed();
            }

            public void mouseEntered(MouseEvent e) {
                lblPasswordEyeball.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(MouseEvent e) {
                lblPasswordEyeball.setCursor(Cursor.getDefaultCursor());
            }
        });

        // Allow Login to be accessed via 'Enter' key
        btnLogin.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "click");

        btnLogin.getActionMap().put("click", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnLogin.doClick();
            }
        });

        // Allow Cancel/Exit to be accessed via 'ESC' key
        btnExit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        btnExit.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    // =================== LOGIN FUNCTIONALITY SECTION ===================

    /**
     * Handles the "Login" button event. Validates credentials and starts a user session.
     */
    private void LoginButtonEvent() {
        try {
            String usernameInput = txtUsername.getText();
            String passwordInput = new String(txtPassword.getPassword());

            // Attempt login
            JSONObject response = LoginRequests.login(usernameInput, passwordInput);

            // Parse response to extract session details
            String username = response.optString("username", "Unknown");
            String firstname = response.optString("firstname", "Unknown");
            String lastname = response.optString("lastname", "Unknown");
            String email = response.optString("email", "Unknown");
            String siteName = response.getJSONObject("site").optString("siteName", "Unknown");

            // Extract roles (permission levels)
            JSONArray rolesArray = response.optJSONArray("roles");
            StringBuilder permissionLevels = new StringBuilder();

            if (rolesArray != null) {
                for (int i = 0; i < rolesArray.length(); i++) {
                    JSONObject roleObject = rolesArray.getJSONObject(i).getJSONObject("posn");
                    permissionLevels.append(roleObject.optString("permissionLevel", "Unknown")).append(", ");
                }
                // Remove trailing comma and space
                if (permissionLevels.length() > 0) {
                    permissionLevels.setLength(permissionLevels.length() - 2);
                }
            } else {
                permissionLevels.append("None");
            }

            // Initialize session
            SessionManager.getInstance().login(
                    username,
                    firstname,
                    lastname,
                    email,
                    permissionLevels.toString(),
                    siteName
            );

            // Proceed to the dashboard
            SwingUtilities.invokeLater(() -> new DashboardForm().showDashboard(frame.getLocation()));
            frame.dispose();

        } catch (Exception ex) {
            // Check for the precondition-required response (password change required)
            if (ex.getMessage().contains("Password change required")) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Your password needs to be changed. Redirecting to password reset form...",
                        "Password Reset Required",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Redirect to password reset form
                String username = txtUsername.getText();
                SwingUtilities.invokeLater(() -> {
                    new PasswordResetForm(username).showPasswordResetForm(frame.getLocation());
                    frame.dispose();
                });
            }
            else if (ex.getMessage().contains("Connection refused")) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Could not connect to the database.\n\nPlease contact a Bullseye Administrator.",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            else {
                // Display other errors
                JOptionPane.showMessageDialog(
                        frame,
                        ex.getMessage(),
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Handles the "Forgot Password" button event. Navigates to the password reset form.
     */
    private void ForgotPasswordButtonEvent() {
        // Prompt the user for a username
        String username = txtUsername.getText();

        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Username is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Fetch employee data for the entered username
            Employee employee = EmployeeRequests.fetchEmployeeByUsername(username);

            if (employee == null) {
                JOptionPane.showMessageDialog(
                        frame,
                        "No employee found with username: " + username,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            if(!employee.isActive()){
                JOptionPane.showMessageDialog(frame,"Invalid username and/or password. Please contact your Administrator at admin@bullseye.ca for assistance.","Error",JOptionPane.ERROR_MESSAGE);
            }
            else if (employee.getLocked()){
                JOptionPane.showMessageDialog(frame,"Your account has been locked due to too many incorrect login attempts.\n Please contact your Administrator at admin@bullseye.ca for assistance.","Error",JOptionPane.ERROR_MESSAGE);
            }
            else {
                // Format and display the employee data
                String employeeData = String.format(
                        "Employee Details:\n\nID: %d\nName: %s %s\nEmail: %s\nUsername: %s",
                        employee.getId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getEmail(),
                        employee.getUsername()
                );

                //JOptionPane.showMessageDialog(frame, employeeData, "Employee Details", JOptionPane.INFORMATION_MESSAGE);

                // Redirect to password reset form
                SwingUtilities.invokeLater(() -> {
                    new PasswordResetForm(employee.getUsername()).showPasswordResetForm(frame.getLocation());
                    frame.dispose();
                });
            }
        } catch (RuntimeException ex) {
            // Handle backend connectivity issues
            JOptionPane.showMessageDialog(
                    frame,
                    ex.getMessage(),
                    "Backend Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =================== PASSWORD VISIBILITY SECTION ===================
    /**
     * Toggles the visibility of the password field when the "eyeball" icon is clicked.
     */
    private void togglePasswordRevealed() {
        // Toggle the echo character for the password field
        txtPassword.setEchoChar(passwordRevealed ? '•' : (char) 0);

        // Update the eyeball icon
        setLogo(passwordRevealed ? "/hide.png" : "/view.png", lblPasswordEyeball, 25, 25);

        // Flip the visibility state
        passwordRevealed = !passwordRevealed;
    }


    // =================== UTILITY FUNCTIONS SECTION ===================

    /**
     * Displays a message dialog to the user.
     *
     * @param message The message to display.
     * @param title   The title of the dialog box.
     * @param type    The type of message (e.g., error, info).
     */
    private void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(frame, message, title, type);
    }

    /**
     * Navigates to another form by disposing of the current frame and initializing the new form.
     *
     * @param formInitializer A runnable that initializes the next form.
     */
    private void switchToForm(Runnable formInitializer) {
        SwingUtilities.invokeLater(formInitializer);
        frame.dispose();
    }

    /**
     * Sets a logo or icon for a JLabel.
     *
     * @param path   The path to the image resource.
     * @param label  The JLabel to set the icon for.
     * @param width  The width of the scaled image.
     * @param height The height of the scaled image.
     */
    private void setLogo(String path, JLabel label, int width, int height) {
        URL logoURL = getClass().getResource(path);
        ImageIcon icon = new ImageIcon(logoURL);
        Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(scaledImage));
    }

    // =================== GETTER SECTION ===================
    /**
     * Returns the main content panel of the form.
     *
     * @return The JPanel containing all form components.
     */
    public JPanel getMainPanel() {
        return ContentPane;
    }

}
