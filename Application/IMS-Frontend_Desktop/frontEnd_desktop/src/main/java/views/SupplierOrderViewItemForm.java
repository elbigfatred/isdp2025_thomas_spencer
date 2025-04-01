package views;

import models.Item;
import utils.HelpBlurbs;
import utils.ItemRequests;
import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.net.URL;

public class SupplierOrderViewItemForm {
    private JPanel ContentPane;
    private JButton btnExit;
    private JLabel lblLogo;
    private JLabel lblWelcome;
    private JLabel lblLocation;
    private JLabel lblName;
    private JLabel lblDescription;
    private JLabel lblNotes;
    private JLabel lblCostPrice;
    private JLabel lblRetailPrice;
    private JLabel lblCategory;
    private JLabel lblSupplier;
    private JLabel lblCaseSize;
    private JLabel lblWeight;
    private JLabel lblItemImage;
    private JLabel lblSkuDetails;
    private JButton btnHelp;

    JDialog frame;
    Item selectedItem;

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
        btnExit.addActionListener(e -> {
            frame.dispose();
        });


        btnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, HelpBlurbs.VIEW_ITEM_DETAILS_SUPPLIER_ORDER,"View Item Help",JOptionPane.INFORMATION_MESSAGE);
        });

        return ContentPane;
    }

    public void showItemEditForm(JDialog parentFrame, Point currentLocation, Item itemToModify, Runnable onCloseCallback) {
        frame = new JDialog(parentFrame, true);

        frame.setTitle("Bullseye Inventory Management System - View Item Details"); // Create the frame
        selectedItem = itemToModify;

        if (selectedItem.getImageLocation() != null){
            frame.setSize(600, 610);                   // Set frame size
        }
        else frame.setSize(600, 500);

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

    private void setupFields() {
        // setup labels
        lblName.setText("Name: " + selectedItem.getName());

        // Truncate description to 60 characters if it's too long
        String description = selectedItem.getDescription();
        if (description != null && description.length() > 60) {
            description = description.substring(0, 60) + "...";
        }
        lblDescription.setText("Description: " + description);

        // Notes
        lblNotes.setText("Notes: " + (selectedItem.getNotes() == null ? "None" : selectedItem.getNotes()));

        // Handle BigDecimal for costPrice and retailPrice
        BigDecimal costPrice = selectedItem.getCostPrice();
        BigDecimal retailPrice = selectedItem.getRetailPrice();

        if (costPrice != null) {
            lblCostPrice.setText("Cost Price ($): " + costPrice.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        } else {
            lblCostPrice.setText("Cost Price ($): N/A");
        }

        if (retailPrice != null) {
            lblRetailPrice.setText("Retail Price ($): " + retailPrice.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        } else {
            lblRetailPrice.setText("Retail Price ($): N/A");
        }

        lblCategory.setText("Category: " + selectedItem.getCategory().getCategoryName());
        lblSupplier.setText("Supplier: " + selectedItem.getSupplier().getName());
        lblCaseSize.setText("Case Size: " + selectedItem.getCaseSize());
        lblWeight.setText("Weight (kg): " + selectedItem.getWeight());
        lblSkuDetails.setText("View details of SKU: " + selectedItem.getSku());

        // Display existing image location (if available)
        if (selectedItem.getImageLocation() != null) {
            ImageIcon itemImage = ItemRequests.fetchItemImage(selectedItem.getId(), 150, 150);
            if (itemImage != null) {
                lblItemImage.setIcon(itemImage);
                lblItemImage.setText("");
            } else {
                lblItemImage.setText("No available image");
            }
        } else {
            lblItemImage.setText("No available image");
        }
    }

    private void SetupBullseyeLogo() {
        String logoPath = "/bullseye.jpg"; // Classpath-relative path
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        lblLogo.setIcon(resizedIcon);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setText("");
    }


}
