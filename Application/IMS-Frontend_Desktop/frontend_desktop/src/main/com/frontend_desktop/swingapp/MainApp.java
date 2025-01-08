package main.com.frontend_desktop.swingapp;

import main.java.com.frontend_desktop.swingapp.views.LoginForm;

import javax.swing.*;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> new LoginForm().showLoginForm());
    }

}
