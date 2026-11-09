<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách sản phẩm - Manage Electronics</title>
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
        <div style="display: flex; gap: 30px; margin-top: 20px;">
            <!-- Sidebar Categories -->
            <div style="width: 250px; flex-shrink: 0;">
                <div style="background: white; padding: 20px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h3 style="margin-bottom: 20px; color: #81c784;"><i class="fas fa-list"></i> Danh mục</h3>
                    <ul style="list-style: none;">
                        <li style="margin-bottom: 10px;">
                            <a href="${pageContext.request.contextPath}/products" 
                               style="display: block; padding: 10px; text-decoration: none; color: #333; border-radius: 4px; ${empty selectedCategoryId ? 'background-color: #e8f5e9;' : ''}">
                                Tất cả sản phẩm
                            </a>
                        </li>
                        <c:forEach var="category" items="${categories}">
                            <li style="margin-bottom: 10px;">
                                <a href="${pageContext.request.contextPath}/products?categoryId=${category.categoryId}" 
                                   style="display: block; padding: 10px; text-decoration: none; color: #333; border-radius: 4px; ${selectedCategoryId == category.categoryId ? 'background-color: #e8f5e9;' : ''}">
                                    <c:out value="${category.name}"/>
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </div>

            <!-- Products Grid -->
            <div style="flex: 1;">
                <h2 style="margin-bottom: 20px; color: #81c784;">
                    <c:choose>
                        <c:when test="${not empty selectedCategoryId}">
                            <c:forEach var="category" items="${categories}">
                                <c:if test="${category.categoryId == selectedCategoryId}">
                                    <c:out value="${category.name}"/>
                                </c:if>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            Tất cả sản phẩm
                        </c:otherwise>
                    </c:choose>
                </h2>

                <!-- Thanh tìm kiếm / lọc / sắp xếp -->
                <form method="get" action="${pageContext.request.contextPath}/products" style="background: white; padding: 15px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; display: flex; flex-wrap: wrap; gap: 10px; align-items: center;">
                    <c:if test="${not empty selectedCategoryId}"><input type="hidden" name="categoryId" value="${selectedCategoryId}"></c:if>
                    <input type="text" name="q" value="<c:out value='${keyword}'/>" placeholder="Tìm sản phẩm (có thể gõ không dấu)..." style="flex: 1; min-width: 200px; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                    <input type="number" name="minPrice" value="<c:out value='${minPrice}'/>" min="0" placeholder="Giá từ" style="width: 120px; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                    <input type="number" name="maxPrice" value="<c:out value='${maxPrice}'/>" min="0" placeholder="Giá đến" style="width: 120px; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                    <select name="sort" style="padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                        <option value="">Mới nhất</option>
                        <option value="price_asc" ${sort == 'price_asc' ? 'selected' : ''}>Giá tăng dần</option>
                        <option value="price_desc" ${sort == 'price_desc' ? 'selected' : ''}>Giá giảm dần</option>
                        <option value="name_asc" ${sort == 'name_asc' ? 'selected' : ''}>Tên A → Z</option>
                    </select>
                    <label style="display: flex; align-items: center; gap: 6px;"><input type="checkbox" name="inStock" value="1" ${inStock ? 'checked' : ''}> Còn hàng</label>
                    <button type="submit" class="btn" style="padding: 10px 18px;"><i class="fas fa-search"></i> Tìm</button>
                    <a href="${pageContext.request.contextPath}/products" style="color: #666; text-decoration: none;">Xóa lọc</a>
                </form>

                <c:if test="${param.wishlist == 'added'}"><div class="alert alert-success">Đã thêm vào danh sách yêu thích!</div></c:if>
                <c:if test="${param.wishlist == 'exists'}"><div class="alert alert-error">Sản phẩm đã có trong danh sách yêu thích.</div></c:if>
                <p style="margin-bottom: 15px; color: #666;">Tìm thấy <strong>${totalProducts}</strong> sản phẩm<c:if test="${not empty keyword}"> cho từ khóa "<strong><c:out value='${keyword}'/></strong>"</c:if>.</p>

                <c:if test="${empty products}">
                    <p style="text-align: center; padding: 40px; color: #666;">Không có sản phẩm nào.</p>
                </c:if>

                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 20px;">
                    <c:forEach var="product" items="${products}">
                        <div class="feature-card" style="position: relative; display: flex; flex-direction: column; height: 100%;">
                            <a href="${pageContext.request.contextPath}/product-detail?id=${product.productId}" style="text-decoration: none; color: inherit; display: flex; flex-direction: column; flex: 1;">
                                <img src="${pageContext.request.contextPath}/${product.imageUrl}" alt="<c:out value="${product.name}"/>" 
                                     style="width: 100%; height: 200px; object-fit: cover; border-radius: 4px; margin-bottom: 15px;"
                                     onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/no-image.png'">
                                <h3 style="color: #333; margin-bottom: 10px; font-size: 16px; height: 48px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.5;">
                                    <c:out value="${product.name}"/>
                                </h3>
                                <p style="color: #81c784; font-size: 18px; font-weight: bold; margin-bottom: 10px;">
                                    <fmt:formatNumber value="${product.price}" pattern="#,##0"/> VNĐ
                                </p>
                                <p style="color: ${product.stock > 0 ? '#666' : '#e57373'}; font-size: 14px; margin-bottom: 15px;">
                                    <c:choose>
                                        <c:when test="${product.stock > 0}">
                                            Còn lại: ${product.stock} sản phẩm
                                        </c:when>
                                        <c:otherwise>
                                            Hết hàng
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </a>
                            
                            <!-- Action Buttons -->
                            <div style="margin-top: auto;">
                                <c:if test="${not empty sessionScope.user && product.stock > 0}">
                                    <div style="display: flex; gap: 5px;">
                                        <form method="post" action="${pageContext.request.contextPath}/cart" style="flex: 1;">
                                            <input type="hidden" name="action" value="add">
                                            <input type="hidden" name="productId" value="${product.productId}">
                                            <input type="hidden" name="quantity" value="1">
                                            <button type="submit" class="btn" style="width: 100%; padding: 8px; font-size: 14px;" 
                                                    onclick="event.stopPropagation();">
                                                <i class="fas fa-shopping-cart"></i> Giỏ hàng
                                            </button>
                                        </form>
                                        <form method="post" action="${pageContext.request.contextPath}/wishlist" style="width: auto;">
                                            <input type="hidden" name="action" value="add">
                                            <input type="hidden" name="productId" value="${product.productId}">
                                            <button type="submit" class="btn btn-secondary" style="padding: 8px 12px; font-size: 14px;" 
                                                    onclick="event.stopPropagation();" title="Thêm vào yêu thích">
                                                <i class="fas fa-heart"></i>
                                            </button>
                                        </form>
                                    </div>
                                </c:if>
                                
                                <c:if test="${empty sessionScope.user}">
                                    <div style="text-align: center;">
                                        <a href="${pageContext.request.contextPath}/login" style="color: #81c784; font-size: 14px;">
                                            Đăng nhập để mua hàng
                                        </a>
                                    </div>
                                </c:if>
                                
                                <c:if test="${not empty sessionScope.user && product.stock <= 0}">
                                    <div style="text-align: center;">
                                        <span style="color: #e57373; font-size: 14px;">Sản phẩm tạm hết hàng</span>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <div style="display: flex; justify-content: center; gap: 10px; margin-top: 30px;">
                        <c:if test="${currentPage > 1}">
                            <a href="${pageContext.request.contextPath}/products?<c:out value="${baseQuery}"/>page=${currentPage - 1}" 
                               style="padding: 8px 16px; background: #81c784; color: white; text-decoration: none; border-radius: 4px;">
                                Trước
                            </a>
                        </c:if>

                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <a href="${pageContext.request.contextPath}/products?<c:out value="${baseQuery}"/>page=${i}" 
                               style="padding: 8px 16px; ${i == currentPage ? 'background: #81c784; color: white;' : 'background: white; color: #333;'} text-decoration: none; border-radius: 4px; border: 1px solid #ddd;">
                                ${i}
                            </a>
                        </c:forEach>

                        <c:if test="${currentPage < totalPages}">
                            <a href="${pageContext.request.contextPath}/products?<c:out value="${baseQuery}"/>page=${currentPage + 1}" 
                               style="padding: 8px 16px; background: #81c784; color: white; text-decoration: none; border-radius: 4px;">
                                Sau
                            </a>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</body>
</html>
