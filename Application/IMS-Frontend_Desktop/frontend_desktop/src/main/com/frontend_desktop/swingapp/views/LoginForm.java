package main.java.com.frontend_desktop.swingapp.views;

import main.java.com.frontend_desktop.swingapp.utils.LoginRequest;
import main.java.com.frontend_desktop.swingapp.utils.ReadEmployeesRequest;


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

            // get text inputs
            String usernameInput = txtUsername.getText();
            String passwordInput = new String(txtPassword.getPassword()); // this is more secure

            //Send login request to backend
            String response = LoginRequest.login(usernameInput, passwordInput);

            System.out.println(response);

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
