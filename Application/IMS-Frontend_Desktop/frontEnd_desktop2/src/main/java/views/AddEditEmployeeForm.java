package views;

import models.Employee;
import models.Posn;
import models.Site;
import utils.ReadEmployeesRequest;
import utils.ReadPositionsRequest;
import utils.ReadSitesRequest;
import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.List;

public class AddEditEmployeeForm {
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

    private JDialog frame;

    public void showAddEditEmployeeForm(Frame parentFrame, Point currentLocation, String usage, Employee employeeToModify, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);
        if (Objects.equals(usage, "ADD")){
            frame.setTitle("Bullseye Inventory Management System - Add New Employee"); // Create the frame
        }
        else if (Objects.equals(usage, "EDIT")){
            frame.setTitle("Bullseye Inventory Management System - Modify Employee"); // Create the frame
        }
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Set close operation
        frame.setSize(500, 400);                   // Set frame size
        if(currentLocation != null) {
            frame.setLocation(currentLocation);
        }
        frame.setLocationRelativeTo(null);         // Center the frame

        // Delay setup methods until the dialog is visible
        SwingUtilities.invokeLater(() -> {
            SetupBullseyeLogo();
            setupFields(usage);
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

    private void setupFields(String usage) {
        System.out.println("setting up fields...");
        populatePositionComboBox();
        populateSitesComboBox();
        if (Objects.equals(usage, "ADD")){
            System.out.println("add mode");
            // add mode
            assignEmployeeID();
            btnSave.addActionListener(e -> {
                // replace with actual save haha
//                Posn cmbPositionSelectedItem = (Posn) cmbPosition.getSelectedItem();
//                System.out.println(cmbPositionSelectedItem.getId());
//                System.out.println(cmbPositionSelectedItem.getPermissionLevel());
//                System.out.println(cmbPositionSelectedItem.isActive());
//                Site cmbLocationSelectedItem = (Site) cmbLocation.getSelectedItem();
//                System.out.println(cmbLocationSelectedItem.getId());
//                System.out.println(cmbLocationSelectedItem.getAddress());
//                System.out.println(cmbLocationSelectedItem.getDayOfWeek());
//                System.out.println(cmbLocationSelectedItem.getDistanceFromWH());
                AddNewEmployee();
            });

        }
        else{
            //edit mode
        }
    }

    private void AddNewEmployee() {
        // Construct the Employee object from form inputs
        try {
            Employee newEmployee = new Employee();
            newEmployee.setId(Integer.parseInt(txtEmpId.getText()));
            newEmployee.setUsername(txtUsername.getText().trim());
            newEmployee.setPassword(new String(txtPassword.getPassword()).trim());
            newEmployee.setFirstName(txtFirstname.getText().trim());
            newEmployee.setLastName(txtLastname.getText().trim());
            newEmployee.setEmail(txtEmail.getText().trim());
            newEmployee.setActive(chkActive.isSelected());
            newEmployee.setLocked(chkLocked.isSelected() ? 1 : 0);
            newEmployee.setPermissionLevel(((Posn) cmbPosition.getSelectedItem()));
            newEmployee.setSite((Site) cmbLocation.getSelectedItem());

            // Call the backend to save the new employee
            boolean success = ReadEmployeesRequest.addEmployee(newEmployee);

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

    private void populateSitesComboBox() {
        List<Site> sites = ReadSitesRequest.fetchSites();
        cmbLocation.removeAllItems();

        for (Site site : sites) {
            if(site.isActive()){
                cmbLocation.addItem(site);
            }
        }
    }

    private void populatePositionComboBox() {
        List<Posn> positions = ReadPositionsRequest.fetchPositions();
        cmbPosition.removeAllItems();

        for (Posn posn : positions) {
            if(posn.isActive()){
                cmbPosition.addItem(posn);
            }
        }
    }

    private void assignEmployeeID() {
        List<Employee> allEmployees = ReadEmployeesRequest.fetchEmployees();
        List<Integer> existingIds = new ArrayList<>();
        for (Employee employee : allEmployees) {
            existingIds.add(employee.getId());
        }

        int newId = findFirstAvailableID(existingIds);
        txtEmpId.setText(String.valueOf(newId));
    }

    private int findFirstAvailableID(List<Integer> existingIds) {
        for (int i = 1000; i <= 9998; i++){
            if (!existingIds.contains(i)){
                System.out.println(i);
                return i;
            }
        }
        throw new RuntimeException("No available employee IDs in the range 1000-9998.");
    }

    public JPanel getMainPanel() {
        // Check if a user is logged in
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(
                    null,
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


        return ContentPane;
    }

    private void SetupBullseyeLogo() {
        String logoPath = "/bullseye.jpg"; // Classpath-relative path
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        lblLogo.setIcon(resizedIcon);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setText("");
    }


}
