package views;





import models.Employee;
import org.json.JSONObject;
import utils.LoginRequest;
import utils.ReadEmployeesRequest;
import utils.SessionManager;

import javax.swing.*;
import java.util.List;

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
            JOptionPane.showMessageDialog(null, "Getting all employee data", "Info", JOptionPane.INFORMATION_MESSAGE);

            // Fetch employees and parse the response
            List<Employee> employees = ReadEmployeesRequest.fetchEmployees();

            // Format the employee data into a readable string
            StringBuilder employeeData = new StringBuilder("Employee Data:\n");
            for (Employee employee : employees) {
                employeeData.append("ID: ").append(employee.getId())
                        .append(", Name: ").append(employee.getFirstName()).append(" ").append(employee.getLastName())
                        .append(", Email: ").append(employee.getEmail())
                        .append(", Username: ").append(employee.getUsername())
                        .append("\n");
            }

            // Display the data in a dialog box
            JOptionPane.showMessageDialog(null, employeeData.toString());
        });

        return ContentPane;
    }
}
