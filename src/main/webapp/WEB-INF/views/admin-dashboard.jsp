<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
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
                <a href="${pageContext.request.contextPath}/products"><i class="fas fa-home"></i> Trang chủ</a>
                <span class="user-info"><i class="fas fa-user"></i> <c:out value="${sessionScope.user.fullName}"/></span>
                <a href="${pageContext.request.contextPath}/logout"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a>
            </nav>
        </div>
    </header>

    <!-- Main Content -->
    <div class="container">
        <h1 style="color: #81c784; margin-bottom: 30px;"><i class="fas fa-chart-line"></i> Dashboard Tổng Quan</h1>

        <!-- Statistics Cards -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 40px;">
            <div class="feature-card" style="background: linear-gradient(135deg, #81c784 0%, #66bb6a 100%); color: white;">
                <h3 style="color: white; font-size: 48px; margin-bottom: 10px;">${totalProducts}</h3>
                <p style="color: white; font-size: 18px;"><i class="fas fa-box"></i> Tổng sản phẩm</p>
            </div>

            <div class="feature-card" style="background: linear-gradient(135deg, #64b5f6 0%, #42a5f5 100%); color: white;">
                <h3 style="color: white; font-size: 48px; margin-bottom: 10px;">${totalOrders}</h3>
                <p style="color: white; font-size: 18px;"><i class="fas fa-shopping-cart"></i> Tổng đơn hàng</p>
            </div>

            <div class="feature-card" style="background: linear-gradient(135deg, #ffb74d 0%, #ffa726 100%); color: white;">
                <h3 style="color: white; font-size: 48px; margin-bottom: 10px;">${totalUsers}</h3>
                <p style="color: white; font-size: 18px;"><i class="fas fa-users"></i> Tổng người dùng</p>
            </div>

            <div class="feature-card" style="background: linear-gradient(135deg, #e57373 0%, #ef5350 100%); color: white;">
                <h3 style="color: white; font-size: 32px; margin-bottom: 10px;"><fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/> VNĐ</h3>
                <p style="color: white; font-size: 18px;"><i class="fas fa-dollar-sign"></i> Tổng doanh thu</p>
            </div>
        </div>

        <!-- Quick Actions -->
        <div style="background: white; padding: 30px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
            <h2 style="color: #81c784; margin-bottom: 20px;"><i class="fas fa-bolt"></i> Thao tác nhanh</h2>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px;">
                <a href="${pageContext.request.contextPath}/admin/products" class="btn" style="text-decoration: none;"><i class="fas fa-box"></i> Quản lý sản phẩm</a>
                <a href="${pageContext.request.contextPath}/admin/categories" class="btn" style="text-decoration: none;" ><i class="fas fa-tags"></i> Quản lý danh mục</a>
                <a href="${pageContext.request.contextPath}/admin/orders" class="btn" style="text-decoration: none;"><i class="fas fa-shopping-bag"></i> Quản lý đơn hàng</a>
                <a href="${pageContext.request.contextPath}/admin/vouchers" class="btn" style="text-decoration: none;"><i class="fas fa-ticket-alt"></i> Quản lý voucher</a>
                <a href="${pageContext.request.contextPath}/admin/users" class="btn" style="text-decoration: none;"><i class="fas fa-users"></i> Quản lý người dùng</a>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary" style="text-decoration: none;"><i class="fas fa-eye"></i> Xem trang chủ</a>
            </div>
        </div>
    </div>
</body>
</html>
