package com.mycompany.manage_electronics.servlet;

import com.mycompany.manage_electronics.dao.VoucherDAO;
import com.mycompany.manage_electronics.model.User;
import com.mycompany.manage_electronics.util.DaoResult;
import com.mycompany.manage_electronics.model.Voucher;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet("/admin/vouchers")
public class AdminVoucherServlet extends HttpServlet {
    private VoucherDAO voucherDAO = new VoucherDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!checkAdmin(request, response)) return;
        
        String q = request.getParameter("q");
        String state = request.getParameter("state");
        List<Voucher> vouchers = voucherDAO.searchVouchers(q, state);
        request.setAttribute("q", q == null ? "" : q.trim());
        request.setAttribute("state", state == null ? "" : state);
        request.setAttribute("vouchers", vouchers);
        
        request.getRequestDispatcher("/WEB-INF/views/admin-vouchers.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!checkAdmin(request, response)) return;
        
        String action = request.getParameter("action");
        
        if ("add".equals(action)) {
            handleAdd(request, response);
        } else if ("edit".equals(action)) {
            handleEdit(request, response);
        } else if ("delete".equals(action)) {
            handleDelete(request, response);
        }
    }
    
    private void handleAdd(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            String code = request.getParameter("code");
            int discountPercent = Integer.parseInt(request.getParameter("discountPercent"));
            BigDecimal maxDiscount = new BigDecimal(request.getParameter("maxDiscount"));
            String expirationDateStr = request.getParameter("expirationDate");
            int usageLimit = Integer.parseInt(request.getParameter("usageLimit"));
            
            if (discountPercent < 1 || discountPercent > 100 || usageLimit < 0 || maxDiscount.signum() < 0) {
                throw new IllegalArgumentException("Giá trị voucher không hợp lệ");
            }
            Timestamp expirationDate = null;
            if (expirationDateStr != null && !expirationDateStr.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                expirationDate = new Timestamp(sdf.parse(expirationDateStr).getTime() + 24L * 3600 * 1000 - 1000); // hết hạn cuối ngày
            }
            
            boolean success = voucherDAO.addVoucher(code, discountPercent, maxDiscount, expirationDate, usageLimit);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/vouchers?success=add");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/vouchers?error=add");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/vouchers?error=add");
        }
    }
    
    private void handleEdit(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            int voucherId = Integer.parseInt(request.getParameter("voucherId"));
            String code = request.getParameter("code");
            int discountPercent = Integer.parseInt(request.getParameter("discountPercent"));
            BigDecimal maxDiscount = new BigDecimal(request.getParameter("maxDiscount"));
            String expirationDateStr = request.getParameter("expirationDate");
            int usageLimit = Integer.parseInt(request.getParameter("usageLimit"));
            boolean isActive = "1".equals(request.getParameter("isActive"));
            
            if (discountPercent < 1 || discountPercent > 100 || usageLimit < 0 || maxDiscount.signum() < 0) {
                throw new IllegalArgumentException("Giá trị voucher không hợp lệ");
            }
            Timestamp expirationDate = null;
            if (expirationDateStr != null && !expirationDateStr.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                expirationDate = new Timestamp(sdf.parse(expirationDateStr).getTime() + 24L * 3600 * 1000 - 1000); // hết hạn cuối ngày
            }
            
            boolean success = voucherDAO.updateVoucher(voucherId, code, discountPercent, maxDiscount, 
                                                      expirationDate, usageLimit, isActive);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/vouchers?success=edit");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/vouchers?error=edit");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/vouchers?error=edit");
        }
    }
    
    private void handleDelete(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            int voucherId = Integer.parseInt(request.getParameter("voucherId"));
            int result = voucherDAO.deleteVoucher(voucherId);
            
            if (result == DaoResult.OK) {
                response.sendRedirect(request.getContextPath() + "/admin/vouchers?success=delete");
            } else if (result == DaoResult.IN_USE) {
                response.sendRedirect(request.getContextPath() + "/admin/vouchers?error=inuse");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/vouchers?error=delete");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/vouchers?error=delete");
        }
    }
    
    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
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
