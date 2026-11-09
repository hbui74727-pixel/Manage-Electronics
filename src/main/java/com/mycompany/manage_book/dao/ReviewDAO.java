package com.mycompany.manage_book.dao;

import com.mycompany.manage_book.model.Review;
import com.mycompany.manage_book.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    // Danh sách đánh giá của một sản phẩm (mới nhất trước)
    public List<Review> getReviewsByProduct(int productId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.review_id, r.user_id, r.product_id, r.rating, r.comment, r.created_at, " +
                     "u.username, u.full_name " +
                     "FROM Reviews r JOIN Users u ON r.user_id = u.user_id " +
                     "WHERE r.product_id = ? ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Review r = new Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setUserId(rs.getInt("user_id"));
                r.setProductId(rs.getInt("product_id"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));
                r.setCreatedAt(rs.getTimestamp("created_at"));
                r.setUsername(rs.getString("username"));
                r.setUserFullName(rs.getString("full_name"));
                reviews.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // Điểm trung bình (0 nếu chưa có đánh giá)
    public double getAverageRating(int productId) {
        String sql = "SELECT AVG(CAST(rating AS FLOAT)) FROM Reviews WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1); // NULL -> 0.0
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Tìm 1 order item mà user đã mua (đơn hoàn thành) và chưa đánh giá; không có thì trả về 0
    public int findReviewableOrderItemId(int userId, int productId) {
        String sql = "SELECT TOP 1 oi.order_item_id FROM OrderItems oi " +
                     "JOIN Orders o ON oi.order_id = o.order_id " +
                     "WHERE o.user_id = ? AND oi.product_id = ? AND o.status = 2 AND oi.is_reviewed = 0 " +
                     "ORDER BY o.created_at";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Thêm đánh giá và đánh dấu order item đã đánh giá (trong 1 transaction).
    // Trả về product_id nếu thành công, 0 nếu thất bại.
    public int addReview(int userId, int orderItemId, int rating, String comment) {
        String checkSql = "SELECT oi.product_id FROM OrderItems oi " +
                          "JOIN Orders o ON oi.order_id = o.order_id " +
                          "WHERE oi.order_item_id = ? AND o.user_id = ? AND o.status = 2 AND oi.is_reviewed = 0";
        String insertSql = "INSERT INTO Reviews (user_id, product_id, rating, comment) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE OrderItems SET is_reviewed = 1 WHERE order_item_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int productId = 0;
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, orderItemId);
                    ps.setInt(2, userId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        productId = rs.getInt(1);
                    }
                }
                if (productId == 0) {
                    conn.rollback();
                    return 0;
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, productId);
                    ps.setInt(3, rating);
                    ps.setString(4, comment);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, orderItemId);
                    ps.executeUpdate();
                }

                conn.commit();
                return productId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
