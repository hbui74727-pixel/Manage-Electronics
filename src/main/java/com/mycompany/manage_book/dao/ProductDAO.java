package com.mycompany.manage_book.dao;

import com.mycompany.manage_book.model.Product;
import com.mycompany.manage_book.util.DBConnection;
import com.mycompany.manage_book.util.DaoResult;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    // Lấy tất cả sản phẩm với phân trang
    public List<Product> getProducts(int page, int pageSize) {
        List<Product> products = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        
        String sql = "SELECT p.*, c.name as category_name FROM Products p " +
                     "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                     "ORDER BY p.created_at DESC " +
                     "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, offset);
            ps.setInt(2, pageSize);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    // Lấy sản phẩm theo category với phân trang
    public List<Product> getProductsByCategory(int categoryId, int page, int pageSize) {
        List<Product> products = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        
        String sql = "SELECT p.*, c.name as category_name FROM Products p " +
                     "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                     "WHERE p.category_id = ? " +
                     "ORDER BY p.created_at DESC " +
                     "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            ps.setInt(2, offset);
            ps.setInt(3, pageSize);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    // Đếm tổng số sản phẩm
    public int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM Products";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Đếm số sản phẩm theo category
    public int getTotalProductsByCategory(int categoryId) {
        String sql = "SELECT COUNT(*) FROM Products WHERE category_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Lấy chi tiết sản phẩm
    public Product getProductById(int productId) {
        String sql = "SELECT p.*, c.name as category_name FROM Products p " +
                     "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                     "WHERE p.product_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return extractProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Thêm sản phẩm mới
    public boolean addProduct(int categoryId, String name, String description, 
                             BigDecimal price, String imageUrl, int stock) {
        String sql = "INSERT INTO Products (category_id, name, description, price, image_url, stock) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setBigDecimal(4, price);
            ps.setString(5, imageUrl);
            ps.setInt(6, stock);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Cập nhật sản phẩm
    public boolean updateProduct(int productId, int categoryId, String name, String description,
                                BigDecimal price, String imageUrl, int stock) {
        String sql = "UPDATE Products SET category_id = ?, name = ?, description = ?, " +
                     "price = ?, image_url = ?, stock = ? WHERE product_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setBigDecimal(4, price);
            ps.setString(5, imageUrl);
            ps.setInt(6, stock);
            ps.setInt(7, productId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Xóa sản phẩm. IN_USE nếu sản phẩm đã nằm trong đơn hàng (giữ lịch sử đơn).
    public int deleteProduct(int productId) {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0 ? DaoResult.OK : DaoResult.FAIL;
        } catch (SQLException e) {
            if (DaoResult.isConstraintViolation(e)) return DaoResult.IN_USE;
            e.printStackTrace();
        }
        return DaoResult.FAIL;
    }
    
    // Lấy tất cả sản phẩm (cho admin, không phân trang)
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name FROM Products p " +
                     "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                     "ORDER BY p.product_id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    

    // ===== TÌM KIẾM / LỌC / SẮP XẾP =====

    private static final String SEARCH_FROM =
            " FROM Products p LEFT JOIN Categories c ON p.category_id = c.category_id WHERE 1=1";

    /** Ghép điều kiện WHERE động; mọi giá trị đều đi qua tham số (chống SQL injection). */
    private String buildWhere(String keyword, Integer categoryId, BigDecimal minPrice,
                              BigDecimal maxPrice, boolean inStockOnly, List<Object> params) {
        StringBuilder w = new StringBuilder();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            String like = "%" + ProductSearchHelper.escapeLike(ProductSearchHelper.stripD(kw)) + "%";
            w.append(" AND (").append(ProductSearchHelper.col("p.name")).append(" LIKE ? ESCAPE '\\'")
             .append(" OR ").append(ProductSearchHelper.col("p.description")).append(" LIKE ? ESCAPE '\\'")
             .append(" OR ").append(ProductSearchHelper.col("c.name")).append(" LIKE ? ESCAPE '\\'")
             .append(" OR CAST(p.product_id AS VARCHAR(20)) = ?)");
            params.add(like); params.add(like); params.add(like); params.add(kw);
        }
        if (categoryId != null) {
            w.append(" AND p.category_id = ?");
            params.add(categoryId);
        }
        if (minPrice != null) {
            w.append(" AND p.price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            w.append(" AND p.price <= ?");
            params.add(maxPrice);
        }
        if (inStockOnly) {
            w.append(" AND p.stock > 0");
        }
        return w.toString();
    }

    /** Chỉ cho phép các kiểu sắp xếp trong danh sách trắng. */
    private String orderBy(String sort) {
        if ("price_asc".equals(sort)) return " ORDER BY p.price ASC, p.product_id";
        if ("price_desc".equals(sort)) return " ORDER BY p.price DESC, p.product_id";
        if ("name_asc".equals(sort)) return " ORDER BY p.name ASC, p.product_id";
        if ("id_desc".equals(sort)) return " ORDER BY p.product_id DESC";
        return " ORDER BY p.created_at DESC, p.product_id DESC";
    }

    /**
     * Tìm kiếm sản phẩm. pageSize <= 0 nghĩa là lấy tất cả (không phân trang, dùng cho trang admin).
     */
    public List<Product> searchProducts(String keyword, Integer categoryId, BigDecimal minPrice,
                                        BigDecimal maxPrice, boolean inStockOnly, String sort,
                                        int page, int pageSize) {
        List<Product> products = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.*, c.name AS category_name")
                .append(SEARCH_FROM)
                .append(buildWhere(keyword, categoryId, minPrice, maxPrice, inStockOnly, params))
                .append(orderBy(sort));
        if (pageSize > 0) {
            sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            params.add(Math.max(0, (page - 1) * pageSize));
            params.add(pageSize);
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) products.add(extractProduct(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public int countSearchProducts(String keyword, Integer categoryId, BigDecimal minPrice,
                                   BigDecimal maxPrice, boolean inStockOnly) {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*)" + SEARCH_FROM
                + buildWhere(keyword, categoryId, minPrice, maxPrice, inStockOnly, params);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Helper method để extract Product từ ResultSet
    private Product extractProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setImageUrl(rs.getString("image_url"));
        product.setStock(rs.getInt("stock"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setCategoryName(rs.getString("category_name"));
        return product;
    }
}
