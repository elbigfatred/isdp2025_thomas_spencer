package views;


import models.Employee;
import models.Item;
import models.Posn;
import utils.ReadEmployeesRequest;
import utils.ReadItemsRequest;
import utils.ReadPositionsRequest;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardForm {
    private JPanel ContentPane;
    private JTabbedPane DashboardTabPane;
    private JPanel OrdersTab;
    private JPanel InventoryTab;
    private JPanel LossReturnTab;
    private JPanel ReportsTab;
    private JPanel EditPermissionsTab;
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
    private JTextField txtEmployeeSearch;
    private JPanel ItemsTab;
    private JPanel warehouseManagerCRUDpane;
    private JButton btnDeleteItem;
    private JButton btnEditItem;
    private JTable ItemsTabTable;
    private JTextField txtItemSearch;
    private JList lstEmployees;
    private JList lstRoles;
    private JButton btnEditPermissions;
    private JTable tblEditPermissionsEmployees;
    private JPanel pnlEditPermissionsRoles;

    private JFrame frame;
    private Timer idleTimer;
    private Timer countdownTimer;
    private boolean sessionActive = true;
    private int employeeTableSelectedId = -1;
    private List<Employee> allEmployees;
    private List<Item> allItems;
    private List<Posn> allPosns;
    private String[] accessPosition;
    private int itemTableSelectedId = -1;

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

        // Fetch roles as a single string and split into an array
        String permissionLevels = SessionManager.getInstance().getPermissionLevel(); // Example: "Administrator, Warehouse Manager"
        String[] roles = permissionLevels.split(",\\s*"); // Split by ", " (comma followed by optional whitespace)

        accessPosition = roles;
        adminCRUDpane.setVisible(false);

        // Check roles and configure tabs accordingly
        if (Arrays.asList(roles).contains("Administrator")) {
            DashboardTabPane.add("Edit Permissions", EditPermissionsTab);
            adminCRUDpane.setVisible(true);

            loadInitialData();
            populateEmployeeTable(allEmployees);
            populateEditPermissionsEmployeesList(allEmployees);
            populateEditPermissionsPositionList(allPosns);
        }

        if (Arrays.asList(roles).contains("Warehouse Manager")) {
            DashboardTabPane.add("Items", ItemsTab);

            loadInitialData();
            populateEmployeeTable(allEmployees);
            populateItemsTable(allItems);
        }

        if (Arrays.asList(roles).contains("Financial Manager")) {
            DashboardTabPane.add("Reports", ReportsTab);

            loadInitialData();
            // Add any additional logic for Financial Manager
        }

        if (roles.length == 0) {
            // If the user has no roles or is not permitted, display an access denied message
            JOptionPane.showMessageDialog(
                    frame,
                    "Access denied: You do not have permission to access this area.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            Logout();
        }

        DashboardTabPane.add("Employees", EmployeesTab);
    }

    private void loadInitialData() {
        allEmployees = ReadEmployeesRequest.fetchEmployees();

        if (Arrays.asList(accessPosition).contains("Administrator")) {
            allPosns = ReadPositionsRequest.fetchPositions();
        }

        if (Arrays.asList(accessPosition).contains("Warehouse Manager")) {
            allItems = ReadItemsRequest.fetchItems();
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

        btnEditEmployee.addActionListener(e -> {
            editEmployee();
        });

        txtEmployeeSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateEmployeeTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateEmployeeTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateEmployeeTableBySearch();
            }
        });

        txtItemSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateItemTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateItemTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateItemTableBySearch();
            }
        });

        return ContentPane;
    }

    private void updateItemTableBySearch() {
        // If the search bar is empty, populate the table with all items
        if (txtItemSearch.getText().trim().isEmpty()) {
            populateItemsTable(allItems); // Populate with the full list of items
            return;
        }

        List<Item> itemsToSearch = allItems; // Use the class-level list of items
        String search = txtItemSearch.getText().trim().toLowerCase(); // Get the search term and convert it to lowercase
        List<Item> itemsToList = new ArrayList<>();

        // Search through all items
        for (Item item : itemsToSearch) {
            if (item.getName().toLowerCase().contains(search) || // Match item name
                    item.getSku().toLowerCase().contains(search) || // Match SKU
                    String.valueOf(item.getId()).contains(search) || // Match ID
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(search)) || // Match description
                    item.getCategory().getCategoryName().toLowerCase().contains(search) || // Match category name
                    String.valueOf(item.getWeight()).contains(search) || // Match weight
                    String.valueOf(item.getCaseSize()).contains(search) || // Match case size
                    String.valueOf(item.getCostPrice()).contains(search) || // Match cost price
                    String.valueOf(item.getRetailPrice()).contains(search) || // Match retail price
                    (item.getNotes() != null && item.getNotes().toLowerCase().contains(search)) || // Match notes
                    (item.getActive() ? "yes" : "no").contains(search) || // Match active status
                    (item.getImageLocation() != null && item.getImageLocation().toLowerCase().contains(search)) || // Match image location
                    item.getSupplier().getName().toLowerCase().contains(search)) { // Match supplier name

                itemsToList.add(item); // Add matching item to the filtered list
            }
        }

        // Populate the table with the filtered items
        populateItemsTable(itemsToList);
    }

    private void updateEmployeeTableBySearch() {
        if (txtEmployeeSearch.getText().trim().isEmpty()) {
            populateEmployeeTable(allEmployees);
            return;
        }

        List<Employee> empsToSearch = allEmployees;
        String search = txtEmployeeSearch.getText().trim().toLowerCase();
        List<Employee> empsToList = new ArrayList<>();

        if (Arrays.asList(accessPosition).contains("Administrator")) {
            for (Employee emp : empsToSearch) {
                boolean matchesRoles = false;

                // Check if any role matches the search term
                if (emp.getRoles() != null) {
                    for (Posn role : emp.getRoles()) {
                        if (role.getPermissionLevel().toLowerCase().contains(search)) {
                            matchesRoles = true;
                            break;
                        }
                    }
                }

                // Check if any other field matches the search term
                if (emp.getFirstName().toLowerCase().contains(search) ||
                        emp.getLastName().toLowerCase().contains(search) ||
                        String.valueOf(emp.getId()).contains(search) ||
                        emp.getEmail().toLowerCase().contains(search) ||
                        emp.getUsername().toLowerCase().contains(search) ||
                        matchesRoles ||
                        (emp.isActive() ? "yes" : "no").contains(search) ||
                        (emp.getLocked() ? "yes" : "no").contains(search)) {
                    empsToList.add(emp);
                }
            }
        } else {
            for (Employee emp : empsToSearch) {
                // Skip inactive employees for non-admin roles
                if (!emp.isActive()) {
                    continue;
                }

                // Match against non-admin-specific fields
                if (emp.getFirstName().toLowerCase().contains(search) ||
                        String.valueOf(emp.getId()).contains(search) ||
                        emp.getLastName().toLowerCase().contains(search) ||
                        emp.getEmail().toLowerCase().contains(search) ||
                        emp.getUsername().toLowerCase().contains(search)) {
                    empsToList.add(emp);
                }
            }
        }

        populateEmployeeTable(empsToList);
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
            loadInitialData();
            populateEmployeeTable(allEmployees);
        }));

    }

    private void editEmployee() {
        //get employee
        Employee employeeToEdit = null;

        if (employeeTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an employee to modify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Employee> allEmployees = ReadEmployeesRequest.fetchEmployees();
        for (Employee employee : allEmployees) {
            if (employee.getId() == employeeTableSelectedId) {
                employeeToEdit = employee;
            }
        }

        sessionActive = false;
        idleTimer.stop();
        countdownTimer.stop();

        Employee finalEmployeeToEdit = employeeToEdit;

        SwingUtilities.invokeLater(()-> new AddEditEmployeeForm().showAddEditEmployeeForm(frame, frame.getLocation(), "EDIT", finalEmployeeToEdit,() ->{
            // Resume session when the dialog is closed
            idleTimer.restart();
            countdownTimer.restart();
            sessionActive = true;
            loadInitialData();
            populateEmployeeTable(allEmployees);
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
                    loadInitialData();
                    populateEmployeeTable(allEmployees); // Refresh the table after the operation
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
        ConfigureTabsBasedOnPosition();
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

    private void populateEmployeeTable(List<Employee> filteredEmployees) {
        // Column names for the table
        String[] columns;
        if (Arrays.asList(accessPosition).contains("Administrator")) {
            columns = new String[]{"ID", "First Name", "Last Name", "Email", "Username", "Roles", "Active", "Locked"};
        } else {
            columns = new String[]{"ID", "Username", "First Name", "Last Name", "Email"};
        }

        // Create a table model and add columns
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Populate the table model with employee data
        for (Employee employee : filteredEmployees) {
            Object[] rowData;
            if (Arrays.asList(accessPosition).contains("Administrator")) {
                // Combine all roles into a single string
                StringBuilder roles = new StringBuilder();
                if (employee.getRoles() != null && !employee.getRoles().isEmpty()) {
                    for (Posn posn : employee.getRoles()) {
                        roles.append(posn.getPermissionLevel()).append(", ");
                    }
                    // Remove the trailing comma and space
                    if (roles.length() > 0) {
                        roles.setLength(roles.length() - 2);
                    }
                } else {
                    roles.append("None");
                }

                rowData = new Object[]{
                        employee.getId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getEmail(),
                        employee.getUsername(),
                        roles.toString(), // Display combined roles
                        employee.isActive() ? "Yes" : "No",
                        employee.getLocked() ? "Yes" : "No"
                };
            } else {
                if (!employee.isActive()) {
                    continue;
                }
                rowData = new Object[]{
                        employee.getId(),
                        employee.getUsername(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getEmail()
                };
            }
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
                    System.out.println("Selected Employee ID: " + employeeTableSelectedId);
                } else {
                    employeeTableSelectedId = -1;
                }
            }
        });
    }

    private void populateItemsTable(List<Item> filteredItems) {
        // Define the column names for the table
        String[] columns = {
                "ID",
                "Name",
                "SKU",
                "Description",
                "Category",
                "Weight",
                "Case Size",
                "Cost Price",
                "Retail Price",
                "Notes",
                "Active",
                "Image Location",
                "Supplier"
        };

        // Create a table model and add columns
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing of table cells
            }
        };

        // Populate the table model with item data
        for (Item item : filteredItems) {
            Object[] rowData = {
                    item.getId(),
                    item.getName(),
                    item.getSku(),
                    item.getDescription(),
                    item.getCategory().getCategoryName(), // Get category name
                    item.getWeight(),
                    item.getCaseSize(),
                    item.getCostPrice(),
                    item.getRetailPrice(),
                    item.getNotes(),
                    item.getActive() ? "Yes" : "No", // Convert active status to Yes/No
                    item.getImageLocation(),
                    item.getSupplier().getName() // Get supplier name
            };
            tableModel.addRow(rowData);
        }

        // Set the table model to the JTable
        ItemsTabTable.setModel(tableModel);

        // Allow selection of entire rows only
        ItemsTabTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a listener to handle row selection
        ItemsTabTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = ItemsTabTable.getSelectedRow();
                if (selectedRow != -1) {
                    int selectedItemId = (Integer) ItemsTabTable.getValueAt(selectedRow, 0); // Assuming column 0 is 'ID'
                    System.out.println("Selected Item ID: " + selectedItemId);
                    itemTableSelectedId = selectedItemId;
                    // Store or process the selected item ID as needed
                }
                else{
                    itemTableSelectedId = -1;
                }
            }
        });
    }

    private void populateEditPermissionsEmployeesList(List<Employee> employees) {
        // Ensure the JList is cleared before populating
        DefaultListModel<String> listModel = new DefaultListModel<>();

        if (employees != null && !employees.isEmpty()) {
            for (Employee employee : employees) {
                // Format the employee details for display in the list
                String employeeDetails = String.format("%d - %s, %s (%s) [%s]",
                        employee.getId(), employee.getLastName(), employee.getFirstName(), employee.getUsername(),
                        (employee.getSite() != null ? employee.getSite().getSiteName() : "No Site Assigned"));
                listModel.addElement(employeeDetails);
            }
        } else {
            listModel.addElement("No employees available.");
        }

        // Set the model to the JList
        lstEmployees.setModel(listModel);


    }


    private void populateEditPermissionsPositionList(List<Posn> positions) {
        pnlEditPermissionsRoles.removeAll(); // Clear any existing roles

        // Set the panel layout to BoxLayout for vertical alignment
        pnlEditPermissionsRoles.setLayout(new BoxLayout(pnlEditPermissionsRoles, BoxLayout.Y_AXIS));

        if (positions != null && !positions.isEmpty()) {
            for (Posn posn : positions) {
                JCheckBox chkRole = new JCheckBox(posn.getPermissionLevel());
                pnlEditPermissionsRoles.add(chkRole);
            }
        } else {
            pnlEditPermissionsRoles.add(new JLabel("No roles available."));
        }

        pnlEditPermissionsRoles.revalidate();
        pnlEditPermissionsRoles.repaint();
    }
}
