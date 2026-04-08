package com.dbsl.shop.ui;

import com.dbsl.shop.model.UserSession;
import com.dbsl.shop.service.AuthService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Online Shopping Portal");
        setSize(920, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 10, 28));
        header.add(Theme.titleLabel("Online Shopping Portal"), BorderLayout.WEST);

        JLabel subtitle = new JLabel("Secure Oracle DB mini project with separate customer and admin access");
        subtitle.setForeground(Theme.ACCENT_DARK);
        subtitle.setFont(Theme.BODY_FONT);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        JButton customerButton = Theme.primaryButton("Customer Login");
        JButton adminButton = Theme.secondaryButton("Admin Login");
        customerButton.addActionListener(event -> cardLayout.show(cardPanel, "CUSTOMER"));
        adminButton.addActionListener(event -> cardLayout.show(cardPanel, "ADMIN"));
        switchPanel.add(customerButton);
        switchPanel.add(adminButton);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(8, 28, 28, 28));
        center.add(switchPanel, BorderLayout.NORTH);
        cardPanel.add(buildLoginCard("CUSTOMER"), "CUSTOMER");
        cardPanel.add(buildLoginCard("ADMIN"), "ADMIN");
        center.add(cardPanel, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
        cardLayout.show(cardPanel, "CUSTOMER");
    }

    private JPanel buildLoginCard(String role) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        JPanel card = new JPanel(new GridBagLayout());
        Theme.styleCard(card);
        card.setPreferredSize(new java.awt.Dimension(420, 280));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;

        JLabel heading = new JLabel(role.equals("CUSTOMER") ? "Customer Page" : "Admin Page");
        heading.setFont(Theme.HEADER_FONT);
        heading.setForeground(Theme.ACCENT_DARK);
        card.add(heading, constraints);

        constraints.gridy++;
        constraints.gridwidth = 1;
        card.add(new JLabel("Username"), constraints);

        JTextField usernameField = new JTextField();
        Theme.styleField(usernameField);
        constraints.gridx = 1;
        card.add(usernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        card.add(new JLabel("Password"), constraints);

        JPasswordField passwordField = new JPasswordField();
        Theme.styleField(passwordField);
        constraints.gridx = 1;
        card.add(passwordField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        JLabel hint = new JLabel(role.equals("CUSTOMER")
                ? "Demo: cust01 / Cust@123"
                : "Demo: admin01 / Admin@123");
        hint.setForeground(Theme.ACCENT_DARK);
        card.add(hint, constraints);

        constraints.gridy++;
        JButton loginButton = Theme.primaryButton("Login as " + role);
        loginButton.addActionListener(event -> attemptLogin(role, usernameField.getText(), new String(passwordField.getPassword())));
        card.add(loginButton, constraints);

        wrapper.add(card);
        return wrapper;
    }

    private void attemptLogin(String role, String username, String password) {
        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.");
            return;
        }

        try {
            UserSession session = authService.login(username, password, role);
            dispose();
            if ("CUSTOMER".equals(session.getRole())) {
                new CustomerDashboardFrame(session).setVisible(true);
            } else {
                new AdminDashboardFrame(session).setVisible(true);
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Login failed.\n" + exception.getMessage(),
                    "Database Response",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
