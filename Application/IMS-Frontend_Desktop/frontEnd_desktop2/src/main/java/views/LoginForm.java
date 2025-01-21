package views;





import models.Employee;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.LoginRequest;
import utils.ReadEmployeesRequest;
import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class LoginForm {
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

    private JFrame frame;
    private boolean passwordRevealed = true; // will be toggled on display to run functionality

    // Method to set up and display the dashboard frame
    public void showLoginForm(Point currentLocation) {
        frame = new JFrame("Bullseye Inventory Management System - Login"); // Create the frame
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set close operation
        frame.setSize(500, 350);                   // Set frame size
        if(currentLocation != null) {
            frame.setLocation(currentLocation);
        }
        frame.setLocationRelativeTo(null);         // Center the frame
        frame.setVisible(true);                    // Make it visible


        togglePasswordRevealed();

        SetupBullseyeLogo();
    }

    private void SetupBullseyeLogo() {
        String logoPath = "/bullseye.jpg"; // Classpath-relative path
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        logoLabel.setIcon(resizedIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setText("");
    }

    public JPanel getMainPanel() {

        // action listeners for buttons
        btnLogin.addActionListener(e -> {
            LoginButtonEvent();
        });

        btnExit.addActionListener(e -> {
            System.exit(0); // Exit the application
        });

        btnForgotPassword.addActionListener(e -> {
            ForgotPasswordButtonEvent();
        });

        lblPasswordEyeball.addMouseListener(new MouseAdapter() {
           @Override
           public void mouseClicked(MouseEvent e) {
               togglePasswordRevealed();
           }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lblPasswordEyeball.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor to hand
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                lblPasswordEyeball.setCursor(Cursor.getDefaultCursor()); // Revert cursor to default
            }
        });

        return ContentPane;
    }

    private void ForgotPasswordButtonEvent() {
        // Prompt the user for a username
        String username = txtUsername.getText();

        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Username is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Fetch employee data for the entered username
            Employee employee = ReadEmployeesRequest.fetchEmployeeByUsername(username);

            if (employee == null) {
                JOptionPane.showMessageDialog(
                        frame,
                        "No employee found with username: " + username,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            if (employee.getLocked()){
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

    private void LoginButtonEvent() {
        try {
            String usernameInput = txtUsername.getText();
            String passwordInput = new String(txtPassword.getPassword());

            // Attempt login
            JSONObject response = LoginRequest.login(usernameInput, passwordInput);

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
            } else {
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
    public void togglePasswordRevealed() {

        txtPassword.setEchoChar(passwordRevealed? '•' : ((char) 0));

        String logoPath = passwordRevealed ? "/hide.png" : "/view.png";
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        lblPasswordEyeball.setIcon(resizedIcon);
        lblPasswordEyeball.setHorizontalAlignment(SwingConstants.CENTER);
        lblPasswordEyeball.setText("");

        passwordRevealed = !passwordRevealed;
    }
}
