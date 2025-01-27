package views;

import models.Item;
import utils.ItemUploader;
import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class EditItemForm {
    private JPanel ContentPane;
    private JLabel lblEditItem;
    private JLabel logoLabel;
    private JTextArea txtNotes;
    private JLabel lblNotes;
    private JButton btnAddImage;
    private JButton btnSave;
    private JButton btnCancel;

    JDialog frame;
    private Item selectedItem;
    private String selectedImagePath = null;

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

        return ContentPane;
    }

    public void showItemEditForm(Frame parentFrame, Point currentLocation, Item itemToModify, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);

        frame.setTitle("Bullseye Inventory Management System - Modify Item"); // Create the frame

        if (itemToModify != null) {
            selectedItem = itemToModify;
        }
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Set close operation
        frame.setSize(700, 475);                   // Set frame size
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

    private void setupFields() {
        if (selectedItem != null) {
            // Populate the notes field
            String existingNotes = selectedItem.getNotes();
            txtNotes.setText(existingNotes != null ? existingNotes : "");

            // Display existing image location (if available)
            if (selectedItem.getImageLocation() != null) {
                btnAddImage.setText("Change Image");
            } else {
                btnAddImage.setText("Add Image");
            }
        }
    }

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

    private void addImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image");

        // Restrict file selection to images only
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = fileChooser.getSelectedFile().getAbsolutePath();
            JOptionPane.showMessageDialog(frame, "Selected image: " + selectedImagePath, "Image Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
