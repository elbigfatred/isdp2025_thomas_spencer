package views;

import models.Employee;
import models.Posn;
import models.Site;
import utils.*;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * AddEditEmployeeForm provides a UI for adding and editing employees.
 * It includes form validation, automatic username generation, and password handling.
 */
public class AddEditEmployeeForm {

    // =================== UI COMPONENTS ===================

    private JPanel ContentPane;
    private JLabel lblWelcome;
    private JLabel lblLocation;
    private JCheckBox chkActive;
    private JCheckBox chkLocked;
    private JButton btnSave;
    private JButton btnExit;
    private JTextField txtEmpId;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtFirstname;
    private JTextField txtLastname;
    private JTextField txtEmail;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JLabel SPACER4;
    private JLabel lblEmpID;
    private JLabel lblPassword;
    private JLabel lblUsername;
    private JLabel lblFirstName;
    private JLabel lblLastName;
    private JLabel lblEmail;
    private JLabel lblPos;
    private JLabel lblLocationinput;
    private JComboBox cmbPosition;
    private JComboBox cmbLocation;
    private JLabel lblLogo;
    private JLabel lblPasswordEyeball;
    private JButton btnGenerateStrongPassword;
    private JLabel lblStrengthAdvisor;

    // =================== FRAME VARIABLES ===================

    private JDialog frame;
    private List<Employee> allEmployees;
    private boolean passwordRevealed = true; // will be toggled on display to run functionality
    private Employee selectedEmployee;
    private String mode = null;

    // =================== INITIALIZATION SECTION ===================

    /**
     * Displays the form for adding or editing an employee.
     *
     * @param parentFrame     The parent frame (used for modal dialog behavior).
     * @param currentLocation The location to center the dialog.
     * @param usage           Determines whether the form is for "ADD" or "EDIT".
     * @param employeeToModify The employee to edit (null if adding a new employee).
     * @param onCloseCallback A callback function to execute when the dialog closes.
     */
    public void showAddEditEmployeeForm(Frame parentFrame, Point currentLocation, String usage, Employee employeeToModify, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);
        if (Objects.equals(usage, "ADD")){
            frame.setTitle("Bullseye Inventory Management System - Add New Employee"); // Create the frame
            frame.setSize(700, 445);                   // Set frame size

        }
        else if (Objects.equals(usage, "EDIT")){
            frame.setTitle("Bullseye Inventory Management System - Modify Employee"); // Create the frame
            frame.setSize(700, 525);                   // Set frame size

        }
        if (employeeToModify != null) {
            selectedEmployee = employeeToModify;
        }
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Set close operation
        if(currentLocation != null) {
            frame.setLocation(currentLocation);
        }
        frame.setLocationRelativeTo(null);         // Center the frame

        // Delay setup methods until the dialog is visible
        SwingUtilities.invokeLater(() -> {
            SetupBullseyeLogo();
            setupFields(usage);
            togglePasswordRevealed();
        });

        frame.setVisible(true);                    // Make it visible

        // Add a listener to detect when the dialog is closed
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (onCloseCallback != null) {
                    onCloseCallback.run(); // Execute the callback when dialog is closed
                }
            }
        });
    }

    /**
     * Returns the main panel for the form and sets up UI event listeners.
     *
     * @return JPanel The main panel of the form.
     */
    public JPanel getMainPanel() {
        // Check if a user is logged in
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "No user is currently logged in. Not sure how you got here.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE
            );
            //Logout(); // Log out and redirect to the login screen
            return null; // Return null to avoid further execution
        }

        // Set up initial components
        lblWelcome.setText("User: " + session.getUsername());

        lblLocation.setText("Location: " + session.getSiteName());

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

        btnExit.addActionListener(e -> {
            frame.dispose(); // Close the password reset form
        });

        btnGenerateStrongPassword.addActionListener(e -> {
            generateStrongPassword();
        });

        // Allow Cancel/Exit to be accessed via 'ESC' key
        btnExit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        btnExit.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnExit.doClick();
            }
        });

        // Allow Cancel/Exit to be accessed via 'ESC' key
        btnSave.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "cancel");

        btnSave.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSave.doClick();
            }
        });

        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                advisePasswordStrength(txtPassword.getText());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                advisePasswordStrength(txtPassword.getText());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                advisePasswordStrength(txtPassword.getText());
            }
        });

        return ContentPane;
    }

    // =================== UI SETUP & FIELD POPULATION ===================

    /**
     * Populates the UI fields when adding or editing an employee.
     *
     * @param usage Determines whether the form is for "ADD" or "EDIT".
     */
    private void setupFields(String usage) {
        populatePositionComboBox();
        populateSitesComboBox();
        allEmployees = EmployeeRequests.fetchEmployees();

        if (Objects.equals(usage, "ADD")){
            // add mode
            mode = "ADD";
            lblPassword.setVisible(false);
            txtPassword.setVisible(false);
            lblPasswordEyeball.setVisible(false);
            btnGenerateStrongPassword.setVisible(false);
            lblStrengthAdvisor.setVisible(false);

            assignEmployeeID();
            btnSave.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        frame,
                        "Save new Employee?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) AddNewEmployee();
            });

            txtFirstname.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    generateUsername();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    generateUsername();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    generateUsername();
                }
            });

            txtLastname.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    generateUsername();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    generateUsername();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    generateUsername();
                }
            });

        }

        else if (Objects.equals(usage, "EDIT") && selectedEmployee != null) {
            //edit mode
            mode = "EDIT";
            populateFieldsForEditing();
            btnSave.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        frame,
                        "Modify Employee?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) updateEmployee();
            });
        }
    }

    /**
     * Populates the form fields when editing an employee.
     */
    private void populateFieldsForEditing() {

        // Set text fields and checkboxes
        txtEmpId.setText(String.valueOf(selectedEmployee.getId()));
        txtEmpId.setEnabled(false); // ID is not editable
        txtFirstname.setText(selectedEmployee.getFirstName());
        txtLastname.setText(selectedEmployee.getLastName());
        txtUsername.setText(selectedEmployee.getUsername());
        txtUsername.setEnabled(false); // Username is not editable
        txtEmail.setText(selectedEmployee.getEmail());
        chkActive.setSelected(selectedEmployee.isActive());
        chkLocked.setSelected(selectedEmployee.getLocked());

        // Display all roles in lblPositions
        StringBuilder positionsText = new StringBuilder("<html>Roles:<br>");
        for (Posn userPosn : selectedEmployee.getRoles()) {
            positionsText.append(userPosn.getPermissionLevel()).append("<br>");
        }
        positionsText.append("</html>");
        //lblPos.setVisible(false);

        // Populate the site combo box
        for (int i = 0; i < cmbLocation.getItemCount(); i++) {
            Site site = (Site) cmbLocation.getItemAt(i);
            if (site.getId() == selectedEmployee.getSite().getId()) {
                cmbLocation.setSelectedItem(site);
                break;
            }
        }

        // Populate the position combo box
        for (int i = 0; i < cmbPosition.getItemCount(); i++) {
            Posn posn = (Posn) cmbPosition.getItemAt(i);
            if(Objects.equals(posn.getPermissionLevel(), selectedEmployee.getMainRole())){
                cmbPosition.setSelectedItem(posn);
                break;
            }
        }

        // Leave the password field blank for security reasons
        txtPassword.setText("");
    }

    /**
     * Populates the position (roles) dropdown menu with available positions.
     */
    private void populatePositionComboBox() {
        List<Posn> positions = PositionRequests.fetchPositions();
        cmbPosition.removeAllItems();

        for (Posn posn : positions) {
            if(posn.getActive()){
                cmbPosition.addItem(posn);
            }
        }
    }

    /**
     * Populates the site (location) dropdown menu with active sites.
     */
    private void populateSitesComboBox() {
        List<Site> sites = SiteRequests.fetchSites();
        cmbLocation.removeAllItems();

        for (Site site : sites) {
            if(site.isActive()){
                cmbLocation.addItem(site);
            }
        }
    }

    /**
     * Loads and sets the Bullseye logo in the UI.
     */
    private void SetupBullseyeLogo() {
        String logoPath = "/bullseye.jpg"; // Classpath-relative path
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        lblLogo.setIcon(resizedIcon);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setText("");
    }

    // =================== EMPLOYEE OPERATIONS ===================

    /**
     * Updates an existing employee with modified details and sends the update request.
     */
    private void updateEmployee() {
        try {
            if (!validateFields()) {
                return; // Stop execution if validation fails
            }

            // Update basic employee details
            selectedEmployee.setFirstName(txtFirstname.getText().trim());
            selectedEmployee.setLastName(txtLastname.getText().trim());
            selectedEmployee.setEmail(txtEmail.getText().trim());
            selectedEmployee.setActive(chkActive.isSelected());
            selectedEmployee.setLocked(chkLocked.isSelected() ? 1 : 0);
            selectedEmployee.setSite((Site) cmbLocation.getSelectedItem());

            Employee tempEmployee = new Employee();
            for (Employee employee : allEmployees) {
                if (employee.getId() == selectedEmployee.getId()) {
                    tempEmployee = employee;
                    break;
                }
            }

            // Assign the primary role from cmbPosition
            Posn primaryRole = (Posn) cmbPosition.getSelectedItem();
            if (primaryRole != null) {
                List<Posn> roles = new ArrayList<>(selectedEmployee.getRoles()); // Get existing roles

                boolean mainRoleExists = false;
                Posn oldMainRole = null;

                // Check if the main role already exists & track old main role
                for (Posn role : roles) {
                    if (role.getId() == primaryRole.getId()) {
                        mainRoleExists = true;
                    }
                    if (role.getPermissionLevel().equals(selectedEmployee.getMainRole())) {
                        oldMainRole = role; // Store old main role
                    }
                }

                // If the main role is NOT in the list, add it
                if (!mainRoleExists) {
                    roles.add(primaryRole);
                }

                // If the old main role is different from the new one, remove it
                if (oldMainRole != null && oldMainRole.getId() != primaryRole.getId()) {
                    roles.remove(oldMainRole);
                }

                // Assign updated roles list to employee
                selectedEmployee.setRoles(roles);
                selectedEmployee.setMainRole(primaryRole.getPermissionLevel()); // Update main role text field
            }

            selectedEmployee.setMainRole(primaryRole.getPermissionLevel());

            // Update password if the field is not empty
            String newPassword = new String(txtPassword.getPassword()).trim();
            if (!newPassword.isEmpty()) {
                selectedEmployee.setPassword(newPassword);
            }

            // Call the backend to update the employee
            boolean success = EmployeeRequests.updateEmployee(selectedEmployee);
            if (success) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Employee updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                frame.dispose(); // Close the form after successful update
            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Failed to update employee. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Adds a new employee to the system after validating input fields.
     */
    private void AddNewEmployee() {
        // Construct the Employee object from form inputs
        try {
            // Validate input fields
            if (!validateFields()) {
                return; // Stop execution if validation fails
            }
            Employee newEmployee = new Employee();
            newEmployee.setId(Integer.parseInt(txtEmpId.getText()));
            newEmployee.setUsername(txtUsername.getText().trim());
            newEmployee.setPassword("");
            newEmployee.setFirstName(txtFirstname.getText().trim());
            newEmployee.setLastName(txtLastname.getText().trim());
            newEmployee.setEmail(txtEmail.getText().trim());
            newEmployee.setActive(chkActive.isSelected());
            newEmployee.setLocked(chkLocked.isSelected() ? 1 : 0);
            newEmployee.setSite((Site) cmbLocation.getSelectedItem());

            // Assign the primary role from cmbPosition
            Posn primaryRole = (Posn) cmbPosition.getSelectedItem();
            if (primaryRole != null) {
                List<Posn> roles = new ArrayList<>();
                roles.add(primaryRole);
                newEmployee.setRoles(roles); // Assign the primary role
            }

            newEmployee.setMainRole(primaryRole.getPermissionLevel());

            // Call the backend to save the new employee
            boolean success = EmployeeRequests.addEmployee(newEmployee);

            if (success) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Employee added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                frame.dispose(); // Close the form after successful addition
            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Failed to add employee. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }}
        catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =================== PASSWORD MANAGEMENT ===================

    /**
     * Toggles password visibility between hidden and visible.
     */
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

    /**
     * Generates a strong password following security best practices.
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

        txtPassword.setText(generatedPassword);
        //evaluatePasswordStrength(new String(passwordArray));
    }

    /**
     * Advises the user on password strength based on security rules.
     *
     * @param password The password to evaluate.
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

    // =================== UI UTILITIES ===================
    /**
     * Automatically generates a unique username based on first and last name input.
     */
    private void generateUsername() {
        String firstName = txtFirstname.getText().trim().toLowerCase();
        String lastName = txtLastname.getText().trim().toLowerCase();

        if (!firstName.isEmpty() && !lastName.isEmpty()) {
            String baseUsername = firstName.charAt(0) + lastName;
            String uniqueUsername = getUniqueUsername(baseUsername);
            txtUsername.setText(uniqueUsername);
            txtEmail.setText(uniqueUsername + "@bullseye.ca");
        }
        else{
            txtUsername.setText("");
            txtEmail.setText("");
        }
    }

    /**
     * Ensures the generated username is unique by appending numbers if necessary.
     *
     * @param baseUsername The base username derived from first and last name.
     * @return String A unique username.
     */
    private String getUniqueUsername(String baseUsername) {
        Set<String> existingUsernames = new HashSet<>();
        for (Employee employee : allEmployees) {
            existingUsernames.add(employee.getUsername().toLowerCase());
        }

        if (!existingUsernames.contains(baseUsername)) {
            return baseUsername;
        }

        // Add numbers to the base username to make it unique
        int counter = 1;
        String newUsername;
        do {
            newUsername = baseUsername + String.format("%02d", counter);
            counter++;
        } while (existingUsernames.contains(newUsername));

        return newUsername;
    }

    /**
     * Validates form fields to ensure all required inputs are provided.
     *
     * @return boolean True if all fields are valid, false otherwise.
     */
    private boolean validateFields() {
        // Ensure no fields are empty except the password
        if (txtFirstname.getText().trim().isEmpty() ||
                txtLastname.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() ||
                cmbPosition.getSelectedItem() == null ||
                cmbLocation.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(
                    frame,
                    "All fields must be filled in.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        // Validate email format
        if (!txtEmail.getText().matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,3}$")) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Invalid email format.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        return true; // Password is skipped for validation since it's defaulted
    }

    /**
     * Assigns a new employee ID by finding the first available ID in the system.
     */
    private void assignEmployeeID() {
        List<Integer> existingIds = new ArrayList<>();
        for (Employee employee : allEmployees) {
            existingIds.add(employee.getId());
        }

        int newId = findFirstAvailableID(existingIds);
        txtEmpId.setText(String.valueOf(newId));
    }

    /**
     * Finds the first available employee ID within the range 1000-9998.
     *
     * @param existingIds List of currently assigned employee IDs.
     * @return int The first available ID.
     */
    private int findFirstAvailableID(List<Integer> existingIds) {
        for (int i = 1000; i <= 9998; i++){
            if (!existingIds.contains(i)){
                return i;
            }
        }
        throw new RuntimeException("No available employee IDs in the range 1000-9998.");
    }
}
