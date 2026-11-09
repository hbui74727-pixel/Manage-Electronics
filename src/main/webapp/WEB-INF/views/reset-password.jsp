<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt lại mật khẩu - Manage Electronics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <div class="auth-container">
        <h2><i class="fas fa-key"></i> Đặt lại mật khẩu</h2>
        
        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>
        
        <form action="${pageContext.request.contextPath}/forgot-password" method="post">
            <input type="hidden" name="action" value="reset">
            <input type="hidden" name="token" value="${token}">
            
            <div class="form-group">
                <label for="newPassword"><i class="fas fa-lock"></i> Mật khẩu mới</label>
                <input type="password" id="newPassword" name="newPassword" required minlength="6">
            </div>
            
            <div class="form-group">
                <label for="confirmPassword"><i class="fas fa-lock"></i> Xác nhận mật khẩu mới</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required minlength="6">
            </div>
            
            <button type="submit" class="btn"><i class="fas fa-check"></i> Đặt lại mật khẩu</button>
        </form>
    </div>
</body>
</html>
