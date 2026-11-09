package com.mycompany.manage_electronics.servlet;

import com.mycompany.manage_electronics.dao.UserDAO;
import com.mycompany.manage_electronics.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null || password == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user;
        try {
            user = userDAO.login(username.trim(), password);
        } catch (IllegalStateException e) {
            request.setAttribute("error", "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage()
                    + " — kiểm tra SQL Server, đã chạy file Manage_Electronics.sql chưa, và DB_PASSWORD.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }
        
        if (user != null) {
            HttpSession session = request.getSession();
            request.changeSessionId(); // chống session fixation
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            request.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}
