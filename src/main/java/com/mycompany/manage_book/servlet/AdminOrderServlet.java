package com.mycompany.manage_book.servlet;

import com.mycompany.manage_book.dao.OrderDAO;
import com.mycompany.manage_book.model.Order;
import com.mycompany.manage_book.model.OrderItem;
import com.mycompany.manage_book.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {
    private OrderDAO orderDAO = new OrderDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!checkAdmin(request, response)) return;
        
        String orderIdParam = request.getParameter("orderId");
        
        if (orderIdParam != null) {
            // Xem chi tiết đơn hàng
            int orderId;
            try {
                orderId = Integer.parseInt(orderIdParam);
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/admin/orders");
                return;
            }
            Order order = orderDAO.getOrderById(orderId);
            if (order == null) {
                response.sendRedirect(request.getContextPath() + "/admin/orders?error=notfound");
                return;
            }
            List<OrderItem> orderItems = orderDAO.getOrderItems(orderId);
            
            request.setAttribute("order", order);
            request.setAttribute("orderItems", orderItems);
            request.getRequestDispatcher("/WEB-INF/views/admin-order-detail.jsp").forward(request, response);
        } else {
            // Danh sách đơn hàng
            String q = request.getParameter("q");
            Integer status = null;
            try {
                String st = request.getParameter("status");
                if (st != null && !st.isEmpty()) status = Integer.valueOf(st);
            } catch (NumberFormatException e) {
                status = null;
            }
            List<Order> orders = orderDAO.searchOrders(q, status);
            request.setAttribute("orders", orders);
            request.setAttribute("q", q == null ? "" : q.trim());
            request.setAttribute("statusFilter", status);
            request.getRequestDispatcher("/WEB-INF/views/admin-orders.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!checkAdmin(request, response)) return;
        
        String action = request.getParameter("action");
        
        if ("update_status".equals(action)) {
            handleUpdateStatus(request, response);
        }
    }
    
    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            int status = Integer.parseInt(request.getParameter("status"));
            
            boolean success = orderDAO.updateOrderStatus(orderId, status);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/orders?success=update");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/orders?error=update");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/orders?error=update");
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
