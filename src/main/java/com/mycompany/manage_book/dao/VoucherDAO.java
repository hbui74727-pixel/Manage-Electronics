package com.mycompany.manage_book.dao;

import com.mycompany.manage_book.model.Voucher;
import com.mycompany.manage_book.util.DBConnection;
import com.mycompany.manage_book.util.DaoResult;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {
    
    // Lấy tất cả vouchers
    public List<Voucher> getAllVouchers() {
        String sql = "SELECT * FROM Vouchers ORDER BY voucher_id DESC";
        List<Voucher> vouchers = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                vouchers.add(mapVoucher(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return vouchers;
    }
    
    // Lấy vouchers đang active
    public List<Voucher> getActiveVouchers() {
        String sql = "SELECT * FROM Vouchers WHERE is_active = 1 AND (expiration_date IS NULL OR expiration_date > GETDATE()) ORDER BY voucher_id DESC";
        List<Voucher> vouchers = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                vouchers.add(mapVoucher(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return vouchers;
    }
    
    // Lấy voucher theo code
    public Voucher getVoucherByCode(String code) {
        String sql = "SELECT * FROM Vouchers WHERE code = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapVoucher(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Thêm voucher
    public boolean addVoucher(String code, int discountPercent, BigDecimal maxDiscount, Timestamp expirationDate, int usageLimit) {
        String sql = "INSERT INTO Vouchers (code, discount_percent, max_discount, expiration_date, usage_limit) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, code);
            ps.setInt(2, discountPercent);
            ps.setBigDecimal(3, maxDiscount);
            ps.setTimestamp(4, expirationDate);
            ps.setInt(5, usageLimit);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Cập nhật voucher
    public boolean updateVoucher(int voucherId, String code, int discountPercent, BigDecimal maxDiscount, Timestamp expirationDate, int usageLimit, boolean isActive) {
        String sql = "UPDATE Vouchers SET code = ?, discount_percent = ?, max_discount = ?, expiration_date = ?, usage_limit = ?, is_active = ? WHERE voucher_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, code);
            ps.setInt(2, discountPercent);
            ps.setBigDecimal(3, maxDiscount);
            ps.setTimestamp(4, expirationDate);
            ps.setInt(5, usageLimit);
            ps.setBoolean(6, isActive);
            ps.setInt(7, voucherId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Xóa voucher. IN_USE nếu voucher đã được dùng trong đơn hàng (khi đó nên tắt kích hoạt thay vì xóa).
    public int deleteVoucher(int voucherId) {
        String sql = "DELETE FROM Vouchers WHERE voucher_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, voucherId);
            return ps.executeUpdate() > 0 ? DaoResult.OK : DaoResult.FAIL;
        } catch (SQLException e) {
            if (DaoResult.isConstraintViolation(e)) return DaoResult.IN_USE;
            e.printStackTrace();
        }
        return DaoResult.FAIL;
    }

    // Tìm voucher theo mã (không phân biệt hoa thường)
    public List<Voucher> searchVouchers(String keyword, String state) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Vouchers WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND code LIKE ? ESCAPE '\\'");
            params.add("%" + ProductSearchHelper.escapeLike(keyword.trim()) + "%");
        }
        if ("active".equals(state)) {
            sql.append(" AND is_active = 1 AND (expiration_date IS NULL OR expiration_date > GETDATE()) AND usage_limit > 0");
        } else if ("inactive".equals(state)) {
            sql.append(" AND NOT (is_active = 1 AND (expiration_date IS NULL OR expiration_date > GETDATE()) AND usage_limit > 0)");
        }
        sql.append(" ORDER BY voucher_id DESC");
        List<Voucher> vouchers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) vouchers.add(mapVoucher(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vouchers;
    }
    
    // Giảm usage_limit khi sử dụng voucher
    public boolean decreaseUsageLimit(int voucherId) {
        String sql = "UPDATE Vouchers SET usage_limit = usage_limit - 1 WHERE voucher_id = ? AND usage_limit > 0";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, voucherId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private Voucher mapVoucher(ResultSet rs) throws SQLException {
        Voucher voucher = new Voucher();
        voucher.setVoucherId(rs.getInt("voucher_id"));
        voucher.setCode(rs.getString("code"));
        voucher.setDiscountPercent(rs.getInt("discount_percent"));
        voucher.setMaxDiscount(rs.getBigDecimal("max_discount"));
        voucher.setExpirationDate(rs.getTimestamp("expiration_date"));
        voucher.setUsageLimit(rs.getInt("usage_limit"));
        voucher.setActive(rs.getBoolean("is_active"));
        return voucher;
    }
}
