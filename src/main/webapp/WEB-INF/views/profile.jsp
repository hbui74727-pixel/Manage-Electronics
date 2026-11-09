<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông tin cá nhân - Manage Electronics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-container">
            <a href="${pageContext.request.contextPath}/home" class="logo">💻 Manage Electronics</a>
            <nav class="nav-menu">
                <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
                <span class="user-info">Xin chào, <c:out value="${sessionScope.user.fullName}"/></span>
                <a href="${pageContext.request.contextPath}/profile">Tài khoản</a>
                <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
            </nav>
        </div>
    </header>

    <!-- Main Content -->
    <div class="container">
        <div class="profile-container">
            <div class="profile-header">
                <h2><i class="fas fa-user-circle"></i> Thông tin cá nhân</h2>
                <p style="color: #666;">Quản lý thông tin tài khoản của bạn</p>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>
            
            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>

            <div class="profile-info">
                <div class="info-row">
                    <div class="info-label">Tên đăng nhập:</div>
                    <div class="info-value"><c:out value="${sessionScope.user.username}"/></div>
                </div>
                <div class="info-row">
                    <div class="info-label">Vai trò:</div>
                    <div class="info-value">
                        <c:choose>
                            <c:when test="${sessionScope.user.role == 1}">
                                <span style="color: #81c784; font-weight: bold;">Quản trị viên</span>
                            </c:when>
                            <c:otherwise>
                                Khách hàng
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="info-row">
                    <div class="info-label">Ngày tạo:</div>
                    <div class="info-value">${sessionScope.user.createdAt}</div>
                </div>
            </div>

            <h3 style="margin-bottom: 20px; color: #81c784;">Cập nhật thông tin</h3>
            
            <form action="${pageContext.request.contextPath}/profile" method="post">
                <div class="form-group">
                    <label for="fullName"><i class="fas fa-id-card"></i> Họ và tên</label>
                    <input type="text" id="fullName" name="fullName" value="<c:out value="${sessionScope.user.fullName}"/>" required>
                </div>
                
                <div class="form-group">
                    <label for="email"><i class="fas fa-envelope"></i> Email</label>
                    <input type="email" id="email" name="email" value="<c:out value="${sessionScope.user.email}"/>" required>
                </div>
                
                <button type="submit" class="btn"><i class="fas fa-save"></i> Cập nhật thông tin</button>
            </form>
        </div>
    </div>
</body>
</html>
