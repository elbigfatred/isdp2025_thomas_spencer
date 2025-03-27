package views;

import models.*;
import utils.*;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReturnLossForm {
    private JPanel ContentPane;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JButton btnHelp;
    private JPanel InvCartPanel;
    private JPanel InvPanel;
    private JPanel DetailsPanel;
    private JPanel CDPanel;
    private JButton btnSubmit;
    private JButton btnExit;
    private JTable tblInventory;
    private JTextField txtTxnNotes;
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
    private JCheckBox chkResellable;
    private JPanel RadioButtonPane;
    private JPanel NotesPanel;
    private JLabel lblNotes;
    private JPanel CartPanel;
    private JTable tblCart;
    private JButton btnSave;

    private JDialog frame;
    private Site selectedSite;
    private Runnable onCloseCallback;
    private TxnItem selectedTxnItem;
    private int itemQuantity;
    private List<Inventory> inventoryList;
    private Map<Integer, TxnItem> inventoryItems = new HashMap<>();
    public enum method {LOSS, DAMAGE, RETURN}
    private method currentTxnType;

    public void showReturnLossForm(Frame parentFrame, Point currentLocation, Site siteToWorkWith, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);
        this.selectedSite = siteToWorkWith;
        this.onCloseCallback = onCloseCallback;

        frame.setTitle("Create Loss/Return - Site: " + selectedSite.getSiteName());
        frame.setSize(760, 635);
        frame.setContentPane(getMainPanel());
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        //setupFields(); // Populate the UI with txn data

        if (currentLocation != null) {
            frame.setLocation(currentLocation);
        }

        frame.setVisible(true);
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
        btnSubmit.addActionListener(e -> handleSave());

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

        radLoss.addActionListener(e -> handleTxnTypeChange(method.LOSS));
        radDamage.addActionListener(e -> handleTxnTypeChange(method.DAMAGE));
        radReturn.addActionListener(e -> handleTxnTypeChange(method.RETURN));

        radLoss.setSelected(true);
        currentTxnType = method.LOSS;

        btnAdd.addActionListener(e -> handleIncrement());
        btnDecrement.addActionListener(e -> handleDecrement());

        chkResellable.setEnabled(false);
        chkResellable.setSelected(false);

        loadInventoryforSite();
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

    private void loadInventoryforSite(){

        inventoryList = InventoryRequests.fetchInventoryBySite(selectedSite.getId());
        updateInventoryTablebySearch();

    }

    private void updateInventoryTablebySearch() {
        String search = txtInventorySearch.getText().trim().toLowerCase();

        if (search.isEmpty()) {
            populateInventoryTable(inventoryList);
            return;
        }

        List<Inventory> filtered = inventoryList.stream()
                .filter(inv -> {
                    Item item = inv.getItem();
                    String category = (item.getCategory() != null) ? item.getCategory().getCategoryName().toLowerCase() : "";
                    return item.getName().toLowerCase().contains(search)
                            || item.getSku().toLowerCase().contains(search)
                            || inv.getItemLocation().toLowerCase().contains(search)
                            || category.contains(search);
                })
                .toList();

        populateInventoryTable(filtered);
    }

    private void populateInventoryTable(List<Inventory> filteredInventory){
        String[] columnNames = {"ID", "Name", "SKU", "Qty"};
        Object[][] rowData = new Object[filteredInventory.size()][columnNames.length];

        inventoryItems.clear(); // Reset the map

        for (int i = 0; i < filteredInventory.size(); i++) {
            Inventory inv = filteredInventory.get(i);
            Item item = inv.getItem();


            rowData[i][0] = inv.getItemID();
            rowData[i][1] = item.getName();
            rowData[i][2] = item.getSku();
            rowData[i][3] = inv.getQuantity();

            // Map for selection/reference later
            TxnItem txnItem = new TxnItem();
            txnItem.setItemID(inv.getItemID());
            txnItem.setItemName(item.getName());
            txnItem.setItemSku(item.getSku());
            txnItem.setQuantity(inv.getQuantity());
            txnItem.setNotes(inv.getNotes()); // Optional, could store notes in txn item context
            inventoryItems.put(inv.getItemID(), txnItem);
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
                    int itemId = (int) tblInventory.getValueAt(selectedRow, 0); // assuming ID is in column 0
                    selectedTxnItem = inventoryItems.get(itemId);
                    itemQuantity = 0;
                    updateItemDisplay();
                }
            }
        });

        tblInventory.getTableHeader().setReorderingAllowed(false);
    }

    private void updateItemDisplay() {
        if (selectedTxnItem != null) {
            lblItemDetails.setText("Selected: " + selectedTxnItem.getItemName() + " (Qty: " + itemQuantity + ")");

            // Always disable decrement if quantity is 0
            btnDecrement.setEnabled(itemQuantity > 0);

            // Add button logic varies by txn type
            switch (currentTxnType) {
                case RETURN -> {
                    // No upper bound for RETURN
                    btnAdd.setEnabled(true);
                }
                case LOSS, DAMAGE -> {
                    int stockQty = selectedTxnItem.getQuantity();
                    boolean canAdd = itemQuantity < stockQty;
                    btnAdd.setEnabled(canAdd);
                }
                default -> btnAdd.setEnabled(false);
            }
            btnSubmit.setEnabled(itemQuantity > 0);
        } else {
            // No item selected — disable both buttons and clear label
            lblItemDetails.setText("Please select an item...");
            btnAdd.setEnabled(false);
            btnDecrement.setEnabled(false);
            btnSubmit.setEnabled(false);
        }
    }

    private void handleTxnTypeChange(method newType) {
        currentTxnType = newType;
        selectedTxnItem = null;
        itemQuantity = 0;
        txtTxnNotes.setText("");
        chkResellable.setSelected(false);
        chkResellable.setEnabled(currentTxnType == method.RETURN);
        updateItemDisplay();
    }

    private void handleIncrement() {
        if (selectedTxnItem == null || currentTxnType == null) return;

        int maxAvailable = selectedTxnItem.getQuantity(); // quantity in inventory
        if (currentTxnType == method.RETURN || itemQuantity < maxAvailable) {
            itemQuantity++;
            updateItemDisplay();
        }
    }

    private void handleDecrement() {
        if (selectedTxnItem == null || itemQuantity <= 0) return;

        itemQuantity--;
        updateItemDisplay();
    }

    private void handleSave() {
        if (txtTxnNotes.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a note!", "Insufficent Information", JOptionPane.WARNING_MESSAGE);
            return;
        }


        // we'll show the selected item, itemquantity, method, and notes

        String notes = txtTxnNotes.getText().trim();
        boolean resellable = chkResellable.isSelected();

        List<Employee> employees = EmployeeRequests.fetchEmployees();
        Employee selectedEmployee = null;
        for (Employee employee : employees) {
            if(Objects.equals(employee.getUsername(), SessionManager.getInstance().getUsername())){
                selectedEmployee = employee;
                break;
            }
        }

        // 🔹 Build base TxnItem
        TxnItem txnItem = new TxnItem();
        txnItem.setItemID(selectedTxnItem.getItemID());
        txnItem.setItemName(selectedTxnItem.getItemName());
        txnItem.setItemSku(selectedTxnItem.getItemSku());
        txnItem.setQuantity(itemQuantity);
        txnItem.setNotes(notes);

        // 🔹 Create Txn wrapper object (you can define this however your backend expects)
        Txn txn = new Txn();
        txn.setSiteFrom(selectedSite);
        txn.setEmployee(selectedEmployee);
        TxnType txntype = new TxnType();
        txntype.setTxnType(currentTxnType.name());
        txn.setTxnType(txntype);
        txn.setNotes(notes);

        //JOptionPane.showMessageDialog(frame, txn.toString() + "\n" + itemQuantity + "\n" + selectedTxnItem, "Insufficent Information", JOptionPane.WARNING_MESSAGE);

        Inventory invChange = new Inventory();
        invChange.setItemID(selectedTxnItem.getItemID());
        invChange.setQuantity(itemQuantity);

        List<Inventory> delta = List.of(invChange);

        switch (currentTxnType) {
            case LOSS, DAMAGE -> {
                boolean success = LossReturnRequests.submitLossReturnTxn(txn, txnItem, resellable, selectedEmployee);
                if (!success) {
                    JOptionPane.showMessageDialog(frame, "There was a problem saving the transaction.", "Save Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 🔻 Decrement inventory
                InventoryRequests.decrementInventory(selectedSite.getId(), delta);
            }

            case RETURN -> {
                boolean success = LossReturnRequests.submitLossReturnTxn(txn, txnItem, resellable, selectedEmployee);
                if (!success) {
                    JOptionPane.showMessageDialog(frame, "There was a problem saving the transaction.", "Save Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 🔼 Increment inventory only if resellable
                if (resellable) {
                    InventoryRequests.incrementInventory(selectedSite.getId(), delta);
                }
            }
        }

        // ✅ Once everything completes successfully
        JOptionPane.showMessageDialog(frame, "Transaction saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        frame.dispose();

        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
}
