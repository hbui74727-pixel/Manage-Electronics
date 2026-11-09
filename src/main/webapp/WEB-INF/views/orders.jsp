<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đơn hàng của tôi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .order-card { background: white; padding: 20px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .order-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ddd; padding-bottom: 15px; margin-bottom: 15px; }
        .status-badge { padding: 5px 15px; border-radius: 4px; font-size: 14px; font-weight: bold; }
        .status-0 { background: #fff3cd; color: #856404; }
        .status-1 { background: #cfe2ff; color: #084298; }
        .status-2 { background: #d1e7dd; color: #0f5132; }
        .status-3 { background: #f8d7da; color: #842029; }
        .btn-cancel {
            background: #e57373;
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }
        .btn-cancel:hover { background: #ef5350; }
    </style>
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-container">
            <a href="${pageContext.request.contextPath}/products" class="logo"><i class="fas fa-laptop"></i> Manage Electronics</a>
            <nav class="nav-menu">
                <a href="${pageContext.request.contextPath}/products"><i class="fas fa-home"></i> Trang chủ</a>
                <a href="${pageContext.request.contextPath}/wishlist"><i class="fas fa-heart"></i> Yêu thích</a>
                <a href="${pageContext.request.contextPath}/cart"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>
                <a href="${pageContext.request.contextPath}/orders"><i class="fas fa-box"></i> Đơn hàng</a>
                <c:if test="${sessionScope.user.role == 1}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard"><i class="fas fa-user-shield"></i> Admin</a>
                </c:if>
                <a href="${pageContext.request.contextPath}/profile"><i class="fas fa-user"></i> <c:out value="${sessionScope.user.fullName}"/></a>
                <a href="${pageContext.request.contextPath}/logout"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a>
            </nav>
        </div>
    </header>

    <div class="container">
        <h1 style="color: #81c784; margin-bottom: 20px;"><i class="fas fa-box"></i> Đơn hàng của tôi</h1>

        <!-- Messages -->
        <c:if test="${param.success == 'order'}">
            <div class="alert alert-success">Đặt hàng thành công!</div>
        </c:if>
        <c:if test="${param.success == 'cancel'}">
            <div class="alert alert-success">Đã hủy đơn hàng!</div>
        </c:if>
        <c:if test="${param.error == 'cancel'}">
            <div class="alert alert-error">Không thể hủy đơn hàng này!</div>
        </c:if>

        <c:choose>
            <c:when test="${empty orders}">
                <div class="card" style="text-align: center; padding: 40px;">
                    <i class="fas fa-box" style="font-size: 64px; color: #ddd; margin-bottom: 20px;"></i>
                    <p style="font-size: 18px; color: #666;">Bạn chưa có đơn hàng nào</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn" style="margin-top: 20px; display: inline-block; text-decoration: none;">
                        <i class="fas fa-shopping-bag"></i> Mua sắm ngay
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach var="order" items="${orders}">
                    <div class="order-card">
                        <div class="order-header">
                            <div>
                                <strong>Đơn hàng #${order.orderId}</strong>
                                <p style="color: #666; font-size: 14px; margin-top: 5px;">${order.createdAt}</p>
                            </div>
                            <span class="status-badge status-${order.status}">${order.statusText}</span>
                        </div>
                        
                        <div style="margin-bottom: 15px;">
                            <p><strong>Người nhận:</strong> <c:out value="${order.receiverName}"/></p>
                            <p><strong>Số điện thoại:</strong> <c:out value="${order.receiverPhone}"/></p>
                            <p><strong>Địa chỉ:</strong> <c:out value="${order.shippingAddress}"/></p>
                            <c:if test="${not empty order.voucherCode}">
                                <p><strong>Mã giảm giá:</strong> <c:out value="${order.voucherCode}"/></p>
                            </c:if>
                        </div>
                        
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong style="font-size: 18px; color: #81c784;">Tổng: <fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> VNĐ</strong>
                            </div>
                            <div style="display: flex; gap: 10px;">
                                <a href="${pageContext.request.contextPath}/orders?orderId=${order.orderId}" class="btn" style="text-decoration: none; padding: 8px 16px;">
                                    <i class="fas fa-eye"></i> Chi tiết
                                </a>
                                <c:if test="${order.status == 0}">
                                    <form method="post" action="${pageContext.request.contextPath}/orders" style="display: inline;">
                                        <input type="hidden" name="action" value="cancel">
                                        <input type="hidden" name="orderId" value="${order.orderId}">
                                        <button type="submit" class="btn-cancel" style="padding: 8px 16px;" onclick="return confirm('Bạn có chắc muốn hủy đơn hàng này?')">
                                            <i class="fas fa-times"></i> Hủy đơn
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
