package com.mycompany.manage_electronics.servlet;

import com.mycompany.manage_electronics.dao.WishlistDAO;
import com.mycompany.manage_electronics.model.User;
import com.mycompany.manage_electronics.model.Wishlist;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/wishlist")
public class WishlistServlet extends HttpServlet {
    private WishlistDAO wishlistDAO = new WishlistDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        List<Wishlist> wishlists = wishlistDAO.getWishlistByUser(user.getUserId());
        request.setAttribute("wishlists", wishlists);
        
        request.getRequestDispatcher("/WEB-INF/views/wishlist.jsp").forward(request, response);
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
        } else if ("remove".equals(action)) {
            handleRemove(request, response, user);
        }
    }
    
    private void handleAdd(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            boolean success = wishlistDAO.addToWishlist(user.getUserId(), productId);
            
            String back = safeReferer(request);
            if (back != null) {
                response.sendRedirect(back + (back.contains("?") ? "&" : "?")
                        + (success ? "wishlist=added" : "wishlist=exists"));
            } else {
                response.sendRedirect(request.getContextPath() + "/wishlist");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/wishlist?error=add");
        }
    }
    
    /** Lấy Referer nếu cùng máy chủ (chống open redirect), bỏ tham số wishlist cũ và phần #fragment. */
    private String safeReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) return null;
        try {
            java.net.URI uri = new java.net.URI(referer);
            if (uri.getHost() == null || !uri.getHost().equals(request.getServerName())) return null;
            String path = uri.getRawPath() == null ? "" : uri.getRawPath();
            String query = uri.getRawQuery();
            StringBuilder q = new StringBuilder();
            if (query != null) {
                for (String part : query.split("&")) {
                    if (!part.isEmpty() && !part.startsWith("wishlist=")) {
                        q.append(q.length() == 0 ? "" : "&").append(part);
                    }
                }
            }
            return path + (q.length() > 0 ? "?" + q : "");
        } catch (java.net.URISyntaxException e) {
            return null;
        }
    }
    
    private void handleRemove(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException {
        try {
            int wishlistId = Integer.parseInt(request.getParameter("wishlistId"));
            wishlistDAO.removeFromWishlist(wishlistId, user.getUserId());
            
            response.sendRedirect(request.getContextPath() + "/wishlist?success=remove");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/wishlist?error=remove");
        }
    }
}
