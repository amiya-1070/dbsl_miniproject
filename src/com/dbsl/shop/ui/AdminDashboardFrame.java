package com.dbsl.shop.ui;

import com.dbsl.shop.model.UserSession;
import com.dbsl.shop.service.AdminService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class AdminDashboardFrame extends JFrame {
    private final UserSession session;
    private final AdminService adminService = new AdminService();
    private final JTable productTable = new JTable();
    private final JTable salesTable = new JTable();
    private final JTable usersTable = new JTable();
    private final JTextField productIdField = new JTextField();
    private final JTextField categoryIdField = new JTextField();
    private final JTextField skuField = new JTextField();
    private final JTextField productNameField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField stockField = new JTextField();
    private final JTextField reorderField = new JTextField();
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"AVAILABLE", "OUT_OF_STOCK", "DISCONTINUED"});
    private final JTextArea descriptionArea = new JTextArea(4, 20);

    public AdminDashboardFrame(UserSession session) {
        this.session = session;
        setTitle("Admin Dashboard - " + session.getFullName());
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        header.add(Theme.titleLabel("Admin Dashboard"), BorderLayout.WEST);
        JLabel info = new JLabel("Signed in as " + session.getFullName());
        info.setFont(Theme.HEADER_FONT);
        info.setForeground(Theme.ACCENT_DARK);
        header.add(info, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Product Management", buildProductsPanel());
        tabs.add("Sales Report", buildSalesPanel());
        tabs.add("Users", buildUsersPanel());
        add(tabs, BorderLayout.CENTER);

        refreshTables();
    }

    private JPanel buildProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(9, 2, 10, 10));
        Theme.styleCard(form);
        Theme.styleField(productIdField);
        Theme.styleField(categoryIdField);
        Theme.styleField(skuField);
        Theme.styleField(productNameField);
        Theme.styleField(priceField);
        Theme.styleField(stockField);
        Theme.styleField(reorderField);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        form.add(new JLabel("Product ID (for update/delete)"));
        form.add(productIdField);
        form.add(new JLabel("Category ID"));
        form.add(categoryIdField);
        form.add(new JLabel("SKU"));
        form.add(skuField);
        form.add(new JLabel("Product Name"));
        form.add(productNameField);
        form.add(new JLabel("Price"));
        form.add(priceField);
        form.add(new JLabel("Stock"));
        form.add(stockField);
        form.add(new JLabel("Reorder Level"));
        form.add(reorderField);
        form.add(new JLabel("Status"));
        form.add(statusBox);
        form.add(new JLabel("Description"));
        form.add(new JScrollPane(descriptionArea));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JButton insertButton = Theme.primaryButton("Insert");
        JButton updateButton = Theme.secondaryButton("Update");
        JButton deleteButton = Theme.secondaryButton("Delete");
        JButton refreshButton = Theme.secondaryButton("Refresh");

        insertButton.addActionListener(event -> insertProduct());
        updateButton.addActionListener(event -> updateProduct());
        deleteButton.addActionListener(event -> deleteProduct());
        refreshButton.addActionListener(event -> refreshTables());

        buttonRow.add(insertButton);
        buttonRow.add(updateButton);
        buttonRow.add(deleteButton);
        buttonRow.add(refreshButton);

        panel.add(form, BorderLayout.NORTH);
        panel.add(buttonRow, BorderLayout.CENTER);
        panel.add(new JScrollPane(productTable), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));
        panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        return panel;
    }

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
        } catch (Exception exception) {
            showError(exception);
        }
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
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void deleteProduct() {
        try {
            adminService.deleteProduct(Integer.parseInt(productIdField.getText().trim()));
            refreshTables();
            JOptionPane.showMessageDialog(this, "Product deleted.");
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void refreshTables() {
        try {
            productTable.setModel(adminService.loadProducts());
            salesTable.setModel(adminService.loadSalesSummary());
            usersTable.setModel(adminService.loadUsers());
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void showError(Exception exception) {
        JOptionPane.showMessageDialog(this,
                "Operation failed.\n" + exception.getMessage(),
                "Admin Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
