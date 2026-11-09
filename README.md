# MANAGE ELECTRONICS – HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY

Website quản lý & bán thiết bị điện tử viết bằng **Java Servlet/JSP (Jakarta EE 10)**, CSDL **SQL Server**, build bằng **Maven**, chạy trên **Apache Tomcat 10.1**.

## 1. Yêu cầu môi trường

| Thành phần | Phiên bản | Ghi chú |
|---|---|---|
| JDK | 17 trở lên | `java -version` |
| Maven | 3.8+ | hoặc dùng Maven có sẵn trong NetBeans |
| Apache Tomcat | **10.1.x** | Bắt buộc 10.x (namespace `jakarta.*`). **Tomcat 9 sẽ không chạy.** |
| SQL Server | 2017 trở lên (Express được) | bật TCP/IP cổng 1433, bật chế độ đăng nhập *SQL Server and Windows Authentication* |
| SSMS | bất kỳ | để chạy file SQL |

## 2. Tạo cơ sở dữ liệu

1. Mở **SQL Server Management Studio**, kết nối vào server.
2. Mở file `Manage_Electronics.sql` ở thư mục gốc dự án → bấm **Execute (F5)** *một lần, chạy toàn bộ*.
3. Script sẽ **xóa và tạo lại** database `Manage_Electronics`, tạo 10 bảng, ràng buộc, chỉ mục và nạp dữ liệu mẫu (5 danh mục, 10 sản phẩm, 3 người dùng, 2 voucher, 3 đơn hàng).
4. Kiểm tra: câu `SELECT` cuối script trả về `5 | 10 | 3`.

## 3. Cấu hình kết nối CSDL

Mặc định (file `util/DBConnection.java`): `localhost:1433`, database `Manage_Electronics`, user `sa`, mật khẩu `123456`.

**Cách 1 – không sửa code (khuyên dùng):** đặt biến môi trường trước khi chạy Tomcat

| Biến | Ví dụ |
|---|---|
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=Manage_Electronics;encrypt=false;trustServerCertificate=true` |
| `DB_USER` | `sa` |
| `DB_PASSWORD` | mật khẩu SQL Server của bạn |

- Windows: tạo file `TOMCAT/bin/setenv.bat` với `set DB_PASSWORD=matkhau_cua_ban`.
- Linux/macOS: tạo `TOMCAT/bin/setenv.sh` với `export DB_PASSWORD=matkhau_cua_ban`.
- NetBeans: *Services → Servers → Tomcat → Properties → Platform → VM Options* hoặc đặt biến môi trường hệ điều hành rồi khởi động lại NetBeans.

**Cách 2:** sửa trực tiếp giá trị mặc định trong `DBConnection.java`.

## 4. Build và chạy

### Cách A – NetBeans
1. *File → Open Project* → chọn thư mục dự án (có `pom.xml`).
2. *Tools → Servers → Add Server → Apache Tomcat* → trỏ tới thư mục Tomcat 10.1.
3. Chuột phải dự án → **Clean and Build**, sau đó **Run**.
4. Mở: `http://localhost:8080/Manage_Electronics/`

### Cách B – Maven + Tomcat thủ công
```bash
mvn clean package                         # tạo target/Manage_Electronics.war
cp target/Manage_Electronics.war  <TOMCAT>/webapps/
<TOMCAT>/bin/startup.sh                   # Windows: startup.bat
```
Mở: `http://localhost:8080/Manage_Electronics/`

> Lần build đầu cần Internet để Maven tải thư viện (Servlet API, JSTL, mssql-jdbc, Jakarta Mail…).

## 5. Tài khoản và dữ liệu mẫu

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Khách hàng | `user1` | `password1` |
| Khách hàng | `user2` | `password2` |
| Quản trị viên | `admin1` | `123456` |

Mã giảm giá: `SALE10` (giảm 10%, tối đa 500.000đ), `WELCOME20` (giảm 20%, tối đa 1.000.000đ).
Mật khẩu được lưu dạng băm PBKDF2-HMAC-SHA256 (có salt); mật khẩu thô của dữ liệu cũ tự động được nâng cấp khi đăng nhập.

## 6. Cấu hình gửi email (chức năng Quên mật khẩu – tùy chọn)

Đặt hai biến môi trường (cùng cách như mục 3): `MAIL_USER` (địa chỉ Gmail) và `MAIL_PASSWORD` (**App Password** 16 ký tự của Google, không phải mật khẩu Gmail). Nếu không cấu hình, mọi chức năng khác vẫn chạy bình thường; chỉ riêng gửi email đặt lại mật khẩu báo lỗi. Liên kết đặt lại có hiệu lực **15 phút**.

## 7. Kiểm tra nhanh sau khi cài (smoke test ~5 phút)

1. Trang chủ hiện 10 sản phẩm → gõ `dien thoai` vào ô tìm kiếm → ra các sản phẩm liên quan (iPhone, Samsung, củ sạc, pin dự phòng…); tìm không phân biệt dấu và hoa/thường.
2. Đăng nhập `user1` → thêm sản phẩm vào giỏ → *Thanh toán* → áp mã `SALE10` → đặt hàng → xem trong *Đơn hàng*.
3. Đăng xuất, đăng nhập `admin1` → *Đơn hàng* → **Duyệt** đơn → **Hoàn thành**.
4. *Admin → Sản phẩm*: thêm / sửa / xóa / tìm kiếm một sản phẩm (thử tải ảnh lên).
5. *Admin → Người dùng*: tìm `mai`, sửa họ tên, đặt lại mật khẩu.
6. Đăng nhập lại `user1` → vào sản phẩm đã mua và nhận hàng → gửi đánh giá.

Bộ test case đầy đủ nằm trong file báo cáo (mục Kiểm thử).

## 8. Cấu trúc dự án

```
Manage-Electronics/
├── pom.xml                      # cấu hình Maven (đóng gói WAR: Manage_Electronics.war)
├── Manage_Electronics.sql       # script tạo CSDL + dữ liệu mẫu
├── HUONG_DAN.md                 # tài liệu này
├── docs/                        # ERD, mô hình CSDL (png/svg)
└── src/main
    ├── java/com/mycompany/manage_electronics
    │   ├── model/               # lớp thực thể (User, Product, Order, ...)
    │   ├── dao/                 # truy cập CSDL bằng JDBC + PreparedStatement
    │   ├── servlet/             # controller xử lý request
    │   ├── filter/              # EncodingFilter (UTF-8)
    │   └── util/                # DBConnection, PasswordUtil, EmailUtil, DaoResult
    └── webapp
        ├── WEB-INF/views/*.jsp  # giao diện (view)
        ├── css/style.css
        └── images/              # ảnh sản phẩm
```

## 9. Xử lý lỗi thường gặp

| Hiện tượng | Nguyên nhân / cách xử lý |
|---|---|
| HTTP 404 khi mở trang | Sai đường dẫn: phải là `/Manage_Electronics/` (tên file WAR). |
| `ClassNotFoundException: jakarta.servlet...` hoặc trang trắng/500 ngay khi khởi động | Đang dùng Tomcat 9. Cài **Tomcat 10.1**. |
| `The TCP/IP connection to the host localhost, port 1433 has failed` | SQL Server chưa bật TCP/IP: *SQL Server Configuration Manager → Protocols → TCP/IP = Enabled*, khởi động lại dịch vụ. |
| `Login failed for user 'sa'` | Sai `DB_PASSWORD`, hoặc chưa bật chế độ đăng nhập SQL Server authentication / tài khoản `sa` bị khóa. |
| `Cannot open database "Manage_Electronics"` | Chưa chạy file SQL (mục 2). |
| Tiếng Việt bị lỗi font | Mở file SQL/JSP bằng UTF-8; dự án đã có `EncodingFilter` để ép UTF-8 cho request/response. |
| Không xóa được sản phẩm/danh mục/người dùng/voucher | Đúng thiết kế: dữ liệu đang được tham chiếu (vd. sản phẩm đã có trong đơn hàng) – hệ thống báo lỗi thân thiện thay vì xóa mất lịch sử. |
| Ảnh tải lên biến mất sau khi build lại | Ảnh lưu trong thư mục triển khai; khi chạy từ `target/`, hệ thống tự chép thêm vào `src/main/webapp/images`. Khi triển khai WAR độc lập, ảnh nằm trong `webapps/Manage_Electronics/images`. |

### Lỗi HTTP 503 "Servlet [jsp] is currently unavailable"
JSP engine của Tomcat không khởi động được, thường do xung đột thư viện hoặc bản deploy cũ còn sót:
1. Dừng Tomcat, xóa thư mục `webapps/Manage_Book*`, `webapps/Manage_Electronics*` và `work/Catalina/localhost/*` (nếu có).
2. Chạy **Clean and Build** (hoặc `mvn clean package`) để lấy thư viện mới, rồi deploy lại.
3. Dùng **Tomcat 10.1.x** và **JDK 17+** (Tomcat 10.1.44 ở trên là phù hợp).
4. Nếu vẫn lỗi: mở `TOMCAT/logs/catalina.*.log` hoặc `localhost.*.log`, tìm dòng `SEVERE`/`Caused by` đầu tiên để biết nguyên nhân chính xác.
