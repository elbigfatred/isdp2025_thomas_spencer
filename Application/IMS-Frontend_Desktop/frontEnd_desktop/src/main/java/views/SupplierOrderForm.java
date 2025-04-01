package views;

import models.*;
import utils.*;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierOrderForm {
    private JPanel ContentPane;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JButton btnHelp;
    private JPanel RadioButtonPanel;
    private JPanel InvCartPanel;
    private JPanel InvPanel;
    private JPanel DetailsPanel;
    private JPanel NotesPanel;
    private JPanel CDPanel;
    private JButton btnSave;
    private JButton btnExit;
    private JTable tblInventory;
    private JTextField txtTxnNotes;
    private JLabel lblNotes;
    private JButton btnDecrement;
    private JButton btnAdd;
    private JLabel lblItemDetails;
    private JRadioButton radLoss;
    private JRadioButton radDamage;
    private JRadioButton radReturn;
    private JTextField txtInventorySearch;
    private JLabel lblLogo;
    private JLabel lblWelcome;
    private JLabel lblLocation;
    private JButton btnSubmit;
    private JPanel CartPanel;
    private JTable tblCart;
    private JButton btnViewDetails;
    private JComboBox cmbSuppliers;


    private JDialog frame;
    private int warehouseId = 2;
    private Runnable onCloseCallback;
    private Item selectedItem;
    private int caseSize;
    private int itemQuantity;
    private List<Inventory> warehouseInventoryList;
    private List<Inventory> cartItems = new ArrayList<>();
    private Map<Integer, Item> items = new HashMap<>();
    String employeeUsername = SessionManager.getInstance().getUsername();
    int employeeId;
    boolean hasActiveOrder;
    int currTxnId;

    public void showSupplierOrderForm(Frame parentFrame, Point currentLocation, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);
        this.onCloseCallback = onCloseCallback;

        frame.setTitle("Create Supplier Order");
        frame.setSize(1000, 635);
        frame.setContentPane(getMainPanel());
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        if (currentLocation != null) {
            frame.setLocation(currentLocation);
        }

        System.out.println("[DEBUG] Active supplier order exists? " + hasActiveOrder);

        btnAdd.setEnabled(false);
        btnDecrement.setEnabled(false);
        btnViewDetails.setEnabled(false);

        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (onCloseCallback != null) {
                    onCloseCallback.run(); // Execute the callback when dialog is closed
                }
            }
        });
    }

    private JPanel getMainPanel() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(frame, "No user is logged in. Cannot modify transaction.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        SetupBullseyeLogo();

        lblWelcome.setText("Welcome, " + session.getUsername());
        lblLocation.setText("Location: " + session.getSiteName());

        btnExit.addActionListener(e -> frame.dispose());
        btnSave.addActionListener(e -> handleUpdate());
        btnSubmit.addActionListener(e -> handleSubmit());

        btnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.EDIT_TXN_VIEW,"Transactions Help",JOptionPane.INFORMATION_MESSAGE);
        });

        txtInventorySearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateInventoryTablebySearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateInventoryTablebySearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateInventoryTablebySearch();
            }
        });

        btnAdd.addActionListener(e -> handleIncrement());
        btnDecrement.addActionListener(e -> handleDecrement());
        btnViewDetails.addActionListener(e -> viewDetails());

        cmbSuppliers.addActionListener(e ->{
            updateInventoryTablebySearch();
        });

        btnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.SUPPLIER_ORDER_CREATE_SCREEN,"Supplier Orders Help",JOptionPane.INFORMATION_MESSAGE);
        });

        loadInventoryandOrderforSite();
        loadSuppliers();
        updateItemDisplay();

        return ContentPane;
    }

    private void SetupBullseyeLogo() {
        String logoPath = "/bullseye.jpg"; // Classpath-relative path
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(100, 100
                , Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        lblLogo.setIcon(resizedIcon);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setText("");
    }

    private void loadInventoryandOrderforSite() {
        List<Item> allitems = ItemRequests.fetchItems();
        for (Item item : allitems) {
            items.put(item.getId(), item);
        }

        List<Employee> allEmps = EmployeeRequests.fetchEmployees();
        for (Employee employee : allEmps) {
            if (employee.getUsername().equals(employeeUsername)){
                employeeId = employee.getId();
                break;
            }
        }

        warehouseInventoryList = InventoryRequests.getViableSupplierInventory();
        Integer activeTxnId = SupplierOrderRequests.getActiveOrderId();

        System.out.println(activeTxnId);

        if (activeTxnId != null) {
            hasActiveOrder = true;
            List<TxnItem> existingItems = SupplierOrderRequests.fetchTxnItems(activeTxnId);
            populateCartTable(existingItems); // Populate cart with items from active order
            List<Inventory> conv = new ArrayList<>();
            for (TxnItem txnItem : existingItems) {
                Inventory inventory = new Inventory();
                inventory.setItemID(txnItem.getItemID());
                inventory.setQuantity(txnItem.getQuantity());
                conv.add(inventory);
            }
            cartItems = conv;
            currTxnId = activeTxnId;
        } else {
            boolean success = SupplierOrderRequests.createNewSupplierOrder(employeeId);
            if (!success) {
                JOptionPane.showMessageDialog(frame, "Failed to create new supplier order.", "Error", JOptionPane.ERROR_MESSAGE);
                frame.dispose();
            }

            Integer newTxnId = SupplierOrderRequests.getActiveOrderId();
            System.out.println("[DEBUG] Created new supplier order with ID: " + newTxnId);

            currTxnId = newTxnId;

            List<TxnItem> prepopulatedItems = SupplierOrderRequests.fetchTxnItems(newTxnId);
            populateCartTable(prepopulatedItems); // Populate cart with prepopulated items
            List<Inventory> conv = new ArrayList<>();
            for (TxnItem txnItem : prepopulatedItems) {
                Inventory inventory = new Inventory();
                inventory.setItemID(txnItem.getItemID());
                inventory.setQuantity(txnItem.getQuantity());
                conv.add(inventory);
            }
            cartItems = conv;
        }
        updateInventoryTablebySearch();
    }

    private void loadSuppliers() {
        List<Supplier> allActiveSuppliers = SupplierUtil.fetchAllSuppliers(false);
        cmbSuppliers.removeAllItems();
        cmbSuppliers.addItem("All");
        for (Supplier supplier : allActiveSuppliers) {
            cmbSuppliers.addItem(supplier);
        }
    }

    private void populateCartTable(List<TxnItem> txnItems) {
        String[] columnNames = {"ID", "Item Name", "SKU", "Qty", "Supplier"};
        Object[][] rowData = new Object[txnItems.size()][columnNames.length];

        for (int i = 0; i < txnItems.size(); i++) {
            TxnItem txnItem = txnItems.get(i);
            Item item = items.get(txnItem.getItemID());  // Fetch full item details from items map
            rowData[i][0] = txnItem.getItemID();
            rowData[i][1] = item != null ? item.getName() : "Unknown Item";
            rowData[i][2] = item != null ? item.getSku() : "N/A";
            rowData[i][3] = txnItem.getQuantity();
            rowData[i][4] = item.getSupplier().getName();
        }

        tblCart.setModel(new javax.swing.table.DefaultTableModel(rowData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        tblCart.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblCart.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblCart.getSelectedRow();
                if (selectedRow >= 0) {
                    int itemId = (int) tblCart.getValueAt(selectedRow, 0);
                    System.out.println("[DEBUG] Selected Item: " + itemId);
                    Item selectedItemFromInventory = items.get(itemId);
                    handleItemSelection(selectedItemFromInventory);
                }
            }
        });

        tblCart.getTableHeader().setReorderingAllowed(false);
    }

    private void updateInventoryTablebySearch() {
        String search = txtInventorySearch.getText().trim().toLowerCase();

        // Get selected supplier from the combo box
        Supplier selectedSupplier = null;
        int selectedSupplierId; // Default value indicating no supplier selected
        if (cmbSuppliers.getSelectedItem() instanceof Supplier) {
            selectedSupplier = (Supplier) cmbSuppliers.getSelectedItem();
            selectedSupplierId = selectedSupplier.getId();
        } else {
            selectedSupplierId = -1;
        }

        // If search is empty and no supplier is selected, show all items
        if (search.isEmpty() && selectedSupplierId == -1) {
            populateInventoryTable(warehouseInventoryList);
            return;
        }

        // Filter inventory based on search and selected supplier
        List<Inventory> filtered = warehouseInventoryList.stream()
                .filter(inv -> {
                    Item item = inv.getItem();
                    Supplier supplier = item.getSupplier();

                    // Prepare category and case size for search
                    String category = item.getCategory() != null ? item.getCategory().getCategoryName().toLowerCase() : "";
                    String supplierName = supplier != null ? supplier.getName().toLowerCase() : "";
                    String caseSize = String.valueOf(item.getCaseSize());

                    // Check if item matches search term
                    boolean matchesSearch = item.getName().toLowerCase().contains(search)
                            || item.getSku().toLowerCase().contains(search)
                            || inv.getItemLocation().toLowerCase().contains(search)
                            || category.contains(search)
                            || supplierName.contains(search)
                            || caseSize.contains(search);

                    // Filter by selected supplier if applicable
                    if (selectedSupplierId != -1) {
                        return matchesSearch && supplier != null && supplier.getId() == selectedSupplierId;
                    }

                    // If no supplier is selected, return items based only on search
                    return matchesSearch;
                })
                .toList();

        // Populate table with filtered data
        populateInventoryTable(filtered);
    }

    private void populateInventoryTable(List<Inventory> filteredInventory){
        String[] columnNames = {"ID", "Name", "SKU", "Qty in Stock", "Supplier", "Case Size"};
        Object[][] rowData = new Object[filteredInventory.size()][columnNames.length];


        for (int i = 0; i < filteredInventory.size(); i++) {
            Inventory inv = filteredInventory.get(i);
            Item item = inv.getItem();
            Supplier supplier = item.getSupplier();

            rowData[i][0] = inv.getItemID();
            rowData[i][1] = item.getName();
            rowData[i][2] = item.getSku();
            rowData[i][3] = inv.getQuantity();
            rowData[i][4] = supplier != null ? supplier.getName() : "N/A";
            rowData[i][5] = item.getCaseSize();

            // Track it in case it's selected later
            TxnItem txnItem = new TxnItem();
            txnItem.setItemID(inv.getItemID());
            txnItem.setItemName(item.getName());
            txnItem.setItemSku(item.getSku());
            txnItem.setQuantity(inv.getQuantity());
            txnItem.setNotes(inv.getNotes());
        }

        tblInventory.setModel(new javax.swing.table.DefaultTableModel(rowData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        tblInventory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblInventory.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblInventory.getSelectedRow();
                if (selectedRow >= 0) {
                    int itemId = (int) tblInventory.getValueAt(selectedRow, 0);
                    Item selectedItemFromInventory = items.get(itemId);
                    handleItemSelection(selectedItemFromInventory);
                }
            }
        });

        tblInventory.getTableHeader().setReorderingAllowed(false);
    }

    private void updateItemDisplay() {
        if (selectedItem != null) {
            lblItemDetails.setText("Item: " + selectedItem.getName()); // Set item name
            lblItemDetails.setText(lblItemDetails.getText() + " | SKU: " + selectedItem.getSku()); // Set SKU
            lblItemDetails.setText(lblItemDetails.getText() + " | Case Size: " + caseSize); // Set case size
            lblItemDetails.setText(lblItemDetails.getText() + " | Quantity in Cart: " + itemQuantity); // Set quantity in cart
            if(itemQuantity == 0){
                btnDecrement.setEnabled(false);
                btnAdd.setEnabled(true);
                btnViewDetails.setEnabled(true);
            }
            else{
                btnDecrement.setEnabled(true);
                btnAdd.setEnabled(true);
                btnViewDetails.setEnabled(true);
            }

        } else {
            lblItemDetails.setText("No item selected.");
            btnDecrement.setEnabled(false);
            btnAdd.setEnabled(false);
            btnViewDetails.setEnabled(false);
        }
    }

    private void handleItemSelection(Item item) {
        if (item != null) {
            selectedItem = item;  // Store the selected item

            caseSize = selectedItem.getCaseSize();  // Set the case size
            itemQuantity = 0; // Start with 0 quantity in cart initially

            // Check if the item is already in the cart
            for (Inventory cartItem : cartItems) {
                if (cartItem.getItemID() == selectedItem.getId()) {
                    itemQuantity = cartItem.getQuantity(); // Update the quantity if the item is already in the cart
                    break;
                }
            }

            // Now update the item display with the current details
            updateItemDisplay();
        }
    }

    private void handleIncrement() {
        if (selectedItem != null) {
            // Increment the quantity by the case size
            itemQuantity += caseSize;

            if (itemQuantity > 0) {
                // Check if the item is already in the cart
                boolean itemExistsInCart = false;
                for (Inventory cartItem : cartItems) {
                    if (cartItem.getItemID() == selectedItem.getId()) {
                        itemExistsInCart = true;
                        // Increment the quantity if the item exists in the cart
                        cartItem.setQuantity(itemQuantity); // Update the quantity in the cart
                        break;
                    }
                }

                // If the item doesn't exist in the cart, create a new inventory item and add it to the cart
                if (!itemExistsInCart) {
                    Inventory newInventoryItem = new Inventory();
                    newInventoryItem.setItemID(selectedItem.getId());
                    newInventoryItem.setQuantity(itemQuantity); // Set the quantity as case size
                    cartItems.add(newInventoryItem);
                }
            }

            // Now update the cart with the new quantity
            List<TxnItem> txnItems = new ArrayList<>();
            for (Inventory cartItem : cartItems) {
                TxnItem txnItem = new TxnItem();
                txnItem.setItemID(cartItem.getItemID());
                txnItem.setQuantity(cartItem.getQuantity());
                txnItems.add(txnItem);
            }

            populateCartTable(txnItems); // Update cart table with new txn items
            updateItemDisplay(); // Update item display to show the new quantity
        }
    }

    private void handleDecrement() {
        if (selectedItem != null && itemQuantity >= caseSize) {
            // Decrement the quantity by the case size
            itemQuantity -= caseSize;

            if (itemQuantity > 0) {
                // Update the cart accordingly
                for (Inventory cartItem : cartItems) {
                    if (cartItem.getItemID() == selectedItem.getId()) {
                        cartItem.setQuantity(itemQuantity); // Update the quantity in the cart
                        break;
                    }
                }
            } else {
                // If quantity is 0, remove the item from the cart
                cartItems.removeIf(cartItem -> cartItem.getItemID() == selectedItem.getId());
            }

            // Now update the cart with the new quantity
            List<TxnItem> txnItems = new ArrayList<>();
            for (Inventory cartItem : cartItems) {
                TxnItem txnItem = new TxnItem();
                txnItem.setItemID(cartItem.getItemID());
                txnItem.setQuantity(cartItem.getQuantity());
                txnItems.add(txnItem);
            }

            populateCartTable(txnItems); // Update cart table with new txn items
            updateItemDisplay(); // Update item display to show the new quantity
        }
    }

    private void handleUpdate() {

        if (cartItems.size() < 1) {
            JOptionPane.showMessageDialog(frame, "There are no items in the order!", "Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Prepare inventory items for saving
        List<Inventory> inventoryItemsToSave = new ArrayList<>();

        // Populate inventoryItemsToSave from cartItems (this assumes the cartItems list has been updated properly)
        for (Inventory cartItem : cartItems) {
            Inventory inventory = new Inventory();
            inventory.setItemID(cartItem.getItemID());
            inventory.setQuantity(cartItem.getQuantity()); // Save the updated quantity
            inventoryItemsToSave.add(inventory);
        }

        // Call updateItems to save the changes
        boolean success = SupplierOrderRequests.updateItems(inventoryItemsToSave, currTxnId, employeeUsername);

        if (success) {
            JOptionPane.showMessageDialog(frame, "Supplier order saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to save the supplier order.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSubmit() {
        // First, save the order before submitting
        handleUpdate();

        // Now, call submitSupplierOrder to finalize the transaction
        boolean success = SupplierOrderRequests.submitSupplierOrder(currTxnId, employeeUsername);

        if (success) {
            JOptionPane.showMessageDialog(frame, "Supplier order submitted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Optionally, close the window or reset the form
            frame.dispose();
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to submit the supplier order.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDetails(){
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(frame, "No item is selected.", "Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SupplierOrderViewItemForm viewItemForm = new SupplierOrderViewItemForm();

        // Assuming you want to center the form on the screen
        viewItemForm.showItemEditForm(frame, frame.getLocation(), selectedItem, () -> {

        });

    }

}


