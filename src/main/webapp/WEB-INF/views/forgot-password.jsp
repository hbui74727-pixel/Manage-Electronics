<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên mật khẩu - Manage Electronics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <div class="auth-container">
        <h2><i class="fas fa-key"></i> Quên mật khẩu</h2>
        
        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>
        
        <c:if test="${not empty success}">
            <div class="alert alert-success">${success}</div>
        </c:if>
        
        <p style="text-align: center; margin-bottom: 20px; color: #666;">
            Nhập email của bạn để nhận link đặt lại mật khẩu
        </p>
        
        <form action="${pageContext.request.contextPath}/forgot-password" method="post">
            <input type="hidden" name="action" value="request">
            
            <div class="form-group">
                <label for="email"><i class="fas fa-envelope"></i> Email</label>
                <input type="email" id="email" name="email" required>
            </div>
            
            <button type="submit" class="btn"><i class="fas fa-paper-plane"></i> Gửi link đặt lại mật khẩu</button>
        </form>
        
        <div class="form-footer">
            <p><a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></p>
        </div>
    </div>
</body>
</html>
