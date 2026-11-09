package com.mycompany.manage_book.servlet;

import com.mycompany.manage_book.dao.UserDAO;
import com.mycompany.manage_book.model.User;
import com.mycompany.manage_book.util.DaoResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

/** Quản lý người dùng (admin): tìm kiếm, sửa thông tin/vai trò, đặt lại mật khẩu, xóa. */
@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!checkAdmin(request, response)) return;
        
        String q = request.getParameter("q");
        Integer role = null;
        try {
            String r = request.getParameter("role");
            if (r != null && !r.isEmpty()) role = Integer.valueOf(r);
        } catch (NumberFormatException e) {
            role = null;
        }
        List<User> users = userDAO.searchUsers(q, role);
        request.setAttribute("users", users);
        request.setAttribute("q", q == null ? "" : q.trim());
        request.setAttribute("roleFilter", role);
        request.getRequestDispatcher("/WEB-INF/views/admin-users.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!checkAdmin(request, response)) return;
        User current = (User) request.getSession().getAttribute("user");
        String base = request.getContextPath() + "/admin/users";
        String action = request.getParameter("action");
        
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            
            if ("edit".equals(action)) {
                String fullName = request.getParameter("fullName");
                String email = request.getParameter("email");
                int role = Integer.parseInt(request.getParameter("role"));
                fullName = fullName == null ? "" : fullName.trim();
                email = email == null ? "" : email.trim();
                
                if (fullName.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") 
                        || (role != 0 && role != 1)) {
                    response.sendRedirect(base + "?error=invalid");
                    return;
                }
                if (userId == current.getUserId() && role != 1) {
                    response.sendRedirect(base + "?error=self"); // không tự hạ quyền chính mình
                    return;
                }
                if (userDAO.isEmailUsedByOther(email, userId)) {
                    response.sendRedirect(base + "?error=email");
                    return;
                }
                response.sendRedirect(base + (userDAO.updateUserByAdmin(userId, fullName, email, role)
                        ? "?success=edit" : "?error=edit"));
                
            } else if ("reset_password".equals(action)) {
                String newPassword = request.getParameter("newPassword");
                if (newPassword == null || newPassword.length() < 6) {
                    response.sendRedirect(base + "?error=password");
                    return;
                }
                response.sendRedirect(base + (userDAO.resetPasswordByAdmin(userId, newPassword)
                        ? "?success=password" : "?error=edit"));
                
            } else if ("delete".equals(action)) {
                if (userId == current.getUserId()) {
                    response.sendRedirect(base + "?error=self");
                    return;
                }
                int result = userDAO.deleteUser(userId);
                if (result == DaoResult.OK) {
                    response.sendRedirect(base + "?success=delete");
                } else if (result == DaoResult.IN_USE) {
                    response.sendRedirect(base + "?error=inuse");
                } else {
                    response.sendRedirect(base + "?error=delete");
                }
            } else {
                response.sendRedirect(base);
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(base + "?error=invalid");
        }
    }
    
    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (user.getRole() != 1) {
            response.sendRedirect(request.getContextPath() + "/home");
            return false;
        }
        return true;
    }
}
