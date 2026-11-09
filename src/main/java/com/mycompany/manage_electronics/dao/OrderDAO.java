package com.mycompany.manage_electronics.dao;

import com.mycompany.manage_electronics.model.Order;
import com.mycompany.manage_electronics.model.OrderItem;
import com.mycompany.manage_electronics.util.DBConnection;
import com.mycompany.manage_electronics.util.DaoResult;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    
    // Tạo đơn hàng mới (transaction). Trả về order_id > 0 nếu thành công, hoặc mã DaoResult âm/0 nếu lỗi.
    public int createOrder(int userId, Integer voucherId, String receiverName, String receiverPhone, 
                          String shippingAddress, BigDecimal totalAmount, List<OrderItem> items) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Tạo order
            String orderSql = "INSERT INTO Orders (user_id, voucher_id, receiver_name, receiver_phone, shipping_address, total_amount) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement orderPs = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderPs.setInt(1, userId);
            if (voucherId != null) {
                orderPs.setInt(2, voucherId);
            } else {
                orderPs.setNull(2, Types.INTEGER);
            }
            orderPs.setString(3, receiverName);
            orderPs.setString(4, receiverPhone);
            orderPs.setString(5, shippingAddress);
            orderPs.setBigDecimal(6, totalAmount);
            orderPs.executeUpdate();
            
            ResultSet rs = orderPs.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            
            // 2. Thêm order items và trừ tồn kho
            String itemSql = "INSERT INTO OrderItems (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            String updateStockSql = "UPDATE Products SET stock = stock - ? WHERE product_id = ? AND stock >= ?";
            
            PreparedStatement itemPs = conn.prepareStatement(itemSql);
            PreparedStatement stockPs = conn.prepareStatement(updateStockSql);
            
            for (OrderItem item : items) {
                itemPs.setInt(1, orderId);
                itemPs.setInt(2, item.getProductId());
                itemPs.setInt(3, item.getQuantity());
                itemPs.setBigDecimal(4, item.getPrice());
                itemPs.executeUpdate();
                
                stockPs.setInt(1, item.getQuantity());
                stockPs.setInt(2, item.getProductId());
                stockPs.setInt(3, item.getQuantity());
                if (stockPs.executeUpdate() == 0) {
                    // Không đủ tồn kho: hủy toàn bộ giao dịch
                    conn.rollback();
                    return DaoResult.OUT_OF_STOCK;
                }
            }
            
            // 3. Giảm usage_limit của voucher (nếu có)
            if (voucherId != null) {
                String voucherSql = "UPDATE Vouchers SET usage_limit = usage_limit - 1 WHERE voucher_id = ? AND usage_limit > 0 " +
                        "AND is_active = 1 AND (expiration_date IS NULL OR expiration_date > GETDATE())";
                PreparedStatement voucherPs = conn.prepareStatement(voucherSql);
                voucherPs.setInt(1, voucherId);
                if (voucherPs.executeUpdate() == 0) {
                    conn.rollback();
                    return DaoResult.BAD_VOUCHER;
                }
            }
            
            conn.commit();
            return orderId;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // Lấy đơn hàng theo user
    public List<Order> getOrdersByUser(int userId) {
        String sql = "SELECT o.*, v.code as voucher_code FROM Orders o " +
                    "LEFT JOIN Vouchers v ON o.voucher_id = v.voucher_id " +
                    "WHERE o.user_id = ? ORDER BY o.created_at DESC";
        
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }
    
    // Lấy tất cả đơn hàng (admin)
    public List<Order> getAllOrders() {
        String sql = "SELECT o.*, v.code as voucher_code, u.username FROM Orders o " +
                    "LEFT JOIN Vouchers v ON o.voucher_id = v.voucher_id " +
                    "LEFT JOIN Users u ON o.user_id = u.user_id " +
                    "ORDER BY o.created_at DESC";
        
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Order order = mapOrder(rs);
                order.setUsername(rs.getString("username"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }
    
    // Lấy chi tiết đơn hàng
    public Order getOrderById(int orderId) {
        String sql = "SELECT o.*, v.code as voucher_code FROM Orders o " +
                    "LEFT JOIN Vouchers v ON o.voucher_id = v.voucher_id " +
                    "WHERE o.order_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapOrder(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Lấy order items
    public List<OrderItem> getOrderItems(int orderId) {
        String sql = "SELECT oi.*, p.name as product_name, p.image_url FROM OrderItems oi " +
                    "JOIN Products p ON oi.product_id = p.product_id " +
                    "WHERE oi.order_id = ?";
        
        List<OrderItem> items = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setOrderItemId(rs.getInt("order_item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setReviewed(rs.getBoolean("is_reviewed"));
                item.setProductName(rs.getString("product_name"));
                item.setProductImageUrl(rs.getString("image_url"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return items;
    }
    
    // Cập nhật trạng thái đơn hàng (admin). Chỉ cho phép: 0->1, 0->3, 1->2, 1->3.
    // Khi chuyển sang "Đã hủy" thì hoàn lại tồn kho và lượt dùng voucher.
    public boolean updateOrderStatus(int orderId, int newStatus) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            if (!changeStatus(conn, orderId, newStatus)) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /** Đổi trạng thái trong một transaction có sẵn; trả về false nếu chuyển trạng thái không hợp lệ. */
    private boolean changeStatus(Connection conn, int orderId, int newStatus) throws SQLException {
        int current;
        Integer voucherId = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT status, voucher_id FROM Orders WITH (UPDLOCK) WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            current = rs.getInt("status");
            int v = rs.getInt("voucher_id");
            if (!rs.wasNull()) voucherId = v;
        }
        boolean allowed = (current == 0 && (newStatus == 1 || newStatus == 3))
                       || (current == 1 && (newStatus == 2 || newStatus == 3));
        if (!allowed) return false;

        try (PreparedStatement ps = conn.prepareStatement("UPDATE Orders SET status = ? WHERE order_id = ?")) {
            ps.setInt(1, newStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
        if (newStatus == 3) {
            restock(conn, orderId, voucherId);
        }
        return true;
    }

    private void restock(Connection conn, int orderId, Integer voucherId) throws SQLException {
        try (PreparedStatement items = conn.prepareStatement(
                "SELECT product_id, quantity FROM OrderItems WHERE order_id = ?");
             PreparedStatement restock = conn.prepareStatement(
                "UPDATE Products SET stock = stock + ? WHERE product_id = ?")) {
            items.setInt(1, orderId);
            ResultSet rs = items.executeQuery();
            while (rs.next()) {
                restock.setInt(1, rs.getInt("quantity"));
                restock.setInt(2, rs.getInt("product_id"));
                restock.executeUpdate();
            }
        }
        if (voucherId != null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Vouchers SET usage_limit = usage_limit + 1 WHERE voucher_id = ?")) {
                ps.setInt(1, voucherId);
                ps.executeUpdate();
            }
        }
    }

    // Khách hủy đơn của chính mình (chỉ khi đang chờ xác nhận, status = 0)
    public boolean cancelOrder(int orderId, int userId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT status FROM Orders WHERE order_id = ? AND user_id = ?")) {
                ps.setInt(1, orderId);
                ps.setInt(2, userId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next() || rs.getInt("status") != 0) {
                    conn.rollback();
                    return false;
                }
            }
            if (!changeStatus(conn, orderId, 3)) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // Tìm kiếm đơn hàng (admin): theo mã đơn, tên người nhận, SĐT, tên đăng nhập; lọc theo trạng thái (null = tất cả)
    public List<Order> searchOrders(String keyword, Integer status) {
        StringBuilder sql = new StringBuilder(
            "SELECT o.*, v.code as voucher_code, u.username FROM Orders o " +
            "LEFT JOIN Vouchers v ON o.voucher_id = v.voucher_id " +
            "LEFT JOIN Users u ON o.user_id = u.user_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            String like = "%" + ProductSearchHelper.escapeLike(ProductSearchHelper.stripD(kw)) + "%";
            sql.append(" AND (CAST(o.order_id AS VARCHAR(20)) = ? OR o.receiver_phone LIKE ? ESCAPE '\\'")
               .append(" OR ").append(ProductSearchHelper.col("o.receiver_name")).append(" LIKE ? ESCAPE '\\'")
               .append(" OR ").append(ProductSearchHelper.col("u.username")).append(" LIKE ? ESCAPE '\\')");
            params.add(kw.replace("#", "")); params.add(like); params.add(like); params.add(like);
        }
        if (status != null) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY o.created_at DESC");
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = mapOrder(rs);
                order.setUsername(rs.getString("username"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
    
    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        
        int voucherId = rs.getInt("voucher_id");
        if (!rs.wasNull()) {
            order.setVoucherId(voucherId);
            order.setVoucherCode(rs.getString("voucher_code"));
        }
        
        order.setReceiverName(rs.getString("receiver_name"));
        order.setReceiverPhone(rs.getString("receiver_phone"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getInt("status"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        return order;
    }
}
