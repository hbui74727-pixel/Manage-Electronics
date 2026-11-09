<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý sản phẩm - Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        table { width: 100%; border-collapse: collapse; background: white; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #81c784; color: white; }
        .btn-small { padding: 6px 12px; font-size: 14px; margin-right: 5px; }
        .modal { 
            display: none; 
            position: fixed; 
            z-index: 1000; 
            left: 0; 
            top: 0; 
            width: 100%; 
            height: 100%; 
            background-color: rgba(0,0,0,0.5); 
            overflow-y: auto;
        }
        .modal-content { 
            background-color: white; 
            margin: 30px auto; 
            padding: 30px; 
            width: 80%; 
            max-width: 600px; 
            border-radius: 4px;
            max-height: calc(100vh - 60px);
            overflow-y: auto;
        }
        .close { color: #aaa; float: right; font-size: 28px; font-weight: bold; cursor: pointer; }
        .close:hover { color: #000; }
    </style>
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-container">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="logo"><i class="fas fa-user-shield"></i> Admin Panel</a>
            <nav class="nav-menu">
                <a href="${pageContext.request.contextPath}/admin/dashboard"><i class="fas fa-tachometer-alt"></i> Dashboard</a>
                <a href="${pageContext.request.contextPath}/admin/products"><i class="fas fa-box"></i> Sản phẩm</a>
                <a href="${pageContext.request.contextPath}/admin/categories"><i class="fas fa-tags"></i> Danh mục</a>
                <a href="${pageContext.request.contextPath}/admin/vouchers"><i class="fas fa-ticket-alt"></i> Voucher</a>
                <a href="${pageContext.request.contextPath}/admin/orders"><i class="fas fa-shopping-bag"></i> Đơn hàng</a>
                <a href="${pageContext.request.contextPath}/admin/users"><i class="fas fa-users"></i> Người dùng</a>
                <a href="${pageContext.request.contextPath}/products"><i class="fas fa-home"></i> Trang chủ</a>
                <span class="user-info"><i class="fas fa-user"></i> <c:out value="${sessionScope.user.fullName}"/></span>
                <a href="${pageContext.request.contextPath}/logout"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a>
            </nav>
        </div>
    </header>

    <div class="container">
        <h1 style="color: #81c784; margin-bottom: 20px;"><i class="fas fa-box"></i> Quản lý sản phẩm</h1>

        <!-- Messages -->
        <c:if test="${param.success == 'add'}">
            <div class="alert alert-success">Thêm sản phẩm thành công!</div>
        </c:if>
        <c:if test="${param.success == 'edit'}">
            <div class="alert alert-success">Cập nhật sản phẩm thành công!</div>
        </c:if>
        <c:if test="${param.success == 'delete'}">
            <div class="alert alert-success">Xóa sản phẩm thành công!</div>
        </c:if>
        <c:if test="${param.error != null}">
            <div class="alert alert-error">
                <c:choose>
                    <c:when test="${param.error == 'inuse'}">Không thể xóa vì dữ liệu này đang được sử dụng (đơn hàng, sản phẩm...). Hãy chỉnh sửa/vô hiệu hóa thay vì xóa.</c:when>
                    <c:when test="${param.error == 'invalid'}">Dữ liệu nhập không hợp lệ! Vui lòng kiểm tra lại.</c:when>
                    <c:otherwise>Có lỗi xảy ra! Vui lòng thử lại.</c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <button class="btn" onclick="openAddModal()" style="margin-bottom: 20px;"><i class="fas fa-plus"></i> Thêm sản phẩm mới</button>

        <form method="get" action="${pageContext.request.contextPath}/admin/products" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center; margin-bottom: 20px; background: white; padding: 15px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
            <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Tìm theo mã, tên, mô tả hoặc danh mục..." style="flex: 1; min-width: 220px; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
            <select name="categoryId" style="padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                <option value="">Tất cả danh mục</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${cat.categoryId}" ${filterCategoryId == cat.categoryId ? 'selected' : ''}><c:out value="${cat.name}"/></option>
                </c:forEach>
            </select>
            <label style="display: flex; align-items: center; gap: 6px;"><input type="checkbox" name="lowStock" value="1" ${lowStock ? 'checked' : ''}> Sắp hết hàng (≤ 5)</label>
            <button type="submit" class="btn btn-small"><i class="fas fa-search"></i> Tìm kiếm</button>
            <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-small btn-secondary" style="text-decoration: none;">Xóa lọc</a>
        </form>

        <p style="margin-bottom: 10px; color: #666;">Tìm thấy <strong>${products.size()}</strong> sản phẩm.</p>

        <!-- Products Table -->
        <div style="background: white; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow-x: auto;">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên sản phẩm</th>
                        <th>Danh mục</th>
                        <th>Giá</th>
                        <th>Tồn kho</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="product" items="${products}">
                        <tr>
                            <td>${product.productId}</td>
                            <td><c:out value="${product.name}"/></td>
                            <td><c:out value="${product.categoryName}"/></td>
                            <td><fmt:formatNumber value="${product.price}" pattern="#,##0"/> VNĐ</td>
                            <td>${product.stock}</td>
                            <td>
                                <button class="btn btn-small" data-id="${product.productId}" data-name="<c:out value='${product.name}'/>" data-description="<c:out value='${product.description}'/>" data-category="${product.categoryId}" data-price="${product.price}" data-image="<c:out value='${product.imageUrl}'/>" data-stock="${product.stock}" onclick="editFromButton(this)"><i class="fas fa-edit"></i> Sửa</button>
                                <button class="btn btn-small btn-secondary" data-id="${product.productId}" data-name="<c:out value='${product.name}'/>" onclick="deleteFromButton(this)"><i class="fas fa-trash"></i> Xóa</button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty products}">
                        <tr><td colspan="6" style="text-align: center; color: #666;">Không tìm thấy sản phẩm nào.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Add Modal -->
    <div id="addModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeAddModal()">&times;</span>
            <h2 style="color: #81c784; margin-bottom: 20px;">Thêm sản phẩm mới</h2>
            <form action="${pageContext.request.contextPath}/admin/products" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="add">
                <div class="form-group">
                    <label>Tên sản phẩm</label>
                    <input type="text" name="name" required>
                </div>
                <div class="form-group">
                    <label>Danh mục</label>
                    <select name="categoryId" required style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px;">
                        <c:forEach var="category" items="${categories}">
                            <option value="${category.categoryId}"><c:out value="${category.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label>Mô tả</label>
                    <textarea name="description" rows="4" style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px;"></textarea>
                </div>
                <div class="form-group">
                    <label>Giá (VNĐ)</label>
                    <input type="number" name="price" step="0.01" required>
                </div>
                <div class="form-group">
                    <label>Hình ảnh sản phẩm</label>
                    <input type="file" name="imageFile" accept="image/*" required style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px;">
                    <small style="color: #666;">Chấp nhận: JPG, JPEG, PNG, GIF, WEBP (Tối đa 10MB)</small>
                </div>
                <div class="form-group">
                    <label>Số lượng tồn kho</label>
                    <input type="number" name="stock" required>
                </div>
                <button type="submit" class="btn">Thêm sản phẩm</button>
            </form>
        </div>
    </div>

    <!-- Edit Modal -->
    <div id="editModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeEditModal()">&times;</span>
            <h2 style="color: #81c784; margin-bottom: 20px;">Sửa sản phẩm</h2>
            <form action="${pageContext.request.contextPath}/admin/products" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="productId" id="editProductId">
                <input type="hidden" name="oldImageUrl" id="editOldImageUrl">
                <div class="form-group">
                    <label>Tên sản phẩm</label>
                    <input type="text" name="name" id="editName" required>
                </div>
                <div class="form-group">
                    <label>Danh mục</label>
                    <select name="categoryId" id="editCategoryId" required style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px;">
                        <c:forEach var="category" items="${categories}">
                            <option value="${category.categoryId}"><c:out value="${category.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label>Mô tả</label>
                    <textarea name="description" id="editDescription" rows="4" style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px;"></textarea>
                </div>
                <div class="form-group">
                    <label>Giá (VNĐ)</label>
                    <input type="number" name="price" id="editPrice" step="0.01" required>
                </div>
                <div class="form-group">
                    <label>Hình ảnh hiện tại</label>
                    <div style="margin-bottom: 10px;">
                        <img id="editCurrentImage" src="" alt="Current Image" style="max-width: 200px; max-height: 200px; border: 1px solid #ddd; border-radius: 4px;">
                    </div>
                    <label>Thay đổi hình ảnh (để trống nếu không muốn thay đổi)</label>
                    <input type="file" name="imageFile" accept="image/*" style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px;">
                    <small style="color: #666;">Chấp nhận: JPG, JPEG, PNG, GIF, WEBP (Tối đa 10MB)</small>
                </div>
                <div class="form-group">
                    <label>Số lượng tồn kho</label>
                    <input type="number" name="stock" id="editStock" required>
                </div>
                <button type="submit" class="btn">Cập nhật</button>
            </form>
        </div>
    </div>

    <script>
        function openAddModal() {
            document.getElementById('addModal').style.display = 'block';
        }
        function closeAddModal() {
            document.getElementById('addModal').style.display = 'none';
        }
        function openEditModal(id, name, description, categoryId, price, imageUrl, stock) {
            document.getElementById('editProductId').value = id;
            document.getElementById('editName').value = name;
            document.getElementById('editDescription').value = description;
            document.getElementById('editCategoryId').value = categoryId;
            document.getElementById('editPrice').value = price;
            document.getElementById('editOldImageUrl').value = imageUrl;
            document.getElementById('editStock').value = stock;
            
            // Hiển thị ảnh hiện tại
            var imgElement = document.getElementById('editCurrentImage');
            imgElement.src = '${pageContext.request.contextPath}/' + imageUrl;
            imgElement.onerror = function() {
                this.onerror = null;
                this.src = '${pageContext.request.contextPath}/images/no-image.png';
            };
            
            document.getElementById('editModal').style.display = 'block';
        }
        function closeEditModal() {
            document.getElementById('editModal').style.display = 'none';
        }
        function editFromButton(b) {
            var d = b.dataset;
            openEditModal(d.id, d.name, d.description, d.category, d.price, d.image, d.stock);
        }
        function deleteFromButton(b) { deleteProduct(b.dataset.id, b.dataset.name); }
        function deleteProduct(id, name) {
            if (confirm('Bạn có chắc muốn xóa sản phẩm "' + name + '"?')) {
                var form = document.createElement('form');
                form.method = 'POST';
                form.action = '${pageContext.request.contextPath}/admin/products';
                form.innerHTML = '<input type="hidden" name="action" value="delete"><input type="hidden" name="productId" value="' + id + '">';
                document.body.appendChild(form);
                form.submit();
            }
        }
        window.onclick = function(event) {
            if (event.target.className === 'modal') {
                event.target.style.display = 'none';
            }
        }
    </script>
</body>
</html>
