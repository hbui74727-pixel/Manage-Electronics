package com.mycompany.manage_book.servlet;

import com.mycompany.manage_book.dao.CategoryDAO;
import com.mycompany.manage_book.dao.ProductDAO;
import com.mycompany.manage_book.model.Category;
import com.mycompany.manage_book.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Danh sách sản phẩm cho khách: tìm kiếm theo từ khóa, lọc theo danh mục / khoảng giá / còn hàng,
 * sắp xếp và phân trang. Mọi tham số đều được giữ lại khi chuyển trang.
 */
@WebServlet("/products")
public class ProductListServlet extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private static final int PAGE_SIZE = 12;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int page = parseInt(request.getParameter("page"), 1);
        if (page < 1) page = 1;
        
        String keyword = trimToNull(request.getParameter("q"));
        if (keyword != null && keyword.length() > 100) keyword = keyword.substring(0, 100);
        Integer categoryId = parseIntObj(request.getParameter("categoryId"));
        BigDecimal minPrice = parseMoney(request.getParameter("minPrice"));
        BigDecimal maxPrice = parseMoney(request.getParameter("maxPrice"));
        boolean inStock = "1".equals(request.getParameter("inStock"));
        String sort = request.getParameter("sort");
        if (sort == null) sort = "";
        
        List<Category> categories = categoryDAO.getAllCategories();
        
        int totalProducts = productDAO.countSearchProducts(keyword, categoryId, minPrice, maxPrice, inStock);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalProducts / PAGE_SIZE));
        if (page > totalPages) page = totalPages;
        List<Product> products = productDAO.searchProducts(keyword, categoryId, minPrice, maxPrice,
                inStock, sort, page, PAGE_SIZE);
        
        // Chuỗi query (không có page) để dựng liên kết phân trang
        StringBuilder qs = new StringBuilder();
        append(qs, "q", keyword);
        append(qs, "categoryId", categoryId == null ? null : categoryId.toString());
        append(qs, "minPrice", minPrice == null ? null : minPrice.toPlainString());
        append(qs, "maxPrice", maxPrice == null ? null : maxPrice.toPlainString());
        append(qs, "sort", sort.isEmpty() ? null : sort);
        append(qs, "inStock", inStock ? "1" : null);
        
        request.setAttribute("categories", categories);
        request.setAttribute("products", products);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("selectedCategoryId", categoryId);
        request.setAttribute("keyword", keyword);
        request.setAttribute("minPrice", minPrice == null ? "" : minPrice.toPlainString());
        request.setAttribute("maxPrice", maxPrice == null ? "" : maxPrice.toPlainString());
        request.setAttribute("inStock", inStock);
        request.setAttribute("sort", sort);
        request.setAttribute("baseQuery", qs.toString());
        
        request.getRequestDispatcher("/WEB-INF/views/product-list.jsp").forward(request, response);
    }
    
    private static void append(StringBuilder sb, String key, String value) {
        if (value == null || value.isEmpty()) return;
        sb.append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8)).append('&');
    }
    
    private static String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
    
    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
    
    private static Integer parseIntObj(String s) {
        try { return (s == null || s.isEmpty()) ? null : Integer.valueOf(s); } catch (NumberFormatException e) { return null; }
    }
    
    private static BigDecimal parseMoney(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            BigDecimal v = new BigDecimal(s.trim());
            return v.signum() < 0 ? null : v;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
