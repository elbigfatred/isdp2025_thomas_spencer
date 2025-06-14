package views;

import org.json.JSONObject;
import utils.HelpBlurbs;
import utils.LoginRequests;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * PasswordResetForm provides a UI for users to reset their passwords.
 * It includes validation, password strength checking, and a password generator.
 */
public class PasswordResetForm {

    // =================== UI COMPONENTS ===================

    private JPanel ContentPane;
    private JLabel LblChangePassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel LblUsername;
    private JButton BtnReset;
    private JButton BtnExit;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JLabel SPACER4;
    private JLabel logoLabel;
    private JLabel lblNewPasswordEyeball;
    private JLabel lblConfirmPasswordEyeball;
    private JLabel lblStrengthAdvisor;
    private JButton btnGenerateStrongPassword;
    private JButton btnHelp;

    // =================== FRAME VARIABLES ===================

    private JFrame frame;
    private String username; // To store the username for this form; passed from login
    private boolean newPasswordRevealed = true; // will be toggled on display to run functionality
    private boolean confirmPasswordRevealed = true; // will be toggled on display to run functionality

    // =================== CONSTRUCTOR & INITIALIZATION ===================

    /**
     * Constructs the password reset form.
     *
     * @param username The username of the account for which the password is being reset.
     */
    public PasswordResetForm(String username) {
        this.username = username; // Store the username
        initializeForm(); // Initialize form elements
    }

    /**
     * Initializes the form elements, ensuring the username label is correctly set.
     */
    private void initializeForm() {
        // Set the username label to display the user's username
        if (LblUsername != null) {
            LblUsername.setText(username);
        }
    }

    /**
     * Displays the password reset form centered on the screen or at the provided location.
     *
     * @param currentLocation Optional parameter to place the form at a specific location.
     */
    public void showPasswordResetForm(Point currentLocation) {
        frame = new JFrame("Bullseye Inventory Management System - Password Reset"); // Create the frame
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set close operation
        frame.setSize(475, 445);                   // Set frame size
        if(currentLocation != null) {
            frame.setLocation(currentLocation);
        }
        frame.setLocationRelativeTo(null);         // Center the frame
        frame.setVisible(true);                    // Make it visible

        toggleConfirmPasswordRevealed();
        toggleNewPasswordRevealed();

        applyNoWhitespaceFilter(txtNewPassword);
        applyNoWhitespaceFilter(txtConfirmPassword);

        String logoPath = "/bullseye.jpg"; // Classpath-relative path
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        logoLabel.setIcon(resizedIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setText("");
    }

    // =================== EVENT LISTENERS SECTION ===================

    /**
     * Returns the main content panel and sets up event listeners for user interaction.
     *
     * @return The main JPanel containing all UI elements.
     */
    public JPanel getMainPanel() {
        // action listeners for buttons
        BtnExit.addActionListener(e -> {
            frame.dispose(); // Close the password reset form
            new LoginForm().showLoginForm(frame.getLocation()); // Redirect to login form
        });

        BtnReset.addActionListener(e -> {
            ResetButtonEvent();
        });

        btnGenerateStrongPassword.addActionListener(e -> {
            generateStrongPassword();
        });

        lblNewPasswordEyeball.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleNewPasswordRevealed();
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lblNewPasswordEyeball.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor to hand
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                lblNewPasswordEyeball.setCursor(Cursor.getDefaultCursor()); // Revert cursor to default
            }
        });

        lblConfirmPasswordEyeball.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleConfirmPasswordRevealed();
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lblConfirmPasswordEyeball.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor to hand
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                lblConfirmPasswordEyeball.setCursor(Cursor.getDefaultCursor()); // Revert cursor to default
            }
        });

        txtNewPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                advisePasswordStrength(txtNewPassword.getText());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                advisePasswordStrength(txtNewPassword.getText());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                advisePasswordStrength(txtNewPassword.getText());
            }
        });

        // Allow Login to be accessed via 'Enter' key
        BtnReset.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "click");

        BtnReset.getActionMap().put("click", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BtnReset.doClick();
            }
        });

        // Allow Cancel/Exit to be accessed via 'ESC' key
        BtnExit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        BtnExit.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Close the password reset form
                new LoginForm().showLoginForm(frame.getLocation()); // Redirect to login form
            }
        });

        btnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.RESET_PASSWORD_HELP,"Password Reset Help",JOptionPane.INFORMATION_MESSAGE);
        });

        return ContentPane;
    }

    // =================== PASSWORD HANDLING SECTION ===================

    /**
     * Handles password reset logic, ensuring validation, strength, and backend communication.
     */
    private void ResetButtonEvent() {
        // Validate and reset password logic here
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Password fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(frame, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate password strength
        if (!isStrongPassword(newPassword)) {
            JOptionPane.showMessageDialog(frame, "Password is not strong enough.\nPlease follow the guidelines for a strong password.", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Save new password?",
                "Confirm Password Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.NO_OPTION) return;

        try {
            // Call backend API to reset the password
            JSONObject response = LoginRequests.resetPassword(username, newPassword);

            if (response != null && response.optString("status", "failure").equals("success")) {
                JOptionPane.showMessageDialog(frame, "Password successfully reset.", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose(); // Close the password reset form
                new LoginForm().showLoginForm(frame.getLocation()); // Redirect to login form
            } else {
                String errorMessage = response != null ? response.optString("message", "Unknown error occurred.") : "Failed to reset password.";
                JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to reset password. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Validates if a password meets security criteria.
     *
     * @param password The password to validate.
     * @return True if the password is strong, otherwise false.
     */
    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[-!@#$%^&*(),.?\":{}|<>].*");
    }

    // =================== UTILITY FUNCTIONS ===================

    /**
     * Generates a strong password containing uppercase, digits, and special characters.
     */
    private void generateStrongPassword() {
        // Character pools
        final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
        final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String DIGITS = "0123456789";
        final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+{}[]|:;'<>,.?/";
        final String ALL_CHARACTERS = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARACTERS;

        StringBuilder password = new StringBuilder();

        // Add one special character, one digit, and one uppercase letter
        password.append(SPECIAL_CHARACTERS.charAt((int) (Math.random() * SPECIAL_CHARACTERS.length())));
        password.append(DIGITS.charAt((int) (Math.random() * DIGITS.length())));
        password.append(UPPERCASE.charAt((int) (Math.random() * UPPERCASE.length())));

        // Fill the remaining characters randomly from all character pools
        for (int i = 3; i < 12; i++) {
            password.append(ALL_CHARACTERS.charAt((int) (Math.random() * ALL_CHARACTERS.length())));
        }

        // Shuffle the password to ensure randomness
        char[] passwordArray = password.toString().toCharArray();
//        for (int i = 0; i < passwordArray.length; i++) {
//            int randomIndex = (int) (Math.random() * passwordArray.length);
//            char temp = passwordArray[i];
//            passwordArray[i] = passwordArray[randomIndex];
//            passwordArray[randomIndex] = temp;
//        }

        // Set the generated password in the new password text field
        String generatedPassword = new String(passwordArray);

        txtNewPassword.setText(generatedPassword);
        txtConfirmPassword.setText(generatedPassword);
        //evaluatePasswordStrength(new String(passwordArray));
    }

    /**
     * Toggles the visibility of the new password field.
     * Updates the field to show or hide characters and changes the eyeball icon accordingly.
     */
    public void toggleNewPasswordRevealed() {

        txtNewPassword.setEchoChar(newPasswordRevealed? '•' : ((char) 0));

        String logoPath = newPasswordRevealed ? "/hide.png" : "/view.png";
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        lblNewPasswordEyeball.setIcon(resizedIcon);
        lblNewPasswordEyeball.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewPasswordEyeball.setText("");

        newPasswordRevealed = !newPasswordRevealed;
    }

    /**
     * Toggles the visibility of the confirm password field.
     * Updates the field to show or hide characters and changes the eyeball icon accordingly.
     */
    public void toggleConfirmPasswordRevealed() {

        txtConfirmPassword.setEchoChar(confirmPasswordRevealed? '•' : ((char) 0));

        String logoPath = confirmPasswordRevealed ? "/hide.png" : "/view.png";
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        lblConfirmPasswordEyeball.setIcon(resizedIcon);
        lblConfirmPasswordEyeball.setHorizontalAlignment(SwingConstants.CENTER);
        lblConfirmPasswordEyeball.setText("");

        confirmPasswordRevealed = !confirmPasswordRevealed;
    }

    /**
     * Evaluates the strength of the given password and provides feedback to the user.
     * Updates the UI label with a strength indicator based on password complexity.
     *
     * @param password The password entered by the user.
     */
    private void advisePasswordStrength(String password) {

        if(password.length() < 4){
            lblStrengthAdvisor.setText("<html><br><br><br><br></html>");
            return;
        }

        String strengthMessage;
        Color strengthColor;

        // Minimum rules for a password
        boolean hasMinimumLength = password.length() >= 8;
        boolean hasCapitalLetter = password.matches(".*[A-Z].*");
        boolean hasSpecialCharacter = password.matches(".*[!@#$%^&*()_+=\\-{}|:;\"'<>,.?/].*");
        boolean hasNumber = password.matches(".*\\d.*");

        if (!hasMinimumLength || !hasCapitalLetter || !hasSpecialCharacter) {
            // Weak Password
            strengthMessage = "<html><div style='text-align:left;'><b>Weak:</b> Password must be:<br>"
                    + "- At least 8 characters<br>"
                    + "- Include a capital letter<br>"
                    + "- Include a special character</div></html>";
            strengthColor = new Color(139, 0, 0); // Dark red
        } else if (hasMinimumLength && hasCapitalLetter && hasSpecialCharacter) {
            // Strong Password
            if (hasNumber && password.length() >= 12) {
                strengthMessage = "<html><div style='text-align:left;'><b>Strong:</b><br> Great password!<br><br><br></div></html>";
                strengthColor = new Color(0, 100, 0); // Dark green
            } else {
                // Medium Password
                strengthMessage = "<html><div style='text-align:left;'><b>Medium:</b><br> Consider adding numbers or extra<br>characters to strengthen it.<br><br></div></html>";
                strengthColor = new Color(204, 153, 0); // Dark yellow
            }
        } else {
            // Default fallback (shouldn't happen with current logic)
            strengthMessage = "Invalid Password";
            strengthColor = Color.GRAY;
        }

        // Update the UI
        lblStrengthAdvisor.setText(strengthMessage);
        lblStrengthAdvisor.setForeground(strengthColor);
    }

    /**
     * Applies a filter to the given JTextField to prevent whitespace input.
     * Ensures that users cannot enter spaces in password fields.
     *
     * @param textField The JTextField to apply the filter to.
     */
    private void applyNoWhitespaceFilter(JTextField textField) {
        AbstractDocument document = (AbstractDocument) textField.getDocument();
        document.setDocumentFilter(new NoWhitespaceFilter());
    }

    // =================== INNER CLASSES ===================

    /**
     * Custom DocumentFilter to prevent whitespace input in text fields.
     */
    public class NoWhitespaceFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null) {
                string = string.replaceAll("\\s", ""); // Remove all whitespace
            }
            super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null) {
                text = text.replaceAll("\\s", ""); // Remove all whitespace
            }
            super.replace(fb, offset, length, text, attrs);
        }
    }
}

