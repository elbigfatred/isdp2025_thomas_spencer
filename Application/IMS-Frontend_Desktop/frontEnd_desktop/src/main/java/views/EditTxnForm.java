package views;

import models.Txn;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;
import models.*;
import utils.TxnsModsRequests;

import java.util.List;


public class EditTxnForm {
    private JPanel ContentPane;
    private JLabel lblLogo;
    private JLabel lblWelcome;
    private JLabel lblLocation;
    private JComboBox cmbDestinationSite;
    private JComboBox cmbTxnStatus;
    private JComboBox cmbTxnType;
    private JTextField txtBarcode;
    private JComboBox cmbDeliveryId;
    private JCheckBox chkEmergencyOrder;
    private JButton btnSave;
    private JButton btnExit;
    private JButton button1;
    private JPanel pnlCalenderHolder;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;

    private JDialog frame;
    private Txn selectedTxn;
    private Runnable onCloseCallback;
    private JDatePickerImpl datePicker;

    public void showEditTxnForm(Frame parentFrame, Point currentLocation, Txn txnToEdit, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);
        this.selectedTxn = txnToEdit;
        this.onCloseCallback = onCloseCallback;

        frame.setTitle("Edit Transaction - " + txnToEdit.getId());
        frame.setSize(700, 430);
        frame.setContentPane(getMainPanel());
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setupFields(); // Populate the UI with txn data

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
        btnSave.addActionListener(e -> handleSave());

        return ContentPane;
    }

    private void setupFields() {
        // ✅ Populate Sites
        List<Site> allSites = TxnsModsRequests.getAllSites();
        cmbDestinationSite.setModel(new DefaultComboBoxModel<>(allSites.toArray(new Site[0])));
        cmbDestinationSite.setSelectedItem(
                allSites.stream().filter(site -> site.getId() == selectedTxn.getSiteTo().getId()).findFirst().orElse(null)
        );

        // ✅ Populate Transaction Statuses
        List<TxnStatus> allStatuses = TxnsModsRequests.getAllTxnStatuses();
        cmbTxnStatus.setModel(new DefaultComboBoxModel<>(allStatuses.toArray(new TxnStatus[0])));
        cmbTxnStatus.setSelectedItem(
                allStatuses.stream().filter(status -> status.getStatusName().equals(selectedTxn.getTxnStatus().getStatusName()))
                        .findFirst().orElse(null)
        );

        // ✅ Populate Transaction Types
        List<TxnType> allTypes = TxnsModsRequests.getAllTxnTypes();
        cmbTxnType.setModel(new DefaultComboBoxModel<>(allTypes.toArray(new TxnType[0])));
        cmbTxnType.setSelectedItem(
                allTypes.stream().filter(type -> type.getTxnType().equals(selectedTxn.getTxnType().getTxnType()))
                        .findFirst().orElse(null)
        );

        // ✅ Populate Barcode
        txtBarcode.setText(selectedTxn.getBarCode());

        // ✅ Populate Delivery ID
        List<Integer> allDeliveries = TxnsModsRequests.getAllDeliveryIds();
        cmbDeliveryId.setModel(new DefaultComboBoxModel<>(allDeliveries.toArray(new Integer[0])));
        cmbDeliveryId.setSelectedItem(selectedTxn.getDeliveryID());

        // ✅ Set Emergency Order Checkbox
        chkEmergencyOrder.setSelected(selectedTxn.isEmergencyDelivery());

        // ✅ Setup Date Picker for Ship Date
        setupDatePicker(selectedTxn.getShipDate());
    }


    private void setupDatePicker(LocalDateTime shipDate) {
        UtilDateModel model = new UtilDateModel();
        if (shipDate != null) {
            model.setValue(java.sql.Date.valueOf(shipDate.toLocalDate()));
        }

        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        datePicker = new JDatePickerImpl(datePanel, new ViewReceiveOrder.DateLabelFormatter());

        pnlCalenderHolder.setLayout(new BorderLayout());
        pnlCalenderHolder.add(datePicker, BorderLayout.CENTER);
    }

    private void handleSave() {
        // 🚨 Prevent saving if TXN is CANCELLED or COMPLETE
        if (Objects.equals(selectedTxn.getTxnStatus().getStatusName(), "CANCELLED") ||
                Objects.equals(selectedTxn.getTxnStatus().getStatusName(), "COMPLETE")) {
            JOptionPane.showMessageDialog(frame, "Cannot modify a completed or cancelled transaction.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ✅ Extract only the necessary fields
        int siteIDTo = ((Site) cmbDestinationSite.getSelectedItem()).getId();
        String txnStatus = ((TxnStatus) cmbTxnStatus.getSelectedItem()).getStatusName();
        String txnType = ((TxnType) cmbTxnType.getSelectedItem()).getTxnType();
        String barCode = txtBarcode.getText();
        Integer deliveryID = (Integer) cmbDeliveryId.getSelectedItem();
        boolean emergencyDelivery = chkEmergencyOrder.isSelected();

        // ✅ Fetch Date from DatePicker
        String shipDate = null;
        java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
        if (selectedDate != null) {
            shipDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().toString();
        }

        // ✅ Send Update Request with only required fields
        boolean success = TxnsModsRequests.updateTxn(
                selectedTxn.getId(), siteIDTo, txnStatus, shipDate, txnType, barCode, deliveryID, emergencyDelivery
        );

        if (success) {
            JOptionPane.showMessageDialog(frame, "Transaction updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            if (onCloseCallback != null) onCloseCallback.run();
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update transaction.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // ✅ Custom Date Formatter
    private static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) {
            try {
                return dateFormatter.parse(text);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String valueToString(Object value) {
            if (value != null) {
                return dateFormatter.format(value);
            }
            return "";
        }
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

}
