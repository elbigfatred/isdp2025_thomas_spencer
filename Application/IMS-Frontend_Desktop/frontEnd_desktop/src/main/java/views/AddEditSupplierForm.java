package views;

import models.Province;
import models.Supplier;
import utils.HelpBlurbs;
import utils.ProvinceRequests;
import utils.SessionManager;
import utils.SupplierUtil;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;

public class AddEditSupplierForm {
    private JPanel ContentPane;
    private JButton btnHelp;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JLabel lblLogo;
    private JLabel lblWelcome;
    private JLabel lblLocation;
    private JTextField txtName;
    private JTextField txtAddress1;
    private JTextField txtAddress2;
    private JTextField txtCity;
    private JTextField txtCountry;
    private JFormattedTextField txtPostalCode;
    private JFormattedTextField txtPhone;
    private JTextField txtContactPerson;
    private JTextField txtNotes;
    private JComboBox cmbProvince;
    private JButton btnExit;
    private JButton btnSave;
    private JCheckBox chkActive;

    private JDialog frame;
    private Supplier selectedSupplier;
    private String mode;

    /**
     * Opens the form for adding or editing a supplier.
     *
     * @param parentFrame The parent frame (used for modal behavior).
     * @param currentLocation The location to center the dialog.
     * @param usage Determines whether the form is for "ADD" or "EDIT".
     * @param supplierToModify The supplier to edit (null if adding a new supplier).
     * @param onCloseCallback A callback function to execute when the dialog closes.
     */
    public void showAddEditSupplierForm(Frame parentFrame, Point currentLocation, String usage, Supplier supplierToModify, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);

        if ("ADD".equals(usage)) {
            frame.setTitle("Add New Supplier");
            frame.setSize(500, 540);
        } else if ("EDIT".equals(usage)) {
            frame.setTitle("Edit Supplier");
            frame.setSize(500, 540);
            this.selectedSupplier = supplierToModify;
        }

        SetupBullseyeLogo();
        mode = usage;
        frame.setContentPane(getMainPanel());
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        if (currentLocation != null) {
            frame.setLocation(currentLocation);
        }
        frame.setLocationRelativeTo(null);

        SwingUtilities.invokeLater(() -> {
            setupFields(usage);
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

    public JPanel getMainPanel() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "No user is currently logged in.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        lblWelcome.setText("User: " + session.getUsername());
        lblLocation.setText("Location: " + session.getSiteName());

        btnSave.addActionListener(e -> handleSubmit());
        btnExit.addActionListener(e -> frame.dispose());

        btnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.ADD_EDIT_SUPPLIER_VIEW, "Supplier Help", JOptionPane.INFORMATION_MESSAGE);
        });

        // Allow Cancel/Exit to be accessed via 'ESC' key
        btnExit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        btnExit.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnExit.doClick();
            }
        });

        // Allow Cancel/Exit to be accessed via 'ESC' key
        btnSave.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "cancel");

        btnSave.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSave.doClick();
            }
        });

        return ContentPane;
    }

    private void setupFields(String usage) {
        populateProvincesComboBox();
        initializeFormatters();

        if ("EDIT".equals(usage) && selectedSupplier != null) {
            txtName.setText(selectedSupplier.getName());
            txtAddress1.setText(selectedSupplier.getAddress1());
            txtAddress2.setText(selectedSupplier.getAddress2());
            txtCity.setText(selectedSupplier.getCity());

            for (int i = 0; i < cmbProvince.getItemCount(); i++) {
                Province province = (Province) cmbProvince.getItemAt(i);
                if (province.getProvinceId().equals(selectedSupplier.getProvinceId())) {
                    cmbProvince.setSelectedItem(province);
                    break;
                }
            }

            txtCountry.setText(selectedSupplier.getCountry());
            txtPostalCode.setText(formatPostalCode(selectedSupplier.getPostalcode()));
            txtPhone.setText(formatPhoneNumber(selectedSupplier.getPhone()));
            txtContactPerson.setText(selectedSupplier.getContact());
            txtNotes.setText(selectedSupplier.getNotes());
            chkActive.setSelected(selectedSupplier.getActive());
        } else {
            txtCountry.setText("Canada");
            for (int i = 0; i < cmbProvince.getItemCount(); i++) {
                if ("NB".equals(((Province) cmbProvince.getItemAt(i)).getProvinceId())) {
                    cmbProvince.setSelectedIndex(i);
                    break;
                }
            }
        }

        txtCountry.setEnabled(false);
    }

    private void populateProvincesComboBox() {
        List<Province> provinces = ProvinceRequests.fetchActiveProvinces();
        cmbProvince.removeAllItems();
        for (Province province : provinces) {
            cmbProvince.addItem(province);
        }
    }

    private void handleSubmit() {
        try {
            String validationError = validateFormFields();
            if (validationError != null) {
                showError(validationError);
                return;
            }

            Supplier supplier = new Supplier();
            if (selectedSupplier != null) {
                supplier.setId(selectedSupplier.getId());
            }
            supplier.setName(txtName.getText().trim());
            supplier.setAddress1(txtAddress1.getText().trim());
            supplier.setAddress2(txtAddress2.getText().trim());
            supplier.setCity(txtCity.getText().trim());
            supplier.setProvinceId(((Province) cmbProvince.getSelectedItem()).getProvinceId());
            supplier.setCountry(txtCountry.getText().trim());
            supplier.setPostalcode(sanitizePostalCode(txtPostalCode.getText().trim().toUpperCase()));
            supplier.setPhone(sanitizePhoneNumber(txtPhone.getText().trim()));
            supplier.setContact(txtContactPerson.getText().trim());
            supplier.setNotes(txtNotes.getText().trim());
            supplier.setActive(chkActive.isSelected());

            boolean success;
            if ("ADD".equals(mode)) {
                success = SupplierUtil.addSupplier(supplier);
            } else {
                success = SupplierUtil.updateSupplier(supplier.getId(), supplier);
            }

            if (success) {
                JOptionPane.showMessageDialog(frame, "Supplier saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Error saving supplier.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.length() != 10) {
            return phone;
        }
        return String.format("(%s) %s-%s", phone.substring(0, 3), phone.substring(3, 6), phone.substring(6));
    }

    private String formatPostalCode(String postalCode) {
        if (postalCode == null || postalCode.length() != 6) {
            return postalCode;
        }
        return postalCode.substring(0, 3) + " " + postalCode.substring(3);
    }

    private boolean isValidPostalCode(String postalCode) {
        return postalCode.matches("^[A-Za-z]\\d[A-Za-z] \\d[A-Za-z]\\d$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches(".*\\d.*");
    }

    private String sanitizePostalCode(String postalCode) {
        return postalCode.replaceAll("\\s+", "").toUpperCase();
    }

    private String sanitizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^\\d]", "");
    }

    private String validateFormFields() {
        if (txtName.getText().trim().isEmpty()) return "Supplier Name cannot be empty.";
        if (txtAddress1.getText().trim().isEmpty()) return "Address Line 1 cannot be empty.";
        if (txtCity.getText().trim().isEmpty()) return "City cannot be empty.";
        if (cmbProvince.getSelectedItem() == null) return "Please select a province.";
        if (!isValidPostalCode(txtPostalCode.getText().trim())) return "Invalid postal code.";
        if (!isValidPhoneNumber(txtPhone.getText().trim())) return "Invalid phone number.";

        return null;
    }

    private void initializeFormatters() {
        try {
            // Phone Number Formatter: (###) ###-####
            MaskFormatter phoneFormatter = new MaskFormatter("(###) ###-####");
            phoneFormatter.setPlaceholderCharacter('_');
            txtPhone.setFormatterFactory(new DefaultFormatterFactory(phoneFormatter));

            // Postal Code Formatter: A1A 1A1 (Canadian Format)
            MaskFormatter postalFormatter = new MaskFormatter("U#U #U#");
            postalFormatter.setPlaceholderCharacter('_');
            txtPostalCode.setFormatterFactory(new DefaultFormatterFactory(postalFormatter));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an error message in a JOptionPane.
     *
     * @param message The validation message to display.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
                frame,
                message,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
        );
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
