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
    private JProgressBar pbarFulfillItems;
    private JButton btnScanItem;
    private JLabel lblFullfillItemDesc;
    private JPanel fullfillPanel;

    // =================== FRAME VARIABLES ===================

    // =================== FRAME VARIABLES ===================
    private String[] accessPosition;
    private JDialog frame;
    private Txn selectedtxn;
    private String mode;
    private List<TxnItem> txnItems;
    private Map<Integer, Integer> warehouseStock;
    private int currentItemIndex = 0; // Tracks current item in fulfillment
    private List<TxnItem> assemblingItems; // Local list of items for assembly

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

        btnConfirmReceived.addActionListener(e -> {
            handleSubmit();
        });

        btnFulfillOrder.addActionListener(e -> {
            String empUsername = SessionManager.getInstance().getUsername();
            boolean success = TxnRequests.updateOrderStatus(selectedtxn.getId(), "ASSEMBLING", empUsername);

            if (success) {
                TxnStatus txnStatus = new TxnStatus();
                txnStatus.setStatusName("ASSEMBLING");
                selectedtxn.setTxnStatus(txnStatus);
                JOptionPane.showMessageDialog(frame, "Order is now being assembled.");
                checkAndSetupFulfillment(); // Refresh UI
            } else {
                JOptionPane.showMessageDialog(frame, "Error updating order status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnConfirmAssembled.addActionListener(e -> {
            confirmAssembledTransaction();
        });

        if (currentLocation != null) {
            frame.setLocation(currentLocation);
        }

        SwingUtilities.invokeLater(() -> {
            fullfillPanel.setVisible(false);
            SetupBullseyeLogo();
            loadTxnItems();

            if (Objects.equals(mode, "RECEIVE")) {
                loadWarehouseStock();
            }

            // Determine how to populate the table
            updateTableDisplay();
            checkAndSetupFulfillment();  // ✅ NEW: Check roles + status and adjust UI

            frame.setVisible(true);
        });


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

        //btnConfirmReceived.addActionListener(e -> handleSubmit());
        btnExit.addActionListener(e -> frame.dispose());

        btnExit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        btnExit.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnExit.doClick();
            }
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
            JOptionPane.showMessageDialog(frame, "Order has been received and is ready to be fulfilled.");
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
                        || role.equalsIgnoreCase("Administrator"));

        // Hide everything by default
        btnConfirmReceived.setVisible(false);
        btnFulfillOrder.setVisible(false);
        btnConfirmAssembled.setVisible(false);
        fullfillPanel.setVisible(false);

        if (!isWarehouseRole) return; // ✅ Ignore if user has no warehouse access

        switch (txnStatus.toUpperCase()) {
            case "SUBMITTED":
                btnConfirmReceived.setVisible(true);
                btnConfirmReceived.setEnabled(true);
                break;

            case "RECEIVED":
                btnFulfillOrder.setVisible(true);
                btnFulfillOrder.setEnabled(true);
                btnConfirmAssembled.setVisible(true);
                btnConfirmAssembled.setEnabled(false); // Locked until all items scanned
                break;

            case "ASSEMBLING":
                btnConfirmAssembled.setVisible(true);
                btnFulfillOrder.setVisible(true);
                btnConfirmAssembled.setEnabled(false);
                btnFulfillOrder.setEnabled(false);
                startAssemblyProcess();
                break;
        }
    }

    private void startAssemblyProcess() {
        assemblingItems = new ArrayList<>(txnItems);
        currentItemIndex = 0;
        fullfillPanel.setVisible(true);
        btnScanItem.setEnabled(true);
        btnScanItem.addActionListener(e -> handleScanItem());
        updateFulfillmentPanel();
    }

    private void handleScanItem() {
        if (currentItemIndex < assemblingItems.size()) {
            TxnItem currentItem = assemblingItems.get(currentItemIndex);
            System.out.println("[SCANNING ITEM] " + currentItem.getItemName() + " | Qty: " + currentItem.getQuantity());

            int progress = (int) (((double) (currentItemIndex + 1) / assemblingItems.size()) * 100);
            pbarFulfillItems.setValue(progress);

            currentItemIndex++;
            if (currentItemIndex < assemblingItems.size()) {
                updateFulfillmentPanel();
            } else {
                lblFullfillItemDesc.setText("Scanning Complete");
                btnScanItem.setEnabled(false);
                btnConfirmAssembled.setEnabled(true); // ✅ Enable confirm button
            }
        }
    }

    private void updateFulfillmentPanel() {
        if (currentItemIndex < assemblingItems.size()) {
            TxnItem item = assemblingItems.get(currentItemIndex);
            lblFullfillItemDesc.setText("Scanning: " + item.getItemName() + " (Qty: " + item.getQuantity() + ")");
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
            frame.dispose();
            System.out.println("[SUCCESS] Order " + selectedtxn.getId() + " successfully assembled!");
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update order status!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
