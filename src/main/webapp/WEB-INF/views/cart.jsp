<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giỏ hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .cart-table { width: 100%; border-collapse: collapse; background: white; }
        .cart-table th, .cart-table td { padding: 15px; text-align: left; border-bottom: 1px solid #ddd; }
        .cart-table th { background-color: #81c784; color: white; }
        .cart-table img { width: 80px; height: 100px; object-fit: cover; border-radius: 4px; }
        .quantity-input { width: 60px; padding: 5px; text-align: center; border: 1px solid #ddd; border-radius: 4px; }
        .cart-summary { background: white; padding: 20px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        .btn-delete { 
            background: #e57373; 
            color: white; 
            border: none; 
            padding: 8px 12px; 
            border-radius: 4px; 
            cursor: pointer;
            font-size: 14px;
        }
        .btn-delete:hover { background: #ef5350; }
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
        <h1 style="color: #81c784; margin-bottom: 20px;"><i class="fas fa-shopping-cart"></i> Giỏ hàng</h1>

        <!-- Messages -->
        <c:if test="${param.error == 'stock'}">
            <div class="alert alert-error">Số lượng vượt quá tồn kho của sản phẩm!</div>
        </c:if>
        <c:if test="${param.success == 'add'}">
            <div class="alert alert-success">Đã thêm vào giỏ hàng!</div>
        </c:if>
        <c:if test="${param.success == 'remove'}">
            <div class="alert alert-success">Đã xóa khỏi giỏ hàng!</div>
        </c:if>

        <c:choose>
            <c:when test="${empty cartItems}">
                <div class="card" style="text-align: center; padding: 40px;">
                    <i class="fas fa-shopping-cart" style="font-size: 64px; color: #ddd; margin-bottom: 20px;"></i>
                    <p style="font-size: 18px; color: #666; margin-bottom: 20px;">Giỏ hàng của bạn đang trống</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn" style="display: inline-block; text-decoration: none;">
                        <i class="fas fa-shopping-bag"></i> Tiếp tục mua sắm
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 20px;">
                    <!-- Cart Items -->
                    <div>
                        <table class="cart-table">
                            <thead>
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th>Giá</th>
                                    <th>Số lượng</th>
                                    <th>Tổng</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${cartItems}">
                                    <tr>
                                        <td>
                                            <div style="display: flex; align-items: center; gap: 15px;">
                                                <img src="${pageContext.request.contextPath}/${item.productImageUrl}" 
                                                     alt="<c:out value="${item.productName}"/>"
                                                     onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/no-image.png';">
                                                <div>
                                                    <strong><c:out value="${item.productName}"/></strong>
                                                    <p style="color: #666; font-size: 14px;">Còn: ${item.productStock}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td><fmt:formatNumber value="${item.productPrice}" pattern="#,##0"/> VNĐ</td>
                                        <td>
                                            <form method="post" action="${pageContext.request.contextPath}/cart" style="display: flex; align-items: center; gap: 5px;">
                                                <input type="hidden" name="action" value="update">
                                                <input type="hidden" name="cartItemId" value="${item.cartItemId}">
                                                <input type="number" name="quantity" value="${item.quantity}" 
                                                       min="1" max="${item.productStock}" class="quantity-input"
                                                       onchange="this.form.submit()">
                                            </form>
                                        </td>
                                        <td><strong><fmt:formatNumber value="${item.productPrice * item.quantity}" pattern="#,##0"/> VNĐ</strong></td>
                                        <td>
                                            <form method="post" action="${pageContext.request.contextPath}/cart">
                                                <input type="hidden" name="action" value="remove">
                                                <input type="hidden" name="cartItemId" value="${item.cartItemId}">
                                                <button type="submit" class="btn-delete">
                                                    <i class="fas fa-trash"></i> Xóa
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- Cart Summary -->
                    <div class="cart-summary">
                        <h2 style="color: #81c784; margin-bottom: 20px;">Tổng đơn hàng</h2>
                        <div style="border-bottom: 1px solid #ddd; padding-bottom: 15px; margin-bottom: 15px;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
                                <span>Tạm tính:</span>
                                <strong><fmt:formatNumber value="${totalAmount}" pattern="#,##0"/> VNĐ</strong>
                            </div>
                        </div>
                        <div style="display: flex; justify-content: space-between; font-size: 18px; margin-bottom: 20px;">
                            <strong>Tổng cộng:</strong>
                            <strong style="color: #81c784;"><fmt:formatNumber value="${totalAmount}" pattern="#,##0"/> VNĐ</strong>
                        </div>
                        <a href="${pageContext.request.contextPath}/checkout" class="btn" style="width: 100%; text-align: center; display: block; text-decoration: none;">
                            <i class="fas fa-credit-card"></i> Thanh toán
                        </a>
                        <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary" style="width: 100%; text-align: center; margin-top: 10px; display: block; text-decoration: none;">
                            <i class="fas fa-arrow-left"></i> Tiếp tục mua sắm
                        </a>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
