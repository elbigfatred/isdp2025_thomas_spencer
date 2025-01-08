package views;





import utils.LoginRequest;
import utils.ReadEmployeesRequest;

import javax.swing.*;

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

    private JFrame frame;

    // Method to set up and display the dashboard frame
    public void showLoginForm() {
        frame = new JFrame("Bullseye Inventory Management System - Login"); // Create the frame
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set close operation
        frame.setSize(400, 300);                   // Set frame size
        frame.setLocationRelativeTo(null);         // Center the frame
        frame.setVisible(true);                    // Make it visible
    }

    public JPanel getMainPanel() {

        // action listeners for buttons
        btnLogin.addActionListener(e -> {


            // Get text inputs
            String usernameInput = txtUsername.getText();
            String passwordInput = new String(txtPassword.getPassword()); // Safely retrieve password input

            // Send login request to backend
            String response = LoginRequest.login(usernameInput, passwordInput);

            // Log the response for debugging purposes
            System.out.println("Backend Response: " + response);

            try {
                // Parse the JSON response using org.json.JSONObject
                org.json.JSONObject jsonResponse = new org.json.JSONObject(response);

                // Example: Extract specific fields from the JSON response
                String username = jsonResponse.optString("username", "Unknown");
                String location = jsonResponse.optString("location", "Unknown");
                String rolesString = jsonResponse.optString("roles", ""); // Comma-separated roles
                String[] roles = rolesString.isEmpty() ? new String[]{} : rolesString.split(",");

                // Print extracted fields to the console for debugging
                System.out.println("Parsed Username: " + username);
                System.out.println("Parsed Location: " + location);
                System.out.println("Parsed Roles: " + java.util.Arrays.toString(roles));

                // Use the extracted data (e.g., initialize the session)
                main.java.com.frontend_desktop.swingapp.utils.SessionManager.getInstance().login(username, location, roles);

                // Proceed to the dashboard
                SwingUtilities.invokeLater(() -> new DashboardForm().showDashboard());
                frame.dispose();

            } catch (org.json.JSONException ex) {
                // Handle JSON parsing errors
                JOptionPane.showMessageDialog(
                        frame,
                        "Error parsing response: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });

        btnExit.addActionListener(e -> {
            System.exit(0); // Exit the application
        });

        btnForgotPassword.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "Getting all employee data", "Info", JOptionPane.INFORMATION_MESSAGE);
            String response = ReadEmployeesRequest.fetchEmployeesRaw();
            JOptionPane.showMessageDialog(null, response);
        });

        return ContentPane;
    }
}
