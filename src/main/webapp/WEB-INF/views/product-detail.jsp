<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${product.name}"/> - Manage Electronics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-container">
            <a href="${pageContext.request.contextPath}/home" class="logo"><i class="fas fa-laptop"></i> Manage Electronics</a>
            <nav class="nav-menu">
                <a href="${pageContext.request.contextPath}/products"><i class="fas fa-home"></i> Trang chủ</a>
                <c:choose>
                    <c:when test="${not empty sessionScope.user}">
                        <a href="${pageContext.request.contextPath}/wishlist"><i class="fas fa-heart"></i> Yêu thích</a>
                        <a href="${pageContext.request.contextPath}/cart"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>
                        <a href="${pageContext.request.contextPath}/orders"><i class="fas fa-box"></i> Đơn hàng</a>
                        <c:if test="${sessionScope.user.role == 1}">
                            <a href="${pageContext.request.contextPath}/admin/dashboard"><i class="fas fa-user-shield"></i> Admin</a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/profile"><i class="fas fa-user"></i> <c:out value="${sessionScope.user.fullName}"/></a>
                        <a href="${pageContext.request.contextPath}/logout"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login"><i class="fas fa-sign-in-alt"></i> Đăng nhập</a>
                        <a href="${pageContext.request.contextPath}/register"><i class="fas fa-user-plus"></i> Đăng ký</a>
                    </c:otherwise>
                </c:choose>
            </nav>
        </div>
    </header>

    <!-- Main Content -->
    <div class="container">
        <c:if test="${param.wishlist == 'added'}"><div class="alert alert-success">Đã thêm vào danh sách yêu thích!</div></c:if>
        <c:if test="${param.wishlist == 'exists'}"><div class="alert alert-error">Sản phẩm đã có trong danh sách yêu thích.</div></c:if>
        <div style="background: white; padding: 30px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-top: 20px;">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 40px;">
                <!-- Product Image -->
                <div>
                    <img src="${pageContext.request.contextPath}/${product.imageUrl}" alt="<c:out value="${product.name}"/>" 
                         style="width: 100%; border-radius: 4px;"
                         onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/no-image.png'">
                </div>

                <!-- Product Info -->
                <div>
                    <h1 style="color: #81c784; margin-bottom: 15px;"><c:out value="${product.name}"/></h1>
                    
                    <p style="color: #666; margin-bottom: 10px;">
                        <strong>Danh mục:</strong> <c:out value="${product.categoryName}"/>
                    </p>
                    
                    <p style="color: #81c784; font-size: 32px; font-weight: bold; margin: 20px 0;">
                        <fmt:formatNumber value="${product.price}" pattern="#,##0"/> VNĐ
                    </p>
                    
                    <p style="color: ${product.stock > 0 ? '#4caf50' : '#f44336'}; font-size: 16px; margin-bottom: 20px;">
                        <c:choose>
                            <c:when test="${product.stock > 0}">
                                Còn hàng: ${product.stock} sản phẩm
                            </c:when>
                            <c:otherwise>
                                Hết hàng
                            </c:otherwise>
                        </c:choose>
                    </p>
                    
                    <div style="margin-bottom: 30px;">
                        <h3 style="margin-bottom: 10px;">Mô tả sản phẩm:</h3>
                        <p style="color: #666; line-height: 1.6;">
                            <c:out value="${product.description}"/>
                        </p>
                    </div>
                    
                    <c:if test="${not empty sessionScope.user && product.stock > 0}">
                        <div style="margin-bottom: 20px;">
                            <label style="display: block; margin-bottom: 10px;">Số lượng:</label>
                            <input type="number" id="quantity" value="1" min="1" max="${product.stock}" 
                                   style="width: 100px; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                        </div>
                        <div style="display: flex; gap: 10px;">
                            <form method="post" action="${pageContext.request.contextPath}/cart" style="flex: 1;">
                                <input type="hidden" name="action" value="add">
                                <input type="hidden" name="productId" value="${product.productId}">
                                <input type="hidden" name="quantity" id="cartQuantity" value="1">
                                <button type="submit" class="btn" style="width: 100%;" onclick="document.getElementById('cartQuantity').value = document.getElementById('quantity').value;">
                                    <i class="fas fa-shopping-cart"></i> Thêm vào giỏ hàng
                                </button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/wishlist">
                                <input type="hidden" name="action" value="add">
                                <input type="hidden" name="productId" value="${product.productId}">
                                <button type="submit" class="btn btn-secondary">
                                    <i class="fas fa-heart"></i> Yêu thích
                                </button>
                            </form>
                        </div>
                    </c:if>
                    
                    <c:if test="${empty sessionScope.user}">
                        <p style="color: #666; margin-top: 20px;">
                            <a href="${pageContext.request.contextPath}/login" style="color: #81c784;">Đăng nhập</a> để mua hàng
                        </p>
                    </c:if>
                </div>
            </div>

            <!-- Reviews Section -->
            <div id="reviews" style="margin-top: 40px; padding-top: 40px; border-top: 2px solid #f0f0f0;">
                <h2 style="color: #81c784; margin-bottom: 10px;"><i class="fas fa-star"></i> Đánh giá sản phẩm</h2>

                <c:choose>
                    <c:when test="${reviewCount > 0}">
                        <p style="margin-bottom: 20px; font-size: 18px;">
                            <span style="color: #f9a825; font-weight: bold;">
                                <fmt:formatNumber value="${avgRating}" maxFractionDigits="1" minFractionDigits="1"/> / 5
                            </span>
                            <span style="color: #888;">(${reviewCount} đánh giá)</span>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <p style="color: #888; margin-bottom: 20px;">Chưa có đánh giá nào cho sản phẩm này.</p>
                    </c:otherwise>
                </c:choose>

                <c:if test="${param.review == 'success'}">
                    <div class="alert alert-success">Cảm ơn bạn đã đánh giá sản phẩm!</div>
                </c:if>
                <c:if test="${param.review == 'error' || param.review == 'invalid'}">
                    <div class="alert alert-error">Không thể gửi đánh giá. Bạn chỉ đánh giá được sản phẩm đã mua và nhận hàng.</div>
                </c:if>

                <c:if test="${not empty reviewOrderItemId}">
                    <form id="review-form" method="post" action="${pageContext.request.contextPath}/review"
                          style="background: rgba(129,199,132,0.08); padding: 20px; border-radius: 8px; margin-bottom: 25px;">
                        <h3 style="margin-bottom: 15px;">Viết đánh giá của bạn</h3>
                        <input type="hidden" name="productId" value="${product.productId}">
                        <input type="hidden" name="orderItemId" value="${reviewOrderItemId}">
                        <div class="form-group">
                            <label for="rating">Số sao</label>
                            <select name="rating" id="rating" required>
                                <option value="5">5 sao - Rất tốt</option>
                                <option value="4">4 sao - Tốt</option>
                                <option value="3">3 sao - Bình thường</option>
                                <option value="2">2 sao - Kém</option>
                                <option value="1">1 sao - Rất kém</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="comment">Nhận xét</label>
                            <textarea name="comment" id="comment" rows="4" maxlength="1000"
                                      placeholder="Chia sẻ cảm nhận của bạn về sản phẩm..."></textarea>
                        </div>
                        <button type="submit" class="btn"><i class="fas fa-paper-plane"></i> Gửi đánh giá</button>
                    </form>
                </c:if>

                <c:forEach var="r" items="${reviews}">
                    <div style="padding: 15px 0; border-bottom: 1px solid #f0f0f0;">
                        <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
                            <strong><c:out value="${empty r.userFullName ? r.username : r.userFullName}"/></strong>
                            <span style="color: #888; font-size: 13px;">
                                <fmt:formatDate value="${r.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </span>
                        </div>
                        <div style="color: #f9a825; margin-bottom: 6px;">
                            <c:forEach begin="1" end="5" var="i">
                                <c:choose>
                                    <c:when test="${i <= r.rating}"><i class="fas fa-star"></i></c:when>
                                    <c:otherwise><i class="far fa-star"></i></c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </div>
                        <p style="margin: 0;"><c:out value="${r.comment}"/></p>
                    </div>
                </c:forEach>
            </div>
        </div>

        <div style="margin-top: 20px;">
            <a href="${pageContext.request.contextPath}/products" style="color: #81c784; text-decoration: none;">
                ← Quay lại danh sách sản phẩm
            </a>
        </div>
    </div>
</body>
</html>
