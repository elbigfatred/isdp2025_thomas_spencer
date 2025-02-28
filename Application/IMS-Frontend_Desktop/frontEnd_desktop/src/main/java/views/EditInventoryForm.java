package views;

import models.Inventory;
import models.Item;
import models.Site;
import utils.HelpBlurbs;
import utils.InventoryRequests;
import utils.ItemRequests;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

public class EditInventoryForm {
    private JPanel contentPane;
    private JLabel logoLabel;
    private JLabel lblWelcome;
    private JLabel lblLocation;
    private JLabel lblSKULabel;
    private JButton btnSave;
    private JButton btnCancel;
    private JTextField textField1;
    private JSpinner spinner1;
    private JSpinner spinner2;
    private JLabel SPACER1;
    private JLabel SPACER4;
    private JLabel SPACER3;
    private JLabel SPACER2;

    // =================== FRAME VARIABLES ===================

    JDialog frame;
    private Inventory selectedItem;

    // =================== FORM INITIALIZATION ===================

    /**
     * Initializes and returns the main panel for editing an item.
     * Ensures that a user is logged in before displaying the form.
     *
     * @return The JPanel containing all form components or null if the user is not logged in.
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
            return null; // Return null to avoid further execution
        }

        // Set up initial components
        lblWelcome.setText("User: " + session.getUsername());
        lblLocation.setText("Location: " + session.getSiteName());

        // action listeners for buttons
        btnSave.addActionListener(e -> {
            saveEvent();
        });

        btnCancel.addActionListener(e -> {
            frame.dispose(); // Close the password reset form
        });

        // Allow Cancel/Exit to be accessed via 'ESC' key
        btnCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        btnCancel.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        // Allow Login to be accessed via 'Enter' key
        btnSave.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "click");

        btnSave.getActionMap().put("click", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSave.doClick();
            }
        });

//        btnHelp.addActionListener(e -> {
//            JOptionPane.showMessageDialog(frame, HelpBlurbs.EDIT_ITEM_HELP,"Edit Item Help",JOptionPane.INFORMATION_MESSAGE);
//        });

        return contentPane;
    }

    /**
     * Displays the item edit form as a modal dialog.
     *
     * @param parentFrame    The parent frame for modal behavior.
     * @param currentLocation The screen location to center the form.
     * @param itemToModify   The item to be modified.
     * @param onCloseCallback A callback executed when the form is closed.
     */
    public void showItemEditForm(Frame parentFrame, Point currentLocation, Inventory itemToModify, Site itemLocation, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);

        frame.setTitle("Bullseye Inventory Management System - Modify Inventory Item"); // Create the frame

        lblSKULabel.setText("Editing details of SKU " +  itemToModify.getItem().getSku() + " at " + itemLocation.getSiteName() + " Location");

        if (itemToModify != null) {
            selectedItem = itemToModify;
        }
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Set close operation
        frame.setSize(630, 300);                   // Set frame size
        if(currentLocation != null) {
            frame.setLocation(currentLocation);
        }
        frame.setLocationRelativeTo(null);         // Center the frame

        // Delay setup methods until the dialog is visible
        SwingUtilities.invokeLater(() -> {
            SetupBullseyeLogo();
            setupFields();
        });

        frame.setVisible(true);                    // Make it visible

        // Add a listener to detect when the dialog is closed
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (onCloseCallback != null) {
                    onCloseCallback.run(); // Execute the callback when dialog is closed
                }
            }
        });
    }

    // =================== UI SETUP SECTION ===================
    /**
     * Loads and sets the Bullseye logo in the form.
     */
    private void SetupBullseyeLogo() {
        String logoPath = "/bullseye.jpg"; // Classpath-relative path
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        logoLabel.setIcon(resizedIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setText("");
    }

    /**
     * Populates the form fields with existing item data.
     * If the item has an image, updates the button text accordingly.
     */
    /**
     * Populates the form fields with existing inventory data.
     */
    private void setupFields() {
        if (selectedItem == null) return;

        // Set notes text field
        textField1.setText(selectedItem.getNotes());

        // Configure spinners for reorder threshold and optimum threshold
        SpinnerNumberModel reorderModel = new SpinnerNumberModel(selectedItem.getReorderThreshold(), 0, Integer.MAX_VALUE, 1);
        SpinnerNumberModel optimumModel = new SpinnerNumberModel(selectedItem.getOptimumThreshold(), 0, Integer.MAX_VALUE, 1);

        spinner1.setModel(reorderModel);
        spinner2.setModel(optimumModel);
    }

    /**
     * Handles the save event for updating the inventory.
     */
    private void saveEvent() {
        if (selectedItem == null) {
            System.out.println("[ERROR] No inventory item selected.");
            return;
        }

        // Retrieve updated values
        String updatedNotes = textField1.getText().trim();
        int updatedReorderThreshold = (Integer) spinner1.getValue();
        int updatedOptimumThreshold = (Integer) spinner2.getValue();

        if(updatedOptimumThreshold < updatedReorderThreshold) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Optimum threshold must be higher than Reorder threshold.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
            spinner1.transferFocus();
            return;
        }

        // Log the values retrieved from UI components
        System.out.println("[DEBUG] Updated Reorder Threshold: " + updatedReorderThreshold);
        System.out.println("[DEBUG] Updated Optimum Threshold: " + updatedOptimumThreshold);
        System.out.println("[DEBUG] Updated Notes: " + updatedNotes);

        // Validate input: Ensure thresholds are non-negative
        if (updatedReorderThreshold < 0 || updatedOptimumThreshold < 0) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Threshold values cannot be negative.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
            System.out.println("[ERROR] One or both threshold values are negative.");
            return;
        }

        // Log before updating the inventory object
        System.out.println("[DEBUG] Before update - " + selectedItem.toString());

        // Update the selected inventory item
        selectedItem.setNotes(updatedNotes);
        selectedItem.setReorderThreshold(updatedReorderThreshold);
        selectedItem.setOptimumThreshold(updatedOptimumThreshold);

        // Log after updating the object
        System.out.println("[DEBUG] After update - " + selectedItem.toString());

        // Send update request to backend
        boolean success = InventoryRequests.updateInventory(selectedItem);

        // Log the response from backend
        if (success) {
            System.out.println("[INFO] Inventory updated successfully.");
            JOptionPane.showMessageDialog(
                    frame,
                    "Inventory updated successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            frame.dispose(); // Close the form
        } else {
            System.out.println("[ERROR] Failed to update inventory.");
            JOptionPane.showMessageDialog(
                    frame,
                    "Failed to update inventory. Please try again.",
                    "Update Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }}
