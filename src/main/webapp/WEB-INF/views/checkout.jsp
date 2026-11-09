<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-container">
            <a href="${pageContext.request.contextPath}/products" class="logo"><i class="fas fa-laptop"></i> Electronics Store</a>
            <nav class="nav-menu">
                <a href="${pageContext.request.contextPath}/products"><i class="fas fa-home"></i> Trang chủ</a>
                <a href="${pageContext.request.contextPath}/cart"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>
                <a href="${pageContext.request.contextPath}/profile"><i class="fas fa-user"></i> <c:out value="${sessionScope.user.fullName}"/></a>
            </nav>
        </div>
    </header>

    <div class="container">
        <h1 style="color: #81c784; margin-bottom: 20px;"><i class="fas fa-credit-card"></i> Thanh toán</h1>

        <c:if test="${param.error == 'stock'}"><div class="alert alert-error">Một số sản phẩm không đủ tồn kho. Vui lòng kiểm tra lại giỏ hàng.</div></c:if>
        <c:if test="${param.error == 'voucher'}"><div class="alert alert-error">Mã giảm giá không còn hiệu lực. Vui lòng nhập lại.</div></c:if>
        <c:if test="${param.error == 'info'}"><div class="alert alert-error">Vui lòng nhập đủ họ tên, địa chỉ và số điện thoại hợp lệ (10-11 số, bắt đầu bằng 0).</div></c:if>
        <c:if test="${not empty voucherError}">
            <div class="alert alert-error">${voucherError}</div>
        </c:if>
        <c:if test="${not empty voucherSuccess}">
            <div class="alert alert-success">${voucherSuccess}</div>
        </c:if>

        <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 20px;">
            <!-- Checkout Form -->
            <div class="card">
                <h2 style="color: #81c784; margin-bottom: 20px;">Thông tin giao hàng</h2>
                <form method="post" action="${pageContext.request.contextPath}/checkout">
                    <input type="hidden" name="action" value="place_order">
                    <input type="hidden" name="voucherCode" value="<c:out value="${voucher.code}"/>">
                    
                    <div class="form-group">
                        <label>Tên người nhận</label>
                        <input type="text" name="receiverName" value="<c:out value="${user.fullName}"/>" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Số điện thoại</label>
                        <input type="tel" name="receiverPhone" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Địa chỉ giao hàng</label>
                        <textarea name="shippingAddress" rows="3" required></textarea>
                    </div>
                    
                    <button type="submit" class="btn" style="width: 100%;">
                        <i class="fas fa-check"></i> Đặt hàng
                    </button>
                </form>
            </div>

            <!-- Order Summary -->
            <div>
                <!-- Voucher -->
                <div class="card" style="margin-bottom: 20px;">
                    <h3 style="color: #81c784; margin-bottom: 15px;">Mã giảm giá</h3>
                    <form method="post" action="${pageContext.request.contextPath}/checkout">
                        <input type="hidden" name="action" value="apply_voucher">
                        <div style="display: flex; gap: 10px;">
                            <input type="text" name="voucherCode" placeholder="Nhập mã giảm giá" 
                                   value="<c:out value="${voucher.code}"/>" style="flex: 1;">
                            <button type="submit" class="btn">Áp dụng</button>
                        </div>
                    </form>
                    <c:if test="${not empty voucher}">
                        <div style="margin-top: 10px; padding: 10px; background: #e8f5e9; border-radius: 4px;">
                            <strong><c:out value="${voucher.code}"/></strong> - Giảm ${voucher.discountPercent}%
                        </div>
                    </c:if>
                </div>

                <!-- Summary -->
                <div class="card">
                    <h3 style="color: #81c784; margin-bottom: 15px;">Đơn hàng</h3>
                    <c:forEach var="item" items="${cartItems}">
                        <div style="display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px;">
                            <span><c:out value="${item.productName}"/> x${item.quantity}</span>
                            <span><fmt:formatNumber value="${item.productPrice * item.quantity}" pattern="#,##0"/> VNĐ</span>
                        </div>
                    </c:forEach>
                    <hr>
                    <div style="display: flex; justify-content: space-between; margin: 10px 0;">
                        <span>Tạm tính:</span>
                        <strong><fmt:formatNumber value="${totalAmount}" pattern="#,##0"/> VNĐ</strong>
                    </div>
                    <c:if test="${not empty discount}">
                        <div style="display: flex; justify-content: space-between; margin: 10px 0; color: #81c784;">
                            <span>Giảm giá:</span>
                            <strong>-<fmt:formatNumber value="${discount}" pattern="#,##0"/> VNĐ</strong>
                        </div>
                    </c:if>
                    <hr>
                    <div style="display: flex; justify-content: space-between; font-size: 18px; margin-top: 15px;">
                        <strong>Tổng cộng:</strong>
                        <strong style="color: #81c784;">
                            <c:choose>
                                <c:when test="${not empty finalAmount}">${finalAmount}</c:when>
                                <c:otherwise>${totalAmount}</c:otherwise>
                            </c:choose> VNĐ
                        </strong>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
