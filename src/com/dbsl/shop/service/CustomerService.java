package com.dbsl.shop.service;

import com.dbsl.shop.db.OracleConnectionManager;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class CustomerService {

    public DefaultTableModel loadCatalog(String searchText, String categoryName) throws SQLException {
        String sql = """
                SELECT product_id, product_name, category_name, unit_price, stock_qty, status
                FROM vw_product_catalog
                WHERE (LOWER(product_name) LIKE ? OR LOWER(category_name) LIKE ?)
                """;

        if (categoryName != null && !categoryName.equals("All Categories")) {
            sql += " AND LOWER(category_name) = ?";
        }
        sql += " ORDER BY category_name, product_name";

        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + searchText.toLowerCase().trim() + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            if (categoryName != null && !categoryName.equals("All Categories")) {
                statement.setString(3, categoryName.toLowerCase());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return buildTableModel(resultSet);
            }
        }
    }

    // Keep old method working so nothing else breaks
    public DefaultTableModel loadCatalog(String searchText) throws SQLException {
        return loadCatalog(searchText, null);
    }

    public List<String> loadCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        String sql = "SELECT category_name FROM category ORDER BY category_name";
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(resultSet.getString("category_name"));
            }
        }
        return categories;
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
    


	public java.util.Map<String, String> loadCustomerProfile(int userId) throws SQLException {
		String sql = """
		        SELECT u.username, u.email, u.phone, u.full_name,
		               cp.default_address, cp.city, cp.pincode, cp.loyalty_points
		        FROM app_user u
		        LEFT JOIN customer_profile cp ON cp.customer_id = u.user_id
		        WHERE u.user_id = ?
		        """;
		try (Connection connection = OracleConnectionManager.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {
		    statement.setInt(1, userId);
		    try (ResultSet resultSet = statement.executeQuery()) {
		        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
		        if (resultSet.next()) {
		            map.put("username",        nullSafe(resultSet.getString("username")));
		            map.put("email",           nullSafe(resultSet.getString("email")));
		            map.put("phone",           nullSafe(resultSet.getString("phone")));
		            map.put("full_name",       nullSafe(resultSet.getString("full_name")));
		            map.put("default_address", nullSafe(resultSet.getString("default_address")));
		            map.put("city",            nullSafe(resultSet.getString("city")));
		            map.put("pincode",         nullSafe(resultSet.getString("pincode")));
		            map.put("loyalty_points",  nullSafe(resultSet.getString("loyalty_points")));
		        }
		        return map;
		    }
		}
	}

	public void updateCustomerProfile(int userId, String username, String email, String phone,
		                               String fullName, String newPassword,
		                               String address, String city, String pincode) throws SQLException {
		try (Connection connection = OracleConnectionManager.getConnection()) {
		    // Update app_user
		    if (newPassword != null && !newPassword.isBlank()) {
		        String sql = """
		                UPDATE app_user SET username=?, email=?, phone=?, full_name=?,
		                password_hash=STANDARD_HASH(?, 'SHA256') WHERE user_id=?
		                """;
		        try (PreparedStatement st = connection.prepareStatement(sql)) {
		            st.setString(1, username);
		            st.setString(2, email);
		            st.setString(3, phone);
		            st.setString(4, fullName);
		            st.setString(5, newPassword);
		            st.setInt(6, userId);
		            st.executeUpdate();
		        }
		    } else {
		        String sql = "UPDATE app_user SET username=?, email=?, phone=?, full_name=? WHERE user_id=?";
		        try (PreparedStatement st = connection.prepareStatement(sql)) {
		            st.setString(1, username);
		            st.setString(2, email);
		            st.setString(3, phone);
		            st.setString(4, fullName);
		            st.setInt(5, userId);
		            st.executeUpdate();
		        }
		    }
		    // Update customer_profile
		    String cpSql = "UPDATE customer_profile SET default_address=?, city=?, pincode=? WHERE customer_id=?";
		    try (PreparedStatement st = connection.prepareStatement(cpSql)) {
		        st.setString(1, address);
		        st.setString(2, city);
		        st.setString(3, pincode);
		        st.setInt(4, userId);
		        st.executeUpdate();
		    }
		    connection.commit();
		}
	}

	private String nullSafe(String value) {
		return value == null ? "" : value;
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
