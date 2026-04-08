package com.dbsl.shop.ui;

import com.dbsl.shop.model.UserSession;
import com.dbsl.shop.service.AdminService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class AdminDashboardFrame extends JFrame {
    private final UserSession session;
    private final AdminService adminService = new AdminService();

    // Product fields
    private final JTable productTable   = new JTable();
    private final JTable salesTable     = new JTable();
    private final JTable usersTable     = new JTable();
    private final JTextField productIdField   = new JTextField();
    private final JTextField categoryIdField  = new JTextField();
    private final JTextField skuField         = new JTextField();
    private final JTextField productNameField = new JTextField();
    private final JTextField priceField       = new JTextField();
    private final JTextField stockField       = new JTextField();
    private final JTextField reorderField     = new JTextField();
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"AVAILABLE", "OUT_OF_STOCK", "DISCONTINUED"});
    private final JTextArea descriptionArea   = new JTextArea(4, 20);

    // Profile fields
    private final JTextField profUsernameField = new JTextField();
    private final JTextField profEmailField    = new JTextField();
    private final JTextField profPhoneField    = new JTextField();
    private final JTextField profFullNameField = new JTextField();
    private final JPasswordField profPasswordField = new JPasswordField();

    public AdminDashboardFrame(UserSession session) {
        this.session = session;
        setTitle("Admin Dashboard - " + session.getFullName());
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        header.add(Theme.titleLabel("Admin Dashboard"), BorderLayout.WEST);
        JLabel info = new JLabel("Signed in as " + session.getFullName());
        info.setFont(Theme.HEADER_FONT);
        info.setForeground(Theme.ACCENT_DARK);
        header.add(info, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BODY_FONT);
        tabs.add("Product Management", buildProductsPanel());
        tabs.add("Sales Report",       buildSalesPanel());
        tabs.add("Users",              buildUsersPanel());
        tabs.add("My Profile",         buildProfilePanel());
        add(tabs, BorderLayout.CENTER);

        refreshTables();
        loadProfileFields();
    }

    // ── Product Management (two subtabs) ──────────────────
    private JPanel buildProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(Theme.BODY_FONT);
        subTabs.add("Add / Edit Product", buildProductFormPanel());
        subTabs.add("View Products",      buildProductTablePanel());
        panel.add(subTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildProductFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        Theme.styleCard(form);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 12, 10, 12);
        c.fill   = GridBagConstraints.HORIZONTAL;

        Theme.styleField(productIdField);
        Theme.styleField(categoryIdField);
        Theme.styleField(skuField);
        Theme.styleField(productNameField);
        Theme.styleField(priceField);
        Theme.styleField(stockField);
        Theme.styleField(reorderField);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(Theme.BODY_FONT);

        String[] labels = {"Product ID (for update/delete)", "Category ID", "SKU",
                           "Product Name", "Price", "Stock", "Reorder Level", "Status", "Description"};
        Object[] fields = {productIdField, categoryIdField, skuField, productNameField,
                           priceField, stockField, reorderField, statusBox, new JScrollPane(descriptionArea)};

        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(Theme.BODY_FONT);
            lbl.setForeground(Theme.TEXT);
            form.add(lbl, c);
            c.gridx = 1; c.weightx = 0.7;
            form.add((java.awt.Component) fields[i], c);
        }

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        buttonRow.setBackground(Theme.BACKGROUND);
        JButton insertButton  = Theme.primaryButton("Insert");
        JButton updateButton  = Theme.secondaryButton("Update");
        JButton deleteButton  = Theme.secondaryButton("Delete");
        JButton refreshButton = Theme.secondaryButton("Refresh");
        insertButton .addActionListener(e -> insertProduct());
        updateButton .addActionListener(e -> updateProduct());
        deleteButton .addActionListener(e -> deleteProduct());
        refreshButton.addActionListener(e -> refreshTables());
        buttonRow.add(insertButton);
        buttonRow.add(updateButton);
        buttonRow.add(deleteButton);
        buttonRow.add(refreshButton);

        panel.add(form,      BorderLayout.CENTER);
        panel.add(buttonRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildProductTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        productTable.setRowHeight(36);
        productTable.setFont(Theme.BODY_FONT);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        return panel;
    }

    // ── Sales ─────────────────────────────────────────────
    private JPanel buildSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        salesTable.setRowHeight(36);
        salesTable.setFont(Theme.BODY_FONT);
        panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        return panel;
    }

    // ── Users ─────────────────────────────────────────────
    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        usersTable.setRowHeight(36);
        usersTable.setFont(Theme.BODY_FONT);
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        return panel;
    }

    // ── Profile ───────────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.BACKGROUND);

        JPanel card = new JPanel(new GridBagLayout());
        Theme.styleCard(card);
        card.setPreferredSize(new java.awt.Dimension(560, 460));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 14, 12, 14);
        c.fill   = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel heading = new JLabel("My Profile");
        heading.setFont(Theme.HEADER_FONT);
        heading.setForeground(Theme.ACCENT_DARK);
        card.add(heading, c);

        Theme.styleField(profUsernameField);
        Theme.styleField(profEmailField);
        Theme.styleField(profPhoneField);
        Theme.styleField(profFullNameField);
        profPasswordField.setFont(Theme.BODY_FONT);
        profPasswordField.setBackground(java.awt.Color.WHITE);
        profPasswordField.setForeground(Theme.TEXT);

        String[] labels = {"Full Name", "Username", "Email", "Phone", "New Password (leave blank to keep)"};
        JTextField[] fields = {profFullNameField, profUsernameField, profEmailField, profPhoneField, null};

        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i + 1; c.gridwidth = 1; c.weightx = 0.35;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(Theme.BODY_FONT);
            lbl.setForeground(Theme.TEXT);
            card.add(lbl, c);
            c.gridx = 1; c.weightx = 0.65;
            if (i == labels.length - 1) {
                card.add(profPasswordField, c);
            } else {
                card.add(fields[i], c);
            }
        }

        c.gridx = 0; c.gridy = labels.length + 1; c.gridwidth = 2; c.weightx = 1.0;
        JButton saveButton = Theme.primaryButton("Save Changes");
        saveButton.addActionListener(e -> saveAdminProfile());
        card.add(saveButton, c);

        wrapper.add(card);
        return wrapper;
    }

    private void loadProfileFields() {
        try {
            Map<String, String> p = adminService.loadAdminProfile(session.getUserId());
            profFullNameField.setText(p.getOrDefault("full_name", ""));
            profUsernameField.setText(p.getOrDefault("username",  ""));
            profEmailField   .setText(p.getOrDefault("email",     ""));
            profPhoneField   .setText(p.getOrDefault("phone",     ""));
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void saveAdminProfile() {
        String username = profUsernameField.getText().trim();
        String email    = profEmailField.getText().trim();
        String phone    = profPhoneField.getText().trim();
        String fullName = profFullNameField.getText().trim();
        String password = new String(profPasswordField.getPassword()).trim();

        if (username.isBlank() || email.isBlank() || fullName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Full name, username and email are required.");
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }
        try {
            adminService.updateAdminProfile(session.getUserId(), username, email,
                    phone.isBlank() ? null : phone, fullName,
                    password.isBlank() ? null : password);
            JOptionPane.showMessageDialog(this, "Profile updated successfully.");
            profPasswordField.setText("");
        } catch (SQLException e) {
            showError(e);
        }
    }

    // ── Product CRUD ──────────────────────────────────────
    private void insertProduct() {
        try {
            adminService.insertProduct(
                    Integer.parseInt(categoryIdField.getText().trim()),
                    skuField.getText().trim(),
                    productNameField.getText().trim(),
                    descriptionArea.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    Integer.parseInt(stockField.getText().trim()),
                    Integer.parseInt(reorderField.getText().trim()),
                    statusBox.getSelectedItem().toString()
            );
            refreshTables();
            JOptionPane.showMessageDialog(this, "Product inserted.");
        } catch (Exception e) { showError(e); }
    }

    private void updateProduct() {
        try {
            adminService.updateProduct(
                    Integer.parseInt(productIdField.getText().trim()),
                    Integer.parseInt(categoryIdField.getText().trim()),
                    skuField.getText().trim(),
                    productNameField.getText().trim(),
                    descriptionArea.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    Integer.parseInt(stockField.getText().trim()),
                    Integer.parseInt(reorderField.getText().trim()),
                    statusBox.getSelectedItem().toString()
            );
            refreshTables();
            JOptionPane.showMessageDialog(this, "Product updated.");
        } catch (Exception e) { showError(e); }
    }

    private void deleteProduct() {
        try {
            adminService.deleteProduct(Integer.parseInt(productIdField.getText().trim()));
            refreshTables();
            JOptionPane.showMessageDialog(this, "Product deleted.");
        } catch (Exception e) { showError(e); }
    }

    private void refreshTables() {
        try {
            productTable.setModel(adminService.loadProducts());
            salesTable  .setModel(adminService.loadSalesSummary());
            usersTable  .setModel(adminService.loadUsers());
        } catch (SQLException e) { showError(e); }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this,
                "Operation failed.\n" + e.getMessage(),
                "Admin Error", JOptionPane.ERROR_MESSAGE);
    }
}
