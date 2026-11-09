package com.mycompany.manage_book.dao;

import com.mycompany.manage_book.model.Category;
import com.mycompany.manage_book.util.DBConnection;
import com.mycompany.manage_book.util.DaoResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories ORDER BY name";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    
    public Category getCategoryById(int categoryId) {
        String sql = "SELECT * FROM Categories WHERE category_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                return category;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean addCategory(String name, String description) {
        String sql = "INSERT INTO Categories (name, description) VALUES (?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, name);
            ps.setString(2, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateCategory(int categoryId, String name, String description) {
        String sql = "UPDATE Categories SET name = ?, description = ? WHERE category_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /** Xóa danh mục. IN_USE nếu danh mục còn sản phẩm. */
    public int deleteCategory(int categoryId) {
        String sql = "DELETE FROM Categories WHERE category_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0 ? DaoResult.OK : DaoResult.FAIL;
        } catch (SQLException e) {
            if (DaoResult.isConstraintViolation(e)) return DaoResult.IN_USE;
            e.printStackTrace();
        }
        return DaoResult.FAIL;
    }

    /** Tìm danh mục theo từ khóa (tên/mô tả/mã), không phân biệt hoa thường và dấu. */
    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllCategories();
        List<Category> list = new ArrayList<>();
        String kw = keyword.trim();
        String like = "%" + ProductSearchHelper.escapeLike(ProductSearchHelper.stripD(kw)) + "%";
        String sql = "SELECT * FROM Categories WHERE " + ProductSearchHelper.col("name") + " LIKE ? ESCAPE '\\' OR "
                + ProductSearchHelper.col("description") + " LIKE ? ESCAPE '\\' OR CAST(category_id AS VARCHAR(20)) = ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                list.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
