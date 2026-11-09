package com.mycompany.manage_electronics.servlet;

import com.mycompany.manage_electronics.dao.CartDAO;
import com.mycompany.manage_electronics.dao.OrderDAO;
import com.mycompany.manage_electronics.dao.VoucherDAO;
import com.mycompany.manage_electronics.model.*;
import com.mycompany.manage_electronics.util.DaoResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private CartDAO cartDAO = new CartDAO();
    private VoucherDAO voucherDAO = new VoucherDAO();
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
        
        Cart cart = cartDAO.getOrCreateCart(user.getUserId());
        List<CartItem> cartItems = cartDAO.getCartItems(cart.getCartId());
        
        if (cartItems.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        
        // Tính tổng tiền
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("totalAmount", totalAmount);
        request.setAttribute("user", user);
        
        request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);
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
        
        if ("apply_voucher".equals(action)) {
            handleApplyVoucher(request, response, user);
        } else if ("place_order".equals(action)) {
            handlePlaceOrder(request, response, user);
        }
    }
    
    private void handleApplyVoucher(HttpServletRequest request, HttpServletResponse response, User user) 
            throws ServletException, IOException {
        
        String voucherCode = request.getParameter("voucherCode");
        Voucher voucher = voucherDAO.getVoucherByCode(voucherCode);
        
        Cart cart = cartDAO.getOrCreateCart(user.getUserId());
        List<CartItem> cartItems = cartDAO.getCartItems(cart.getCartId());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        
        if (voucher == null || !voucher.isActive() || voucher.getUsageLimit() <= 0) {
            request.setAttribute("voucherError", "Mã giảm giá không hợp lệ hoặc đã hết lượt sử dụng");
        } else if (voucher.getExpirationDate() != null && voucher.getExpirationDate().before(new java.util.Date())) {
            request.setAttribute("voucherError", "Mã giảm giá đã hết hạn");
        } else {
            // Tính giảm giá
            BigDecimal discount = totalAmount.multiply(new BigDecimal(voucher.getDiscountPercent())).divide(new BigDecimal(100));
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
            
            BigDecimal finalAmount = totalAmount.subtract(discount);
            
            request.setAttribute("voucher", voucher);
            request.setAttribute("discount", discount);
            request.setAttribute("finalAmount", finalAmount);
            request.setAttribute("voucherSuccess", "Áp dụng mã giảm giá thành công!");
        }
        
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("totalAmount", totalAmount);
        request.setAttribute("user", user);
        
        request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);
    }
    
    private void handlePlaceOrder(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        try {
            String receiverName = request.getParameter("receiverName");
            String receiverPhone = request.getParameter("receiverPhone");
            String shippingAddress = request.getParameter("shippingAddress");
            String voucherCode = request.getParameter("voucherCode");
            
            receiverName = receiverName == null ? "" : receiverName.trim();
            receiverPhone = receiverPhone == null ? "" : receiverPhone.trim();
            shippingAddress = shippingAddress == null ? "" : shippingAddress.trim();
            if (receiverName.isEmpty() || shippingAddress.isEmpty()
                    || !receiverPhone.matches("^(0|\\+84)[0-9]{9,10}$")) {
                response.sendRedirect(request.getContextPath() + "/checkout?error=info");
                return;
            }
            
            Cart cart = cartDAO.getOrCreateCart(user.getUserId());
            List<CartItem> cartItems = cartDAO.getCartItems(cart.getCartId());
            
            if (cartItems.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
            for (CartItem ci : cartItems) {
                if (ci.getQuantity() > ci.getProductStock()) {
                    response.sendRedirect(request.getContextPath() + "/checkout?error=stock");
                    return;
                }
            }
            
            // Tính tổng tiền
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getQuantity())));
            }
            
            // Áp dụng voucher nếu có
            Integer voucherId = null;
            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                Voucher voucher = voucherDAO.getVoucherByCode(voucherCode);
                boolean valid = voucher != null && voucher.isActive() && voucher.getUsageLimit() > 0
                        && (voucher.getExpirationDate() == null || !voucher.getExpirationDate().before(new java.util.Date()));
                if (!valid) {
                    response.sendRedirect(request.getContextPath() + "/checkout?error=voucher");
                    return;
                }
                voucherId = voucher.getVoucherId();
                
                BigDecimal discount = totalAmount.multiply(new BigDecimal(voucher.getDiscountPercent())).divide(new BigDecimal(100));
                if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                    discount = voucher.getMaxDiscount();
                }
                totalAmount = totalAmount.subtract(discount);
            }
            
            // Tạo order items
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getProductPrice());
                orderItems.add(orderItem);
            }
            
            // Tạo đơn hàng
            int orderId = orderDAO.createOrder(user.getUserId(), voucherId, receiverName, receiverPhone, 
                                              shippingAddress, totalAmount, orderItems);
            
            if (orderId > 0) {
                // Xóa giỏ hàng
                cartDAO.clearCart(cart.getCartId());
                response.sendRedirect(request.getContextPath() + "/orders?success=order");
            } else if (orderId == DaoResult.OUT_OF_STOCK) {
                response.sendRedirect(request.getContextPath() + "/checkout?error=stock");
            } else if (orderId == DaoResult.BAD_VOUCHER) {
                response.sendRedirect(request.getContextPath() + "/checkout?error=voucher");
            } else {
                response.sendRedirect(request.getContextPath() + "/checkout?error=order");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/checkout?error=order");
        }
    }
}
