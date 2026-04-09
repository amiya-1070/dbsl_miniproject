//AdminService.java

package com.dbsl.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.dbsl.shop.db.OracleConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class AdminService {
    public DefaultTableModel loadProducts() throws SQLException {
        String sql = """
                SELECT product_id, category_name, sku, product_name, unit_price, stock_qty, reorder_level, status
                FROM vw_admin_product_editor
                ORDER BY product_id
                """;
        return runSelect(sql);
    }

    public void insertProduct(int categoryId, String sku, String name, String description,
                              double price, int stock, int reorderLevel, String status) throws SQLException {
        String sql = """
                INSERT INTO vw_admin_product_editor
                (category_id, sku, product_name, description, unit_price, stock_qty, reorder_level, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            statement.setInt(1, categoryId);
            statement.setString(2, sku);
            statement.setString(3, name);
            statement.setString(4, description);
            statement.setDouble(5, price);
            statement.setInt(6, stock);
            statement.setInt(7, reorderLevel);
            statement.setString(8, status);
            statement.executeUpdate();
            connection.commit();
        }
    }

    public void updateProduct(int productId, int categoryId, String sku, String name, String description,
                              double price, int stock, int reorderLevel, String status) throws SQLException {
        String sql = """
                UPDATE vw_admin_product_editor
                SET category_id = ?, sku = ?, product_name = ?, description = ?, unit_price = ?,
                    stock_qty = ?, reorder_level = ?, status = ?
                WHERE product_id = ?
                """;
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            statement.setInt(1, categoryId);
            statement.setString(2, sku);
            statement.setString(3, name);
            statement.setString(4, description);
            statement.setDouble(5, price);
            statement.setInt(6, stock);
            statement.setInt(7, reorderLevel);
            statement.setString(8, status);
            statement.setInt(9, productId);
            statement.executeUpdate();
            connection.commit();
        }
    }

    public void deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM product WHERE product_id = ?";
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            statement.setInt(1, productId);
            statement.executeUpdate();
            connection.commit();
        }
    }

    public DefaultTableModel loadUsers() throws SQLException {
        String sql = """
                SELECT user_id, username, role, full_name, email, status, created_at
                FROM app_user
                ORDER BY role, user_id
                """;
        return runSelect(sql);
    }

    public DefaultTableModel loadSalesSummary() throws SQLException {
        String sql = """
                SELECT order_day, category_name, orders_count, units_sold, revenue
                FROM vw_sales_summary
                ORDER BY order_day DESC, revenue DESC
                """;
        return runSelect(sql);
    }
    

	public java.util.Map<String, String> loadAdminProfile(int userId) throws SQLException {
		String sql = "SELECT username, email, phone, full_name FROM app_user WHERE user_id = ?";
		try (Connection connection = OracleConnectionManager.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {
		    statement.setInt(1, userId);
		    try (ResultSet resultSet = statement.executeQuery()) {
		        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
		        if (resultSet.next()) {
		            map.put("username",  nullSafe(resultSet.getString("username")));
		            map.put("email",     nullSafe(resultSet.getString("email")));
		            map.put("phone",     nullSafe(resultSet.getString("phone")));
		            map.put("full_name", nullSafe(resultSet.getString("full_name")));
		        }
		        return map;
		    }
		}
	}

	public void updateAdminProfile(int userId, String username, String email,
		                            String phone, String fullName, String newPassword) throws SQLException {
		try (Connection connection = OracleConnectionManager.getConnection()) {
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
		    connection.commit();
		}
	}


	public DefaultTableModel loadSalesSummaryFiltered(String fromDate, String toDate, String categoryName) throws SQLException {
		String sql = "SELECT order_day, category_name, orders_count, units_sold, revenue FROM vw_sales_summary WHERE 1=1";
		List<String> params = new ArrayList<>();
		if (fromDate != null && !fromDate.isBlank()) {
		    sql += " AND order_day >= TO_DATE(?, 'YYYY-MM-DD')";
		    params.add(fromDate);
		}
		if (toDate != null && !toDate.isBlank()) {
		    sql += " AND order_day <= TO_DATE(?, 'YYYY-MM-DD')";
		    params.add(toDate);
		}
		if (categoryName != null && !categoryName.equals("All Categories")) {
		    sql += " AND LOWER(category_name) = ?";
		    params.add(categoryName.toLowerCase());
		}
		sql += " ORDER BY order_day DESC, revenue DESC";

		try (Connection connection = OracleConnectionManager.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {
		    for (int i = 0; i < params.size(); i++) {
		        statement.setString(i + 1, params.get(i));
		    }
		    try (ResultSet resultSet = statement.executeQuery()) {
		        return buildTableModel(resultSet);
		    }
		}
	}

	public List<String> loadCategories() throws SQLException {
		List<String> categories = new ArrayList<>();
		categories.add("All Categories");
		String sql = "SELECT category_name FROM category ORDER BY display_order, category_name";
		try (Connection connection = OracleConnectionManager.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql);
		     ResultSet resultSet = statement.executeQuery()) {
		    while (resultSet.next()) {
		        categories.add(resultSet.getString("category_name"));
		    }
		}
		return categories;
	}

	private String nullSafe(String value) {
		return value == null ? "" : value;
	}

		private DefaultTableModel runSelect(String sql) throws SQLException {
		    try (Connection connection = OracleConnectionManager.getConnection();
		         PreparedStatement statement = connection.prepareStatement(sql);
		         ResultSet resultSet = statement.executeQuery()) {
		        return buildTableModel(resultSet);
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
