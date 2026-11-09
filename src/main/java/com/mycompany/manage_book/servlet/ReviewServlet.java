package com.mycompany.manage_book.servlet;

import com.mycompany.manage_book.dao.ReviewDAO;
import com.mycompany.manage_book.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/review")
public class ReviewServlet extends HttpServlet {
    private ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/products");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String productIdStr = request.getParameter("productId");
        try {
            int productId = Integer.parseInt(productIdStr);
            int orderItemId = Integer.parseInt(request.getParameter("orderItemId"));
            int rating = Integer.parseInt(request.getParameter("rating"));
            String comment = request.getParameter("comment");
            comment = (comment == null) ? "" : comment.trim();
            if (comment.length() > 1000) {
                comment = comment.substring(0, 1000);
            }

            if (rating < 1 || rating > 5) {
                redirectToProduct(request, response, productId, "invalid");
                return;
            }

            int reviewedProductId = reviewDAO.addReview(user.getUserId(), orderItemId, rating, comment);
            redirectToProduct(request, response, productId, reviewedProductId > 0 ? "success" : "error");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/products");
        }
    }

    private void redirectToProduct(HttpServletRequest request, HttpServletResponse response,
                                   int productId, String result) throws IOException {
        response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId
                + "&review=" + result + "#reviews");
    }
}
