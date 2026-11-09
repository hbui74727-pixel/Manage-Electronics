<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết đơn hàng #${order.orderId}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .order-detail-card { background: white; padding: 20px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .status-badge { padding: 5px 15px; border-radius: 4px; font-size: 14px; font-weight: bold; display: inline-block; }
        .status-0 { background: #fff3cd; color: #856404; }
        .status-1 { background: #cfe2ff; color: #084298; }
        .status-2 { background: #d1e7dd; color: #0f5132; }
        .status-3 { background: #f8d7da; color: #842029; }
        .item-row { display: flex; gap: 15px; padding: 15px; border-bottom: 1px solid #f0f0f0; align-items: center; }
        .item-row:last-child { border-bottom: none; }
    </style>
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-container">
            <a href="${pageContext.request.contextPath}/products" class="logo"><i class="fas fa-laptop"></i> Electronics Store</a>
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
        <div style="margin: 20px 0;">
            <a href="${pageContext.request.contextPath}/orders" style="color: #81c784; text-decoration: none;">
                <i class="fas fa-arrow-left"></i> Quay lại danh sách đơn hàng
            </a>
        </div>

        <h1 style="color: #81c784; margin-bottom: 20px;">
            <i class="fas fa-file-invoice"></i> Chi tiết đơn hàng #${order.orderId}
        </h1>

        <!-- Order Info -->
        <div class="order-detail-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h2 style="color: #81c784; margin: 0;">Thông tin đơn hàng</h2>
                <span class="status-badge status-${order.status}">${order.statusText}</span>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
                <div>
                    <p style="margin-bottom: 10px;"><strong>Ngày đặt:</strong> ${order.createdAt}</p>
                    <p style="margin-bottom: 10px;"><strong>Người nhận:</strong> <c:out value="${order.receiverName}"/></p>
                    <p style="margin-bottom: 10px;"><strong>Số điện thoại:</strong> <c:out value="${order.receiverPhone}"/></p>
                    <p style="margin-bottom: 10px;"><strong>Địa chỉ:</strong> <c:out value="${order.shippingAddress}"/></p>
                </div>
                <div>
                    <c:if test="${not empty order.voucherCode}">
                        <p style="margin-bottom: 10px;"><strong>Mã giảm giá:</strong> <c:out value="${order.voucherCode}"/></p>
                    </c:if>
                    <p style="margin-bottom: 10px; font-size: 24px; color: #81c784;">
                        <strong>Tổng tiền: <fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/> VNĐ</strong>
                    </p>
                </div>
            </div>
        </div>

        <!-- Order Items -->
        <div class="order-detail-card">
            <h2 style="color: #81c784; margin-bottom: 20px;">Sản phẩm trong đơn hàng</h2>
            
            <c:forEach var="item" items="${orderItems}">
                <div class="item-row">
                    <img src="${pageContext.request.contextPath}/${item.productImageUrl}" 
                         alt="<c:out value="${item.productName}"/>"
                         style="width: 80px; height: 100px; object-fit: cover; border-radius: 4px;"
                         onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/no-image.png';">
                    <div style="flex: 1;">
                        <h3 style="margin-bottom: 5px; font-size: 16px;"><c:out value="${item.productName}"/></h3>
                        <p style="color: #666; font-size: 14px;">Số lượng: ${item.quantity}</p>
                        <p style="color: #81c784; font-size: 16px; font-weight: bold;">
                            <fmt:formatNumber value="${item.price}" pattern="#,##0"/> VNĐ x ${item.quantity} = <fmt:formatNumber value="${item.price * item.quantity}" pattern="#,##0"/> VNĐ
                        </p>
                        <c:if test="${order.status == 2}">
                            <c:choose>
                                <c:when test="${item.reviewed}">
                                    <span style="color: #888; font-size: 14px;"><i class="fas fa-check"></i> Đã đánh giá</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/product-detail?id=${item.productId}#reviews"
                                       style="color: #81c784; font-size: 14px;"><i class="fas fa-star"></i> Viết đánh giá</a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
        </div>

        <!-- Actions -->
        <c:if test="${order.status == 0}">
            <div style="text-align: center; margin-top: 20px;">
                <form method="post" action="${pageContext.request.contextPath}/orders" style="display: inline;">
                    <input type="hidden" name="action" value="cancel">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="btn btn-secondary" onclick="return confirm('Bạn có chắc muốn hủy đơn hàng này?')">
                        <i class="fas fa-times"></i> Hủy đơn hàng
                    </button>
                </form>
            </div>
        </c:if>
    </div>
</body>
</html>
