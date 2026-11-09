HƯỚNG DẪN SỬ DỤNG THƯ MỤC IMAGES
=====================================

Thư mục này chứa các hình ảnh cho website Manage Electronics.

CẤU TRÚC:
---------
/images/
  - no-image.png          : Ảnh mặc định khi không có ảnh sản phẩm
  - product-sample-1.jpg  : Ảnh mẫu sản phẩm 1
  - product-sample-2.jpg     : Ảnh mẫu sản phẩm 2 (màu xanh dương)
  - product-sample-3.jpg     : Ảnh mẫu sản phẩm 3 (màu cam)

CÁCH SỬ DỤNG:
-------------
1. Khi thêm sản phẩm mới trong Admin, nhập URL ảnh:
   - Dùng ảnh mẫu: ${pageContext.request.contextPath}/images/product-sample-1.jpg
   - Dùng ảnh online: https://example.com/image.jpg
   - Dùng ảnh local: ${pageContext.request.contextPath}/images/ten-anh.jpg

2. Để thêm ảnh mới:
   - Copy file ảnh vào thư mục: src/main/webapp/images/
   - Đặt tên file: vd: dien-thoai-1.jpg
   - Sử dụng URL: ${pageContext.request.contextPath}/images/dien-thoai-1.jpg

3. Định dạng ảnh khuyến nghị:
   - Kích thước: 300x400px (tỷ lệ 3:4 hoặc 1:1)
   - Định dạng: JPG, PNG
   - Dung lượng: < 500KB

LƯU Ý:
------
- Nếu URL ảnh bị lỗi, hệ thống tự động hiển thị no-image.png
- Không xóa file no-image.png
- Đặt tên file không dấu, không khoảng trắng
