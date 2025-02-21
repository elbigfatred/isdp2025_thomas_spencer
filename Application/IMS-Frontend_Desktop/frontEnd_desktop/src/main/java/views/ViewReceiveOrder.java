package views;

import models.*;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import utils.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private JPanel editBackOrderPanel;
    private JLabel lblBackOrderItemLabel;
    private JPanel calendarBackOrder;
    private JButton btnUpdateBackorder;
    private JSpinner spnOrderQuantity;
    private JLabel lblOrderDate;
    private JButton btnHelp;
    private JLabel lblOrderDetails;
    private JLabel lblWeight;
    private JLabel lblTotalQuantity;
    private JLabel lblTotalCost;
    private JDatePickerImpl datePicker; // ✅ Declare the date picker

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
    private Integer selectedItemIndex = -1;
    private Map<Integer, Item> itemMap = new HashMap<>();

    // =================== FRAME VARIABLES ===================

    public void showViewReceveOrderForm(Frame parentFrame, Point currentLocation, String usage, Txn txnToViewReceive, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);
        this.selectedtxn = txnToViewReceive;
        this.mode = usage;

        //editBackOrderPanel.setVisible(false);

        frame.setTitle(Objects.equals(usage, "VIEW") ? "Bullseye Inventory Management System - View Order"
                : "Bullseye Inventory Management System - View/Receive Order");
        frame.setSize(750, 615);

        frame.setContentPane(getMainPanel());
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loadItems();

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
                showTxnDetails();
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

            System.out.println(selectedtxn.getTxnType().getTxnType());

            if (Objects.equals(mode, "RECEIVE") &&
                    (Objects.equals(selectedtxn.getTxnStatus().getStatusName(),"SUBMITTED")) &&
                    (Objects.equals(selectedtxn.getTxnType().getTxnType(), "Store Order") ||
                            Objects.equals(selectedtxn.getTxnType().getTxnType(), "Emergency Order"))) {
                loadWarehouseStock();
            }

            if (Objects.equals(mode, "RECEIVE") &&
                    (Objects.equals(selectedtxn.getTxnStatus().getStatusName(),"NEW")) &&
                    (Objects.equals(selectedtxn.getTxnType().getTxnType(), "Back Order"))) {
                setupDatePicker();
                loadItems();
                DisplayBackOrderItem();
                btnUpdateBackorder.addActionListener(e -> handleBackorderUpdate());
            }

            // Determine how to populate the table
            updateTableDisplay();
            checkAndSetupFulfillment();  // ✅ NEW: Check roles + status and adjust UI
            showTxnDetails();


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

        btnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.VIEW_RECEIVE_ORDER_HELP,"View Order Help",JOptionPane.INFORMATION_MESSAGE);
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
        if (Objects.equals(mode, "RECEIVE") &&
                (Objects.equals(selectedtxn.getTxnStatus().getStatusName(),"SUBMITTED")) &&
                (Objects.equals(selectedtxn.getTxnType().getTxnType(), "Store Order") ||
                        Objects.equals(selectedtxn.getTxnType().getTxnType(), "Emergency Order"))){
            updateFulfillmentTable();
            editBackOrderPanel.setVisible(true);
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
            editBackOrderPanel.setVisible(true);
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

        // ✅ Dispose of the existing table model first
        DefaultTableModel oldModel = (DefaultTableModel) tblTxnItems.getModel();
        oldModel.setRowCount(0); // Clears the table

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
        tblTxnItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // ✅ Add selection listener to update SelectedTableIndex
        tblTxnItems.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { // Ensures event fires once per selection
                    selectedItemIndex = tblTxnItems.getSelectedRow();
                    System.out.println("Selected Row Index: " + selectedItemIndex); // Debugging

                    if (isUserWarehouseManagerOrAdmin() && Objects.equals(selectedtxn.getTxnType().getTxnType(), "Back Order")) {
                        DisplayBackOrderItem();
                    }
                }
            }
        });
    }

    /**
     * Checks if the user is a Warehouse Manager or Administrator.
     */
    private boolean isUserWarehouseManagerOrAdmin() {
        return Arrays.stream(accessPosition)
                .anyMatch(role -> role.equalsIgnoreCase("Warehouse Manager")
                        || role.equalsIgnoreCase("Administrator"));
    }

    /**
     * Updates the fulfillment table for RECEIVE mode.
     */
    private void updateFulfillmentTable() {
        editBackOrderPanel.setVisible(true);
        lblOrderDate.setVisible(false);
        spnOrderQuantity.setEnabled(false);
        loadItems();

        System.out.println("UPDATING FULFILLMENT TABLE");
        String[] columns = {"Item ID", "Item Name", "Ordered Qty", "Stock in Warehouse", "To Fulfill", "To Backorder"};

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
        tblTxnItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ✅ Add listener for row selection
        tblTxnItems.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblTxnItems.getSelectedRow();
                if (selectedRow != -1) {
                    setSelectedFulfillmentItem(selectedRow);
                } else {
                    clearFulfillmentSelection();
                }
            }
        });


        // ✅ Single spinner listener for updating the selected item's quantity
        spnOrderQuantity.addChangeListener(e -> {
            int selectedRow = tblTxnItems.getSelectedRow();
            if (selectedRow != -1) {
                updateOrderedQuantity(selectedRow);
                showTxnDetails();
            }
        });

        btnUpdateBackorder.removeActionListener(btnUpdateBackorder.getActionListeners().length > 0 ? btnUpdateBackorder.getActionListeners()[0] : null);
        btnUpdateBackorder.addActionListener(e -> handleTxnItemsUpdate());

    }

    private void setSelectedFulfillmentItem(int selectedRow) {
        int itemID = (int) tblTxnItems.getValueAt(selectedRow, 0);

        // ✅ Find the corresponding TxnItem in txnItems
        TxnItem selectedItem = txnItems.stream()
                .filter(item -> item.getItemID() == itemID)
                .findFirst()
                .orElse(null);

        if (selectedItem == null) {
            System.out.println("[ERROR] Selected item not found in txnItems list.");
            return;
        }

        System.out.println("Selected Item: " + selectedItem.getItemID() + selectedItem.getQuantity());

        // ✅ Update UI elements
        lblBackOrderItemLabel.setText(selectedItem.getItemName() + ", Case Size: " + itemMap.get(selectedItem.getItemID()).getCaseSize());
        spnOrderQuantity.setEnabled(true);
        spnOrderQuantity.setValue(selectedItem.getQuantity());
        btnUpdateBackorder.setEnabled(true);

        // ✅ Adjust spinner step size based on caseSize
        int caseSize = itemMap.getOrDefault(itemID, new Item()).getCaseSize();
        setSpinnerStepSize(caseSize > 0 ? caseSize : 1);
    }

    private void updateOrderedQuantity(int selectedRow) {
        int itemID = (int) tblTxnItems.getValueAt(selectedRow, 0);
        int newOrderedQty = (int) spnOrderQuantity.getValue();

        // ✅ Find the corresponding TxnItem
        TxnItem selectedItem = txnItems.stream()
                .filter(item -> item.getItemID() == itemID)
                .findFirst()
                .orElse(null);

        if (selectedItem == null) {
            System.out.println("[ERROR] Item not found in txnItems list.");
            return;
        }

        // ✅ Update Ordered Quantity in txnItems
        selectedItem.setQuantity(newOrderedQty);

        int stockAvailable = warehouseStock.getOrDefault(itemID, 0);
        int toFulfill = Math.min(stockAvailable, newOrderedQty);
        int backorderQty = newOrderedQty - toFulfill;

        // ✅ Update the corresponding row in tblTxnItems
        tblTxnItems.setValueAt(newOrderedQty, selectedRow, 2); // Ordered Qty column
        tblTxnItems.setValueAt(toFulfill, selectedRow, 4); // To Fulfill column
        tblTxnItems.setValueAt(backorderQty > 0 ? backorderQty : "N/A", selectedRow, 5); // To Backorder column

        System.out.println("[INFO] Updated Order Item: " + selectedItem.getItemName());
        System.out.println("  - New Ordered Qty: " + newOrderedQty);
        System.out.println("  - New To Fulfill: " + toFulfill);
        System.out.println("  - New Backorder: " + backorderQty);
    }

    private void clearFulfillmentSelection() {
        lblBackOrderItemLabel.setText("Please select an item...");
        spnOrderQuantity.setEnabled(false);
        spnOrderQuantity.setValue(0);
        btnUpdateBackorder.setEnabled(false);
    }

    private void checkAndSetupFulfillment() {
        String txnStatus = selectedtxn.getTxnStatus().getStatusName();
        String txnType = selectedtxn.getTxnType().getTxnType(); // Store Order, Emergency Order, Back Order

        boolean isWarehouseEmployee = Arrays.stream(accessPosition)
                .anyMatch(role -> role.equalsIgnoreCase("Warehouse Worker"));

        boolean isWarehouseManagerOrAdmin = Arrays.stream(accessPosition)
                .anyMatch(role -> role.equalsIgnoreCase("Warehouse Manager")
                        || role.equalsIgnoreCase("Administrator"));

        // Hide everything by default
        btnConfirmReceived.setVisible(false);
        btnFulfillOrder.setVisible(false);
        btnConfirmAssembled.setVisible(false);
        fullfillPanel.setVisible(false);
        //editBackOrderPanel.setVisible(false); // Default to hidden

        if (!(isWarehouseEmployee || isWarehouseManagerOrAdmin)) return; // ✅ Ignore if user has no warehouse access

        // ✅ Show `backorderEditPanel` only for Back Orders in "NEW" status
        if ("Back Order".equalsIgnoreCase(txnType) && "NEW".equalsIgnoreCase(txnStatus) && isWarehouseManagerOrAdmin) {
            editBackOrderPanel.setVisible(true);
        }

        switch (txnStatus.toUpperCase()) {
            case "SUBMITTED":
                if (isWarehouseManagerOrAdmin) {
                    btnConfirmReceived.setVisible(true);
                    btnConfirmReceived.setEnabled(true);
                }
                break;

            case "RECEIVED":
                if (isWarehouseManagerOrAdmin) {
                    btnFulfillOrder.setVisible(true);
                    btnFulfillOrder.setEnabled(true);
                }else {
                    btnFulfillOrder.setVisible(true);
                    btnFulfillOrder.setEnabled(true); // Locked until all items scanned
                }
                break;

            case "ASSEMBLING":
                btnConfirmAssembled.setVisible(true);
                if (isWarehouseManagerOrAdmin || isWarehouseEmployee){
                    btnFulfillOrder.setVisible(true);
                    btnConfirmAssembled.setEnabled(false);
                    btnFulfillOrder.setEnabled(false);
                    startAssemblyProcess();
                } else break;
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

    /**
     * Displays details of a selected backorder item and updates UI elements.
     * Only called when an Admin or Warehouse Manager selects an item.
     */
    private void DisplayBackOrderItem() {
        btnUpdateBackorder.setEnabled(true);
        if (selectedItemIndex < 0 || selectedItemIndex >= txnItems.size()) {
            // ❌ No valid item selected - Reset UI elements
            lblBackOrderItemLabel.setText("Please select an item...");
            spnOrderQuantity.setEnabled(false);
            spnOrderQuantity.setValue(0);
            //btnUpdateBackorder.setEnabled(false);
            //datePicker.setVisible(false);
            //lblOrderDate.setVisible(false);
            return;
        }

        // ✅ Valid selection - Enable UI elements and update details
        TxnItem selectedItem = txnItems.get(selectedItemIndex);
        int itemID = selectedItem.getItemID();
        int orderedQty = selectedItem.getQuantity();

        System.out.println("[INFO] Displaying Backorder Item: " + selectedItem.getItemName() + " (ItemID: " + itemID + ")");

        lblBackOrderItemLabel.setText(selectedItem.getItemName() + ", Case Size: " + itemMap.get(itemID).getCaseSize());
        spnOrderQuantity.setEnabled(true);
        spnOrderQuantity.setValue(orderedQty); // Set to existing quantity
        //btnUpdateBackorder.setEnabled(true);
        datePicker.setVisible(true);
        lblOrderDate.setVisible(true);

        // ✅ Adjust spinner step based on caseSize from itemMap
        if (itemMap.containsKey(itemID)) {

            int caseSize = itemMap.get(itemID).getCaseSize();
            System.out.println("[INFO] Case size: " + caseSize);
            setSpinnerStepSize(caseSize);
        } else {
            System.out.println("[WARN] Item not found in map, using default step size.");
            setSpinnerStepSize(1); // Default step size
        }

        addSpinnerChangeListener();
    }

    /**
     * Adds a listener to update TxnItem quantity in memory when spinner changes.
     */
    private void addSpinnerChangeListener() {
        spnOrderQuantity.addChangeListener(e -> {
            if (selectedItemIndex < 0 || selectedItemIndex >= txnItems.size()) {
                return;
            }

            // ✅ Get new quantity from spinner
            int newQuantity = (int) spnOrderQuantity.getValue();
            TxnItem selectedItem = txnItems.get(selectedItemIndex);

            // ✅ Update quantity in memory
            selectedItem.setQuantity(newQuantity);
            System.out.println("[INFO] Updated item quantity in memory: " + selectedItem.getItemName() + " -> " + newQuantity);

            // ✅ Update the table cell directly
            DefaultTableModel model = (DefaultTableModel) tblTxnItems.getModel();
            model.setValueAt(newQuantity, selectedItemIndex, 2); // Column index 2 = "Ordered Qty"

            showTxnDetails();

        });
    }

    /**
     * Adjusts the step size of spnOrderQuantity.
     */
    private void setSpinnerStepSize(int stepSize) {
        SpinnerNumberModel model = (SpinnerNumberModel) spnOrderQuantity.getModel();
        model.setStepSize(stepSize);
        model.setMinimum(stepSize);
    }

    private void setupDatePicker() {
        UtilDateModel model = new UtilDateModel();

        // ✅ Convert LocalDateTime to Date (removing time component)
        if (selectedtxn.getShipDate() != null) {
            Date shipDate = convertLocalDateTimeToDate(selectedtxn.getShipDate());
            model.setValue(shipDate);
        }

        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        // ✅ Add the date picker to `calendarBackOrder`
        calendarBackOrder.setLayout(new java.awt.BorderLayout());
        calendarBackOrder.add(datePicker, BorderLayout.CENTER);
    }

    /**
     * Converts LocalDateTime to Date while removing time information.
     */
    private Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void loadItems() {
        List<Item> itemList = ItemRequests.fetchItems(); // Fetch items from DB

        // ✅ Store them in a Map where key = itemID
        itemMap = new HashMap<>();
        for (Item item : itemList) {
            itemMap.put(item.getId(), item);
        }

        System.out.println("[DEBUG] Items loaded into map: " + itemMap.size());
    }

    private void handleBackorderUpdate() {
        // ✅ Step 1: Get transaction ID
        int txnId = selectedtxn.getId();

        // ✅ Step 2: Get updated items list
        List<TxnItem> updatedItems = new ArrayList<>();
        for (TxnItem item : txnItems) {
            updatedItems.add(item);
        }

        // ✅ Step 3: Get employee username
        String empUsername = SessionManager.getInstance().getUsername();

        // ✅ Step 4: Get selected date from the date picker
        Date selectedDate = (Date) datePicker.getModel().getValue();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Format as YYYY-MM-DD
        String formattedDate = (selectedDate != null) ? sdf.format(selectedDate) : "No date selected";

        // ✅ Print all gathered data
        System.out.println("[DEBUG] Preparing Backorder Update...");
        System.out.println("[DEBUG] Transaction ID: " + txnId);
        System.out.println("[DEBUG] Employee Username: " + empUsername);
        System.out.println("[DEBUG] Selected Date: " + formattedDate);
        System.out.println("[DEBUG] Items to Update:");

        for (TxnItem item : updatedItems) {
            System.out.println("  - Item ID: " + item.getItemID() + ", Quantity: " + item.getQuantity());
        }

        // ✅ Step 1: Update Order Items
        boolean itemsUpdated = TxnRequests.updateOrderItems(txnId, updatedItems, empUsername);

        // ✅ Step 2: Update Ship Date (if provided)
        boolean dateUpdated = formattedDate != null && TxnRequests.updateTxnShipDate(txnId, formattedDate, empUsername);

        // ✅ Show success message if both updates succeed
        if (itemsUpdated && dateUpdated) {
            JOptionPane.showMessageDialog(frame, "Back Order updated successfully!");
            frame.dispose();
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update backorder.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void handleTxnItemsUpdate() {
        // ✅ Step 1: Get transaction ID
        int txnId = selectedtxn.getId();

        // ✅ Step 2: Get updated items list
        List<TxnItem> updatedItems = new ArrayList<>(txnItems); // Directly use txnItems list

        // ✅ Step 3: Get employee username
        String empUsername = SessionManager.getInstance().getUsername();

        // ✅ Debugging printout
        System.out.println("[DEBUG] Preparing Backorder Update...");
        System.out.println("[DEBUG] Transaction ID: " + txnId);
        System.out.println("[DEBUG] Employee Username: " + empUsername);
        System.out.println("[DEBUG] Items to Update:");

        for (TxnItem item : updatedItems) {
            System.out.println("  - Item ID: " + item.getItemID() + ", Quantity: " + item.getQuantity());
        }

        // ✅ Step 4: Update Order Items (no ship date update)
        boolean itemsUpdated = TxnRequests.updateOrderItems(txnId, updatedItems, empUsername);

        // ✅ Show success message if the update was successful
        if (itemsUpdated) {
            JOptionPane.showMessageDialog(frame, "Submitted Order updated successfully!");
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update backorder.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTxnDetails() {
        Integer txnID = selectedtxn.getId();
        String txnStatus = selectedtxn.getTxnStatus().getStatusName();
        String txnType = selectedtxn.getTxnType().getTxnType();

        String display = "Order ID: " + txnID + " / Status: " + txnStatus + " / Type: " + txnType;
        lblOrderDetails.setText(display);

        // ✅ Initialize totals
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        int totalQuantity = 0;

        // ✅ Iterate over TxnItems
        for (TxnItem txnItem : txnItems) {
            Integer itemID = txnItem.getItemID();

            // ✅ Retrieve item details from itemMap
            if (itemMap.containsKey(itemID)) {
                Item item = itemMap.get(itemID);

                int quantity = txnItem.getQuantity();
                totalQuantity += quantity;
                // ✅ Ensure BigDecimal calculations
                BigDecimal itemCost = item.getCostPrice().multiply(BigDecimal.valueOf(quantity));
                BigDecimal itemWeight = item.getWeight().multiply(BigDecimal.valueOf(quantity));

                totalCost = totalCost.add(itemCost);
                totalWeight = totalWeight.add(itemWeight);
            }
        }

        // ✅ Update UI Labels
        lblTotalQuantity.setText("Total Quantity: " + totalQuantity);
        lblTotalCost.setText("Total Cost: $" + String.format("%.2f", totalCost));
        lblWeight.setText("Total Weight: " + String.format("%.2f", totalWeight) + " kg");
    }

}

class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
    private final String datePattern = "yyyy-MM-dd";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

    @Override
    public Object stringToValue(String text) throws ParseException {
        return dateFormatter.parse(text);
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        if (value != null) {
            Calendar cal = (Calendar) value;
            return dateFormatter.format(cal.getTime());
        }
        return "";
    }
}
