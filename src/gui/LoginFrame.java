package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Bus Reservation System - Login");
        setSize(500, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Bus Reservation System");
        title.setBounds(90, 35, 350, 35);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(44, 62, 80));

        JLabel subtitle = new JLabel("Please enter your credentials to login");
        subtitle.setBounds(120, 70, 300, 20);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(new Color(127, 140, 141));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(70, 125, 100, 30);
        userLabel.setFont(new Font("Arial", Font.BOLD, 13));

        usernameField = new JTextField("admin");
        usernameField.setBounds(170, 125, 230, 32);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(70, 175, 100, 30);
        passLabel.setFont(new Font("Arial", Font.BOLD, 13));

        passwordField = new JPasswordField("1234");
        passwordField.setBounds(170, 175, 230, 32);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(170, 235, 130, 38);
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ActionListener loginAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (username.equals("admin") && password.equals("1234")) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login Successful! Welcome to the Dashboard.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    new Dashboard().setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Invalid Username or Password!\n(Default: admin / 1234)", "Authentication Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        loginButton.addActionListener(loginAction);
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        panel.add(title);
        panel.add(subtitle);
        panel.add(userLabel);
        panel.add(usernameField);
        panel.add(passLabel);
        panel.add(passwordField);
        panel.add(loginButton);

        add(panel);
    }
}