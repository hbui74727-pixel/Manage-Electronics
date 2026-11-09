/* ============================================================
   MANAGE ELECTRONICS - Script tao database + du lieu mau (SQL Server)
   CACH DUNG: mo file trong SSMS -> bam Execute (F5) MOT LAN, chay toan bo.
   CANH BAO: script se XOA va TAO LAI database Manage_Electronics.
   Mat khau trong du lieu mau duoc luu dang bam PBKDF2 (xem util/PasswordUtil.java);
   mat khau dang nhap: user1/password1, user2/password2, admin1/123456.
   ============================================================ */
USE master;
GO
IF DB_ID('Manage_Electronics') IS NOT NULL
BEGIN
    ALTER DATABASE Manage_Electronics SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE Manage_Electronics;
END
GO
CREATE DATABASE Manage_Electronics;
GO
USE Manage_Electronics;
GO

CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100),
    email VARCHAR(100) UNIQUE,
    role INT NOT NULL DEFAULT 0 CHECK (role IN (0, 1)),   -- 0: User, 1: Admin
    created_at DATETIME DEFAULT GETDATE()
);

CREATE TABLE Categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(MAX)
);

CREATE TABLE Products (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    category_id INT,
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(MAX),
    price DECIMAL(18, 2) NOT NULL CHECK (price >= 0),
    image_url VARCHAR(255),
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (category_id) REFERENCES Categories(category_id)
);

CREATE TABLE Reviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    product_id INT,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

CREATE TABLE Wishlists (
    wishlist_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    product_id INT,
    added_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    CONSTRAINT UQ_Wishlists_user_product UNIQUE (user_id, product_id)
);

CREATE TABLE Carts (
    cart_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT UNIQUE,
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE CartItems (
    cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
    cart_id INT,
    product_id INT,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    FOREIGN KEY (cart_id) REFERENCES Carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    CONSTRAINT UQ_CartItems_cart_product UNIQUE (cart_id, product_id)
);

CREATE TABLE Vouchers (
    voucher_id INT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_percent INT CHECK (discount_percent >= 1 AND discount_percent <= 100),
    max_discount DECIMAL(18, 2) CHECK (max_discount >= 0),
    expiration_date DATETIME,
    usage_limit INT NOT NULL DEFAULT 1 CHECK (usage_limit >= 0),
    is_active BIT DEFAULT 1
);

CREATE TABLE Orders (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    voucher_id INT NULL,
    receiver_name NVARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(15) NOT NULL,
    shipping_address NVARCHAR(MAX) NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,
    status INT NOT NULL DEFAULT 0 CHECK (status IN (0, 1, 2, 3)),  -- 0: Cho xac nhan, 1: Dang giao, 2: Hoan thanh, 3: Da huy
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (voucher_id) REFERENCES Vouchers(voucher_id)
);

CREATE TABLE OrderItems (
    order_item_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT NOT NULL CHECK (quantity > 0),
    price DECIMAL(18, 2) NOT NULL CHECK (price >= 0),
    is_reviewed BIT DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);
GO

/* ===================== CHI MUC (tang toc tim kiem / loc / tra cuu) ===================== */
CREATE INDEX IX_Products_category ON Products(category_id);
CREATE INDEX IX_Products_name     ON Products(name);
CREATE INDEX IX_Orders_user       ON Orders(user_id, created_at DESC);
CREATE INDEX IX_OrderItems_order  ON OrderItems(order_id);
CREATE INDEX IX_Reviews_product   ON Reviews(product_id);
GO

/* ===================== DU LIEU MAU (gia tinh bang VND) ===================== */
-- Cot password luu dang bam PBKDF2-HMAC-SHA256: pbkdf2$<so vong>$<salt>$<hash>
INSERT INTO Users (username, password, full_name, email, role) VALUES
('user1',  'pbkdf2$65536$wFaLCh/7h20biVCh9m++vA==$S89zwtoDwNdQkEBerNmDX58zXomZp8/qobb8BOLnSLA=', N'Nguyễn Thị Mai', 'mai@example.com',   0),
('user2',  'pbkdf2$65536$atNwxz7fcPCGd9VDOBJuqQ==$8IaXxSxnoKiriZFGUbDoQZ3Dnc4Zi2Ze/2Gh6otvT7A=', N'Trần Thị Lan',   'lan@example.com',   0),
('admin1', 'pbkdf2$65536$kqKLApEmrdhmfkVfynK2OA==$pZbRA0Fq1uhpQxO5tVvgnNaaCQk2FygYoyiL1mEJ0lc=',    N'Lê Minh Cường',  'cuong@example.com', 1);

INSERT INTO Categories (name, description) VALUES
(N'Điện thoại',          N'Điện thoại thông minh các hãng'),
(N'Laptop',              N'Máy tính xách tay học tập, văn phòng và làm việc'),
(N'Âm thanh',            N'Tai nghe, loa và thiết bị âm thanh'),
(N'Phụ kiện',            N'Sạc, pin dự phòng, cáp và phụ kiện công nghệ'),
(N'Thiết bị thông minh', N'Đồng hồ thông minh, camera và thiết bị nhà thông minh');

-- image_url la duong dan tuong doi trong thu muc webapp/images
-- (giu nguyen ten file anh cu book-1.png ... book-10.png, ban thay anh thu cong bang cach ghi de file cung ten)
INSERT INTO Products (category_id, name, description, price, image_url, stock) VALUES
(1, N'iPhone 15 128GB',                  N'Điện thoại Apple với chip A16 Bionic, màn hình Super Retina XDR 6,1 inch, cổng USB-C và camera chính 48MP.', 19990000, 'images/book-1.png', 30),
(2, N'Laptop Dell XPS 13',               N'Laptop mỏng nhẹ với màn hình InfinityEdge 13,4 inch, vỏ nhôm cao cấp, phù hợp làm việc và di chuyển.',       32990000, 'images/book-2.png', 15),
(3, N'Tai nghe Sony WH-CH720N',          N'Tai nghe chụp tai không dây, chống ồn chủ động, kết nối Bluetooth và thời lượng pin lên đến 35 giờ.',        2990000,  'images/book-3.png', 40),
(4, N'Củ sạc nhanh Anker 65W GaN',       N'Củ sạc công nghệ GaN nhỏ gọn, sạc nhanh cho điện thoại, máy tính bảng và laptop.',                           790000,   'images/book-4.png', 80),
(5, N'Apple Watch SE 2 GPS 40mm',        N'Đồng hồ thông minh theo dõi nhịp tim, vận động, sức khỏe và nhận thông báo từ iPhone.',                       6290000,  'images/book-5.png', 25),
(1, N'Samsung Galaxy S24',               N'Điện thoại Samsung màn hình Dynamic AMOLED 2X 6,2 inch, hiệu năng mạnh mẽ và tích hợp các tính năng Galaxy AI.', 17990000, 'images/book-6.png', 28),
(2, N'MacBook Air M2 13 inch',           N'Laptop Apple chip M2, thiết kế mỏng nhẹ, màn hình Liquid Retina và thời lượng pin dùng cả ngày.',            24990000, 'images/book-7.png', 12),
(3, N'Loa Bluetooth JBL Flip 6',         N'Loa di động âm thanh mạnh mẽ, chống nước chống bụi chuẩn IP67, phù hợp mang đi du lịch, dã ngoại.',           2690000,  'images/book-8.png', 35),
(4, N'Pin sạc dự phòng Xiaomi 20000mAh', N'Pin dự phòng dung lượng 20.000mAh, hỗ trợ sạc nhanh 22.5W, đủ sạc nhiều lần cho điện thoại.',                  650000,   'images/book-9.png', 60),
(5, N'Camera Wi-Fi Xiaomi C300 2K',      N'Camera an ninh trong nhà độ phân giải 2K, xoay 360 độ, quay ban đêm và cảnh báo khi phát hiện chuyển động.',  690000,   'images/book-10.png', 50);

-- max_discount duoc nang len cho phu hop gia san pham dien tu
INSERT INTO Vouchers (code, discount_percent, max_discount, expiration_date, usage_limit, is_active) VALUES
('SALE10',   10,  500000, '2027-12-31', 100, 1),
('WELCOME20', 20, 1000000, '2027-12-31', 50,  1);

-- Don 1: da hoan thanh (status 2) -> user1 co the danh gia 2 san pham: 19.990.000 + 32.990.000 = 52.980.000
-- Don 2: dang giao, dung voucher SALE10: 2.990.000 - 10% (299.000) = 2.691.000
-- Don 3: cho xac nhan: 2 x 790.000 = 1.580.000
INSERT INTO Orders (user_id, voucher_id, receiver_name, receiver_phone, shipping_address, total_amount, status) VALUES
(1, NULL, N'Nguyễn Thị Mai', '0123456789', N'123 Main Street, Hà Nội',    52980000, 2),
(2, 1,    N'Trần Thị Lan',   '0987654321', N'456 Second Street, Hà Nội',  2691000,  1),
(1, NULL, N'Nguyễn Thị Mai', '0123456789', N'123 Main Street, Hà Nội',    1580000,  0);

INSERT INTO OrderItems (order_id, product_id, quantity, price) VALUES
(1, 1, 1, 19990000),
(1, 2, 1, 32990000),
(2, 3, 1, 2990000),
(3, 4, 2, 790000);

-- Danh gia mau cua user2 (chua gan voi don hang)
INSERT INTO Reviews (user_id, product_id, rating, comment) VALUES
(2, 1, 5, N'Máy chạy mượt, camera chụp đẹp, pin dùng được cả ngày.'),
(2, 2, 4, N'Máy mỏng nhẹ, màn hình đẹp nhưng số cổng kết nối hơi ít.');

INSERT INTO Wishlists (user_id, product_id) VALUES (1, 1), (2, 4), (1, 5);

INSERT INTO Carts (user_id) VALUES (1), (2);

INSERT INTO CartItems (cart_id, product_id, quantity) VALUES (1, 6, 1), (1, 10, 1), (2, 3, 1);
GO

SELECT (SELECT COUNT(*) FROM Categories) AS so_danh_muc,
       (SELECT COUNT(*) FROM Products)   AS so_san_pham,
       (SELECT COUNT(*) FROM Users)      AS so_nguoi_dung;
GO
