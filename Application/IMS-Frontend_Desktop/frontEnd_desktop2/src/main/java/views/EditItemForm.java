package views;

import models.Item;
import utils.ItemUploader;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

/**
 * EditItemForm provides a UI for modifying an item's notes and image.
 * Users can update the item's details and submit changes to the backend.
 */
public class EditItemForm {

    // =================== UI COMPONENTS ===================

    private JPanel ContentPane;
    private JLabel logoLabel;
    private JTextField txtNotes;
    private JLabel lblNotes;
    private JButton btnAddImage;
    private JButton btnSave;
    private JButton btnCancel;
    private JLabel lblImagePath;
    private JLabel lblWelcome;
    private JLabel lblLocation;

    // =================== FRAME VARIABLES ===================

    JDialog frame;
    private Item selectedItem;
    private String selectedImagePath = null;

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
                    null,
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

        btnAddImage.addActionListener(e -> {
            addImage();
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
                btnAddImage.doClick();
            }
        });

        return ContentPane;
    }

    /**
     * Displays the item edit form as a modal dialog.
     *
     * @param parentFrame    The parent frame for modal behavior.
     * @param currentLocation The screen location to center the form.
     * @param itemToModify   The item to be modified.
     * @param onCloseCallback A callback executed when the form is closed.
     */
    public void showItemEditForm(Frame parentFrame, Point currentLocation, Item itemToModify, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);

        frame.setTitle("Bullseye Inventory Management System - Modify Item"); // Create the frame

        if (itemToModify != null) {
            selectedItem = itemToModify;
        }
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Set close operation
        frame.setSize(600, 350);                   // Set frame size
        if(currentLocation != null) {
            frame.setLocation(currentLocation);
        }
        frame.setLocationRelativeTo(null);         // Center the frame

        // Delay setup methods until the dialog is visible
        SwingUtilities.invokeLater(() -> {
            System.out.println(itemToModify);
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
        Image scaledImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        logoLabel.setIcon(resizedIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setText("");
    }

    /**
     * Populates the form fields with existing item data.
     * If the item has an image, updates the button text accordingly.
     */
    private void setupFields() {
        if (selectedItem != null) {
            // Populate the notes field
            String existingNotes = selectedItem.getNotes();
            txtNotes.setText(existingNotes != null ? existingNotes : "");

            // Limit notes input to 255 characters
            AbstractDocument document = (AbstractDocument) txtNotes.getDocument();
            document.setDocumentFilter(new CharacterLimitFilter(255));

            // Display existing image location (if available)
            if (selectedItem.getImageLocation() != null) {
                btnAddImage.setText("Change Image");
                lblImagePath.setText("Current Image on server: " + selectedItem.getImageLocation());
            } else {
                btnAddImage.setText("Add Image");
            }
        }
    }

    // =================== EVENT HANDLING SECTION ===================

    /**
     * Handles the save button event.
     * Validates the input and sends an update request to the backend.
     */
    private void saveEvent() {
        // Validate fields
        String updatedNotes = txtNotes.getText().trim();
        if (updatedNotes.length() > 255) {
            JOptionPane.showMessageDialog(frame, "Notes cannot exceed 255 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Send the PUT request
        boolean success = ItemUploader.updateItem(selectedItem.getId(), updatedNotes, selectedImagePath);

        if (success) {
            JOptionPane.showMessageDialog(frame, "Item updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose(); // Close the form
        } else {
            JOptionPane.showMessageDialog(frame, "Failed to update item.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the "Add Image" button click event.
     * Opens a file chooser to select an image file.
     */
    private void addImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image");

        // Restrict file selection to images only
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = fileChooser.getSelectedFile().getAbsolutePath();
            //JOptionPane.showMessageDialog(frame, "Selected image: " + selectedImagePath, "Image Selected", JOptionPane.INFORMATION_MESSAGE);
            lblImagePath.setText("Current Image on server: " + selectedImagePath);
        }
    }

    // =================== INTERNAL CLASSES ===================

    public class CharacterLimitFilter extends DocumentFilter {
        private final int limit;

        public CharacterLimitFilter(int limit) {
            this.limit = limit;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if ((fb.getDocument().getLength() + string.length()) <= limit) {
                super.insertString(fb, offset, string, attr);
            } else {
                Toolkit.getDefaultToolkit().beep(); // Optional: play a beep sound on excess input
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if ((fb.getDocument().getLength() - length + text.length()) <= limit) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                Toolkit.getDefaultToolkit().beep(); // Optional: play a beep sound on excess input
            }
        }
    }
}
