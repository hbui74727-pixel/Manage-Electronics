<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý danh mục - Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        table { width: 100%; border-collapse: collapse; background: white; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #81c784; color: white; }
        .btn-small { padding: 6px 12px; font-size: 14px; margin-right: 5px; }
        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); }
        .modal-content { background-color: white; margin: 10% auto; padding: 30px; width: 80%; max-width: 500px; border-radius: 4px; }
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
        <h1 style="color: #81c784; margin-bottom: 20px;"><i class="fas fa-tags"></i> Quản lý danh mục</h1>

        <!-- Messages -->
        <c:if test="${param.success == 'add'}">
            <div class="alert alert-success">Thêm danh mục thành công!</div>
        </c:if>
        <c:if test="${param.success == 'edit'}">
            <div class="alert alert-success">Cập nhật danh mục thành công!</div>
        </c:if>
        <c:if test="${param.success == 'delete'}">
            <div class="alert alert-success">Xóa danh mục thành công!</div>
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

        <button class="btn" onclick="openAddModal()" style="margin-bottom: 20px;"><i class="fas fa-plus"></i> Thêm danh mục mới</button>

        <form method="get" action="${pageContext.request.contextPath}/admin/categories" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center; margin-bottom: 20px; background: white; padding: 15px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
            <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Tìm theo mã, tên hoặc mô tả danh mục..." style="flex: 1; min-width: 220px; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
            <button type="submit" class="btn btn-small"><i class="fas fa-search"></i> Tìm kiếm</button>
            <a href="${pageContext.request.contextPath}/admin/categories" class="btn btn-small btn-secondary" style="text-decoration: none;">Xóa lọc</a>
        </form>

        <!-- Categories Table -->
        <div style="background: white; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow-x: auto;">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên danh mục</th>
                        <th>Mô tả</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="category" items="${categories}">
                        <tr>
                            <td>${category.categoryId}</td>
                            <td><c:out value="${category.name}"/></td>
                            <td><c:out value="${category.description}"/></td>
                            <td>
                                <button class="btn btn-small" data-id="${category.categoryId}" data-name="<c:out value='${category.name}'/>" data-description="<c:out value='${category.description}'/>" onclick="editFromButton(this)">Sửa</button>
                                <button class="btn btn-small btn-secondary" data-id="${category.categoryId}" data-name="<c:out value='${category.name}'/>" onclick="deleteFromButton(this)">Xóa</button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty categories}">
                        <tr><td colspan="4" style="text-align: center; color: #666;">Không tìm thấy danh mục nào.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Add Modal -->
    <div id="addModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeAddModal()">&times;</span>
            <h2 style="color: #81c784; margin-bottom: 20px;">Thêm danh mục mới</h2>
            <form action="${pageContext.request.contextPath}/admin/categories" method="post">
                <input type="hidden" name="action" value="add">
                <div class="form-group">
                    <label>Tên danh mục</label>
                    <input type="text" name="name" required>
                </div>
                <div class="form-group">
                    <label>Mô tả</label>
                    <textarea name="description" rows="4" style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px;"></textarea>
                </div>
                <button type="submit" class="btn">Thêm danh mục</button>
            </form>
        </div>
    </div>

    <!-- Edit Modal -->
    <div id="editModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeEditModal()">&times;</span>
            <h2 style="color: #81c784; margin-bottom: 20px;">Sửa danh mục</h2>
            <form action="${pageContext.request.contextPath}/admin/categories" method="post">
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="categoryId" id="editCategoryId">
                <div class="form-group">
                    <label>Tên danh mục</label>
                    <input type="text" name="name" id="editName" required>
                </div>
                <div class="form-group">
                    <label>Mô tả</label>
                    <textarea name="description" id="editDescription" rows="4" style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px;"></textarea>
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
        function openEditModal(id, name, description) {
            document.getElementById('editCategoryId').value = id;
            document.getElementById('editName').value = name;
            document.getElementById('editDescription').value = description;
            document.getElementById('editModal').style.display = 'block';
        }
        function editFromButton(b) { openEditModal(b.dataset.id, b.dataset.name, b.dataset.description); }
        function deleteFromButton(b) { deleteCategory(b.dataset.id, b.dataset.name); }
        function closeEditModal() {
            document.getElementById('editModal').style.display = 'none';
        }
        function deleteCategory(id, name) {
            if (confirm('Bạn có chắc muốn xóa danh mục "' + name + '"?\nLưu ý: Các sản phẩm thuộc danh mục này có thể bị ảnh hưởng.')) {
                var form = document.createElement('form');
                form.method = 'POST';
                form.action = '${pageContext.request.contextPath}/admin/categories';
                form.innerHTML = '<input type="hidden" name="action" value="delete"><input type="hidden" name="categoryId" value="' + id + '">';
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
