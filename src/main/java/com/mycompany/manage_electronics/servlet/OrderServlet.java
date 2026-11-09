package com.mycompany.manage_electronics.servlet;

import com.mycompany.manage_electronics.dao.OrderDAO;
import com.mycompany.manage_electronics.model.Order;
import com.mycompany.manage_electronics.model.OrderItem;
import com.mycompany.manage_electronics.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {
    private OrderDAO orderDAO = new OrderDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String orderIdParam = request.getParameter("orderId");
        
        if (orderIdParam != null) {
            // Xem chi tiết đơn hàng
            int orderId;
            try {
                orderId = Integer.parseInt(orderIdParam);
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/orders");
                return;
            }
            Order order = orderDAO.getOrderById(orderId);
            
            if (order != null && order.getUserId() == user.getUserId()) {
                List<OrderItem> orderItems = orderDAO.getOrderItems(orderId);
                request.setAttribute("order", order);
                request.setAttribute("orderItems", orderItems);
                request.getRequestDispatcher("/WEB-INF/views/order-detail.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/orders");
            }
        } else {
            // Danh sách đơn hàng
            List<Order> orders = orderDAO.getOrdersByUser(user.getUserId());
            request.setAttribute("orders", orders);
            request.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(request, response);
        }
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
        
        String action = request.getParameter("action");
        
        if ("cancel".equals(action)) {
            handleCancel(request, response, user);
        }
    }
    
    private void handleCancel(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            boolean success = orderDAO.cancelOrder(orderId, user.getUserId());
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/orders?success=cancel");
            } else {
                response.sendRedirect(request.getContextPath() + "/orders?error=cancel");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/orders?error=cancel");
        }
    }
}
