package views;


import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.URL;

public class DashboardForm {
    private JPanel ContentPane;
    private JTabbedPane DashboardTabPane;
    private JPanel OrdersTab;
    private JPanel InventoryTab;
    private JPanel LossReturnTab;
    private JPanel ReportsTab;
    private JPanel AdminTab;
    private JButton BtnRefresh;
    private JButton BtnLogout;
    private JLabel SPACER;
    private JLabel SPACER1;
    private JLabel SPACER2;
    private JLabel SPACER3;
    private JButton someButtonButton;
    private JLabel LblWelcome;
    private JLabel LblLocation;
    private JLabel logoLabel;

    private JFrame frame;
    private Timer idleTimer;
    private Timer countdownTimer;
    private boolean sessionActive = true;

    // Method to set up and display the dashboard frame
    public void showDashboard() {
        frame = new JFrame("Bullseye IMS"); // Create the frame
        frame.setContentPane(getMainPanel());       // Set the content pane
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set close operation
        frame.setSize(800, 600);                   // Set frame size
        frame.setLocationRelativeTo(null);         // Center the frame
        frame.setVisible(true);                    // Make it visible

        setupIdleTimer(); //initialize timeout logic
        setupCountdownTimer();

        String logoPath = "/bullseye.jpg"; // Classpath-relative path
        URL logoURL = getClass().getResource(logoPath);
        ImageIcon icon = new ImageIcon(logoURL); // Load the image
        Image scaledImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Resize
        ImageIcon resizedIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the resized image


        logoLabel.setIcon(resizedIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setText("");
    }

    public JPanel getMainPanel() {
        // setup initial components
        LblWelcome.setText("Welcome, cpatstone");
        LblLocation.setText("Location: Warehouse");

        // action listeners for buttons
        BtnRefresh.addActionListener(e -> {
            idleTimer.stop();
            countdownTimer.stop();
            JOptionPane.showMessageDialog(
                    null, "Refreshing data...", "Info", JOptionPane.INFORMATION_MESSAGE);
            SessionManager.getInfo();
            idleTimer.restart();
            countdownTimer.restart();
        });

        BtnLogout.addActionListener(e -> {
            stopIdleTimer();
            stopCountdownTimer();
            SwingUtilities.invokeLater(()-> new LoginForm().showLoginForm());
            frame.dispose();
        });

        return ContentPane;
    }

    private void setupIdleTimer() {
        // Set up a timer to check for inactivity
        idleTimer = new Timer((int) SessionManager.getMaxSessionTime(), e -> {
            if (sessionActive) {
                stopIdleTimer();
                stopCountdownTimer();
                sessionActive = false;
                JOptionPane.showMessageDialog(frame, "Session timed out due to inactivity. Logging out.");
                SessionManager.getInstance().resetSession();
                SwingUtilities.invokeLater(() -> new LoginForm().showLoginForm());
                frame.dispose();
            }
        });

        idleTimer.start(); // Start the timer

        // Reset the timer when the mouse or keyboard is used
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            int eventId = event.getID();
            if (eventId == MouseEvent.MOUSE_MOVED ||
                    eventId == MouseEvent.MOUSE_CLICKED ||
                    eventId == MouseEvent.MOUSE_PRESSED ||
                    eventId == MouseEvent.MOUSE_RELEASED ||
                    eventId == MouseEvent.MOUSE_ENTERED ||
                    eventId == MouseEvent.MOUSE_EXITED ||
                    eventId == MouseEvent.MOUSE_WHEEL) {
                resetIdleTimer();
            } else if (eventId == KeyEvent.KEY_PRESSED ||
                    eventId == KeyEvent.KEY_RELEASED ||
                    eventId == KeyEvent.KEY_TYPED) {
                resetIdleTimer();
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK |
                AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    private void resetIdleTimer() {
        idleTimer.restart(); // Reset the timer whenever there's activity
        SessionManager.getInstance().updateLastActivityTime(); // Update activity timestamp
    }

    private void stopIdleTimer() {
        if (idleTimer != null && idleTimer.isRunning()) {
            idleTimer.stop(); // Stop the timer
        }
    }

    private void setupCountdownTimer() {
        // Update the remaining idle time every second
        countdownTimer = new Timer(1000, e -> {
            long remainingTime = SessionManager.getMaxSessionTime() -
                    (System.currentTimeMillis() - SessionManager.getInstance().getLastActivityTime());
            if (remainingTime < 0) remainingTime = 0; // Avoid negative values

            // Convert milliseconds to seconds
            long seconds = remainingTime / 1000;
            LblWelcome.setText("Welcome, cpatstone | Idle time left: " + seconds + " seconds");
        });

        countdownTimer.start(); // Start the countdown timer
    }

    private void stopCountdownTimer() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop(); // Stop the countdown timer
        }
    }
}
