<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách yêu thích</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
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
        <h1 style="color: #81c784; margin-bottom: 20px;"><i class="fas fa-heart"></i> Danh sách yêu thích</h1>

        <!-- Messages -->
        <c:if test="${param.success == 'remove'}">
            <div class="alert alert-success">Đã xóa khỏi danh sách yêu thích!</div>
        </c:if>

        <c:choose>
            <c:when test="${empty wishlists}">
                <div class="card" style="text-align: center; padding: 40px;">
                    <i class="fas fa-heart" style="font-size: 64px; color: #ddd; margin-bottom: 20px;"></i>
                    <p style="font-size: 18px; color: #666;">Danh sách yêu thích của bạn đang trống</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn" style="margin-top: 20px;">
                        <i class="fas fa-shopping-bag"></i> Tiếp tục mua sắm
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 20px;">
                    <c:forEach var="item" items="${wishlists}">
                        <div class="feature-card" style="display: flex; flex-direction: column; height: 100%;">
                            <a href="${pageContext.request.contextPath}/product-detail?id=${item.productId}" style="text-decoration: none; color: inherit; flex: 1; display: flex; flex-direction: column;">
                                <img src="${pageContext.request.contextPath}/${item.productImageUrl}" 
                                     alt="<c:out value="${item.productName}"/>"
                                     style="width: 100%; height: 200px; object-fit: cover; border-radius: 4px; margin-bottom: 15px;"
                                     onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/no-image.png';">
                                <h3 style="color: #333; margin-bottom: 10px; font-size: 16px; height: 48px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.5;">
                                    <c:out value="${item.productName}"/>
                                </h3>
                                <p style="color: #81c784; font-size: 18px; font-weight: bold; margin-bottom: 10px;">
                                    <fmt:formatNumber value="${item.productPrice}" pattern="#,##0"/> VNĐ
                                </p>
                                <p style="color: ${item.productStock > 0 ? '#666' : '#e57373'}; font-size: 14px; margin-bottom: 15px;">
                                    <c:choose>
                                        <c:when test="${item.productStock > 0}">
                                            Còn hàng: ${item.productStock}
                                        </c:when>
                                        <c:otherwise>
                                            Hết hàng
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </a>
                            <div style="display: flex; gap: 10px; margin-top: auto;">
                                <a href="${pageContext.request.contextPath}/product-detail?id=${item.productId}" class="btn" style="flex: 1; text-align: center; text-decoration: none;">
                                    <i class="fas fa-eye"></i> Xem
                                </a>
                                <form method="post" action="${pageContext.request.contextPath}/wishlist" style="flex: 1;">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="wishlistId" value="${item.wishlistId}">
                                    <button type="submit" class="btn btn-secondary" style="width: 100%;">
                                        <i class="fas fa-trash"></i> Xóa
                                    </button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
