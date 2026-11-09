<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang chủ - Manage Electronics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <!-- Thêm Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-container">
            <a href="${pageContext.request.contextPath}/home" class="logo"><i class="fas fa-laptop"></i> Manage Electronics</a>
            <nav class="nav-menu">
                <a href="${pageContext.request.contextPath}/home">Trang chủ</a>
                <c:choose>
                    <c:when test="${not empty sessionScope.user}">
                        <span class="user-info">Xin chào, <c:out value="${sessionScope.user.fullName}"/></span>
                        <a href="${pageContext.request.contextPath}/profile">Tài khoản</a>
                        <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                        <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
                    </c:otherwise>
                </c:choose>
            </nav>
        </div>
    </header>

    <!-- Main Content -->
    <div class="container">
        <div class="hero-section">
            <h1>Chào mừng đến với Manage Electronics</h1>
            <p>Hệ thống quản lý và mua bán thiết bị điện tử trực tuyến</p>
            <c:if test="${empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/register" class="btn" style="display: inline-block; width: auto; padding: 12px 40px;">
                    Bắt đầu ngay
                </a>
            </c:if>
        </div>

        <div class="features">
            <div class="feature-card">
                <h3><i class="fas fa-microchip"></i> Đa dạng sản phẩm</h3>
                <p>Hàng ngàn sản phẩm điện tử từ nhiều danh mục khác nhau</p>
            </div>
            <div class="feature-card">
                <h3><i class="fas fa-credit-card"></i> Thanh toán dễ dàng</h3>
                <p>Nhiều phương thức thanh toán an toàn và tiện lợi</p>
            </div>
            <div class="feature-card">
                <h3><i class="fas fa-shipping-fast"></i> Giao hàng nhanh</h3>
                <p>Giao hàng toàn quốc, nhanh chóng và đáng tin cậy</p>
            </div>
            <div class="feature-card">
                <h3><i class="fas fa-star"></i> Đánh giá chất lượng</h3>
                <p>Hệ thống đánh giá và nhận xét từ người dùng</p>
            </div>
        </div>
    </div>
</body>
</html>