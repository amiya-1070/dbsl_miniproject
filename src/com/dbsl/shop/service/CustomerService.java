package com.dbsl.shop.service;

import com.dbsl.shop.db.OracleConnectionManager;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class CustomerService {
    public DefaultTableModel loadCatalog(String searchText) throws SQLException {
        String sql = """
                SELECT product_id, product_name, category_name, unit_price, stock_qty, status
                FROM vw_product_catalog
                WHERE LOWER(product_name) LIKE ?
                   OR LOWER(category_name) LIKE ?
                ORDER BY category_name, product_name
                """;

        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + searchText.toLowerCase().trim() + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                return buildTableModel(resultSet);
            }
        }
    }

    public DefaultTableModel loadCart(int customerId) throws SQLException {
        String sql = """
                SELECT ci.product_id, p.product_name, ci.quantity, ci.unit_price, ci.line_total
                FROM shopping_cart sc
                JOIN cart_item ci ON ci.cart_id = sc.cart_id
                JOIN product p ON p.product_id = ci.product_id
                WHERE sc.customer_id = ?
                ORDER BY p.product_name
                """;

        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return buildTableModel(resultSet);
            }
        }
    }

    public DefaultTableModel loadOrderHistory(int customerId) throws SQLException {
        String sql = """
                SELECT order_number, order_date, status, payment_status, total_amount
                FROM vw_customer_order_history
                WHERE customer_id = ?
                ORDER BY order_date DESC
                """;

        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return buildTableModel(resultSet);
            }
        }
    }

    public void addToCart(int customerId, int productId, int quantity) throws SQLException {
        try (Connection connection = OracleConnectionManager.getConnection();
             CallableStatement statement = connection.prepareCall("{call shop_portal_pkg.add_to_cart(?,?,?)}")) {
            statement.setInt(1, customerId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            statement.execute();
            connection.commit();
        }
    }

    public String placeOrder(int customerId, String paymentMethod, String address) throws SQLException {
        try (Connection connection = OracleConnectionManager.getConnection();
             CallableStatement statement = connection.prepareCall("{call shop_portal_pkg.place_order(?,?,?,?,?,?)}")) {
            connection.setAutoCommit(false);
            statement.setInt(1, customerId);
            statement.setString(2, paymentMethod);
            statement.setString(3, address);
            statement.registerOutParameter(4, Types.INTEGER);
            statement.registerOutParameter(5, Types.INTEGER);
            statement.registerOutParameter(6, Types.DOUBLE);
            statement.execute();
            connection.commit();
            return "Order placed. Order ID: " + statement.getInt(4)
                    + ", Invoice ID: " + statement.getInt(5)
                    + ", Total: " + statement.getDouble(6);
        }
    }

    private DefaultTableModel buildTableModel(ResultSet resultSet) throws SQLException {
        Vector<String> columnNames = new Vector<>();
        int columnCount = resultSet.getMetaData().getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(resultSet.getMetaData().getColumnLabel(column));
        }

        Vector<Vector<Object>> rows = new Vector<>();
        while (resultSet.next()) {
            Vector<Object> row = new Vector<>();
            for (int column = 1; column <= columnCount; column++) {
                row.add(resultSet.getObject(column));
            }
            rows.add(row);
        }
        return new DefaultTableModel(rows, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
