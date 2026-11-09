package com.mycompany.manage_electronics.servlet;

import com.mycompany.manage_electronics.dao.UserDAO;
import com.mycompany.manage_electronics.model.User;
import com.mycompany.manage_electronics.util.EmailUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private static final long TOKEN_TTL_MS = 15 * 60 * 1000L; // token có hiệu lực 15 phút
    private static final Map<String, String> resetTokens = new ConcurrentHashMap<>(); // token -> email
    private static final Map<String, Long> tokenExpiry = new ConcurrentHashMap<>();   // token -> thời điểm hết hạn
    
    private static boolean isValidToken(String token) {
        if (token == null || !resetTokens.containsKey(token)) return false;
        Long exp = tokenExpiry.get(token);
        if (exp == null || exp < System.currentTimeMillis()) {
            resetTokens.remove(token);
            tokenExpiry.remove(token);
            return false;
        }
        return true;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String token = request.getParameter("token");
        
        if (isValidToken(token)) {
            request.setAttribute("token", token);
            request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("request".equals(action)) {
            handleForgotPassword(request, response);
        } else if ("reset".equals(action)) {
            handleResetPassword(request, response);
        }
    }
    
    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String email = request.getParameter("email");
        
        User user = userDAO.getUserByEmail(email);
        
        if (user != null) {
            String token = EmailUtil.generateResetToken();
            resetTokens.put(token, email);
            tokenExpiry.put(token, System.currentTimeMillis() + TOKEN_TTL_MS);
            
            String resetLink = request.getRequestURL().toString() + "?token=" + token;
            String subject = "Đặt lại mật khẩu - Manage Electronics";
            String body = "Xin chào " + user.getFullName() + ",\n\n" +
                         "Bạn đã yêu cầu đặt lại mật khẩu.\n" +
                         "Vui lòng nhấp vào link sau để đặt lại mật khẩu:\n\n" +
                         resetLink + "\n\n" +
                         "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.";
            
            boolean sent = EmailUtil.sendEmail(email, subject, body);
            
            if (sent) {
                request.setAttribute("success", "Link đặt lại mật khẩu đã được gửi đến email của bạn!");
            } else {
                resetTokens.remove(token);
                tokenExpiry.remove(token);
                request.setAttribute("error", "Không gửi được email. Hãy kiểm tra cấu hình MAIL_USER / MAIL_PASSWORD của máy chủ.");
            }
        } else {
            request.setAttribute("error", "Email không tồn tại trong hệ thống!");
        }
        
        request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
    }
    
    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String token = request.getParameter("token");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        if (newPassword == null || newPassword.length() < 6) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
            request.setAttribute("token", token);
            request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp!");
            request.setAttribute("token", token);
            request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
            return;
        }
        
        String email = isValidToken(token) ? resetTokens.get(token) : null;
        
        if (email != null) {
            boolean success = userDAO.updatePassword(email, newPassword);
            
            if (success) {
                resetTokens.remove(token);
                tokenExpiry.remove(token);
                request.setAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            } else {
                request.setAttribute("error", "Đặt lại mật khẩu thất bại!");
                request.setAttribute("token", token);
                request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
            }
        } else {
            request.setAttribute("error", "Token không hợp lệ hoặc đã hết hạn!");
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
        }
    }
}
