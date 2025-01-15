package org.example;


import views.LoginForm;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> new LoginForm().showLoginForm(null));
    }

}
