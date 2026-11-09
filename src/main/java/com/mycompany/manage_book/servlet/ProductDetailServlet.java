package com.mycompany.manage_book.servlet;

import com.mycompany.manage_book.dao.ProductDAO;
import com.mycompany.manage_book.dao.ReviewDAO;
import com.mycompany.manage_book.model.Product;
import com.mycompany.manage_book.model.Review;
import com.mycompany.manage_book.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/product-detail")
public class ProductDetailServlet extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();
    private ReviewDAO reviewDAO = new ReviewDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String productIdStr = request.getParameter("id");
        
        if (productIdStr == null || productIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }
        
        try {
            int productId = Integer.parseInt(productIdStr);
            Product product = productDAO.getProductById(productId);
            
            if (product == null) {
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }
            
            List<Review> reviews = reviewDAO.getReviewsByProduct(productId);
            request.setAttribute("product", product);
            request.setAttribute("reviews", reviews);
            request.setAttribute("reviewCount", reviews.size());
            request.setAttribute("avgRating", reviewDAO.getAverageRating(productId));

            // Nếu người dùng đã mua (đơn hoàn thành) và chưa đánh giá thì cho hiện form đánh giá
            User user = (User) request.getSession().getAttribute("user");
            if (user != null) {
                int orderItemId = reviewDAO.findReviewableOrderItemId(user.getUserId(), productId);
                if (orderItemId > 0) {
                    request.setAttribute("reviewOrderItemId", orderItemId);
                }
            }
            request.getRequestDispatcher("/WEB-INF/views/product-detail.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/products");
        }
    }
}
