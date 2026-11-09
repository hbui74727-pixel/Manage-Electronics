package com.mycompany.manage_book.servlet;

import com.mycompany.manage_book.dao.UserDAO;
import com.mycompany.manage_book.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Lấy thông tin user mới nhất từ database
        User updatedUser = userDAO.getUserById(user.getUserId());
        session.setAttribute("user", updatedUser);
        
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim();
        if (fullName.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            request.setAttribute("error", "Vui lòng nhập họ tên và email hợp lệ!");
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
            return;
        }
        
        // Kiểm tra email đã tồn tại (trừ email của chính user)
        if (!email.equals(user.getEmail()) && userDAO.isEmailExists(email)) {
            request.setAttribute("error", "Email đã được sử dụng bởi tài khoản khác!");
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
            return;
        }
        
        boolean success = userDAO.updateProfile(user.getUserId(), fullName, email);
        
        if (success) {
            User updatedUser = userDAO.getUserById(user.getUserId());
            session.setAttribute("user", updatedUser);
            request.setAttribute("success", "Cập nhật thông tin thành công!");
        } else {
            request.setAttribute("error", "Cập nhật thông tin thất bại!");
        }
        
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }
}
