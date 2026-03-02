USE SMART_PC_STORE
GO

/* ==========================================================================
   1. CLEANUP (Xóa sạch dữ liệu cũ)
   ========================================================================== */
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all"

DELETE FROM DemandForecasts;
DELETE FROM PriceForecasts;
DELETE FROM RevenueDaily;
DELETE FROM Payments;
DELETE FROM OrderItems;
DELETE FROM Orders;
DELETE FROM CartItems;
DELETE FROM Carts;
DELETE FROM InventoryTransactions;
DELETE FROM PurchaseOrderItems;
DELETE FROM PurchaseOrders;
DELETE FROM SupplierPriceHistories;
DELETE FROM Products;
DELETE FROM Suppliers;
DELETE FROM Categories;
DELETE FROM Sessions;
DELETE FROM Users;

EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all"
GO

/* ==========================================================================
   2. INSERT MASTER DATA (ÉP ID CHÍNH XÁC)
   ========================================================================== */

-- ---------------- USERS ----------------
SET IDENTITY_INSERT Users ON;
INSERT INTO Users (Id, Username, PasswordHash, DisplayName, Email, Phone, Address, Status, Role) VALUES 
(1, 'admin', 'hash_pw_1', 'System Admin', 'admin@pcstore.com', '0901000001', 'Server Room', 'Active', 'admin'),
(2, 'user_hcm', 'hash_pw_2', 'Nguyen Van A', 'vana@gmail.com', '0901000002', 'Quan 1, HCM', 'Active', 'user'),
(3, 'user_hn', 'hash_pw_3', 'Tran Thi B', 'thib@yahoo.com', '0901000003', 'Cau Giay, HN', 'Active', 'user'),
(4, 'user_dn', 'hash_pw_4', 'Le Van C', 'vanc@outlook.com', '0901000004', 'Hai Chau, DN', 'Blocked', 'user'),
(5, 'user_vip', 'hash_pw_5', 'Pham Minh Vip', 'vip@tech.com', '0901000005', 'Landmark 81, HCM', 'Active', 'user');
SET IDENTITY_INSERT Users OFF;

-- ---------------- CATEGORIES ----------------
SET IDENTITY_INSERT Categories ON;

-- 1. THÊM CÁC DANH MỤC GỐC (Cấp 1 - ParentId = NULL)
INSERT INTO Categories (Id, CategoryName, Description, Status, ParentId) VALUES 
(1, N'LINH KIỆN PC', N'Danh mục linh kiện', 1, NULL),
(2, N'PC & LAPTOP', N'Danh mục máy tính', 1, NULL),
(3, N'MÀN HÌNH', N'Danh mục màn hình', 1, NULL),
(4, N'PHỤ KIỆN', N'Danh mục phụ kiện', 1, NULL);

-- 2. THÊM CON CỦA "LINH KIỆN PC" (ParentId = 1)
INSERT INTO Categories (Id, CategoryName, Description, Status, ParentId) VALUES
(5, N'CPU', N'Bộ vi xử lý', 1, 1),
(6, N'GPU', N'Card đồ họa', 1, 1),
(7, N'RAM', N'Bộ nhớ trong', 1, 1),
(8, N'Ổ cứng', N'Lưu trữ dữ liệu', 1, 1),
(9, N'Mainboard', N'Bo mạch chủ', 1, 1),
(10, N'Nguồn', N'Nguồn máy tính', 1, 1),
(11, N'Tản nhiệt', N'Hệ thống tản nhiệt', 1, 1),
(12, N'Case', N'Vỏ máy tính', 1, 1);

-- 3. THÊM CON CỦA "PC & LAPTOP" (ParentId = 2)
INSERT INTO Categories (Id, CategoryName, Description, Status, ParentId) VALUES
(13, N'PC Gaming', N'Máy tính bàn chơi game', 1, 2),
(14, N'PC Đồ Họa', N'Máy tính bàn làm đồ họa', 1, 2),
(15, N'PC Văn Phòng', N'Máy tính bàn làm việc', 1, 2),
(16, N'Laptop Gaming', N'Laptop chơi game', 1, 2),
(17, N'Laptop Văn Phòng', N'Laptop làm việc', 1, 2);

-- 4. THÊM CON CỦA "MÀN HÌNH" (ParentId = 3)
INSERT INTO Categories (Id, CategoryName, Description, Status, ParentId) VALUES 
(18, N'Màn Hình Gaming', N'Màn hình tần số quét cao', 1, 3),
(19, N'Màn Hình Đồ Họa', N'Màn hình chuẩn màu', 1, 3),
(20, N'Màn Hình Văn Phòng', N'Màn hình làm việc cơ bản', 1, 3);

-- 5. THÊM CON CỦA "PHỤ KIỆN" (ParentId = 4)
INSERT INTO Categories (Id, CategoryName, Description, Status, ParentId) VALUES 
(21, N'Bàn Phím', N'Bàn phím cơ & thường', 1, 4),
(22, N'Chuột', N'Chuột máy tính', 1, 4),
(23, N'Tai Nghe', N'Tai nghe Gaming & Studio', 1, 4),
(24, N'Loa', N'Loa vi tính', 1, 4),
(25, N'Bàn & Ghế Gaming', N'Nội thất góc máy', 1, 4);

SET IDENTITY_INSERT Categories OFF;
GO

-- ---------------- SUPPLIERS ----------------
SET IDENTITY_INSERT Suppliers ON;
INSERT INTO Suppliers (Id, SupplierName, ContactInfo, LeadTimeDays) VALUES 
(1, 'Intel Global', 'contact@intel.com', 7),
(2, 'NVIDIA Corp', 'sales@nvidia.com', 14),
(3, 'Samsung Semi', 'chip@samsung.com', 5),
(4, 'Asus Tek', 'distributor@asus.com', 3);
SET IDENTITY_INSERT Suppliers OFF;

/* ==========================================================================
   3. INSERT PRODUCTS & HISTORY
   ========================================================================== */

-- ---------------- PRODUCTS ----------------
-- Lưu ý: SupplierId và CategoryId phải khớp với ID ở trên (1-4 và 1-5)
-- ---------------- PRODUCTS ----------------
SET IDENTITY_INSERT Products ON;

INSERT INTO Products (Id, ProductName, SupplierId, CategoryId, Description, CurrentPrice, Quantity) VALUES 
-- Nhóm CPU (CategoryId = 5)
(1, N'Intel Core i9-14900K', 1, 5, N'Intel Gen 14 Flagship siêu mạnh', 14500000, 50),
(2, N'Intel Core i5-13600K', 1, 5, N'Intel Mid-range King quốc dân', 7500000, 100),
(3, N'AMD Ryzen 9 7950X', 2, 5, N'Hiệu năng đa nhân đỉnh cao cho Đồ họa', 13500000, 30),

-- Nhóm GPU (CategoryId = 6)
(4, N'ASUS ROG Strix RTX 4090 OC 24GB', 3, 6, N'Top Tier Gaming GPU hiện nay', 55000000, 10),
(5, N'GIGABYTE RTX 4070 Ti SUPER Windforce', 4, 6, N'High Performance GPU 2K Gaming', 22000000, 25),
(6, N'MSI RTX 4060 Ventus 2X Black', 1, 6, N'Card màn hình quốc dân 1080p', 8500000, 60),

-- Nhóm RAM (CategoryId = 7)
(7, N'Corsair Dominator Platinum RGB 32GB (2x16GB) DDR5', 1, 7, N'RAM DDR5 cao cấp có LED', 4500000, 40),
(8, N'Kingston FURY Beast 16GB (2x8GB) DDR4', 2, 7, N'RAM DDR4 giá rẻ hiệu năng cao', 1200000, 100),

-- Nhóm Ổ Cứng (CategoryId = 8)
(9, N'Samsung 990 Pro 1TB M.2 NVMe PCIe Gen 4.0', 3, 8, N'SSD NVMe tốc độ cao nhất', 2800000, 200),
(10, N'WD Blue 2TB HDD 7200rpm', 4, 8, N'Ổ cứng HDD lưu trữ dung lượng cao', 1500000, 50),

-- Nhóm Mainboard (CategoryId = 9)
(11, N'ASUS ROG Maximus Z790 Hero', 3, 9, N'Mainboard Extreme cho dân chơi', 16000000, 15),
(12, N'MSI MAG B760M Mortar WiFi', 2, 9, N'Mainboard tầm trung m-ATX', 4500000, 40),

-- Nhóm PC Gaming (CategoryId = 13)
(13, N'PC Gaming Smart i5-13400F / RTX 4060', 1, 13, N'Chiến mượt mà mọi game eSport', 18500000, 10),
(14, N'PC Gaming Smart i9-14900K / RTX 4090', 1, 13, N'Cỗ máy chiến game AAA tối thượng', 110000000, 3),

-- Nhóm Laptop Gaming (CategoryId = 16)
(15, N'Acer Nitro 5 (Core i5, RTX 3050)', 2, 16, N'Laptop Gaming sinh viên', 19000000, 20),
(16, N'ASUS ROG Zephyrus G14 (2024)', 3, 16, N'Laptop Gaming cao cấp mỏng nhẹ', 35000000, 8),

-- Nhóm Màn Hình Gaming (CategoryId = 18)
(17, N'LG UltraGear 27GN800-B 27 inch 144Hz', 4, 18, N'Màn hình Gaming 2K IPS', 6500000, 30),
(18, N'Samsung Odyssey G9 49 inch cong', 3, 18, N'Màn hình cong siêu rộng', 28000000, 5),

-- Nhóm Màn Hình Đồ Họa (CategoryId = 19)
(19, N'DELL UltraSharp U2723QE 4K', 2, 19, N'Màn hình chuẩn màu thiết kế', 13500000, 15),

-- Nhóm Phụ Kiện (CategoryId = 21 và 22)
(20, N'Bàn phím cơ Akko 3098B Multi-modes', 3, 21, N'Bàn phím cơ không dây', 2200000, 45),
(21, N'Chuột Logitech G Pro X Superlight 2', 1, 22, N'Chuột Gaming siêu nhẹ cho eSport', 3200000, 35);

SET IDENTITY_INSERT Products OFF;
GO

-- ---------------- HISTORY ----------------
SET IDENTITY_INSERT SupplierPriceHistories ON;
INSERT INTO SupplierPriceHistories (Id, SupplierId, ProductId, ImportPrice, EffectiveDate) VALUES 
(1, 1, 1, 550.00, '2025-01-01'),
(2, 1, 1, 560.00, '2025-02-01'),
(3, 2, 3, 1600.00, '2025-01-15'),
(4, 3, 5, 120.00, '2025-01-10');
SET IDENTITY_INSERT SupplierPriceHistories OFF;

/* ==========================================================================
   4. INVENTORY & PURCHASING
   ========================================================================== */

-- ---------------- PURCHASE ORDERS ----------------
SET IDENTITY_INSERT PurchaseOrders ON;
INSERT INTO PurchaseOrders (Id, SupplierId, OrderDate, Status) VALUES 
(1, 1, '2025-02-01', 'Completed'),
(2, 2, '2025-02-05', 'Pending');
SET IDENTITY_INSERT PurchaseOrders OFF;

-- ---------------- PURCHASE ITEMS ----------------
SET IDENTITY_INSERT PurchaseOrderItems ON;
INSERT INTO PurchaseOrderItems (Id, PoId, ProductId, Quantity, UnitPrice) VALUES 
(1, 1, 1, 20, 560.00),
(2, 1, 2, 50, 280.00),
(3, 2, 3, 10, 1600.00);
SET IDENTITY_INSERT PurchaseOrderItems OFF;

-- ---------------- TRANSACTIONS ----------------
SET IDENTITY_INSERT InventoryTransactions ON;
INSERT INTO InventoryTransactions (Id, ProductId, QuantityChange, TransactionType, TransactionDate) VALUES 
(1, 1, 50, 'Import', '2025-02-01'),
(2, 3, 10, 'Import', '2025-02-02'),
(3, 1, -1, 'Sale', '2025-02-10'),
(4, 5, -2, 'Sale', '2025-02-11'),
(5, 1, 1, 'Return', '2025-02-12');
SET IDENTITY_INSERT InventoryTransactions OFF;

/* ==========================================================================
   5. SALES FLOW
   ========================================================================== */

-- ---------------- SESSIONS ----------------
SET IDENTITY_INSERT Sessions ON;
INSERT INTO Sessions (Id, UserId, RefreshToken, ExpiredAt) VALUES 
(1, 2, 'token_abc_123', DATEADD(day, 7, GETDATE())),
(2, 5, 'token_xyz_999', DATEADD(day, 7, GETDATE()));
SET IDENTITY_INSERT Sessions OFF;

-- ---------------- CARTS ----------------
SET IDENTITY_INSERT Carts ON;
INSERT INTO Carts (Id, UserId, CreatedAt) VALUES (1, 2, GETDATE());
SET IDENTITY_INSERT Carts OFF;

SET IDENTITY_INSERT CartItems ON;
INSERT INTO CartItems (Id, CartId, ProductId, Quantity) VALUES 
(1, 1, 4, 1),
(2, 1, 5, 2);
SET IDENTITY_INSERT CartItems OFF;

-- ---------------- ORDERS ----------------
SET IDENTITY_INSERT Orders ON;
INSERT INTO Orders (Id, UserId, OrderDate, Status, TotalAmount) VALUES 
(1, 2, '2025-02-10 08:30:00', 'Completed', 750.00),
(2, 3, '2025-02-11 09:00:00', 'Processing', 300.00),
(3, 5, '2025-02-12 14:00:00', 'Cancelled', 1800.00);
SET IDENTITY_INSERT Orders OFF;

SET IDENTITY_INSERT OrderItems ON;
INSERT INTO OrderItems (Id, OrderId, ProductId, Quantity, UnitPrice) VALUES 
(1, 1, 1, 1, 600.00),
(2, 1, 5, 1, 150.00),
(3, 2, 2, 1, 300.00),
(4, 3, 3, 1, 1800.00);
SET IDENTITY_INSERT OrderItems OFF;

-- ---------------- PAYMENTS ----------------
SET IDENTITY_INSERT Payments ON;
INSERT INTO Payments (Id, OrderId, PaymentMethod, PaymentStatus, PaymentDate) VALUES 
(1, 1, 'Credit Card', 'Success', '2025-02-10 08:35:00'),
(2, 2, 'COD', 'Pending', NULL),
(3, 3, 'Bank Transfer', 'Failed', '2025-02-12 14:05:00');
SET IDENTITY_INSERT Payments OFF;

/* ==========================================================================
   6. FORECASTS
   ========================================================================== */

INSERT INTO RevenueDaily (RevenueDate, TotalRevenue, TotalOrders) VALUES 
('2025-02-10', 750.00, 1),
('2025-02-11', 0.00, 1),
('2025-02-12', 0.00, 0);

SET IDENTITY_INSERT PriceForecasts ON;
INSERT INTO PriceForecasts (Id, ProductId, ForecastDate, PredictedPrice) VALUES 
(1, 3, '2025-03-01', 1750.00),
(2, 5, '2025-03-01', 140.00);
SET IDENTITY_INSERT PriceForecasts OFF;

SET IDENTITY_INSERT DemandForecasts ON;
INSERT INTO DemandForecasts (Id, ProductId, ForecastDate, PredictedQuantity) VALUES 
(1, 2, '2025-03-01', 150),
(2, 1, '2025-03-01', 40);
SET IDENTITY_INSERT DemandForecasts OFF;

GO