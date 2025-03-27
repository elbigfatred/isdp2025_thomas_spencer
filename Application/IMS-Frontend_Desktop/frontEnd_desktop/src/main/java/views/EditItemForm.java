package views;

import models.Category;
import models.Item;
import models.Supplier;
import utils.HelpBlurbs;
import utils.ItemRequests;
import utils.SessionManager;
import utils.SupplierUtil;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Objects;

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
    private JLabel lblSKUlabel;
    private JTextField txtDesc;
    private JLabel lblDesc;
    private JLabel lblItemImage;
    private JButton btnHelp;
    private JTextField txtName;
    private JCheckBox chkActive;
    private JSpinner spnCaseSize;
    private JSpinner spnWeight;
    private JComboBox cmbCategory;
    private JComboBox cmbSupplier;
    private JSpinner spnCostPrice;
    private JSpinner spnRetailPrice;
    private JLabel lblCostPrice;
    private JLabel lblRetailPrice;

    // =================== FRAME VARIABLES ===================

    JDialog frame;
    private Item selectedItem;
    private String selectedImagePath = null;
    List<Supplier> allSuppliers = SupplierUtil.fetchAllSuppliers(true);
    List<Category> allCategories = ItemRequests.fetchCategories();

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
                btnSave.doClick();
            }
        });

        btnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.EDIT_ITEM_HELP,"Edit Item Help",JOptionPane.INFORMATION_MESSAGE);
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

        if (itemToModify != null) {
            frame.setTitle("Bullseye Inventory Management System - Modify Item"); // Create the frame
            lblCostPrice.setVisible(false);
            lblRetailPrice.setVisible(false);
            spnCostPrice.setVisible(false);
            spnRetailPrice.setVisible(false);
            frame.setSize(600, 750);                   // Set frame size
        }
        else{
            frame.setTitle("Bullseye Inventory Management System - Create Item");
            frame.setSize(600, 890);                   // Set frame size
        }

        if (itemToModify != null) {
            selectedItem = itemToModify;
        }
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Set close operation
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
    private void setupFields() {
        spnWeight.setModel(new SpinnerNumberModel(1, 0.01, 9999, 0.01));
        spnCaseSize.setModel(new SpinnerNumberModel(1, 1, 9999, 1));
        if (selectedItem != null) {
            //clear item text
            lblItemImage.setText("");

            //Title
            lblSKUlabel.setText("Editing Details of SKU: " + selectedItem.getSku());

            txtName.setText(selectedItem.getName());
            chkActive.setSelected(selectedItem.getActive());
            spnWeight.setValue(selectedItem.getWeight());
            spnCaseSize.setValue(selectedItem.getCaseSize());

            for(Supplier supplier : allSuppliers) {
                cmbSupplier.addItem(supplier);
            }
            Supplier itemSupplier = selectedItem.getSupplier();
            for(int i = 0; i < cmbSupplier.getItemCount(); i++) {
                Supplier supplier = (Supplier) cmbSupplier.getItemAt(i);
                if (Objects.equals(supplier.getId(), itemSupplier.getId())) {
                    cmbSupplier.setSelectedIndex(i);
                    break;
                }
            }

            for(Category category : allCategories) {
                cmbCategory.addItem(category);
            }
            Category itemCategory = selectedItem.getCategory();
            for(int i = 0; i < cmbCategory.getItemCount(); i++) {
                Category category = (Category) cmbCategory.getItemAt(i);
                if(Objects.equals(category.getCategoryName(), itemCategory.getCategoryName())) {
                    cmbCategory.setSelectedIndex(i);
                    break;
                }
            }

            // Populate the notes field
            String existingNotes = selectedItem.getNotes();
            txtNotes.setText(existingNotes != null ? existingNotes : "");

            // Populate description field
            String exisitngDesc = selectedItem.getDescription();
            txtDesc.setText(exisitngDesc != null ? exisitngDesc : "");

            // Limit notes & desc input to 255 characters
            AbstractDocument notesDocuments = (AbstractDocument) txtNotes.getDocument();
            notesDocuments.setDocumentFilter(new CharacterLimitFilter(255));
            AbstractDocument descDocuments = (AbstractDocument) txtDesc.getDocument();
            descDocuments.setDocumentFilter(new CharacterLimitFilter(255));


            // Display existing image location (if available)
            if (selectedItem.getImageLocation() != null) {
                btnAddImage.setText("Change Existing Image");
                ImageIcon itemImage = ItemRequests.fetchItemImage(selectedItem.getId(), 150, 150);
                if (itemImage != null) {
                    lblItemImage.setIcon(itemImage);
                } else {
                    lblItemImage.setText("Image Not Found");
                }
            }
         else {
                btnAddImage.setText("Add Image");
            }
        }
        else{
            spnCostPrice.setModel(new SpinnerNumberModel(0.00, 0, 9999, 0.01));
            spnRetailPrice.setModel(new SpinnerNumberModel(0.00, 0, 9999, 0.01));
            lblItemImage.setText("");
            lblSKUlabel.setText("");
            allSuppliers.clear();
            allSuppliers = SupplierUtil.fetchAllSuppliers(false);
            chkActive.setSelected(true);
            for(Supplier supplier : allSuppliers) {
                cmbSupplier.addItem(supplier);
            }
            for(Category category : allCategories) {
                cmbCategory.addItem(category);
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
        String updatedDesc = txtDesc.getText().trim();
        if (updatedNotes.length() > 255) {
            JOptionPane.showMessageDialog(frame, "Notes cannot exceed 255 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (updatedDesc.length() > 255) {
            JOptionPane.showMessageDialog(frame, "Description cannot exceed 255 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Double castedWeight = Double.parseDouble(spnWeight.getValue().toString());
        // Create a new Item object and set its fields based on user input
        Item updatedItem = new Item();
        updatedItem.setName(txtName.getText().trim()); // Set name from text field
        updatedItem.setDescription(updatedDesc);   // Set description
        updatedItem.setNotes(updatedNotes);         // Set notes
        updatedItem.setWeight(BigDecimal.valueOf(castedWeight));  // Convert Integer to BigDecimal
        updatedItem.setCaseSize((int) spnCaseSize.getValue());  // Get case size from spinner
        updatedItem.setActive(chkActive.isSelected());  // Active checkbox status
        updatedItem.setCategory((Category) cmbCategory.getSelectedItem()); // Get selected category from combo box
        updatedItem.setSupplier((Supplier) cmbSupplier.getSelectedItem()); // Get selected supplier from combo box
        updatedItem.setImageLocation(selectedImagePath);  // Image location from the selected image path

        // Send the PUT request
        if(selectedItem != null) {
            updatedItem.setId(selectedItem.getId());
            boolean success = ItemRequests.updateItem(updatedItem);

            if (success) {
                JOptionPane.showMessageDialog(frame, "Item updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose(); // Close the form
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to update item.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else{
            updatedItem.setCostPrice(BigDecimal.valueOf((Double) spnCostPrice.getValue()));
            updatedItem.setRetailPrice(BigDecimal.valueOf((Double) spnRetailPrice.getValue()));
            updatedItem.setId(null);
            boolean success = ItemRequests.createItem(updatedItem);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Item created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose(); // Close the form
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to create item.", "Error", JOptionPane.ERROR_MESSAGE);
            }
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

            if (!fileChooser.getSelectedFile().getName().endsWith(".jpg") && !fileChooser.getSelectedFile().getName().endsWith(".png") && !fileChooser.getSelectedFile().getName().endsWith(".jpeg")) {
                JOptionPane.showMessageDialog(frame, "Please select a valid image file: \n PNG, JPG or JPEG", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selectedImagePath = fileChooser.getSelectedFile().getAbsolutePath();
            // Load and display the new image
            ImageIcon newImage = new ImageIcon(selectedImagePath);
            Image scaledImage = newImage.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            lblItemImage.setIcon(new ImageIcon(scaledImage));
            lblItemImage.setText("");
            btnAddImage.setText("Change Image");
            lblImagePath.setText("Image Ready to Save ✔\n");
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
