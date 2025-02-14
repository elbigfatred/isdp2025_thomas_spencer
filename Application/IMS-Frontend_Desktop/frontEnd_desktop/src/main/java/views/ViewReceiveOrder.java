package views;

import models.*;
import utils.InventoryRequests;
import utils.SessionManager;
import utils.TxnRequests;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.*;
import java.util.List;

public class ViewReceiveOrder {
    private JPanel contentPane;
    private JTable tblTxnItems;
    private JLabel logoLabel;
    private JLabel lblWelcome;
    private JLabel lblLocation;
    private JButton btnExit;
    private JButton btnConfirmReceived;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JButton btnFulfillOrder;
    private JButton btnConfirmAssembled;

    // =================== FRAME VARIABLES ===================

    // =================== FRAME VARIABLES ===================
    private String[] accessPosition;
    private JDialog frame;
    private Txn selectedtxn;
    private String mode;
    private List<TxnItem> txnItems;
    private Map<Integer, Integer> warehouseStock;

    // =================== FRAME VARIABLES ===================

    public void showViewReceveOrderForm(Frame parentFrame, Point currentLocation, String usage, Txn txnToViewReceive, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);
        this.selectedtxn = txnToViewReceive;
        this.mode = usage;

        frame.setTitle(Objects.equals(usage, "VIEW") ? "Bullseye Inventory Management System - View Order"
                : "Bullseye Inventory Management System - View/Receive Order");
        frame.setSize(750, 570);

        frame.setContentPane(getMainPanel());
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ✅ Hide the submit button if we're not in RECEIVE mode
        btnConfirmReceived.setVisible(Objects.equals(mode, "RECEIVE"));

        if (currentLocation != null) {
            frame.setLocation(currentLocation);
        }

        SwingUtilities.invokeLater(() -> {
            SetupBullseyeLogo();
            loadTxnItems();

            if (Objects.equals(mode, "RECEIVE")) {
                loadWarehouseStock();
            }

            // Determine how to populate the table
            updateTableDisplay();
            checkAndSetupFulfillment();  // ✅ NEW: Check roles + status and adjust UI

        });

        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (onCloseCallback != null) {
                    onCloseCallback.run();
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
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(frame, "No user is currently logged in. Not sure how you got here.",
                    "Session Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Fetch roles as a single string and split into an array
        String permissionLevels = SessionManager.getInstance().getPermissionLevel(); // Example: "Administrator, Warehouse Manager"
        String[] roles = permissionLevels.split(",\\s*"); // Split by ", " (comma followed by optional whitespace)
        accessPosition = roles;

        lblWelcome.setText("User: " + session.getUsername());
        lblLocation.setText("Location: " + session.getSiteName());

        btnConfirmReceived.addActionListener(e -> handleSubmit());
        btnExit.addActionListener(e -> frame.dispose());

        btnExit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        btnExit.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnExit.doClick();
            }
        });

        btnFulfillOrder.addActionListener(e -> {
            String empUsername = SessionManager.getInstance().getUsername();
            boolean success = TxnRequests.updateOrderStatus(selectedtxn.getId(), "ASSEMBLING", empUsername);

            if (success) {
                JOptionPane.showMessageDialog(frame, "Order is now being assembled.");
                btnFulfillOrder.setEnabled(false);
                btnConfirmAssembled.setVisible(true); // Enable next step
            } else {
                JOptionPane.showMessageDialog(frame, "Error updating order status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnConfirmAssembled.addActionListener(e -> {
            confirmAssembledTransaction();
        });



        return contentPane;
    }

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

    private void handleSubmit() {
        System.out.println("Processing order fulfillment...");

        List<TxnItem> fulfilledItems = new ArrayList<>();
        List<TxnItem> backorderItems = new ArrayList<>();

        for (TxnItem item : txnItems) {
            int stockAvailable = warehouseStock.getOrDefault(item.getItemID(), 0);
            int orderedQty = item.getQuantity();
            int toFulfill = Math.min(stockAvailable, orderedQty);
            int backorderQty = orderedQty - toFulfill;

            if (toFulfill > 0) {
                TxnItem temp = new TxnItem();
                temp.setTxnID(selectedtxn.getId());
                temp.setItemID(item.getItemID());
                temp.setQuantity(toFulfill);
                fulfilledItems.add(temp);
                //fulfilledItems.add(new TxnItem(selectedtxn.getId(), item.getItemID(), toFulfill));
            }
            if (backorderQty > 0) {
                TxnItem temp = new TxnItem();
                temp.setItemID(selectedtxn.getId());
                temp.setItemID(item.getItemID());
                temp.setQuantity(backorderQty);
                backorderItems.add(temp);
                //backorderItems.add(new TxnItem(selectedtxn.getId(), item.getItemID(), backorderQty));
            }
        }

        System.out.println("[FULFILLED ITEMS] " + fulfilledItems);
        System.out.println("[BACKORDER ITEMS] " + backorderItems);

        boolean updateSuccess = true;
        boolean backorderSuccess = true;

        // ✅ Step 1: Update the original transaction if items were fulfilled
        if (!fulfilledItems.isEmpty()) {
            System.out.println("Updating original transaction...");
            String empUsername = SessionManager.getInstance().getUsername();
            updateSuccess = TxnRequests.updateOrderItems(selectedtxn.getId(), fulfilledItems, empUsername);
            System.out.println("[DEBUG] updateOrderItems Success: " + updateSuccess);
        }

        // ✅ Step 2: Create a backorder if needed
        if (!backorderItems.isEmpty()) {
            System.out.println("Creating new backorder...");
            int siteID = selectedtxn.getSiteTo().getId(); // Fetch Site ID from transaction

            backorderSuccess = TxnRequests.createBackorder(siteID, backorderItems);
            System.out.println("[DEBUG] createBackorder Success: " + backorderSuccess);
        }

        // ✅ Step 3: Determine the new status of the order
        String newStatus;
        if (!fulfilledItems.isEmpty()) {
            newStatus = "RECEIVED"; // ✅ If anything is fulfilled, warehouse needs to process
        } else {
            newStatus = "CANCELLED"; // ❌ If nothing was fulfilled, the order should be void
        }

        System.out.println("[NEW ORDER STATUS] " + newStatus);

        String empUsername = SessionManager.getInstance().getUsername();

        // ✅ Step 4: Update order status
        boolean statusUpdated = TxnRequests.updateOrderStatus(selectedtxn.getId(), newStatus, empUsername);
        System.out.println("[DEBUG] updateOrderStatus Success: " + statusUpdated);

        if (updateSuccess && backorderSuccess && statusUpdated) {
            JOptionPane.showMessageDialog(frame, "Order processed successfully.");
            frame.dispose();
        } else {
            JOptionPane.showMessageDialog(frame, "Error processing order.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTableDisplay() {
        if (Objects.equals(mode, "RECEIVE") && selectedtxn.getTxnStatus().getStatusName().equalsIgnoreCase("SUBMITTED")) {
            updateFulfillmentTable();
        } else {
            populateTxnItemsTable();
        }
    }

    /**
     * Loads warehouse stock for inventory.
     */
    /**
     * Loads transaction items from the API.
     */
    private void loadTxnItems() {
        txnItems = TxnRequests.fetchTxnItems(selectedtxn.getId());
        System.out.println("[LOADED TXN ITEMS:] " + txnItems);
    }

    /**
     * Loads warehouse stock for inventory.
     */
    private void loadWarehouseStock() {
        int warehouseSiteID = 2;
        System.out.println("FETCHING WH INVENTORY");
        List<Inventory> inventoryList = InventoryRequests.fetchInventoryBySite(warehouseSiteID);
        System.out.println("INVENTORY ITEMS FETCHED" + inventoryList.size());
        warehouseStock = new HashMap<>();

        for (Inventory inv : inventoryList) {
            warehouseStock.put(inv.getItem().getId(), inv.getQuantity());
        }

        // If order is SUBMITTED, we update the fulfillment table
        if (selectedtxn.getTxnStatus().getStatusName().equalsIgnoreCase("SUBMITTED")) {
            updateFulfillmentTable();
            btnConfirmReceived.setVisible(true);
        } else {
            btnConfirmReceived.setVisible(false);
        }
    }

    /**
     * Populates transaction items table in VIEW mode.
     */
    private void populateTxnItemsTable() {
        String[] columns = {"Item ID", "Item Name", "Ordered Qty"};

        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (TxnItem item : txnItems) {
            Object[] rowData = {item.getItemID(), item.getItemName(), item.getQuantity()};
            tableModel.addRow(rowData);
        }

        tblTxnItems.setModel(tableModel);
    }

    /**
     * Updates the fulfillment table for RECEIVE mode.
     */
    private void updateFulfillmentTable() {
        String[] columns = {"Item ID", "Item Name", "Ordered Qty", "Stock Available", "To Fulfill", "Backorder"};

        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (TxnItem item : txnItems) {
            int stockAvailable = warehouseStock.getOrDefault(item.getItemID(), 0);
            int orderedQty = item.getQuantity();
            int toFulfill = Math.min(stockAvailable, orderedQty);
            int backorderQty = orderedQty - toFulfill;

            Object[] rowData = {
                    item.getItemID(),
                    item.getItemName(),
                    orderedQty,
                    stockAvailable,
                    toFulfill,
                    backorderQty > 0 ? backorderQty : "N/A"
            };

            tableModel.addRow(rowData);
        }

        tblTxnItems.setModel(tableModel);
    }

    private void checkAndSetupFulfillment() {
        String txnStatus = selectedtxn.getTxnStatus().getStatusName();

        boolean isWarehouseRole = Arrays.stream(accessPosition)
                .anyMatch(role -> role.equalsIgnoreCase("Warehouse Employee")
                        || role.equalsIgnoreCase("Warehouse Manager")
                        || role.equalsIgnoreCase("Admin"));

        if (isWarehouseRole && (txnStatus.equalsIgnoreCase("RECEIVED") || txnStatus.equalsIgnoreCase("ASSEMBLING"))) {
            btnFulfillOrder.setVisible(true);
            btnFulfillOrder.setEnabled(txnStatus.equalsIgnoreCase("RECEIVED")); // Enable only if RECEIVED
            btnConfirmAssembled.setVisible(txnStatus.equalsIgnoreCase("ASSEMBLING")); // Show next step if assembling
        } else {
            btnFulfillOrder.setVisible(false);
            btnConfirmAssembled.setVisible(false);
        }
    }

    private void confirmAssembledTransaction() {
        System.out.println("[INFO] Confirming assembly for order ID: " + selectedtxn.getId());

        // ✅ Fetch transaction items
        List<TxnItem> txnItems = TxnRequests.fetchTxnItems(selectedtxn.getId());

        if (txnItems.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No items found in the order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ✅ Convert transaction items into Inventory items for API calls
        List<Inventory> itemsForWarehouse = new ArrayList<>();
        List<Inventory> itemsForBay = new ArrayList<>();

        for (TxnItem txnItem : txnItems) {
            Inventory warehouseInventory = new Inventory();
            warehouseInventory.setItemID(txnItem.getItemID());
            warehouseInventory.setQuantity(txnItem.getQuantity());
            itemsForWarehouse.add(warehouseInventory);

            Inventory bayInventory = new Inventory();
            bayInventory.setItemID(txnItem.getItemID());
            bayInventory.setQuantity(txnItem.getQuantity());
            itemsForBay.add(bayInventory);
        }

        boolean warehouseDecrementSuccess = InventoryRequests.decrementInventory(2, itemsForWarehouse);
        boolean bayIncrementSuccess = InventoryRequests.incrementInventory(3, itemsForBay);

        if (!warehouseDecrementSuccess) {
            JOptionPane.showMessageDialog(frame, "Failed to decrement warehouse inventory!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!bayIncrementSuccess) {
            JOptionPane.showMessageDialog(frame, "Failed to increment warehouse bay inventory!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String empUsername = SessionManager.getInstance().getUsername();
        // ✅ Update order status to "ASSEMBLED"
        boolean statusUpdated = TxnRequests.updateOrderStatus(selectedtxn.getId(), "ASSEMBLED", empUsername);

        if (statusUpdated) {
            JOptionPane.showMessageDialog(frame, "Order successfully assembled!", "Success", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("[SUCCESS] Order " + selectedtxn.getId() + " successfully assembled!");
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update order status!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
