package com.mycompany.manage_book.servlet;

import com.mycompany.manage_book.dao.CategoryDAO;
import com.mycompany.manage_book.dao.ProductDAO;
import com.mycompany.manage_book.model.Category;
import com.mycompany.manage_book.model.Product;
import com.mycompany.manage_book.model.User;
import com.mycompany.manage_book.util.DaoResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@WebServlet("/admin/products")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class AdminProductServlet extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Kiểm tra quyền admin
        if (!checkAdmin(request, response)) return;
        
        String q = request.getParameter("q");
        Integer categoryId = null;
        try {
            String c = request.getParameter("categoryId");
            if (c != null && !c.isEmpty()) categoryId = Integer.valueOf(c);
        } catch (NumberFormatException e) {
            categoryId = null;
        }
        boolean lowStock = "1".equals(request.getParameter("lowStock"));
        
        // pageSize = 0: lấy toàn bộ kết quả (trang quản trị không phân trang)
        List<Product> products = productDAO.searchProducts(q, categoryId, null, null, false, "id_desc", 1, 0);
        if (lowStock) {
            products.removeIf(p -> p.getStock() > 5);
        }
        List<Category> categories = categoryDAO.getAllCategories();
        
        request.setAttribute("q", q == null ? "" : q.trim());
        request.setAttribute("filterCategoryId", categoryId);
        request.setAttribute("lowStock", lowStock);
        request.setAttribute("products", products);
        request.setAttribute("categories", categories);
        
        request.getRequestDispatcher("/WEB-INF/views/admin-products.jsp").forward(request, response);
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
            throws IOException, ServletException {
        try {
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            BigDecimal price = new BigDecimal(request.getParameter("price"));
            int stock = Integer.parseInt(request.getParameter("stock"));
            if (name == null || name.trim().isEmpty() || price.signum() < 0 || stock < 0) {
                response.sendRedirect(request.getContextPath() + "/admin/products?error=invalid");
                return;
            }
            name = name.trim();
            
            // Xử lý upload ảnh
            String imageUrl = uploadImage(request);
            
            boolean success = productDAO.addProduct(categoryId, name, description, price, imageUrl, stock);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/products?success=add");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/products?error=add");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/products?error=add");
        }
    }
    
    private void handleEdit(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            BigDecimal price = new BigDecimal(request.getParameter("price"));
            int stock = Integer.parseInt(request.getParameter("stock"));
            if (name == null || name.trim().isEmpty() || price.signum() < 0 || stock < 0) {
                response.sendRedirect(request.getContextPath() + "/admin/products?error=invalid");
                return;
            }
            name = name.trim();
            
            // Xử lý upload ảnh mới (nếu có)
            String imageUrl = uploadImage(request);
            
            // Nếu không upload ảnh mới, giữ ảnh cũ
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = request.getParameter("oldImageUrl");
            }
            
            boolean success = productDAO.updateProduct(productId, categoryId, name, description, price, imageUrl, stock);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/products?success=edit");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/products?error=edit");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/products?error=edit");
        }
    }
    
    private void handleDelete(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int result = productDAO.deleteProduct(productId);
            
            if (result == DaoResult.OK) {
                response.sendRedirect(request.getContextPath() + "/admin/products?success=delete");
            } else if (result == DaoResult.IN_USE) {
                response.sendRedirect(request.getContextPath() + "/admin/products?error=inuse");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/products?error=delete");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/products?error=delete");
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
    
    private String uploadImage(HttpServletRequest request) throws IOException, ServletException {
        Part filePart = request.getPart("imageFile");
        
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        
        // Kiểm tra định dạng file
        String fileExtension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = fileName.substring(dotIndex).toLowerCase();
        }
        
        if (!fileExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            return null;
        }
        
        // Tạo tên file unique
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Thư mục images đang chạy (webapp triển khai)
        String realPath = getServletContext().getRealPath("/images");
        File targetDir = new File(realPath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        Path targetFile = targetDir.toPath().resolve(uniqueFileName);
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Khi chạy từ thư mục target/<tên-war> (NetBeans/Maven), chép thêm vào src/main/webapp/images
        // để ảnh không bị mất khi build lại. Bỏ qua nếu không tìm thấy thư mục nguồn.
        try {
            String norm = realPath.replace('\\', '/');
            int idx = norm.indexOf("/target/");
            if (idx > 0) {
                Path srcImages = Paths.get(norm.substring(0, idx), "src", "main", "webapp", "images");
                if (Files.isDirectory(srcImages)) {
                    Files.copy(targetFile, srcImages.resolve(uniqueFileName), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception ignore) {
            // không ảnh hưởng đến chức năng chính
        }
        
        // Trả về đường dẫn tương đối để lưu vào database
        return "images/" + uniqueFileName;
    }
}
