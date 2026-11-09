package com.mycompany.manage_electronics.servlet;

import com.mycompany.manage_electronics.dao.CategoryDAO;
import com.mycompany.manage_electronics.model.Category;
import com.mycompany.manage_electronics.model.User;
import com.mycompany.manage_electronics.util.DaoResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/categories")
public class AdminCategoryServlet extends HttpServlet {
    private CategoryDAO categoryDAO = new CategoryDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Kiểm tra quyền admin
        if (!checkAdmin(request, response)) return;
        
        String q = request.getParameter("q");
        List<Category> categories = categoryDAO.searchCategories(q);
        request.setAttribute("q", q == null ? "" : q.trim());
        request.setAttribute("categories", categories);
        
        request.getRequestDispatcher("/WEB-INF/views/admin-categories.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Kiểm tra quyền admin
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
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        
        boolean success = categoryDAO.addCategory(name, description);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/categories?success=add");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/categories?error=add");
        }
    }
    
    private void handleEdit(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            
            boolean success = categoryDAO.updateCategory(categoryId, name, description);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/categories?success=edit");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/categories?error=edit");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/categories?error=edit");
        }
    }
    
    private void handleDelete(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            int result = categoryDAO.deleteCategory(categoryId);
            
            if (result == DaoResult.OK) {
                response.sendRedirect(request.getContextPath() + "/admin/categories?success=delete");
            } else if (result == DaoResult.IN_USE) {
                response.sendRedirect(request.getContextPath() + "/admin/categories?error=inuse");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/categories?error=delete");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/categories?error=delete");
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
