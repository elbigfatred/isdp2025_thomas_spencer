package views;

import models.Province;
import models.Site;
import utils.HelpBlurbs;
import utils.SessionManager;
import utils.SiteRequests;
import utils.ProvinceRequests; // Assuming there's a utility for fetching provinces

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * AddEditSiteForm provides a UI for adding and editing sites.
 * It includes form validation and dynamic dropdown population.
 */
public class AddEditSiteForm {

    // =================== UI COMPONENTS ===================

    private JPanel ContentPane;
    private JLabel lblWelcome;
    private JLabel lblLocation;
    private JCheckBox chkActive;
    private JButton btnSave;
    private JButton btnExit;
    private JTextField txtSiteName;
    private JTextField txtAddressLine1;
    private JTextField txtAddressLine2;
    private JTextField txtCity;
    private JComboBox cmbProvince;
    private JTextField txtCountry;
    private JFormattedTextField txtPhoneNumber;
    private JFormattedTextField txtPostalCode;
    private JComboBox cmbDeliveryDay;
    private JSpinner spnDistance;
    private JTextField txtNotes;
    private JLabel lblLogo;
    private JButton btnHelp;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;

    // =================== FRAME VARIABLES ===================

    private JDialog frame;
    private Site selectedSite;
    private String mode;

    // =================== INITIALIZATION SECTION ===================

    /**
     * Displays the form for adding or editing a site.
     *
     * @param parentFrame The parent frame (used for modal behavior).
     * @param currentLocation The location to center the dialog.
     * @param usage Determines whether the form is for "ADD" or "EDIT".
     * @param siteToModify The site to edit (null if adding a new site).
     * @param onCloseCallback A callback function to execute when the dialog closes.
     */
    public void showAddEditSiteForm(Frame parentFrame, Point currentLocation, String usage, Site siteToModify, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);

        if (Objects.equals(usage, "ADD")) {
            frame.setTitle("Bullseye Inventory Management System - Add New Site");
            frame.setSize(600, 570);
        } else if (Objects.equals(usage, "EDIT")) {
            frame.setTitle("Bullseye Inventory Management System - Edit Site");
            frame.setSize(600, 570);
            this.selectedSite = siteToModify;
        }

        frame.setContentPane(getMainPanel());
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        spnDistance.setModel(new SpinnerNumberModel(0, 0, 1000000, 1));

        if (currentLocation != null) {
            frame.setLocation(currentLocation);
        }
        frame.setLocationRelativeTo(null);

        SwingUtilities.invokeLater(() -> {
            SetupBullseyeLogo();
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

    /**
     * Returns the main panel for the form and sets up UI event listeners.
     *
     * @return JPanel The main panel of the form.
     */
    public JPanel getMainPanel() {
        // Check if a user is logged in
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "No user is currently logged in. Not sure how you got here.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE
            );
            //Logout(); // Log out and redirect to the login screen
            return null; // Return null to avoid further execution
        }

        // Set up initial components
        lblWelcome.setText("User: " + session.getUsername());

        lblLocation.setText("Location: " + session.getSiteName());

        btnSave.addActionListener(e -> handleSubmit());

        btnExit.addActionListener(e -> frame.dispose());

        btnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.ADD_EDIT_SITE_HELP,"Site Help",JOptionPane.INFORMATION_MESSAGE);
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

    // =================== UI SETUP & FIELD POPULATION ===================

    private void setupFields(String usage) {
        populateProvincesComboBox();
        populateDaysOfWeek();

        // Initialize formatters first
        initializeFormatters();

        if ("EDIT".equals(usage) && selectedSite != null) {
            txtSiteName.setText(selectedSite.getSiteName());
            txtAddressLine1.setText(selectedSite.getAddress());
            txtAddressLine2.setText(selectedSite.getAddress2());
            txtCity.setText(selectedSite.getCity());

            // Select the correct province in the combo box
            for (int i = 0; i < cmbProvince.getItemCount(); i++) {
                Province province = (Province) cmbProvince.getItemAt(i);
                if (Objects.equals(province.getProvinceId(), selectedSite.getProvinceID())) {
                    cmbProvince.setSelectedItem(province);
                    break;
                }
            }

            txtCountry.setText(selectedSite.getCountry());

            // Manually format the phone number and postal code
            System.out.println("Setting phone number to " + selectedSite.getPhone());
            txtPhoneNumber.setText(formatPhoneNumber(selectedSite.getPhone()));

            System.out.println("Setting postal code to " + selectedSite.getPostalCode());
            txtPostalCode.setText(formatPostalCode(selectedSite.getPostalCode()));

            cmbDeliveryDay.setSelectedItem(selectedSite.getDayOfWeek());
            spnDistance.setValue(selectedSite.getDistanceFromWH());
            txtNotes.setText(selectedSite.getNotes());
            chkActive.setSelected(selectedSite.isActive());
        }
    }

    private void populateProvincesComboBox() {
        List<Province> provinces = ProvinceRequests.fetchActiveProvinces();
        cmbProvince.removeAllItems();
        for (Province province : provinces) {
            cmbProvince.addItem(province);  // Province should be added as an object
        }
    }

    private void populateDaysOfWeek() {
        String[] days = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        for (String day : days) {
            cmbDeliveryDay.addItem(day);
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

    private void handleSubmit() {
        try {
            // Validate form fields
            String validationError = validateFormFields();
            if (validationError != null) {
                showError(validationError);
                return;
            }

            // Proceed with creating and saving the site object
            Site site = new Site();
            if (selectedSite != null) {
                site.setId(selectedSite.getId());
            }
            site.setSiteName(txtSiteName.getText().trim());
            site.setAddress(txtAddressLine1.getText().trim());
            site.setAddress2(txtAddressLine2.getText().trim());  // Optional field
            site.setCity(txtCity.getText().trim());
            site.setProvinceID(((Province) cmbProvince.getSelectedItem()).getProvinceId());
            site.setCountry(txtCountry.getText().trim());
            site.setPostalCode(sanitizePostalCode(txtPostalCode.getText().trim().toUpperCase()));  // Normalize to uppercase
            site.setPhone(sanitizePhoneNumber(txtPhoneNumber.getText().trim()));
            site.setDayOfWeek((String) cmbDeliveryDay.getSelectedItem());
            site.setDistanceFromWH((Integer) spnDistance.getValue());
            site.setNotes(txtNotes.getText().trim());  // Optional field
            site.setActive(chkActive.isSelected());

            // Submit the site to the API
            Boolean success = SiteRequests.saveOrUpdateSite(site);
            String addSuccessMessage = "Site added successfully!";
            String editSuccessMessage = "Site modified successfully!";
            String addFailureMessage = "Failed to add employee. Please try again.";
            String editFailureMessage = "Failed to modify employee. Please try again.";

            if (success) {
                JOptionPane.showMessageDialog(
                        frame,
                        mode == "ADD" ? addSuccessMessage : editSuccessMessage,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                frame.dispose(); // Close the form after successful addition
            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        mode == "EDIT" ? editFailureMessage : addFailureMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void initializeFormatters() {
        try {
            MaskFormatter phoneFormatter = new MaskFormatter("(###) ###-####");
            phoneFormatter.setPlaceholderCharacter('_');
            txtPhoneNumber.setFormatterFactory(new DefaultFormatterFactory(phoneFormatter));

            MaskFormatter postalFormatter = new MaskFormatter("U#U #U#");
            postalFormatter.setPlaceholderCharacter('_');
            txtPostalCode.setFormatterFactory(new DefaultFormatterFactory(postalFormatter));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.length() != 10) {
            return phone; // Return as-is if it's not the expected length
        }
        return String.format("(%s) %s-%s", phone.substring(0, 3), phone.substring(3, 6), phone.substring(6));
    }

    private String formatPostalCode(String postalCode) {
        if (postalCode == null || postalCode.length() != 6) {
            return postalCode; // Return as-is if it's not the expected length
        }
        return postalCode.substring(0, 3) + " " + postalCode.substring(3);
    }

    /**
     * Validates all required fields and returns an error message if validation fails.
     * If all fields are valid, returns null.
     */
    private String validateFormFields() {
        if (txtSiteName.getText().trim().isEmpty()) {
            return "Site Name cannot be empty.";
        }

        if (txtAddressLine1.getText().trim().isEmpty()) {
            return "Address Line 1 cannot be empty.";
        }

        if (txtCity.getText().trim().isEmpty()) {
            return "City cannot be empty.";
        }

        if (cmbProvince.getSelectedItem() == null) {
            return "Please select a province.";
        }

        if (txtCountry.getText().trim().isEmpty()) {
            return "Country cannot be empty.";
        }

        if (!isValidPostalCode(txtPostalCode.getText().trim())) {
            return "Invalid postal code format. Use A1A 1A1.";
        }

        if (!isValidPhoneNumber(txtPhoneNumber.getText().trim())) {
            return "Invalid phone number format. Use (506) 111-1111.";
        }

        if (cmbDeliveryDay.getSelectedItem() == null) {
            return "Please select a delivery day.";
        }

        if ((Integer) spnDistance.getValue() < 0) {
            return "Distance from warehouse cannot be negative.";
        }

        return null; // All checks passed
    }

    /**
     * Displays an error message in a JOptionPane.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Validates a Canadian postal code format (A1A 1A1).
     */
    private boolean isValidPostalCode(String postalCode) {
        return postalCode.matches("^[A-Za-z]\\d[A-Za-z] \\d[A-Za-z]\\d$");
    }

    /**
     * Checks if the phone number contains at least one digit.
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches(".*\\d.*");  // Ensures at least one digit exists
    }

    /**
     * Sanitizes a Canadian postal code by removing spaces (e.g., "A2A 2A2" -> "A2A2A2").
     */
    private String sanitizePostalCode(String postalCode) {
        return postalCode.replaceAll("\\s+", "").toUpperCase();  // Remove spaces and enforce uppercase
    }

    /**
     * Sanitizes a phone number by removing all non-numeric characters (e.g., "(506) 696-6228" -> "5066966228").
     */
    private String sanitizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^\\d]", "");  // Keep only digits
    }
}