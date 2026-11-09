<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý người dùng - Admin</title>
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
        <h1 style="color: #81c784; margin-bottom: 20px;"><i class="fas fa-users"></i> Quản lý người dùng</h1>

        <!-- Messages -->
        <c:if test="${param.success == 'edit'}"><div class="alert alert-success">Cập nhật người dùng thành công!</div></c:if>
        <c:if test="${param.success == 'password'}"><div class="alert alert-success">Đặt lại mật khẩu thành công!</div></c:if>
        <c:if test="${param.success == 'delete'}"><div class="alert alert-success">Xóa người dùng thành công!</div></c:if>
        <c:if test="${param.error != null}">
            <div class="alert alert-error">
                <c:choose>
                    <c:when test="${param.error == 'inuse'}">Không thể xóa: người dùng này đã có đơn hàng hoặc đánh giá.</c:when>
                    <c:when test="${param.error == 'self'}">Bạn không thể xóa hoặc hạ quyền chính tài khoản đang đăng nhập.</c:when>
                    <c:when test="${param.error == 'email'}">Email đã được sử dụng bởi tài khoản khác.</c:when>
                    <c:when test="${param.error == 'password'}">Mật khẩu mới phải có ít nhất 6 ký tự.</c:when>
                    <c:when test="${param.error == 'invalid'}">Dữ liệu nhập không hợp lệ! Vui lòng kiểm tra lại.</c:when>
                    <c:otherwise>Có lỗi xảy ra! Vui lòng thử lại.</c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <!-- Tìm kiếm -->
        <form method="get" action="${pageContext.request.contextPath}/admin/users" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center; margin-bottom: 20px; background: white; padding: 15px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
            <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Tìm theo mã, tên đăng nhập, họ tên, email..." style="flex: 1; min-width: 220px; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
            <select name="role" style="padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                <option value="">Tất cả vai trò</option>
                <option value="0" ${roleFilter == 0 ? 'selected' : ''}>Khách hàng</option>
                <option value="1" ${roleFilter == 1 ? 'selected' : ''}>Quản trị viên</option>
            </select>
            <button type="submit" class="btn btn-small"><i class="fas fa-search"></i> Tìm kiếm</button>
            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-small btn-secondary" style="text-decoration: none;">Xóa lọc</a>
        </form>

        <p style="margin-bottom: 10px; color: #666;">Tìm thấy <strong>${users.size()}</strong> người dùng.</p>

        <!-- Users Table -->
        <div style="background: white; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow-x: auto;">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên đăng nhập</th>
                        <th>Họ tên</th>
                        <th>Email</th>
                        <th>Vai trò</th>
                        <th>Ngày tạo</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${users}">
                        <tr>
                            <td>${u.userId}</td>
                            <td><strong><c:out value="${u.username}"/></strong></td>
                            <td><c:out value="${u.fullName}"/></td>
                            <td><c:out value="${u.email}"/></td>
                            <td>${u.role == 1 ? 'Quản trị viên' : 'Khách hàng'}</td>
                            <td><fmt:formatDate value="${u.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                            <td>
                                <button class="btn btn-small" data-id="${u.userId}" data-name="<c:out value='${u.fullName}'/>" data-email="<c:out value='${u.email}'/>" data-role="${u.role}" onclick="editFromButton(this)"><i class="fas fa-edit"></i> Sửa</button>
                                <button class="btn btn-small" data-id="${u.userId}" data-username="<c:out value='${u.username}'/>" onclick="resetFromButton(this)"><i class="fas fa-key"></i> Đặt lại MK</button>
                                <c:if test="${u.userId != sessionScope.user.userId}">
                                    <button class="btn btn-small btn-secondary" data-id="${u.userId}" data-username="<c:out value='${u.username}'/>" onclick="deleteFromButton(this)"><i class="fas fa-trash"></i> Xóa</button>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty users}">
                        <tr><td colspan="7" style="text-align: center; color: #666;">Không tìm thấy người dùng nào.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Edit Modal -->
    <div id="editModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('editModal')">&times;</span>
            <h2 style="color: #81c784; margin-bottom: 20px;">Sửa người dùng</h2>
            <form action="${pageContext.request.contextPath}/admin/users" method="post">
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="userId" id="editUserId">
                <div class="form-group">
                    <label>Họ tên</label>
                    <input type="text" name="fullName" id="editFullName" required>
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" name="email" id="editEmail" required>
                </div>
                <div class="form-group">
                    <label>Vai trò</label>
                    <select name="role" id="editRole" style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px;">
                        <option value="0">Khách hàng</option>
                        <option value="1">Quản trị viên</option>
                    </select>
                </div>
                <button type="submit" class="btn">Cập nhật</button>
            </form>
        </div>
    </div>

    <!-- Reset password Modal -->
    <div id="resetModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('resetModal')">&times;</span>
            <h2 style="color: #81c784; margin-bottom: 20px;">Đặt lại mật khẩu</h2>
            <p id="resetLabel" style="margin-bottom: 15px;"></p>
            <form action="${pageContext.request.contextPath}/admin/users" method="post">
                <input type="hidden" name="action" value="reset_password">
                <input type="hidden" name="userId" id="resetUserId">
                <div class="form-group">
                    <label>Mật khẩu mới (tối thiểu 6 ký tự)</label>
                    <input type="password" name="newPassword" minlength="6" required>
                </div>
                <button type="submit" class="btn">Đặt lại</button>
            </form>
        </div>
    </div>

    <script>
        function closeModal(id) { document.getElementById(id).style.display = 'none'; }
        function editFromButton(b) {
            var d = b.dataset;
            document.getElementById('editUserId').value = d.id;
            document.getElementById('editFullName').value = d.name;
            document.getElementById('editEmail').value = d.email;
            document.getElementById('editRole').value = d.role;
            document.getElementById('editModal').style.display = 'block';
        }
        function resetFromButton(b) {
            document.getElementById('resetUserId').value = b.dataset.id;
            document.getElementById('resetLabel').textContent = 'Tài khoản: ' + b.dataset.username;
            document.getElementById('resetModal').style.display = 'block';
        }
        function deleteFromButton(b) {
            if (confirm('Bạn có chắc muốn xóa người dùng "' + b.dataset.username + '"?')) {
                var form = document.createElement('form');
                form.method = 'POST';
                form.action = '${pageContext.request.contextPath}/admin/users';
                form.innerHTML = '<input type="hidden" name="action" value="delete"><input type="hidden" name="userId" value="' + b.dataset.id + '">';
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
