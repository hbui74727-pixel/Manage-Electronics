package com.mycompany.manage_electronics.dao;

import com.mycompany.manage_electronics.model.Wishlist;
import com.mycompany.manage_electronics.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {
    
    // Lấy danh sách yêu thích của user
    public List<Wishlist> getWishlistByUser(int userId) {
        String sql = "SELECT w.wishlist_id, w.user_id, w.product_id, w.added_at, " +
                    "p.name, p.price, p.image_url, p.stock " +
                    "FROM Wishlists w " +
                    "JOIN Products p ON w.product_id = p.product_id " +
                    "WHERE w.user_id = ? " +
                    "ORDER BY w.added_at DESC";
        
        List<Wishlist> wishlists = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Wishlist wishlist = new Wishlist();
                wishlist.setWishlistId(rs.getInt("wishlist_id"));
                wishlist.setUserId(rs.getInt("user_id"));
                wishlist.setProductId(rs.getInt("product_id"));
                wishlist.setAddedAt(rs.getTimestamp("added_at"));
                wishlist.setProductName(rs.getString("name"));
                wishlist.setProductPrice(rs.getBigDecimal("price"));
                wishlist.setProductImageUrl(rs.getString("image_url"));
                wishlist.setProductStock(rs.getInt("stock"));
                wishlists.add(wishlist);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return wishlists;
    }
    
    // Kiểm tra sản phẩm đã có trong wishlist chưa
    public boolean isInWishlist(int userId, int productId) {
        String sql = "SELECT wishlist_id FROM Wishlists WHERE user_id = ? AND product_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Thêm vào wishlist
    public boolean addToWishlist(int userId, int productId) {
        // Kiểm tra đã tồn tại chưa
        if (isInWishlist(userId, productId)) {
            return false;
        }
        
        String sql = "INSERT INTO Wishlists (user_id, product_id) VALUES (?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Xóa khỏi wishlist
    public boolean removeFromWishlist(int wishlistId, int userId) {
        String sql = "DELETE FROM Wishlists WHERE wishlist_id = ? AND user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, wishlistId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Xóa theo userId và productId
    public boolean removeFromWishlistByProduct(int userId, int productId) {
        String sql = "DELETE FROM Wishlists WHERE user_id = ? AND product_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
