package com.mycompany.manage_electronics.servlet;

import com.mycompany.manage_electronics.dao.CartDAO;
import com.mycompany.manage_electronics.model.Cart;
import com.mycompany.manage_electronics.model.CartItem;
import com.mycompany.manage_electronics.model.User;
import com.mycompany.manage_electronics.util.DaoResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private CartDAO cartDAO = new CartDAO();
    
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
        
        // Tính tổng tiền
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("totalAmount", totalAmount);
        request.setAttribute("cart", cart);
        
        request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
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
        
        if ("add".equals(action)) {
            handleAdd(request, response, user);
        } else if ("update".equals(action)) {
            handleUpdate(request, response, user);
        } else if ("remove".equals(action)) {
            handleRemove(request, response, user);
        }
    }
    
    private void handleAdd(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            
            Cart cart = cartDAO.getOrCreateCart(user.getUserId());
            int result = cartDAO.addToCart(cart.getCartId(), productId, quantity);
            
            if (result == DaoResult.OK) {
                response.sendRedirect(request.getContextPath() + "/cart?success=add");
            } else if (result == DaoResult.OUT_OF_STOCK) {
                response.sendRedirect(request.getContextPath() + "/cart?error=stock");
            } else {
                response.sendRedirect(request.getContextPath() + "/cart?error=add");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/cart?error=add");
        }
    }
    
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        try {
            int cartItemId = Integer.parseInt(request.getParameter("cartItemId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            Cart cart = cartDAO.getOrCreateCart(user.getUserId());
            
            int result = cartDAO.updateQuantity(cartItemId, cart.getCartId(), quantity);
            if (result == DaoResult.OUT_OF_STOCK) {
                response.sendRedirect(request.getContextPath() + "/cart?error=stock");
                return;
            }
            
            response.sendRedirect(request.getContextPath() + "/cart");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/cart?error=update");
        }
    }
    
    private void handleRemove(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        try {
            int cartItemId = Integer.parseInt(request.getParameter("cartItemId"));
            Cart cart = cartDAO.getOrCreateCart(user.getUserId());
            cartDAO.removeItem(cartItemId, cart.getCartId());
            
            response.sendRedirect(request.getContextPath() + "/cart?success=remove");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/cart?error=remove");
        }
    }
}
