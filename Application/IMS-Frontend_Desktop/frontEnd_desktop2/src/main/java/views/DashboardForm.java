package views;


import models.Employee;
import utils.ReadEmployeesRequest;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DashboardForm {
    private JPanel ContentPane;
    private JTabbedPane DashboardTabPane;
    private JPanel OrdersTab;
    private JPanel InventoryTab;
    private JPanel LossReturnTab;
    private JPanel ReportsTab;
    private JPanel AdminTab;
    private JButton BtnRefresh;
    private JButton BtnLogout;
    private JLabel SPACER;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JButton someButtonButton;
    private JLabel LblWelcome;
    private JLabel LblLocation;
    private JLabel logoLabel;
    private JPanel EmployeesTab;
    private JTable EmployeeTabTable;
    private JButton btnAddEmployee;
    private JButton btnEditEmployee;
    private JButton btnDeleteEmployee;
    private JPanel adminCRUDpane;

    private JFrame frame;
    private Timer idleTimer;
    private Timer countdownTimer;
    private boolean sessionActive = true;
    private int employeeTableSelectedId = -1;

    // Method to set up and display the dashboard frame
    public void showDashboard(Point currentLocation) {
        frame = new JFrame("Bullseye IMS"); // Create the frame
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set close operation
        frame.setSize(1600, 600);                   // Set frame size
        if(currentLocation != null) {
            frame.setLocation(currentLocation);
        }
        frame.setLocationRelativeTo(null);         // Center the frame
        frame.setVisible(true);                    // Make it visible

        setupIdleTimer(); //initialize timeout logic
        setupCountdownTimer();

        SetupBullseyeLogo();
        ConfigureTabsBasedOnPosition();
    }

    private void ConfigureTabsBasedOnPosition() {
        hideAllTabs();

        String position = SessionManager.getInstance().getPermissionLevel();

        if("Administrator".equalsIgnoreCase(position)) {
            DashboardTabPane.add("Employees", EmployeesTab);
            DashboardTabPane.add("Admin", AdminTab);
            adminCRUDpane.setVisible(true);

            populateEmployeeTable();
        }
        else if("Financial Manager".equalsIgnoreCase(position)) {
            DashboardTabPane.add("Employees", EmployeesTab);
            DashboardTabPane.add("Admin", AdminTab);

            populateEmployeeTable();
        }
        // more else ifs for other roles
        else {
            stopCountdownTimer();
            stopIdleTimer();
            // If not an admin, you can show a message or logout as needed
            JOptionPane.showMessageDialog(
                    frame,
                    "Access denied: You do not have permission to access this area.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            Logout();
        }
    }

    public void hideAllTabs(){
        while (DashboardTabPane.getTabCount() > 0) {
            DashboardTabPane.remove(0); // Always remove the first tab
        }
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
        // Check if a user is logged in
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(
                    null,
                    "No user is currently logged in. Returning to login screen.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE
            );
            Logout(); // Log out and redirect to the login screen
            return null; // Return null to avoid further execution
        }

        // Set up initial components
        LblWelcome.setText("User: " + session.getUsername());
        LblLocation.setText("Location: " + session.getSiteName());

        // action listeners for buttons
        BtnRefresh.addActionListener(e -> {
            RefreshButtonEvent();
        });

        BtnLogout.addActionListener(e -> {
            Logout();
        });

        btnDeleteEmployee.addActionListener(e -> {
           deleteEmployee();
        });

        btnAddEmployee.addActionListener(e -> {
            addEmployee();
        });

        return ContentPane;
    }

    private void addEmployee() {
        sessionActive = false;
        idleTimer.stop();
        countdownTimer.stop();

        SwingUtilities.invokeLater(()-> new AddEditEmployeeForm().showAddEditEmployeeForm(frame, frame.getLocation(), "ADD", null,() ->{
            // Resume session when the dialog is closed
            idleTimer.restart();
            countdownTimer.restart();
            sessionActive = true;
        }));

    }

    private void deleteEmployee() {
        if (employeeTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an employee to deactivate.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to deactivate this employee?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            String result = ReadEmployeesRequest.deactivateEmployee(employeeTableSelectedId);

            switch (result) {
                case "success":
                    JOptionPane.showMessageDialog(frame, "Employee deactivated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateEmployeeTable(); // Refresh the table after the operation
                    break;

                case "failure":
                    JOptionPane.showMessageDialog(frame, "Failed to deactivate employee. Employee is already inactive.", "Error", JOptionPane.ERROR_MESSAGE);
                    break;

                case "unexpected":
                    JOptionPane.showMessageDialog(frame, "Unexpected error occurred. Please contact support.", "Error", JOptionPane.ERROR_MESSAGE);
                    break;

                case "network_error":
                    JOptionPane.showMessageDialog(frame, "Network error. Please check your connection.", "Error", JOptionPane.ERROR_MESSAGE);
                    break;

                default:
                    JOptionPane.showMessageDialog(frame, "Unknown error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void Logout() {
        sessionActive = false;
        stopIdleTimer();
        stopCountdownTimer();
        SessionManager.getInstance().resetSession();

        SwingUtilities.invokeLater(()-> new LoginForm().showLoginForm(frame.getLocation()));
        frame.dispose();
    }

    private void RefreshButtonEvent() {
        idleTimer.stop();
        countdownTimer.stop();
        JOptionPane.showMessageDialog(
                null, "Refreshing data...", "Info", JOptionPane.INFORMATION_MESSAGE);
        SessionManager.getInfo();
        idleTimer.restart();
        countdownTimer.restart();
    }

    private void setupIdleTimer() {
        // Set up a timer to check for inactivity
        idleTimer = new Timer((int) SessionManager.getMaxSessionTime(), e -> {
            if (sessionActive) {
                stopIdleTimer();
                stopCountdownTimer();
                sessionActive = false;
                JOptionPane.showMessageDialog(frame, "Session timed out due to inactivity. Logging out.");
                SessionManager.getInstance().resetSession();
                SwingUtilities.invokeLater(() -> new LoginForm().showLoginForm(frame.getLocation()));
                frame.dispose();
            }
        });

        idleTimer.start(); // Start the timer

        // Reset the timer when the mouse or keyboard is used
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            int eventId = event.getID();
            if (eventId == MouseEvent.MOUSE_MOVED ||
                    eventId == MouseEvent.MOUSE_CLICKED ||
                    eventId == MouseEvent.MOUSE_PRESSED ||
                    eventId == MouseEvent.MOUSE_RELEASED ||
                    eventId == MouseEvent.MOUSE_ENTERED ||
                    eventId == MouseEvent.MOUSE_EXITED ||
                    eventId == MouseEvent.MOUSE_WHEEL) {
                resetIdleTimer();
            } else if (eventId == KeyEvent.KEY_PRESSED ||
                    eventId == KeyEvent.KEY_RELEASED ||
                    eventId == KeyEvent.KEY_TYPED) {
                resetIdleTimer();
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK |
                AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    private void resetIdleTimer() {
        idleTimer.restart(); // Reset the timer whenever there's activity
        SessionManager.getInstance().updateLastActivityTime(); // Update activity timestamp
    }

    private void stopIdleTimer() {
        if (idleTimer != null && idleTimer.isRunning()) {
            idleTimer.stop(); // Stop the timer
        }
    }

    private void setupCountdownTimer() {
        // Retrieve user information from the SessionManager
        SessionManager session = SessionManager.getInstance();
        String uname = session.getUsername();

        // Update the remaining idle time every second
        countdownTimer = new Timer(1000, e -> {
            long remainingTime = SessionManager.getMaxSessionTime() -
                    (System.currentTimeMillis() - SessionManager.getInstance().getLastActivityTime());
            if (remainingTime < 0) remainingTime = 0; // Avoid negative values

            // Convert milliseconds to seconds
            long seconds = remainingTime / 1000;
            LblWelcome.setText("User: " + uname + " | Idle time left: " + seconds + " seconds");
        });

        countdownTimer.start(); // Start the countdown timer
    }

    private void stopCountdownTimer() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop(); // Stop the countdown timer
        }
    }

    private void populateEmployeeTable() {
        // Column names for the table
        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Username", "Permission Level", "Active"};

        // Fetch employees from the backend
        List<Employee> employees = ReadEmployeesRequest.fetchEmployees();

        // Create a table model and add columns
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Populate the table model with employee data
        for (Employee employee : employees) {
            Object[] rowData = {
                    employee.getId(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getEmail(),
                    employee.getUsername(),
                    employee.getPermissionLevel(), // Assuming Employee has a 'Posn' object with 'PermissionLevel'
                    employee.isActive() ? "Yes" : "No" // Convert active status to Yes/No
            };
            tableModel.addRow(rowData);
        }

        // Set the table model to the JTable
        EmployeeTabTable.setModel(tableModel);

        // Allow selection of entire rows only
        EmployeeTabTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a listener to handle row selection
        EmployeeTabTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = EmployeeTabTable.getSelectedRow();
                if (selectedRow != -1) {
                    employeeTableSelectedId = (Integer) EmployeeTabTable.getValueAt(selectedRow, 0); // Assuming column 4 is 'id'
                    System.out.println("Selected Username: " + employeeTableSelectedId);
                }
                else{
                    employeeTableSelectedId = -1;
                }
            }
        });
    }
}
