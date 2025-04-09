package views;


import models.*;
import utils.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * DashboardForm provides the main UI for the Bullseye Inventory Management System.
 * It dynamically adjusts available tabs and features based on the user's role.
 *
 * Features include:
 * - Role-based access control for Employees, Items, Reports, and Permissions.
 * - Employee management (add, edit, deactivate).
 * - Inventory management (edit, deactivate).
 * - Live search functionality for employees and items.
 * - Session timeout handling with activity tracking.
 * - Secure logout and session cleanup.
 */
public class DashboardForm {

    // =================== UI COMPONENTS ===================

    private JPanel ContentPane;
    private JTabbedPane DashboardTabPane;
    private JPanel SitesTab;
    private JPanel InventoryTab;
    private JPanel ReportsTab;
    private JPanel EditPermissionsTab;
    private JButton BtnRefresh;
    private JButton BtnLogout;
    private JLabel SPACER;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
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
    private JCheckBox chkInactiveEmployees;
    private JLabel lblEditPermissionsEmployeeDetails;
    private JTable tblEmployeesPermissions;
    private JTextField txtEditPermissionsEmployeeSearch;
    private JButton btnHelpDashboard;
    private JButton btnItemHelp;
    private JButton btnEmployeesHelp;
    private JButton btnHelpEditPermissions;
    private JPanel siteAdminCRUDPane;
    private JTable tblSites;
    private JTextField txtSiteSearch;
    private JButton btnSitesHelp;
    private JButton btnAddSite;
    private JButton btnEditSite;
    private JPanel InventoryCRUDPane;
    private JTable tblInventory;
    private JButton btnInventoryEdit;
    private JPanel pnlInventoryAdminSelectSite;
    private JComboBox cmbInventoryAdminSiteSelect;
    private JTextField txtInventorySearch;
    private JButton btnInventoryHelp;
    private JPanel OrdersTab;
    private JTextField txtOrdersSearch;
    private JCheckBox chkNewOrdersOnly;
    private JButton btnOrdersHelp;
    private JTable tblOrders;
    private JButton btnOrdersViewReceive;
    private JPanel pnlWarehouseMgrNotifications;
    private JLabel lblWHMgrNotificationsLine1;
    private JLabel lblWHMgrNotificationsLine2;
    private JComboBox cmbOrdersStatus;
    private JComboBox cmbOrdersLocation;
    private JLabel lblOrderSite;
    private JLabel lblOrderStatus;
    private JPanel SupplierCRUDPane;
    private JTable SupplierTabTable;
    private JPanel SupplierHeaderPane;
    private JButton btnAddSupplier;
    private JButton btnModifySupplier;
    private JTextField txtSupplierSearch;
    private JButton btnSuppliersHelp;
    private JPanel SuppliersTab;
    private JPanel TxnsTab;
    private JPanel TxnHeaderPane;
    private JTable tblTxns;
    private JPanel TxnCrudPane;
    private JButton btnEditTxn;
    private JTextField txtTxnsSearch;
    private JButton btnTxnsHelp;
    private JComboBox cmbTxnStatus;
    private JComboBox cmbTxnType;
    private JPanel LossReturnTab;
    private JPanel LossReturnCRUDPane;
    private JPanel LossReturnHeaderPane;
    private JTable tblLossReturn;
    private JButton btnCreateLossReturn;
    private JComboBox cmbLossReturnSite;
    private JTextField txtLossReturnSearch;
    private JTable tblSupplierOrders;
    private JPanel SupplierOrdersHeaderPane;
    private JPanel SupplierOrdersCRUDPane;
    private JPanel SupplierOrdersTab;
    private JButton btnCreateSupplierOrder;
    private JTextField txtSupplierOrdersSearch;
    private JButton btnSupplierOrdersHelp;
    private JButton btnAddItem;
    private JButton btnLossReturnHelp;
    private JButton btnViewLossReturn;
    private JButton btnSupplierOrderViewOrder;
    private JLabel lblLossReturnSite;

    // =================== FRAME VARIABLES ===================

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
    private List<Posn> EditPermissionsSelectedItems = new ArrayList<>();
    private List<Site> allSites;
    private int siteTableSelectedId = -1;
    private Site inventorySite;
    private int inventoryTableSelectedId = -1;
    private List<Inventory> allInventory;
    private List<Txn> allTxns;
    private int ordersTableSelectedId = -1;
    private String orderViewReceiveMode;
    private List<Supplier> allSuppliers;
    private int supplierTableSelectedId = -1;
    private List<Txn> allModTxns;
    private int modTxnTableSelectedId = -1;
    private Site lossReturnSite = null;
    private Site ordersSite = null;
    private List<Txn> allLossReturns;
    private List<Txn> allSupplierOrders;
    private int selectedLossReturn = -1;
    private int selectedSupplierOrder = -1;

    // =================== DASHBOARD INITIALIZATION & SETUP ===================

    /**
     * Displays the dashboard frame and initializes UI elements.
     *
     * @param currentLocation The point to center the frame at.
     */
    public void showDashboard(Point currentLocation) {
        frame = new JFrame("Bullseye IMS"); // Create the frame
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set close operation
        frame.setSize(1000, 635);                   // Set frame size
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

    /**
     * Configures dashboard tabs based on user roles.
     */
    private void ConfigureTabsBasedOnPosition() {
        hideAllTabs();

        txtEmployeeSearch.setText("");
        txtItemSearch.setText("");
        txtOrdersSearch.setText("");
        txtSiteSearch.setText("");
        txtInventorySearch.setText("");


        // Fetch roles as a single string and split into an array
        String permissionLevels = SessionManager.getInstance().getPermissionLevel(); // Example: "Administrator, Warehouse Manager"
        String[] roles = permissionLevels.split(",\\s*"); // Split by ", " (comma followed by optional whitespace)

        accessPosition = roles;
        adminCRUDpane.setVisible(false);
        siteAdminCRUDPane.setVisible(false);
        chkInactiveEmployees.setVisible(false);
        pnlInventoryAdminSelectSite.setVisible(false);
        //cmbInventoryAdminSiteSelect.setVisible(false);

        // Check roles and configure tabs accordingly
        if (Arrays.asList(roles).contains("Administrator")) {
            chkInactiveEmployees.setVisible(true);
            DashboardTabPane.add("Edit Permissions", EditPermissionsTab);
            adminCRUDpane.setVisible(true);
            siteAdminCRUDPane.setVisible(true);
            DashboardTabPane.add("Inventory", InventoryTab);
            pnlInventoryAdminSelectSite.setVisible(true);
            DashboardTabPane.add("Store Orders", OrdersTab);
            DashboardTabPane.add("Suppliers", SuppliersTab);
            DashboardTabPane.add("Transaction Records", TxnsTab);
            DashboardTabPane.add("Losses/Returns", LossReturnTab);
            DashboardTabPane.add("Supplier Orders", SupplierOrdersTab);
            DashboardTabPane.add("Items", ItemsTab);

            loadInitialData();
            populateItemsTable(allItems);
            populateEditPermissionsEmployeesTable(allEmployees);
            populateEditPermissionsPositionList(allPosns, null);
            populateOrdersTable(allTxns);
            updateOrdersTableBySearch();
            populateSuppliersTable(allSuppliers);
            updateSuppliersTableBySearch();
            populateTxnTable(allTxns);
            updateTxnTableBySearch();
            cmbInventoryAdminSiteSelect.setVisible(true);
            updateLossReturnTableBySearch();
            updateSupplierOrderTableBySearch();
        }

        if (Arrays.asList(roles).contains("Warehouse Manager")) {
            DashboardTabPane.add("Items", ItemsTab);
            DashboardTabPane.add("Inventory", InventoryTab);
            DashboardTabPane.add("Orders", OrdersTab);
            DashboardTabPane.add("Suppliers", SuppliersTab);
            DashboardTabPane.add("Losses/Returns", LossReturnTab);
            DashboardTabPane.add("Supplier Orders", SupplierOrdersTab);


            loadInitialData();
            populateItemsTable(allItems);
            populateOrdersTable(allTxns);
            updateOrdersTableBySearch();
            populateSuppliersTable(allSuppliers);
            updateSuppliersTableBySearch();
            updateLossReturnTableBySearch();
            updateSupplierOrderTableBySearch();
        }

        if (Arrays.asList(roles).contains("Warehouse Worker")) {
            DashboardTabPane.add("Orders", OrdersTab);

            loadInitialData();
            populateOrdersTable(allTxns);
            updateOrdersTableBySearch();
        }

        if (Arrays.asList(roles).contains("Store Manager")) {
            DashboardTabPane.add("Inventory", InventoryTab);
            DashboardTabPane.add("Orders", OrdersTab);
            DashboardTabPane.add("Losses/Returns", LossReturnTab);

            loadInitialData();
            pnlInventoryAdminSelectSite.setVisible(true);
            //populateEmployeeTable(allEmployees);
            populateOrdersTable(allTxns);
            updateOrdersTableBySearch();
            updateLossReturnTableBySearch();
        }

        if (Arrays.asList(roles).contains("Financial Manager")) {
            DashboardTabPane.add("Reports", ReportsTab);

            //loadInitialData();
            // Add any additional logic for Financial Manager
        }

        if (roles.length == 1 && Arrays.asList(roles).contains("Online Customer")) {
            // If the user has no roles or is not permitted, display access denied message
            JOptionPane.showMessageDialog(
                    frame,
                    "Access denied: You do not have permission to access this area.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            Logout();
        }

        if (roles.length == 0) {
            // If the user has no roles or is not permitted, display access denied message
            JOptionPane.showMessageDialog(
                    frame,
                    "Access denied: You do not have permission to access this area.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            Logout();
        }

        loadInitialData();
        DashboardTabPane.add("Employees", EmployeesTab);
        DashboardTabPane.add("Sites", SitesTab);
        populateEmployeeTable(allEmployees);
        populateSitesTable(allSites);

    }

    /**
     * Loads all initial data (employees, items, roles) based on user permissions.
     */
    private void loadInitialData() {
        allEmployees = EmployeeRequests.fetchEmployees();
        allSites = SiteRequests.fetchSites();
        allModTxns = TxnsModsRequests.fetchAllTransactions();

        lblEditPermissionsEmployeeDetails.setText("Please select an employee.");
        cmbLossReturnSite.setVisible(false);
        lblLossReturnSite.setVisible(false);


        if (Arrays.asList(accessPosition).contains("Administrator")) {
            allPosns = PositionRequests.fetchPositions();
            allSuppliers = SupplierUtil.fetchAllSuppliers(true);
            allLossReturns = fetchAllLossReturns();
            allItems = ItemRequests.fetchItems();
            allSupplierOrders = SupplierOrderRequests.fetchSupplierOrders();
            cmbInventoryAdminSiteSelect.removeAllItems();
            loadTypeComboBox();
            loadStatusTXNSComboBox();
            loadOrdersLocationComboBox();
            cmbLossReturnSite.addItem("ALL");
            for(Site site : allSites) {
                if(site.isActive()){
                    cmbInventoryAdminSiteSelect.addItem(site);
                    cmbLossReturnSite.addItem(site);
                }
            }
            lossReturnSite = null;
            cmbLossReturnSite.setVisible(true);
            lblLossReturnSite.setVisible(true);
            cmbLossReturnSite.addActionListener(e -> {
                int selectedIndex = cmbLossReturnSite.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Object selected = cmbLossReturnSite.getSelectedItem();
                    if (selected instanceof Site) {
                        lossReturnSite = (Site) selected;
                    } else {
                        lossReturnSite = null; // "ALL" selected
                    }
                    updateLossReturnTableBySearch();
                }
            });
            ////System.out.println("Fetching txns");
            allTxns = TxnRequests.fetchAllOrders().stream()
                    .filter(txn -> txn.getTxnType().getTxnType().matches("Store Order|Emergency Order|Backorder"))
                    .collect(Collectors.toList());
            ////System.out.println(allTxns);            btnOrdersViewReceive.setText("View/Modify Order");
            orderViewReceiveMode = "RECEIVE";
            pnlWarehouseMgrNotifications.setVisible(true);
            updateWHMgrNotifications();
        }

        if (Arrays.asList(accessPosition).contains("Warehouse Manager")) {
            allItems = ItemRequests.fetchItems();
            allSuppliers = SupplierUtil.fetchAllSuppliers(true);
            allSupplierOrders = SupplierOrderRequests.fetchSupplierOrders();
            ////System.out.println("Fetching txns");
            allTxns = TxnRequests.fetchAllOrders().stream()
                    .filter(txn -> txn.getTxnType().getTxnType().matches("Store Order|Emergency Order|Backorder"))
                    .collect(Collectors.toList());
            ////System.out.println(allTxns);
            btnOrdersViewReceive.setText("View/Modify Order");
            orderViewReceiveMode = "RECEIVE";
            pnlWarehouseMgrNotifications.setVisible(true);
            updateWHMgrNotifications();
            loadOrdersLocationComboBox();
            allLossReturns = fetchAllLossReturns();
            lossReturnSite = SessionManager.getInstance().getSite();
        }

        if (Arrays.asList(accessPosition).contains("Warehouse Worker") && Arrays.asList(accessPosition).size() == 1) {
            ////System.out.println("Fetching txns");
            allTxns = TxnRequests.fetchAllOrders().stream()
                    .filter(txn -> txn.getTxnType().getTxnType().matches("Store Order|Emergency Order|Backorder"))
                    .collect(Collectors.toList());
            ////System.out.println(allTxns);            btnOrdersViewReceive.setText("View/Modify Order");
            orderViewReceiveMode = "RECEIVE";
            loadStatusOrdersComboBox();
            loadOrdersLocationComboBox();
            cmbOrdersLocation.setSelectedItem("ALL");
            cmbOrdersStatus.setVisible(false);
            pnlWarehouseMgrNotifications.setVisible(false);
            cmbOrdersLocation.setVisible(false);
            lblOrderSite.setVisible(false);
            lblOrderStatus.setVisible(false);
        }

        if (Arrays.asList(accessPosition).contains("Warehouse Manager") || Arrays.asList(accessPosition).contains("Administrator") || Arrays.asList(accessPosition).contains("Store Manager")) {
            cmbInventoryAdminSiteSelect.removeAllItems();
            for(Site site : allSites) {
                if(site.isActive()){
                    cmbInventoryAdminSiteSelect.addItem(site);
                }
            }
            pnlInventoryAdminSelectSite.setVisible(true);
            inventorySite = SessionManager.getInstance().getSite();
            //  Ensure site is not null and exists in ComboBox before setting it
            if (inventorySite != null) {
                for (int i = 0; i < cmbInventoryAdminSiteSelect.getItemCount(); i++) {
                    Site existingSite = (Site) cmbInventoryAdminSiteSelect.getItemAt(i);
                    //  Compare using siteName for debugging
                    if (existingSite.getSiteName().equals(inventorySite.getSiteName())) {
                        cmbInventoryAdminSiteSelect.setSelectedItem(existingSite);
                        break; // ✅ Stop once we find a match
                    }
                }
            }

            loadInventoryBySite(inventorySite.getId());
            loadStatusOrdersComboBox();
            cmbOrdersLocation.setSelectedItem("ALL");
            updateLossReturnTableBySearch();
        }

        if (Arrays.asList(accessPosition).contains("Store Manager") && !Arrays.asList(accessPosition).contains("Warehouse Manager") && !Arrays.asList(accessPosition).contains("Administrator")) {
            inventorySite = SessionManager.getInstance().getSite();
            loadInventoryBySite(inventorySite.getId());
            updateInventoryTableBySearch();
            ordersSite = SessionManager.getInstance().getSite();
            cmbOrdersLocation.setVisible(false);
            lblOrderSite.setVisible(false);

            //  Fetch only orders for this site
            //allTxns = TxnRequests.fetchOrdersBySite(inventorySite.getId());
            //System.out.println("Fetching txns");
            allTxns = TxnRequests.fetchAllOrders().stream()
                    .filter(txn -> txn.getTxnType().getTxnType().matches("Store Order|Emergency Order|Backorder"))
                    .collect(Collectors.toList());
            //System.out.println(allTxns);
            btnOrdersViewReceive.setText("View Order");
            orderViewReceiveMode = "VIEW";
            cmbOrdersLocation.setSelectedItem(inventorySite.getSiteName());
            allLossReturns = fetchAllLossReturns();
            lossReturnSite = SessionManager.getInstance().getSite();
        }
    }

    /**
     * Hides all tabs in the dashboard.
     */
    public void hideAllTabs(){
        while (DashboardTabPane.getTabCount() > 0) {
            DashboardTabPane.remove(0); // Always remove the first tab
        }
    }

    /**
     * Loads and sets the Bullseye logo in the UI.
     */
    private void SetupBullseyeLogo() {
        String logoPath = "/bullseye.jpg"; // Classpath-relative path
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(100, 100
                , Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        logoLabel.setIcon(resizedIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setText("");
    }

    /**
     * Returns the main panel and initializes UI elements.
     *
     * @return JPanel The main dashboard panel.
     */
    public JPanel getMainPanel() {
        // Check if a user is logged in
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(
                    frame,
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
            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.NO_OPTION) return;
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

        btnEditPermissions.addActionListener(e -> {
            editPermissions();
        });

        btnDeleteItem.addActionListener(e -> {
            deactivateItem();
        });

        btnEditItem.addActionListener(e -> {
            editItem();
        });
        btnAddItem.addActionListener(e -> {
            createItem();
        });

        // Allow Cancel/Exit to be accessed via 'ESC' key
        BtnLogout.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        BtnLogout.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BtnLogout.doClick();
            }
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

        txtEditPermissionsEmployeeSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateEditEmployeesTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateEditEmployeesTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateEditEmployeesTableBySearch();
            }
        });

        btnHelpDashboard.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.DASHBOARD_HELP,"Dashboard Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnHelpEditPermissions.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.EDIT_PERMISSIONS_HELP,"Edit Permissions Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnEmployeesHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.EMPLOYEES_HELP,"Employees Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnSitesHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.SITES_HELP,"Sites Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnItemHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.ITEMS_HELP,"Items Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnOrdersHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.DASHBOARD_ORDER_VIEWER_HELP,"Orders Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnAddSite.addActionListener(e -> addSite());

        btnEditSite.addActionListener(e -> editSite());

        txtSiteSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateSiteTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateSiteTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateSiteTableBySearch();
            }
        });

        txtInventorySearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateInventoryTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateInventoryTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateInventoryTableBySearch();
            }
        });

        cmbInventoryAdminSiteSelect.addActionListener(e -> {
            Site selectedSite = (Site) cmbInventoryAdminSiteSelect.getSelectedItem(); // Get selected site
            if (selectedSite != null) {
                inventorySite = selectedSite; // Store selected site globally
                loadInventoryBySite(inventorySite.getId()); // Reload inventory
                txtSiteSearch.setText("");
                btnInventoryEdit.setEnabled(true);

                if (!Arrays.asList(accessPosition).contains("Administrator")) {

                    Integer userSiteId = SessionManager.getInstance().getSite().getId();
                    Integer selectedSiteId = selectedSite.getId();

                    if (!userSiteId.equals(selectedSiteId)) {
                        btnInventoryEdit.setEnabled(false);
                    } else {
                        btnInventoryEdit.setEnabled(true);
                    }
                }
            }
        });

        btnInventoryEdit.addActionListener(e -> editInventory());

        txtOrdersSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateOrdersTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateOrdersTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateOrdersTableBySearch();
            }
        });

        btnOrdersViewReceive.addActionListener(e -> {
            handleOrderAction();
        });

        chkNewOrdersOnly.addActionListener(e -> {
            updateOrdersTableBySearch();
        });

        txtSupplierSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateSuppliersTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateSuppliersTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateSuppliersTableBySearch();
            }
        });

        btnAddSupplier.addActionListener(e -> addSupplier());

        btnModifySupplier.addActionListener(e -> editSupplier());

        txtTxnsSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateTxnTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateTxnTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateTxnTableBySearch();
            }
        });

        btnEditTxn.addActionListener(e -> {
            editTransaction();
        });

        btnTxnsHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.TXN_TABLE_VIEW,"Transaction Records Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnSuppliersHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.SUPPLIER_TABLE_VIEW,"Suppliers Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnCreateLossReturn.addActionListener(e -> CreateLossReturn());

        txtInventorySearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateInventoryTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateInventoryTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateInventoryTableBySearch();
            }
        });

        txtLossReturnSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateLossReturnTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateLossReturnTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateLossReturnTableBySearch();
            }
        });

        btnCreateSupplierOrder.addActionListener(e -> {
            CreateSupplierOrder();
        });

        txtSupplierOrdersSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateSupplierOrderTableBySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateSupplierOrderTableBySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateSupplierOrderTableBySearch();
            }
        });

        btnSupplierOrdersHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.SUPPLIER_ORDERS_DASHBOARD,"Supplier Orders Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnLossReturnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.LOSSES_RETURNS_DASHBOARD,"Loss/Return Help",JOptionPane.INFORMATION_MESSAGE);
        });

        btnViewLossReturn.addActionListener(e -> {
            viewLossReturn();
        });

        btnSupplierOrderViewOrder.addActionListener(e -> {
            viewSupplierOrder();
        });




        return ContentPane;
    }

    // =================== SUPPLIER ORDER MANAGEMENT ================

    private void updateSupplierOrderTableBySearch() {
        if (allSupplierOrders == null) return;

        String search = txtSupplierOrdersSearch.getText().trim().toLowerCase();

        List<Txn> filtered = allSupplierOrders.stream()
                .filter(txn -> {
                    // 🔹 If no search text, we're done
                    if (search.isEmpty()) {
                        return true;
                    }

                    // 🔹 Otherwise apply search logic
                    String status = txn.getTxnStatus() != null ? txn.getTxnStatus().getStatusName().toLowerCase() : "";
                    String createdDate = txn.getCreatedDate() != null ? txn.getCreatedDate().toString().toLowerCase() : "";
                    String createdBy = txn.getEmployee() != null ? txn.getEmployee().getUsername().toLowerCase() : "";
                    String txnId = String.valueOf(txn.getId());

                    // Search through status, created date, created by, and txn ID
                    return status.contains(search)
                            || createdDate.contains(search)
                            || createdBy.contains(search)
                            || txnId.contains(search);
                })
                .toList();

        populateSupplierOrderTable(filtered);
    }

    private void populateSupplierOrderTable(List<Txn> txns) {
        String[] columnNames = {"Record ID", "Status", "Created", "Created By (Username)" };
        Object[][] rowData = new Object[txns.size()][columnNames.length];

        for (int i = 0; i < txns.size(); i++) {
            Txn txn = txns.get(i);
            rowData[i][0] = txn.getId();
            rowData[i][1] = txn.getTxnStatus() != null ? txn.getTxnStatus().getStatusName() : "N/A";
            rowData[i][2] = txn.getCreatedDate() != null ? formatDate(txn.getCreatedDate()) : "N/A";
            rowData[i][3] = txn.getEmployee() != null ? txn.getEmployee().getUsername() : "N/A";
        }

        tblSupplierOrders.setModel(new DefaultTableModel(rowData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        tblSupplierOrders.getTableHeader().setReorderingAllowed(false);

        // Allow single row selection
        tblSupplierOrders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Handle row selection
        tblSupplierOrders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblSupplierOrders.getSelectedRow();
                if (selectedRow != -1) {
                    selectedSupplierOrder = (Integer) tblSupplierOrders.getValueAt(selectedRow, 0); // Get selected Txn ID
                } else {
                    selectedSupplierOrder = -1;
                }
            }
        });
    }

    private void CreateSupplierOrder() {
        SwingUtilities.invokeLater(() ->
                new SupplierOrderForm().showSupplierOrderForm(
                        frame,
                        frame.getLocation(),
                        () -> {
                            loadInitialData();
                            updateSupplierOrderTableBySearch();
                        }
                )
        );
    }

    private void viewSupplierOrder() {
        // Ensure a transaction is selected
        if (selectedSupplierOrder == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a Supplier Order to view.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Find the selected transaction
        Txn txntoView = null;
        for (Txn txn : allModTxns) {
            if (txn.getId() == selectedSupplierOrder) {
                txntoView = txn;
                break;
            }
        }

        Txn finalTxn = txntoView;

        SwingUtilities.invokeLater(()-> new ViewReceiveOrder().showViewReceveOrderForm(frame, frame.getLocation(), orderViewReceiveMode, finalTxn,() ->{
            // Resume session when the dialog is closed
//            idleTimer.restart();
//            countdownTimer.restart();
//            sessionActive = true;
            loadInitialData();;
            updateSupplierOrderTableBySearch();
        }));
    }


    // =================== LOSS/RETURN MANAGEMENT ===================

    private List<Txn> fetchAllLossReturns() {
        List<Txn> allTxns = TxnsModsRequests.fetchAllTransactions();
        List<Txn> filteredTxns = new ArrayList<>();

        for (Txn txn : allTxns) {
            if(Objects.equals(txn.getTxnType().getTxnType(), "Loss") ||
                    Objects.equals(txn.getTxnType().getTxnType(), "Return") ||
                    Objects.equals(txn.getTxnType().getTxnType(), "Damage")) {
                filteredTxns.add(txn);
            }
        }
        //System.out.println(filteredTxns);
        return filteredTxns;
    }

    private void CreateLossReturn() {
        System.out.println(lossReturnSite);
        if(lossReturnSite == null) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Please select a specific site before creating a Loss/Return transaction.",
                    "Site Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        SwingUtilities.invokeLater(() ->
                new ReturnLossForm().showReturnLossForm(
                        frame,
                        frame.getLocation(),
                        lossReturnSite,
                        () -> {
                            // Optional: Refresh loss/return table if you’re showing it in the dashboard
                            loadInitialData();
                            updateLossReturnTableBySearch();
                        }
                )
        );
    }

    private void updateLossReturnTableBySearch() {
        if (allLossReturns == null) return;

        String search = txtLossReturnSearch.getText().trim().toLowerCase();

        List<Txn> filtered = allLossReturns.stream()
                .filter(txn -> {
                    // 🔹 Only filter by site if not viewing ALL
                    if (lossReturnSite != null && txn.getSiteFrom().getId() != lossReturnSite.getId()) {
                        return false;
                    }

                    // 🔹 If no search text, we're done
                    if (search.isEmpty()) {
                        return true;
                    }

                    // 🔹 Otherwise apply search logic
                    String siteName = txn.getSiteFrom().getSiteName().toLowerCase();
                    String txnType = txn.getTxnType().getTxnType().toLowerCase();
                    String notes = txn.getNotes() != null ? txn.getNotes().toLowerCase() : "";

                    return siteName.contains(search)
                            || txnType.contains(search)
                            || notes.contains(search)
                            || String.valueOf(txn.getId()).contains(search);
                })
                .toList();

        populateLossReturnTable(filtered);
    }

    private void populateLossReturnTable(List<Txn> txns) {
        String[] columnNames = {"Record ID", "Site", "Type", "Created", "Notes"};
        Object[][] rowData = new Object[txns.size()][columnNames.length];

        for (int i = 0; i < txns.size(); i++) {
            Txn txn = txns.get(i);
            rowData[i][0] = txn.getId();
            rowData[i][1] = txn.getSiteFrom().getSiteName();
            rowData[i][2] = txn.getTxnType().getTxnType();
            rowData[i][3] = txn.getCreatedDate() != null ? formatDate(txn.getCreatedDate()) : "N/A";
            rowData[i][4] = txn.getNotes() != null ? txn.getNotes() : "";
        }

        tblLossReturn.setModel(new DefaultTableModel(rowData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        tblLossReturn.getTableHeader().setReorderingAllowed(false);
        // Allow single row selection
        tblLossReturn.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Handle row selection
        tblLossReturn.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblLossReturn.getSelectedRow();
                if (selectedRow != -1) {
                    selectedLossReturn = (Integer) tblLossReturn.getValueAt(selectedRow, 0); // Get selected Txn ID
                } else {
                    selectedLossReturn = -1;
                }
            }
        });
    }

    private void viewLossReturn() {
        // Ensure a transaction is selected
        if (selectedLossReturn == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a Loss/Return to view.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Find the selected transaction
        Txn txntoView = null;
        for (Txn txn : allModTxns) {
            if (txn.getId() == selectedLossReturn) {
                txntoView = txn;
                break;
            }
        }

        Txn finalTxn = txntoView;

        SwingUtilities.invokeLater(()-> new ViewReceiveOrder().showViewReceveOrderForm(frame, frame.getLocation(), orderViewReceiveMode, finalTxn,() ->{
            // Resume session when the dialog is closed
//            idleTimer.restart();
//            countdownTimer.restart();
//            sessionActive = true;
            loadInitialData();;
            updateLossReturnTableBySearch();
        }));

    }


    // =================== TXN RECORD MANAGEMENT ===================

    /**
     * Populates the JTable with transaction data.
     *
     * @param filteredTxns List of transactions to display.
     */
    private void populateTxnTable(List<Txn> filteredTxns) {
        // Define column headers
        String[] columns = {
                "Txn ID",
                "Status",
                "Txn Type",
                "To Site",
                "From Site",
                "Ship Date",
                "Delivery ID",
                "Bar Code",
                "Emergency"
        };

        // Create a table model
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent cell editing
            }
        };

        // Populate table rows
        for (Txn txn : filteredTxns) {
            //System.out.println(txn);
            String shipDateFormatted = txn.getShipDate() != null ? formatDate(txn.getShipDate()) : "N/A";

            Object[] rowData = {
                    txn.getId(),
                    txn.getTxnStatus().getStatusName(),
                    txn.getTxnType().getTxnType(),
                    txn.getSiteTo().getSiteName(),
                    txn.getSiteFrom() != null ? txn.getSiteFrom().getSiteName() : "N/A",
                    txn.getShipDate() != null ? shipDateFormatted : "Not Set",
                    txn.getDeliveryID() != null ? txn.getDeliveryID() : "None",
                    txn.getBarCode(),
                    txn.isEmergencyDelivery() ? "Yes" : "No"
            };
            tableModel.addRow(rowData);
        }

        // Set model to the JTable
        tblTxns.setModel(tableModel);

        // Hide the Txn ID column
        tblTxns.getColumnModel().getColumn(0).setMinWidth(0);
        tblTxns.getColumnModel().getColumn(0).setMaxWidth(0);
        tblTxns.getColumnModel().getColumn(0).setWidth(0);

        // Allow single row selection
        tblTxns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Handle row selection
        tblTxns.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblTxns.getSelectedRow();
                if (selectedRow != -1) {
                    modTxnTableSelectedId = (Integer) tblTxns.getValueAt(selectedRow, 0); // Get selected Txn ID
                } else {
                    modTxnTableSelectedId = -1;
                }
            }
        });
    }

    /**
     * Updates the transaction table based on search input.
     */
    private void updateTxnTableBySearch() {
        //System.out.println("HELLLLLO");
        // If search field is empty, display all transactions

        String search = txtTxnsSearch.getText().trim().toLowerCase();
        List<Txn> filteredTxns = new ArrayList<>();

        // ✅ Ensure selectedStatus is never null (default to "ALL")
        Object selectedStatusObj = cmbTxnStatus.getSelectedItem();
        String selectedStatus = (selectedStatusObj != null) ? selectedStatusObj.toString() : "ALL";
        //System.out.println(selectedStatus);

        // ✅ Ensure selected type is never null (default to "ALL")
        Object selectedTypeObj = cmbTxnType.getSelectedItem();
        String selectedType = (selectedTypeObj != null) ? selectedTypeObj.toString() : "ALL";
        //System.out.println(selectedType);


        // Filter transactions based on search term
        for (Txn txn : allModTxns) {
            String orderStatus = txn.getTxnStatus().getStatusName();
            String orderType = txn.getTxnType().getTxnType();

            // ✅ Apply Status Filter (Skip if "ALL" is selected)
            if (!"ALL".equalsIgnoreCase(selectedStatus) && !orderStatus.equalsIgnoreCase(selectedStatus)) {
                continue;
            }

            // ✅ Apply Status Filter (Skip if "ALL" is selected)
            if (!"ALL".equalsIgnoreCase(selectedType) && !orderType.equalsIgnoreCase(selectedType)) {
                continue;
            }

            if (search.isEmpty() ||
                    txn.getTxnStatus().getStatusName().toLowerCase().contains(search) ||
                    txn.getTxnType().getTxnType().toLowerCase().contains(search) ||
                    txn.getSiteTo().getSiteName().toLowerCase().contains(search) ||
                    (txn.getSiteFrom() != null && txn.getSiteFrom().getSiteName().toLowerCase().contains(search)) ||
                    (txn.getBarCode() != null && txn.getBarCode().toLowerCase().contains(search)) ||
                    (txn.getShipDate() != null && txn.getShipDate().toString().contains(search)) ||
                    (txn.getDeliveryID() != null && txn.getDeliveryID().toString().contains(search)) ||
                    (txn.isEmergencyDelivery() ? "yes" : "no").contains(search)) {

                filteredTxns.add(txn);
            }
        }

        // Update table with filtered transactions
        populateTxnTable(filteredTxns);
    }

    private void editTransaction() {
        // Ensure a transaction is selected
        if (modTxnTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a transaction to modify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Find the selected transaction
        Txn txnToEdit = null;
        for (Txn txn : allModTxns) {
            if (txn.getId() == modTxnTableSelectedId) {
                txnToEdit = txn;
                break;
            }
        }

        // Ensure the transaction exists
        if (txnToEdit == null) {
            JOptionPane.showMessageDialog(frame, "Selected transaction not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Block editing if status is CANCELLED or COMPLETE
        if ("CANCELLED".equalsIgnoreCase(txnToEdit.getTxnStatus().getStatusName()) ||
                "DELIVERED".equalsIgnoreCase(txnToEdit.getTxnStatus().getStatusName()) ||
                "IN TRANSIT".equalsIgnoreCase(txnToEdit.getTxnStatus().getStatusName()) ||
                "REJECTED".equalsIgnoreCase(txnToEdit.getTxnStatus().getStatusName()) ||
                "COMPLETE".equalsIgnoreCase(txnToEdit.getTxnStatus().getStatusName())) {
            JOptionPane.showMessageDialog(frame,
                    "This transaction cannot be modified because it is already " + txnToEdit.getTxnStatus().getStatusName() + ".",
                    "Edit Restricted", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Open the Edit Transaction Form
        Txn finalTxnToEdit = txnToEdit;
        SwingUtilities.invokeLater(() ->
                new EditTxnForm().showEditTxnForm(
                        frame,
                        frame.getLocation(),
                        finalTxnToEdit,
                        () -> {
                            // Refresh table after editing transaction
                            loadInitialData();
                            populateTxnTable(allModTxns);
                        }
                )
        );
    }


    // =================== SUPPLIER MANAGEMENT MANAGEMENT ===================

    private void populateSuppliersTable(List<Supplier> filteredSuppliers) {
        // Define column names for the table
        String[] columns = {
                "ID",
                "Supplier Name",
                "Address",
                "City",
                "Province",
                "Country",
                "Postal Code",
                "Phone",
                "Contact Person",
                "Active",
                "Notes"
        };

        // Create a table model
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing of table cells
            }
        };

        // Populate the table model with supplier data
        for (Supplier supplier : filteredSuppliers) {
            Object[] rowData = {
                    supplier.getId(),
                    supplier.getName(),
                    supplier.getAddress1() +
                            (supplier.getAddress2() != null && !supplier.getAddress2().isEmpty()
                                    ? " " + supplier.getAddress2() : ""),
                    supplier.getCity(),
                    supplier.getProvinceId(), // Assuming this holds province name
                    supplier.getCountry(),
                    supplier.getPostalcode(),
                    supplier.getPhone(),
                    supplier.getContact(),
                    supplier.getActive() ? "Yes" : "No",
                    supplier.getNotes()
            };
            tableModel.addRow(rowData);
        }

        // Set the table model to the JTable
        SupplierTabTable.setModel(tableModel);

        // Hide the first column (ID)
        SupplierTabTable.getColumnModel().getColumn(0).setMinWidth(0);
        SupplierTabTable.getColumnModel().getColumn(0).setMaxWidth(0);
        SupplierTabTable.getColumnModel().getColumn(0).setWidth(0);

        // Allow selection of entire rows only
        SupplierTabTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a listener to handle row selection
        SupplierTabTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = SupplierTabTable.getSelectedRow();
                if (selectedRow != -1) {
                    supplierTableSelectedId = (Integer) SupplierTabTable.getValueAt(selectedRow, 0); // Get supplier ID
                } else {
                    supplierTableSelectedId = -1;
                }
            }
        });
    }

    /**
     * Updates the suppliers table based on the search field input.
     */
    private void updateSuppliersTableBySearch() {
        // If the search bar is empty, populate the table with all suppliers
        if (txtSupplierSearch.getText().trim().isEmpty()) {
            populateSuppliersTable(allSuppliers); // Populate with the full list of suppliers
            return;
        }

        String search = txtSupplierSearch.getText().trim().toLowerCase(); // Get search term and convert to lowercase
        List<Supplier> filteredSuppliers = new ArrayList<>();

        // Loop through all suppliers and filter based on search term
        for (Supplier supplier : allSuppliers) {
            if (supplier.getName().toLowerCase().contains(search) ||  // Match Supplier Name
                    supplier.getAddress1().toLowerCase().contains(search) ||  // Match Address
                    (supplier.getAddress2() != null && supplier.getAddress2().toLowerCase().contains(search)) ||
                    (supplier.getCity() != null && supplier.getCity().toLowerCase().contains(search)) || // Match City
                    (supplier.getProvinceId() != null && supplier.getProvinceId().toLowerCase().contains(search)) || // Match Province
                    (supplier.getCountry() != null && supplier.getCountry().toLowerCase().contains(search)) || // Match Country
                    (supplier.getPostalcode() != null && supplier.getPostalcode().toLowerCase().contains(search)) || // Match Postal Code
                    (supplier.getPhone() != null && supplier.getPhone().toLowerCase().contains(search)) || // Match Phone
                    (supplier.getContact() != null && supplier.getContact().toLowerCase().contains(search)) || // Match Contact Person
                    (supplier.getNotes() != null && supplier.getNotes().toLowerCase().contains(search)) || // Match Notes
                    (supplier.getActive() ? "yes" : "no").contains(search)) { // Match Active status

                filteredSuppliers.add(supplier); // Add matching supplier to the filtered list
            }
        }

        // Populate the table with the filtered suppliers
        populateSuppliersTable(filteredSuppliers);
    }

    /**
     * Opens the form to add a new supplier.
     */
    private void addSupplier() {
        SwingUtilities.invokeLater(() ->
                new AddEditSupplierForm().showAddEditSupplierForm(
                        frame,
                        frame.getLocation(),
                        "ADD",
                        null,
                        () -> {
                            // Refresh table after adding supplier
                            loadInitialData();
                            populateSuppliersTable(allSuppliers);
                        }
                )
        );
    }

    /**
     * Opens the form to edit the selected supplier.
     */
    private void editSupplier() {
        // Ensure a supplier is selected
        if (supplierTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a supplier to modify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Find the supplier in the list
        Supplier supplierToEdit = null;
        for (Supplier supplier : allSuppliers) {
            if (supplier.getId().equals(supplierTableSelectedId)) {
                supplierToEdit = supplier;
                break;
            }
        }

        // Ensure a valid supplier was found
        if (supplierToEdit == null) {
            JOptionPane.showMessageDialog(frame, "Selected supplier not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Open the edit form
        Supplier finalSupplierToEdit = supplierToEdit;
        SwingUtilities.invokeLater(() ->
                new AddEditSupplierForm().showAddEditSupplierForm(
                        frame,
                        frame.getLocation(),
                        "EDIT",
                        finalSupplierToEdit,
                        () -> {
                            // Refresh table after editing supplier
                            loadInitialData();
                            populateSuppliersTable(allSuppliers);
                        }
                )
        );
    }


    // =================== INVENTORY MANAGEMENT ===================

    private void loadInventoryBySite(int siteID) {
        //new Thread(() -> {
            // Fetch inventory data from backend
            List<Inventory> fetchedInventory = InventoryRequests.fetchInventoryBySite(siteID);

            // Store in class-level variable
            allInventory = fetchedInventory;

            ////System.out.println(fetchedInventory.size());

            // Populate the table on the Swing UI thread
            SwingUtilities.invokeLater(() -> populateInventoryTable(allInventory));
        //}).start();
    }

    /**
     * Opens the form to edit the selected inventory item.
     */
    private void editInventory() {
        // Ensure an inventory item is selected
        if (inventoryTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an inventory item to modify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Find the selected inventory item from allInventory
        Inventory selectedInventory = null;
        for (Inventory inventory : allInventory) {
            if (inventory.getItemID() == inventoryTableSelectedId) {
                selectedInventory = inventory;
                break;
            }
        }
        if (selectedInventory == null) {
            JOptionPane.showMessageDialog(frame, "Selected inventory item not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Arrays.asList(accessPosition).contains("Administrator")) {

            Integer userSiteId = SessionManager.getInstance().getSite().getId();
            Integer inventoryId = selectedInventory.getSiteID();

            if (!userSiteId.equals(inventoryId)) {
                JOptionPane.showMessageDialog(
                        frame,
                        "You can only edit inventory for your assigned site.",
                        "Access Denied",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        }


        // Open the EditInventoryForm
        Inventory finalSelectedInventory = selectedInventory;
        SwingUtilities.invokeLater(() ->
                new EditInventoryForm().showItemEditForm(
                        frame,
                        frame.getLocation(),
                        finalSelectedInventory,
                        inventorySite,  // Pass the currently selected site
                        () -> {
                            // Refresh inventory data when the dialog closes
                            loadInventoryBySite(inventorySite.getId());
                        }
                )
        );
    }

    // =================== EMPLOYEE MANAGEMENT ===================

    /**
     * Opens the form to add a new employee.
     */
    private void addEmployee() {
//        sessionActive = false;
//        idleTimer.stop();
//        countdownTimer.stop();

        SwingUtilities.invokeLater(()-> new AddEditEmployeeForm().showAddEditEmployeeForm(frame, frame.getLocation(), "ADD", null,() ->{
            // Resume session when the dialog is closed
//            idleTimer.restart();
//            countdownTimer.restart();
//            sessionActive = true;
            loadInitialData();
            populateEmployeeTable(allEmployees);
        }));

    }

    /**
     * Opens the form to edit the selected employee.
     */
    private void editEmployee() {
        //get employee
        Employee employeeToEdit = null;

        if (employeeTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an employee to modify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Employee> allEmployees = EmployeeRequests.fetchEmployees();
        for (Employee employee : allEmployees) {
            if (employee.getId() == employeeTableSelectedId) {
                employeeToEdit = employee;
            }
        }

//        sessionActive = false;
//        idleTimer.stop();
//        countdownTimer.stop();

        Employee finalEmployeeToEdit = employeeToEdit;

        SwingUtilities.invokeLater(()-> new AddEditEmployeeForm().showAddEditEmployeeForm(frame, frame.getLocation(), "EDIT", finalEmployeeToEdit,() ->{
            // Resume session when the dialog is closed
//            idleTimer.restart();
//            countdownTimer.restart();
//            sessionActive = true;
            loadInitialData();
            populateEmployeeTable(allEmployees);
            lstEmployees.clearSelection();
        }));

    }

    /**
     * Deactivates the selected employee.
     */
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
            String result = EmployeeRequests.deactivateEmployee(employeeTableSelectedId);

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

    // =================== SITE MANAGEMENT ===================

    /**
     * Opens the form to add a new employee.
     */
    private void addSite() {
//        sessionActive = false;
//        idleTimer.stop();
//        countdownTimer.stop();

        SwingUtilities.invokeLater(()-> new AddEditSiteForm().showAddEditSiteForm(frame, frame.getLocation(), "ADD", null,() ->{
            // Resume session when the dialog is closed
//            idleTimer.restart();
//            countdownTimer.restart();
//            sessionActive = true;

            loadInitialData();
            populateSitesTable(allSites);
        }));

    }

    /**
     * Opens the form to edit the selected employee.
     */
    private void editSite() {
        //get site
        Site siteToEdit = null;

        if (siteTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an site to modify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        AtomicReference<List<Site>> allSites = new AtomicReference<>(SiteRequests.fetchSites());
        for (Site site : allSites.get()) {
            if (site.getId() == siteTableSelectedId) {
                siteToEdit = site;
                break;
            }
        }

//        sessionActive = false;
//        idleTimer.stop();
//        countdownTimer.stop();

        Site finalSiteToEdit = siteToEdit;

        SwingUtilities.invokeLater(()-> new AddEditSiteForm().showAddEditSiteForm(frame, frame.getLocation(), "EDIT", finalSiteToEdit,() ->{
            // Resume session when the dialog is closed
//            idleTimer.restart();
//            countdownTimer.restart();
//            sessionActive = true;
            loadInitialData();
            allSites.set(SiteRequests.fetchSites());
            populateSitesTable(allSites.get());
        }));

    }

    // =================== PERMISSIONS AND ROLE MANAGEMENT ===================

    /**
     * Populates the Edit Permissions tab with employees.
     *
     * @param employees List of employees to display.
     */
    private void populateEditPermissionsEmployeesTable(List<Employee> employees) {
        // Define column names (ID is hidden later)
        String[] columns = {"ID", "Username", "First Name", "Last Name"};

        // Create a table model
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };

        // Populate the table with employee data
        if (employees != null && !employees.isEmpty()) {
            for (Employee employee : employees) {
                Object[] rowData = {
                        employee.getId(),         // Hidden but used for selection
                        employee.getUsername(),
                        employee.getFirstName(),
                        employee.getLastName()
                };
                tableModel.addRow(rowData);
            }
        }

        // Set the table model
        tblEmployeesPermissions.setModel(tableModel);

        // Hide the first column (Employee ID)
        tblEmployeesPermissions.getColumnModel().getColumn(0).setMinWidth(0);
        tblEmployeesPermissions.getColumnModel().getColumn(0).setMaxWidth(0);
        tblEmployeesPermissions.getColumnModel().getColumn(0).setWidth(0);

        // Set selection mode to single row selection
        tblEmployeesPermissions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a listener to handle row selection
        tblEmployeesPermissions.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                EditPermissionsSelectedItems.clear();
                int selectedRow = tblEmployeesPermissions.getSelectedRow();
                if (selectedRow != -1) {
                    employeeTableSelectedId = (Integer) tblEmployeesPermissions.getValueAt(selectedRow, 0); // Get employee ID
                    UpdateEditEmployeeDetailsBox();

                    // Find the selected employee
                    for (Employee employee : allEmployees) {
                        if (employee.getId() == employeeTableSelectedId) {
                            populateEditPermissionsPositionList(allPosns, employee.getRoles());
                            break;
                        }
                    }
                } else {
                    employeeTableSelectedId = -1;
                }
            }
        });
    }

    /**
     * Updates the item table based on the search field input.
     */
    private void updateEditEmployeesTableBySearch() {
        if (txtEditPermissionsEmployeeSearch.getText().trim().isEmpty()) {
            populateEditPermissionsEmployeesTable(allEmployees);
            return;
        }

        List<Employee> empsToSearch = allEmployees;
        String search = txtEditPermissionsEmployeeSearch.getText().trim().toLowerCase();
        List<Employee> empsToList = new ArrayList<>();

            for (Employee emp : empsToSearch) {


                // Check if any otherfield matches the search term
                if (emp.getFirstName().toLowerCase().contains(search) ||
                        emp.getLastName().toLowerCase().contains(search) ||
                        emp.getUsername().toLowerCase().contains(search)) {
                    empsToList.add(emp);
                }
            }

        populateEditPermissionsEmployeesTable(empsToList);
    }

    /**
     * Populates the roles list for Edit Permissions.
     *
     * @param allPositions     All available roles.
     * @param selectedPositions Roles assigned to the selected employee.
     */
    private void populateEditPermissionsPositionList(List<Posn> allPositions, List<Posn> selectedPositions) {
        pnlEditPermissionsRoles.removeAll(); // Clear any existing roles
        // Set the panel layout to BoxLayout for vertical alignment
        pnlEditPermissionsRoles.setLayout(new BoxLayout(pnlEditPermissionsRoles, BoxLayout.Y_AXIS));

        if (allPositions != null && !allPositions.isEmpty()) {
            for (Posn posn : allPositions) {
                JCheckBox chkRole = new JCheckBox(posn.getPermissionLevel());

                // Check if the role is in the selected positions based on ID
                if (selectedPositions != null && selectedPositions.stream().anyMatch(selected -> selected.getId().equals(posn.getId()))) {
                    chkRole.setSelected(true);

                    // Add to EditPermissionsSelectedItems only if not already present
                    if (!EditPermissionsSelectedItems.contains(posn)) {
                        EditPermissionsSelectedItems.add(posn);
                    }
                }

                pnlEditPermissionsRoles.add(chkRole);

                chkRole.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        if (!EditPermissionsSelectedItems.contains(posn)) {
                            EditPermissionsSelectedItems.add(posn);
                        }
                    } else {
                        EditPermissionsSelectedItems.remove(posn);
                    }
                });
            }
        } else {
            pnlEditPermissionsRoles.add(new JLabel("No roles available."));
        }

        pnlEditPermissionsRoles.revalidate();
        pnlEditPermissionsRoles.repaint();
    }

    /**
     * Updates employee roles and permissions.
     */
    private void editPermissions() {
        if (employeeTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an employee to edit permissions.");
            return;
        }

        // Find the selected employee
        Employee selectedEmployee = allEmployees.stream()
                .filter(emp -> emp.getId() == employeeTableSelectedId)
                .findFirst()
                .orElse(null);

        if (selectedEmployee == null) {
            JOptionPane.showMessageDialog(frame, "Selected employee not found.");
            return;
        }

        // Create a temporary Employee object
        Employee tempEmployee = new Employee();
        tempEmployee.setId(selectedEmployee.getId());
        tempEmployee.setFirstName(selectedEmployee.getFirstName());
        tempEmployee.setLastName(selectedEmployee.getLastName());
        tempEmployee.setEmail(selectedEmployee.getEmail());
        tempEmployee.setPassword(selectedEmployee.getPassword()); // Include password if needed
        tempEmployee.setActive(selectedEmployee.isActive());
        tempEmployee.setLocked(selectedEmployee.getLocked() ? 1 : 0);
        tempEmployee.setSite(selectedEmployee.getSite());
        tempEmployee.setMainRole(selectedEmployee.getMainRole());

        // Use the class-level EditPermissionsSelectedItems for roles
        tempEmployee.setRoles(new ArrayList<>(EditPermissionsSelectedItems));

        // Send the updated employee to the server
        boolean success = EmployeeRequests.updateEmployee(tempEmployee);
        if (success) {
            JOptionPane.showMessageDialog(frame, "Employee roles updated successfully.");
            EditPermissionsSelectedItems.clear();
            ConfigureTabsBasedOnPosition();
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update employee roles.");
        }
    }

    /**
     * Displays employee details in permission editing.
     */
    private void UpdateEditEmployeeDetailsBox() {

        for (Employee employee : allEmployees) {
            if (employee.getId() == employeeTableSelectedId) {
                String name = employee.getFirstName() + " " + employee.getLastName();
                String email = employee.getEmail();
                String mainRole = employee.getMainRole();
                String site = employee.getSite().getSiteName();
                String display = "<html>" +
                        "Name: " + name + "<br>" +
                        "Email: " + email + "<br>" +
                        "Main Role: " + mainRole + "<br>" +
                        "Site: " + site +
                        "</html>";
                lblEditPermissionsEmployeeDetails.setText(display);
            }
        }

    }

    // =================== ORDER MANAGEMENT ===================

    private void handleOrderAction(){

        //get site
        Txn txnToViewReceive = null;

        if (ordersTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an order.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (Txn txn : allTxns) {
            if (txn.getId() == ordersTableSelectedId) {
                txnToViewReceive = txn;
                break;
            }
        }

        Txn finalTxnToViewReceive = txnToViewReceive;


        SwingUtilities.invokeLater(()-> new ViewReceiveOrder().showViewReceveOrderForm(frame, frame.getLocation(), orderViewReceiveMode, finalTxnToViewReceive,() ->{
            // Resume session when the dialog is closed
//            idleTimer.restart();
//            countdownTimer.restart();
//            sessionActive = true;
            loadInitialData();;
            updateOrdersTableBySearch();
        }));

    }

    private void loadStatusTXNSComboBox(){
        //  Clear existing items first
        cmbTxnStatus.removeAllItems();

        //  Add "ALL" option at the top
        cmbTxnStatus.addItem("ALL");

        //  Fetch statuses and populate the combo box
        List<TxnStatus> statuses = TxnRequests.fetchTxnStatuses();
        for (TxnStatus status : statuses) {
            cmbTxnStatus.addItem(status.getStatusName());
        }

        //  Set "ALL" as the default selection
        cmbTxnStatus.setSelectedItem("ALL");

        //  Add listener to fire updateOrdersTableBySearch() on selection change
        cmbTxnStatus.addActionListener(e -> updateTxnTableBySearch());

        //System.out.println("[INFO] Loaded order status combo box with " + statuses.size() + " statuses.");
    }

    private void loadStatusOrdersComboBox(){
        //  Clear existing items first
        cmbOrdersStatus.removeAllItems();

        //  Add "ALL" option at the top
        cmbOrdersStatus.addItem("ALL");

        //  Fetch statuses and populate the combo box
        List<TxnStatus> statuses = TxnRequests.fetchTxnStatuses();
        for (TxnStatus status : statuses) {
            cmbOrdersStatus.addItem(status.getStatusName());
        }

        //  Set "ALL" as the default selection
        cmbOrdersStatus.setSelectedItem("ALL");

        //  Add listener to fire updateOrdersTableBySearch() on selection change
        cmbOrdersStatus.addActionListener(e -> updateOrdersTableBySearch());

        //System.out.println("[INFO] Loaded order status combo box with " + statuses.size() + " statuses.");
    }

    private void loadTypeComboBox(){
        //  Clear existing items first
        cmbTxnType.removeAllItems();

        //  Add "ALL" option at the top
        cmbTxnType.addItem("ALL");

        //  Fetch statuses and populate the combo box
        List<TxnType> types = TxnRequests.fetchTxnTypes();
        for (TxnType type : types) {
            cmbTxnType.addItem(type.getTxnType());
        }

        //  Set "ALL" as the default selection
        cmbTxnType.setSelectedItem("ALL");

        //  Add listener to fire updateOrdersTableBySearch() on selection change
        cmbTxnType.addActionListener(e -> updateTxnTableBySearch());

        //System.out.println("[INFO] Loaded order status combo box with " + types.size() + " statuses.");
    }

    private void loadOrdersLocationComboBox() {
        // ✅ Clear any existing items
        cmbOrdersLocation.removeAllItems();

        // ✅ Add "ALL" option for Admins/WHMgrs
        cmbOrdersLocation.addItem("ALL");

        // ✅ Fetch sites and populate combo box
        List<Site> sites = SiteRequests.fetchSites();
        for (Site site : sites) {
            cmbOrdersLocation.addItem(site);
        }

        // ✅ Add listener to update orders table when selection changes
        cmbOrdersLocation.addActionListener(e -> {
            //System.out.println("[INFO] Changing selection");
            Object selected = cmbOrdersLocation.getSelectedItem();
            if (selected instanceof Site) {
                ordersSite = (Site) selected;
            } else {
                ordersSite = null; // Fallback or handle separately if needed
            }
            //System.out.println(ordersSite);
            updateOrdersTableBySearch();
        });

        //System.out.println(ordersSite);

        //System.out.println("[INFO] Loaded orders location combo box with " + sites.size() + " sites.");
    }

    private void updateWHMgrNotifications() {
        // Fetch roles as a single string and split into an array
        String permissionLevels = SessionManager.getInstance().getPermissionLevel(); // Example: "Administrator, Warehouse Manager"
        String[] roles = permissionLevels.split(",\\s*"); // Split by ", " (comma followed by optional whitespace)
        // ✅ Check if user is an Admin or Warehouse Manager
        boolean isWHMgrOrAdmin = Arrays.asList(roles).contains("Administrator") || Arrays.asList(roles).contains("Warehouse Manager");

        if (!isWHMgrOrAdmin) {
            lblWHMgrNotificationsLine1.setText(""); // Hide notifications if not WHMgr/Admin
            lblWHMgrNotificationsLine2.setText("");
            return;
        }

        // ✅ Count submitted orders
        long submittedEmergencyOrders = allTxns.stream()
                .filter(order -> "SUBMITTED".equalsIgnoreCase(order.getTxnStatus().getStatusName()))
                .filter(order -> "Emergency Order".equalsIgnoreCase(order.getTxnType().getTxnType()))
                .count();

        long submittedStoreOrders = allTxns.stream()
                .filter(order -> "SUBMITTED".equalsIgnoreCase(order.getTxnStatus().getStatusName()))
                .filter(order -> "Store Order".equalsIgnoreCase(order.getTxnType().getTxnType()))
                .count();

        // ✅ Update UI Labels
        lblWHMgrNotificationsLine1.setText("🚨 " + submittedEmergencyOrders + " Emergency Order(s) need processing.");
        lblWHMgrNotificationsLine2.setText("📦 " + submittedStoreOrders + " Store Order(s) need processing.");

        // ✅ Hide labels if there are no submitted orders
        if (submittedEmergencyOrders == 0) {
            lblWHMgrNotificationsLine1.setText(""); // Clear label if no emergencies
        }
        if (submittedStoreOrders == 0) {
            lblWHMgrNotificationsLine2.setText(""); // Clear label if no store orders
        }
    }

    // =================== ITEM MANAGEMENT ===================

    /**
     * Deactivates the selected item.
     */
    private void deactivateItem() {
        // Ensure an item is selected
        if (itemTableSelectedId == -1) {
            JOptionPane.showMessageDialog(frame,
                    "Please select an item from the table.",
                    "No Item Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Find the selected item in the list
        Item selectedItem = allItems.stream()
                .filter(item -> item.getId() == itemTableSelectedId)
                .findFirst()
                .orElse(null);

        if (selectedItem == null) {
            JOptionPane.showMessageDialog(frame,
                    "Selected item could not be found in the list.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if the item is already deactivated
        if (!selectedItem.getActive()) {
            JOptionPane.showMessageDialog(frame,
                    "The selected item is already deactivated.",
                    "Item Already Deactivated",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Confirm the action with the user
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to deactivate the item: " + selectedItem.getName() + "?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Attempt to deactivate the item via backend service
            boolean success = ItemRequests.deactivateItem(itemTableSelectedId);

            // Handle the result
            if (success) {
                JOptionPane.showMessageDialog(frame,
                        "Item successfully deactivated.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                ConfigureTabsBasedOnPosition();
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Failed to deactivate the item. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Opens the form to edit the selected item.
     */
    private void editItem(){

        //get employee
        Item itemtoEdit = null;

        if (itemTableSelectedId == -1) {
            JOptionPane.showConfirmDialog(frame, "Please select an item to modify.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        allItems = ItemRequests.fetchItems();
        for (Item item : allItems) {
            if (item.getId() == itemTableSelectedId) {
                itemtoEdit = item;
                break;
            }
        }

//        sessionActive = false;
//        idleTimer.stop();
//        countdownTimer.stop();

        Item finalItemtoEdit = itemtoEdit;

        SwingUtilities.invokeLater(()-> new EditItemForm().showItemEditForm(frame, frame.getLocation(), finalItemtoEdit,() ->{
            // Resume session when the dialog is closed
//            idleTimer.restart();
//            countdownTimer.restart();
//            sessionActive = true;
            loadInitialData();
            populateItemsTable(allItems);
        }));


    }

    private void createItem(){

//        sessionActive = false;
//        idleTimer.stop();
//        countdownTimer.stop();

        SwingUtilities.invokeLater(()-> new EditItemForm().showItemEditForm(frame, frame.getLocation(), null,() ->{
            // Resume session when the dialog is closed
//            idleTimer.restart();
//            countdownTimer.restart();
//            sessionActive = true;
            loadInitialData();
            populateItemsTable(allItems);
        }));


    }


    // =================== TABLE POPULATION AND SEARCH FUNCTIONS ===================

    /**
     * Updates the employee table based on the search field input.
     */
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

    /**
     * Updates the item table based on the search field input.
     */
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
                        emp.getSite().getSiteName().toLowerCase().contains(search) ||
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
                        emp.getSite().getSiteName().toLowerCase().contains(search) ||
                        emp.getEmail().toLowerCase().contains(search) ||
                        emp.getUsername().toLowerCase().contains(search)) {
                    empsToList.add(emp);
                }
            }
        }

        populateEmployeeTable(empsToList);
    }

    /**
     * Populates the employee table with provided employee data.
     *
     * @param filteredEmployees List of employees to display.
     */
    private void populateEmployeeTable(List<Employee> filteredEmployees) {
        // Column names for the table
        String[] columns;
        boolean isAdmin = Arrays.asList(accessPosition).contains("Administrator");
        if (isAdmin) {
            columns = new String[]{"ID", "First Name", "Last Name", "Location", "Email", "Username", "Role", "Permissions", "Active", "Locked"};
        } else {
            columns = new String[]{"Username", "First Name", "Last Name", "Location", "Role"};
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
            Object[] rowData;

            if (Arrays.asList(accessPosition).contains("Administrator")) {

                // don't include inactive employee unless desired.
                if (!chkInactiveEmployees.isSelected() && !employee.isActive()) {
                        continue;
                }

                rowData = new Object[]{
                        employee.getId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getSite(),
                        employee.getEmail(),
                        employee.getUsername(),
                        employee.getMainRole(),
                        roles.toString(), // Display combined roles
                        employee.isActive() ? "Yes" : "No",
                        employee.getLocked() ? "Yes" : "No"
                };
            } else {
                if (!employee.isActive()) {
                    continue;
                }
                rowData = new Object[]{
                        employee.getUsername(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getSite(),
                        employee.getMainRole(),
                };
            }
            tableModel.addRow(rowData);
        }

        // Set the table model to the JTable
        EmployeeTabTable.setModel(tableModel);

        // Hide the first column (ID)
        EmployeeTabTable.getColumnModel().getColumn(0).setMinWidth(0);
        EmployeeTabTable.getColumnModel().getColumn(0).setMaxWidth(0);
        EmployeeTabTable.getColumnModel().getColumn(0).setWidth(0);

        // Allow selection of entire rows only
        EmployeeTabTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a listener to handle row selection
        EmployeeTabTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = EmployeeTabTable.getSelectedRow();
                if (selectedRow != -1) {
                    employeeTableSelectedId = (Integer) EmployeeTabTable.getValueAt(selectedRow, 0); // Assuming column 4 is 'id'
                } else {
                    employeeTableSelectedId = -1;
                }
            }
        });

        chkInactiveEmployees.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateEmployeeTableBySearch();
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                updateEmployeeTableBySearch();
            }
        });
    }

    /**
     * Populates the item table with provided item data.
     *
     * @param filteredItems List of items to display.
     */
    private void populateItemsTable(List<Item> filteredItems) {
        // Define the column names for the table
        String[] columns = {
                "ID",
                "SKU",
                "Name",
                "Description",
                "Category",
                "Weight",
                "Case Size",
                "Cost Price",
                "Retail Price",
                "Notes",
                "Active",
                "Image?",
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
                    item.getSku(),
                    item.getName(),
                    item.getDescription(),
                    item.getCategory().getCategoryName(), // Get category name
                    item.getWeight(),
                    item.getCaseSize(),
                    item.getCostPrice(),
                    item.getRetailPrice(),
                    item.getNotes(),
                    item.getActive() ? "Yes" : "No", // Convert active status to Yes/No
                    item.getImageLocation() == null ? "No" : "Yes",
                    item.getSupplier().getName() // Get supplier name
            };
            tableModel.addRow(rowData);
        }

        // Set the table model to the JTable
        ItemsTabTable.setModel(tableModel);

        // Hide the first column (ID)
        ItemsTabTable.getColumnModel().getColumn(0).setMinWidth(0);
        ItemsTabTable.getColumnModel().getColumn(0).setMaxWidth(0);
        ItemsTabTable.getColumnModel().getColumn(0).setWidth(0);

        // Apply bold renderer to the SKU column (Index 1)
        ItemsTabTable.getColumnModel().getColumn(1).setCellRenderer(new BoldTableCellRenderer());

        // Allow selection of entire rows only
        ItemsTabTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a listener to handle row selection
        ItemsTabTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = ItemsTabTable.getSelectedRow();
                if (selectedRow != -1) {
                    int selectedItemId = (Integer) ItemsTabTable.getValueAt(selectedRow, 0); // Assuming column 0 is 'ID'
                    itemTableSelectedId = selectedItemId;
                    // Store or process the selected item ID as needed
                }
                else{
                    itemTableSelectedId = -1;
                }
            }
        });
    }

    /**
     * Populates the sites table with provided site data.
     *
     * @param filteredSites List of sites to display.
     */
    private void populateSitesTable(List<Site> filteredSites) {
        // Define column names for the table
        String[] columns = {
                "ID",
                "Site Name",
                "Address",
                "City",
                "Province",
                "Country",
                "Postal Code",
                "Phone",
                "Day of Week",
                "Distance from WH",
                "Active",
                "Notes"
        };

        // Create a table model
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing of table cells
            }
        };

        // Populate the table model with site data
        for (Site site : filteredSites) {
            Object[] rowData = {
                    site.getId(),
                    site.getSiteName(),
                    site.getAddress() + (site.getAddress2() != null && !site.getAddress2().isEmpty() ? " " + site.getAddress2() : ""),
                    site.getCity(),
                    site.getProvinceID(), // Display province name, assuming it's fetched properly
                    site.getCountry(),
                    site.getPostalCode(),
                    site.getPhone(),
                    site.getDayOfWeek(),
                    site.getDistanceFromWH(),
                    site.isActive() ? "Yes" : "No",
                    site.getNotes()
            };
            tableModel.addRow(rowData);
        }

        // Set the table model to the JTable
        tblSites.setModel(tableModel);

        // Hide the first column (ID)
        tblSites.getColumnModel().getColumn(0).setMinWidth(0);
        tblSites.getColumnModel().getColumn(0).setMaxWidth(0);
        tblSites.getColumnModel().getColumn(0).setWidth(0);

        // Allow selection of entire rows only
        tblSites.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a listener to handle row selection
        tblSites.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblSites.getSelectedRow();
                if (selectedRow != -1) {
                    siteTableSelectedId = (Integer) tblSites.getValueAt(selectedRow, 0); // Get site ID
                } else {
                    siteTableSelectedId = -1;
                }
            }
        });
    }

    /**
     * Updates the site table based on the search field input.
     */
    private void updateSiteTableBySearch() {
        // If the search bar is empty, populate the table with all sites
        if (txtSiteSearch.getText().trim().isEmpty()) {
            populateSitesTable(allSites); // Populate with the full list of sites
            return;
        }

        String search = txtSiteSearch.getText().trim().toLowerCase(); // Get search term and convert to lowercase
        List<Site> filteredSites = new ArrayList<>();

        // Loop through all sites and filter based on search term
        for (Site site : allSites) {
            if (site.getSiteName().toLowerCase().contains(search) ||  // Match Site Name
                    site.getAddress().toLowerCase().contains(search) ||   // Match Address
                    (site.getCity() != null && site.getCity().toLowerCase().contains(search)) || // Match City
                    (site.getProvinceID() != null && site.getProvinceID().toLowerCase().contains(search)) || // Match Province
                    (site.getCountry() != null && site.getCountry().toLowerCase().contains(search)) || // Match Country
                    (site.getPostalCode() != null && site.getPostalCode().toLowerCase().contains(search)) || // Match Postal Code
                    (site.getPhone() != null && site.getPhone().toLowerCase().contains(search)) || // Match Phone
                    (site.getDayOfWeek() != null && site.getDayOfWeek().toLowerCase().contains(search)) || // Match Day of Week
                    String.valueOf(site.getDistanceFromWH()).contains(search) || // Match Distance from WH
                    (site.isActive() ? "yes" : "no").contains(search)) { // Match Active status

                filteredSites.add(site); // Add matching site to the filtered list
            }
        }

        // Populate the table with the filtered sites
        populateSitesTable(filteredSites);
    }

    private void populateInventoryTable(List<Inventory> inventoryList) {
        // Define the column headers
        String[] columns = {
                "Item ID",
                "Item Name",
                "SKU",
                "Quantity in Stock",
                "Reorder Threshold",
                "Optimum Threshold",
                "Notes"
        };

        // Create a table model
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Makes the table read-only
            }
        };

        // Populate the table with inventory data
        for (Inventory inv : inventoryList) {
            Object[] rowData = {
                    inv.getItemID(),
                    inv.getItem().getName(),  //  Now using the actual item object
                    inv.getItem().getSku(),   //  Added SKU
                    inv.getQuantity(),
                    inv.getReorderThreshold(),
                    inv.getOptimumThreshold(),
                    inv.getNotes()
            };
            tableModel.addRow(rowData);
        }

        // Set the model on the JTable
        tblInventory.setModel(tableModel);

        // Hide the first column (Item ID)
        tblInventory.getColumnModel().getColumn(0).setMinWidth(0);
        tblInventory.getColumnModel().getColumn(0).setMaxWidth(0);
        tblInventory.getColumnModel().getColumn(0).setWidth(0);

        // Enable single row selection
        tblInventory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a listener for row selection
        tblInventory.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblInventory.getSelectedRow();
                if (selectedRow != -1) {
                    inventoryTableSelectedId = (Integer) tblInventory.getValueAt(selectedRow, 0); // Get Item ID
                } else {
                    inventoryTableSelectedId = -1;
                }
            }
        });
    }

    private void updateInventoryTableBySearch() {
        String searchQuery = txtInventorySearch.getText().trim().toLowerCase();

        if (searchQuery.isEmpty()) {
            populateInventoryTable(allInventory);
            return;
        }

        List<Inventory> filteredList = new ArrayList<>();

        for (Inventory inv : allInventory) {
            if (String.valueOf(inv.getItemID()).contains(searchQuery) ||  // Match Item ID
                    inv.getItem().getName().toLowerCase().contains(searchQuery) ||  //  Match Item Name
                    inv.getItem().getSku().toLowerCase().contains(searchQuery) ||  //  Match SKU
                    String.valueOf(inv.getQuantity()).contains(searchQuery) ||  // Match Quantity
                    String.valueOf(inv.getReorderThreshold()).contains(searchQuery) ||  // Match Reorder Threshold
                    String.valueOf(inv.getOptimumThreshold()).contains(searchQuery) ||  // Match Optimum Threshold
                    (inv.getNotes() != null && inv.getNotes().toLowerCase().contains(searchQuery))) {  // Match Notes
                filteredList.add(inv);
            }
        }

        populateInventoryTable(filteredList);
    }

    /**
     * Updates the orders table based on the search field input.
     */
    private void updateOrdersTableBySearch() {
        if (allTxns == null) {
            return;
        }
        boolean showActiveOnly = chkNewOrdersOnly.isSelected();

        // ✅ Ensure selectedStatus is never null (default to "ALL")
        Object selectedStatusObj = cmbOrdersStatus.getSelectedItem();
        String selectedStatus = (selectedStatusObj != null) ? selectedStatusObj.toString() : "ALL";

//        // ✅ Ensure selectedLocation is never null (default to "ALL")
//        Object selectedLocationObj = cmbOrdersLocation.getSelectedItem();
//        String selectedLocation = (selectedLocationObj != null) ? selectedLocationObj.toString() : "ALL";


        // ✅ Determine if user is a Warehouse Worker
        SessionManager session = SessionManager.getInstance();
        boolean isWarehouseWorker = Arrays.asList(accessPosition).contains("Warehouse Worker") && Arrays.asList(accessPosition).size() == 1;


        String search = txtOrdersSearch.getText().trim().toLowerCase();
        List<Txn> filteredOrders = new ArrayList<>();

        for (Txn order : allTxns) {
            String orderStatus = order.getTxnStatus().getStatusName();
            String orderSite = order.getSiteTo().getSiteName();

            // ✅ Ignore CANCELLED orders when "Show Active Only" is checked
            if (showActiveOnly && ("CANCELLED".equalsIgnoreCase(orderStatus) || "COMPLETE".equalsIgnoreCase(orderStatus))) {
                continue;
            }

            // ✅ Apply Status Filter (Skip if "ALL" is selected)
            if (!"ALL".equalsIgnoreCase(selectedStatus) && !orderStatus.equalsIgnoreCase(selectedStatus)) {
                continue;
            }

//            // ✅ Apply Location Filter (Skip if "ALL" is selected)
//            if (!"ALL".equalsIgnoreCase(selectedLocation) && !orderSite.equalsIgnoreCase(selectedLocation)) {
//                continue;
//            }

            // ✅ Apply Location Filter using global ordersSite (null = All)
            if (ordersSite != null) {
                Site orderSiteObj = order.getSiteTo();
                if (orderSiteObj == null || orderSiteObj.getId() != ordersSite.getId()) {
                    continue;
                }
            }

            // ✅ Warehouse Worker filter (Only see RECEIVED and ASSEMBLING orders)
            if (isWarehouseWorker && !("RECEIVED".equalsIgnoreCase(orderStatus) || "ASSEMBLING".equalsIgnoreCase(orderStatus))) {
                continue;
            }

            // ✅ Get formatted dates
            String createdDateFormatted = formatDate(order.getCreatedDate());
            String shipDateFormatted = order.getShipDate() != null ? formatDate(order.getShipDate()) : "N/A";

            // ✅ Apply Search Filter (Include formatted dates)
            if (search.isEmpty() ||
                    String.valueOf(order.getId()).contains(search) ||
                    order.getTxnType().getTxnType().toLowerCase().contains(search) ||
                    orderStatus.toLowerCase().contains(search) ||
                    orderSite.toLowerCase().contains(search) ||
                    (order.getEmployee() != null && order.getEmployee().getUsername().toLowerCase().contains(search)) ||
                    order.getBarCode().toLowerCase().contains(search) ||
                    createdDateFormatted.toLowerCase().contains(search) ||  // ✅ Search Created Date
                    shipDateFormatted.toLowerCase().contains(search)) {    // ✅ Search Ship Date

                filteredOrders.add(order);
            }
        }

        // ✅ Sort Orders: SUBMITTED (Bold) → Emergency Orders → Store Orders → Everything Else (Latest First)
        filteredOrders.sort((o1, o2) -> {
            boolean o1Submitted = "SUBMITTED".equalsIgnoreCase(o1.getTxnStatus().getStatusName());
            boolean o2Submitted = "SUBMITTED".equalsIgnoreCase(o2.getTxnStatus().getStatusName());
            boolean o1Emergency = "Emergency Order".equalsIgnoreCase(o1.getTxnType().getTxnType());
            boolean o2Emergency = "Emergency Order".equalsIgnoreCase(o2.getTxnType().getTxnType());
            boolean o1StoreOrder = "Store Order".equalsIgnoreCase(o1.getTxnType().getTxnType());
            boolean o2StoreOrder = "Store Order".equalsIgnoreCase(o2.getTxnType().getTxnType());

            // 🔹 Step 1: SUBMITTED Orders should always be at the top (bold)
            if (o1Submitted && !o2Submitted) return -1;
            if (!o1Submitted && o2Submitted) return 1;

            // 🔹 Step 2: Emergency Orders come before Store Orders
            if (o1Emergency && !o2Emergency) return -1;
            if (!o1Emergency && o2Emergency) return 1;

            // 🔹 Step 3: Store Orders come before other types
            if (o1StoreOrder && !o2StoreOrder) return -1;
            if (!o1StoreOrder && o2StoreOrder) return 1;

            // 🔹 Step 4: If all else is equal, sort by Created Date (latest first)
            return o2.getCreatedDate().compareTo(o1.getCreatedDate());
        });

        populateOrdersTable(filteredOrders);
    }


    /**
     * Populates the orders table with provided order data.
     *
     * @param filteredOrders List of orders to display.
     */
    private void populateOrdersTable(List<Txn> filteredOrders) {
        String[] columns = {"ID", "Type", "Status", "Site", "Created Date", "Ship Date", "Created By Employee", "Barcode"};

        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        for (Txn order : filteredOrders) {
            Object[] rowData = new Object[]{
                    order.getId(),
                    order.getTxnType().getTxnType(),
                    order.getTxnStatus().getStatusName(),
                    order.getSiteTo().getSiteName(),
                    formatDate(order.getCreatedDate()),
                    order.getShipDate() != null ? formatDate(order.getShipDate()) : "N/A",
                    (order.getEmployee() != null && order.getEmployee().getId() != null) ? order.getEmployee().getUsername() : "N/A",
                    order.getBarCode()
            };
            tableModel.addRow(rowData);
        }

        tblOrders.setModel(tableModel);

        // 🔹 Hide txnID column (first column)
        tblOrders.getColumnModel().getColumn(0).setMinWidth(0);
        tblOrders.getColumnModel().getColumn(0).setMaxWidth(0);
        tblOrders.getColumnModel().getColumn(0).setWidth(0);

        // 🔹 Allow selection of entire rows only
        tblOrders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 🔹 Listen for row selection and update selectedTxnID
        tblOrders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblOrders.getSelectedRow();
                if (selectedRow != -1) {
                    ordersTableSelectedId = (Integer) tblOrders.getValueAt(selectedRow, 0);
                } else {
                    ordersTableSelectedId = -1;
                }
            }
        });

        // 🔹 Apply bold styling ONLY to the "Status" column if status is SUBMITTED
        tblOrders.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value.toString();
                if ("SUBMITTED".equalsIgnoreCase(status)) {
                    label.setFont(label.getFont().deriveFont(Font.BOLD)); // 🔹 Bold ONLY the "Status" column
                }
                return label;
            }
        });

        // 🔹 Auto-size columns for better visibility
        autoResizeColumns(tblOrders);
    }

    /**
     * 🔹 Auto-resize table columns based on content
     */
    private void autoResizeColumns(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 100; // Default minimum width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width); // Add padding
            }
            table.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A"; // Handle null dates

        // Extract day of the month
        int day = dateTime.getDayOfMonth();

        // Determine proper suffix (st, nd, rd, th)
        String suffix;
        if (day >= 11 && day <= 13) {
            suffix = "th"; // 11th, 12th, 13th are special cases
        } else {
            switch (day % 10) {
                case 1: suffix = "st"; break;
                case 2: suffix = "nd"; break;
                case 3: suffix = "rd"; break;
                default: suffix = "th";
            }
        }

        // Format the date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM d'" + suffix + "', yyyy");
        return dateTime.format(formatter);
    }

    // =================== SESSION MANAGEMENT AND LOGOUT ===================

    /**
     * Logs out the current user and redirects to the login screen.
     */
    private void Logout() {
        sessionActive = false;
        stopIdleTimer();
        stopCountdownTimer();
        SessionManager.getInstance().resetSession();

        // Close all open windows (including dialogs and JOptionPanes)
        for (Window window : Window.getWindows()) {
            if (window.isShowing()) {
//                window.setVisible(false);
                // If the window is a dialog, dispose it
//                if (window instanceof Dialog) {
//                    ((Dialog) window).dispose();
//                } else {
                    // For other windows like JFrame
                    window.dispose();
//                }
            }
        }

        for (Frame panes : Frame.getFrames()){
            panes.dispose();
        }

        SwingUtilities.invokeLater(()-> new LoginForm().showLoginForm(frame.getLocation()));
        frame.dispose();
    }

    /**
     * Handles the refresh button event.
     */
    private void RefreshButtonEvent() {
//        idleTimer.stop();
//        countdownTimer.stop();


        // get selected tab if available
        int selectedTab = -1;
        if(DashboardTabPane.getSelectedIndex()!= -1){
            selectedTab = DashboardTabPane.getSelectedIndex();
        }
        int accept = JOptionPane.showConfirmDialog(
                frame, "Refreshing data...", "Info", JOptionPane.OK_OPTION);
        if (accept == JOptionPane.OK_OPTION) {
            SessionManager.getInfo();
            ConfigureTabsBasedOnPosition();

            // reset to selected tab
            if (selectedTab != -1) {
                DashboardTabPane.setSelectedIndex(selectedTab);
            }}

//        idleTimer.restart();
//        countdownTimer.restart();
    }

    // =================== IDLE TIMER & SESSION TIMEOUT ===================

    /**
     * Initializes the idle timer to track inactivity.
     */
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

    /**
     * Resets the idle timer when user activity is detected.
     */
    private void resetIdleTimer() {
        idleTimer.restart(); // Reset the timer whenever there's activity
        SessionManager.getInstance().updateLastActivityTime(); // Update activity timestamp
    }

    /**
     * Stops the idle timer.
     */
    private void stopIdleTimer() {
        if (idleTimer != null && idleTimer.isRunning()) {
            idleTimer.stop(); // Stop the timer
        }
    }

    /**
     * Initializes the countdown timer to display remaining session time.
     */
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

    /**
     * Stops the countdown timer.
     */
    private void stopCountdownTimer() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop(); // Stop the countdown timer
        }
    }

    // =================== INTERNAL CLASS ===================


    public class BoldTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Apply bold font
            label.setFont(label.getFont().deriveFont(Font.BOLD));

            return label;
        }
    }


}
