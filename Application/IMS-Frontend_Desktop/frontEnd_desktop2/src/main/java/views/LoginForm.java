package views;





import models.Employee;
import org.json.JSONObject;
import utils.LoginRequest;
import utils.ReadEmployeesRequest;
import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
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

    private JFrame frame;

    // Method to set up and display the dashboard frame
    public void showLoginForm() {
        frame = new JFrame("Bullseye Inventory Management System - Login"); // Create the frame
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set close operation
        frame.setSize(400, 300);                   // Set frame size
        frame.setLocationRelativeTo(null);         // Center the frame
        frame.setVisible(true);                    // Make it visible


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
            try {
                String usernameInput = txtUsername.getText();
                String passwordInput = new String(txtPassword.getPassword());

                // Attempt login
                JSONObject response = LoginRequest.login(usernameInput, passwordInput);

                // If successful, proceed to the dashboard
                String username = response.optString("username", "Unknown");
                String location = response.optString("location", "Unknown");
                String rolesString = response.optString("roles", "");
                String[] roles = rolesString.isEmpty() ? new String[]{} : rolesString.split(",");

                SessionManager.getInstance().login(username, location, roles);
                SwingUtilities.invokeLater(() -> new DashboardForm().showDashboard());
                frame.dispose();

            } catch (Exception ex) {
                // Display error message from the exception
                JOptionPane.showMessageDialog(
                        frame,
                        ex.getMessage(),
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        btnExit.addActionListener(e -> {
            System.exit(0); // Exit the application
        });

        btnForgotPassword.addActionListener(e -> {
            // Prompt the user for a username
            String username = JOptionPane.showInputDialog(
                    frame,
                    "Enter the username to fetch employee details:",
                    "Forgot Password",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (username == null || username.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Username is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Fetch employee data for the entered username

//            TODO: HANDLE LACK OF CONNECTION

            Employee employee = ReadEmployeesRequest.fetchEmployeeByUsername(username);

            if (employee == null) {
                JOptionPane.showMessageDialog(
                        frame,
                        "No employee found with username: " + username,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } else {
                // Format the employee data into a readable string
                String employeeData = String.format(
                        "Employee Details:\n\nID: %d\nName: %s %s\nEmail: %s\nUsername: %s",
                        employee.getId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getEmail(),
                        employee.getUsername()
                );

                // Display the data in a dialog box
                JOptionPane.showMessageDialog(frame, employeeData, "Employee Details", JOptionPane.INFORMATION_MESSAGE);

                // Redirect to password reset form
                SwingUtilities.invokeLater(() -> {
                    new PasswordResetForm(employee.getUsername()).showPasswordResetForm();
                    frame.dispose();
                });
            }
        });

        return ContentPane;
    }
}
