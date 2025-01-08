package com.frontend_desktop.swingapp;

import com.frontend_desktop.swingapp.views.DashboardForm;

import javax.swing.*;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> {
            JFrame frame = new JFrame("Dashboard");
            frame.setContentPane(new DashboardForm().getMainPanel()); // Set the main panel from DashboardForm
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600); // Set the size of the frame
            frame.setVisible(true); // Make the frame visible
        });
    }

}
