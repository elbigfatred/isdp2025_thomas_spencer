package views;

import models.Employee;
import org.json.JSONObject;
import utils.LoginRequest;
import utils.ReadEmployeesRequest;
import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class PasswordResetForm {
    private JPanel ContentPane;
    private JLabel LblChangePassword;
    private JTextField textField1;
    private JTextField textField2;
    private JLabel LblUsername;
    private JButton BtnReset;
    private JButton BtnExit;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JLabel SPACER4;
    private JLabel logoLabel;

    private JFrame frame;
    private String username; // To store the username for this form


    // Constructor accepting a username
    public PasswordResetForm(String username) {
        this.username = username; // Store the username
        initializeForm(); // Initialize form elements
    }

    // Initialize form elements
    private void initializeForm() {
        // Set the username label to display the user's username
        if (LblUsername != null) {
            LblUsername.setText(username);
        }
    }

    public void showPasswordResetForm() {
        frame = new JFrame("Bullseye Inventory Management System - Password Reset"); // Create the frame
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
        BtnExit.addActionListener(e -> {
            System.exit(0); // Exit the application
        });

        BtnReset.addActionListener(e -> {
            // Validate and reset password logic here
            String newPassword = textField1.getText();
            String confirmPassword = textField2.getText();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Password fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(frame, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call backend API to reset the password
            JSONObject response = LoginRequest.resetPassword(username, newPassword);
            if (response != null && response.optString("status", "failure").equals("success")) {
                JOptionPane.showMessageDialog(frame, "Password successfully reset.", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose(); // Close the password reset form
                new LoginForm().showLoginForm(); // Redirect to login form
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to reset password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return ContentPane;
    }
}

