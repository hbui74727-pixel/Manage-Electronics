package com.mycompany.manage_electronics.dao;

import com.mycompany.manage_electronics.model.Cart;
import com.mycompany.manage_electronics.model.CartItem;
import com.mycompany.manage_electronics.util.DBConnection;
import com.mycompany.manage_electronics.util.DaoResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    
    // Lấy hoặc tạo giỏ hàng cho user
    public Cart getOrCreateCart(int userId) {
        String sql = "SELECT cart_id, user_id, updated_at FROM Carts WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Cart cart = new Cart();
                cart.setCartId(rs.getInt("cart_id"));
                cart.setUserId(rs.getInt("user_id"));
                cart.setUpdatedAt(rs.getTimestamp("updated_at"));
                return cart;
            } else {
                // Tạo giỏ hàng mới
                return createCart(userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Cart createCart(int userId) {
        String sql = "INSERT INTO Carts (user_id) VALUES (?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, userId);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                Cart cart = new Cart();
                cart.setCartId(rs.getInt(1));
                cart.setUserId(userId);
                return cart;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Lấy tất cả items trong giỏ hàng
    public List<CartItem> getCartItems(int cartId) {
        String sql = "SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity, " +
                    "p.name, p.price, p.image_url, p.stock " +
                    "FROM CartItems ci " +
                    "JOIN Products p ON ci.product_id = p.product_id " +
                    "WHERE ci.cart_id = ?";
        
        List<CartItem> items = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, cartId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setCartItemId(rs.getInt("cart_item_id"));
                item.setCartId(rs.getInt("cart_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setProductName(rs.getString("name"));
                item.setProductPrice(rs.getBigDecimal("price"));
                item.setProductImageUrl(rs.getString("image_url"));
                item.setProductStock(rs.getInt("stock"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return items;
    }
    
    // Thêm sản phẩm vào giỏ hàng. Trả về OK / OUT_OF_STOCK (vượt tồn kho) / FAIL.
    public int addToCart(int cartId, int productId, int quantity) {
        if (quantity <= 0) return DaoResult.FAIL;
        String stockSql = "SELECT stock FROM Products WHERE product_id = ?";
        String checkSql = "SELECT cart_item_id, quantity FROM CartItems WHERE cart_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO CartItems (cart_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateSql = "UPDATE CartItems SET quantity = ? WHERE cart_item_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            int stock;
            try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                ps.setInt(1, productId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return DaoResult.FAIL;
                stock = rs.getInt(1);
            }
            int existingId = 0, existingQty = 0;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, cartId);
                ps.setInt(2, productId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    existingId = rs.getInt("cart_item_id");
                    existingQty = rs.getInt("quantity");
                }
            }
            int newQty = existingQty + quantity;
            if (newQty > stock) return DaoResult.OUT_OF_STOCK;
            
            if (existingId > 0) {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, newQty);
                    ps.setInt(2, existingId);
                    return ps.executeUpdate() > 0 ? DaoResult.OK : DaoResult.FAIL;
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, cartId);
                    ps.setInt(2, productId);
                    ps.setInt(3, quantity);
                    return ps.executeUpdate() > 0 ? DaoResult.OK : DaoResult.FAIL;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return DaoResult.FAIL;
        }
    }
    
    // Cập nhật số lượng (chỉ item thuộc giỏ của chính user, và không vượt tồn kho)
    public int updateQuantity(int cartItemId, int cartId, int quantity) {
        if (quantity <= 0) return DaoResult.FAIL;
        String sql = "UPDATE CartItems SET quantity = ? WHERE cart_item_id = ? AND cart_id = ? " +
                     "AND ? <= (SELECT p.stock FROM Products p WHERE p.product_id = CartItems.product_id)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, quantity);
            ps.setInt(2, cartItemId);
            ps.setInt(3, cartId);
            ps.setInt(4, quantity);
            return ps.executeUpdate() > 0 ? DaoResult.OK : DaoResult.OUT_OF_STOCK;
        } catch (SQLException e) {
            e.printStackTrace();
            return DaoResult.FAIL;
        }
    }
    
    // Xóa item khỏi giỏ hàng (chỉ item thuộc giỏ của chính user)
    public boolean removeItem(int cartItemId, int cartId) {
        String sql = "DELETE FROM CartItems WHERE cart_item_id = ? AND cart_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, cartItemId);
            ps.setInt(2, cartId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Xóa tất cả items trong giỏ hàng
    public boolean clearCart(int cartId) {
        String sql = "DELETE FROM CartItems WHERE cart_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, cartId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Đếm số lượng items trong giỏ hàng
    public int getCartItemCount(int cartId) {
        String sql = "SELECT COUNT(*) as count FROM CartItems WHERE cart_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, cartId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
}
