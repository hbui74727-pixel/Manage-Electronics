package com.mycompany.manage_electronics.dao;

import com.mycompany.manage_electronics.model.User;
import com.mycompany.manage_electronics.util.DBConnection;
import com.mycompany.manage_electronics.util.DaoResult;
import com.mycompany.manage_electronics.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String stored = rs.getString("password");
                if (!PasswordUtil.verify(password, stored)) {
                    return null;
                }
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getInt("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                // Nâng cấp mật khẩu thô cũ sang dạng băm
                if (!PasswordUtil.isHashed(stored)) {
                    setPasswordHash(user.getUserId(), PasswordUtil.hash(password));
                }
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Báo lỗi CSDL riêng, không nhầm với "sai mật khẩu"
            throw new IllegalStateException(e.getMessage(), e);
        }
        return null;
    }

    private void setPasswordHash(int userId, String hash) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE Users SET password = ? WHERE user_id = ?")) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean register(String username, String password, String fullName, String email) {
        String sql = "INSERT INTO Users (username, password, full_name, email, role) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, fullName);
            ps.setString(4, email);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM Users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getInt("role"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE Users SET password = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, email);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getInt("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean updateProfile(int userId, String fullName, String email) {
        String sql = "UPDATE Users SET full_name = ?, email = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setInt(3, userId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== Chức năng quản trị người dùng (admin) =====

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getInt("role"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }

    /** Tìm người dùng theo từ khóa (tên đăng nhập, họ tên, email, mã) và vai trò (null = tất cả). */
    public List<User> searchUsers(String keyword, Integer role) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Users WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + ProductSearchHelper.escapeLike(ProductSearchHelper.stripD(keyword.trim())) + "%";
            sql.append(" AND (").append(ProductSearchHelper.col("username")).append(" LIKE ? ESCAPE '\\'")
               .append(" OR ").append(ProductSearchHelper.col("full_name")).append(" LIKE ? ESCAPE '\\'")
               .append(" OR ").append(ProductSearchHelper.col("email")).append(" LIKE ? ESCAPE '\\'")
               .append(" OR CAST(user_id AS VARCHAR(20)) = ?)");
            params.add(like); params.add(like); params.add(like); params.add(keyword.trim());
        }
        if (role != null) {
            sql.append(" AND role = ?");
            params.add(role);
        }
        sql.append(" ORDER BY user_id");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isEmailUsedByOther(String email, int userId) {
        String sql = "SELECT COUNT(*) FROM Users WHERE email = ? AND user_id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUserByAdmin(int userId, String fullName, String email, int role) {
        String sql = "UPDATE Users SET full_name = ?, email = ?, role = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setInt(3, role);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean resetPasswordByAdmin(int userId, String newPassword) {
        String sql = "UPDATE Users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Xóa người dùng. Trả về DaoResult.IN_USE nếu người này đã có đơn hàng/đánh giá. */
    public int deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0 ? DaoResult.OK : DaoResult.FAIL;
        } catch (SQLException e) {
            if (DaoResult.isConstraintViolation(e)) return DaoResult.IN_USE;
            e.printStackTrace();
        }
        return DaoResult.FAIL;
    }
}
