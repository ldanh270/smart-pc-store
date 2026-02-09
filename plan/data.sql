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
INSERT INTO Users (Id, Username, PasswordHash, FullName, Email, Phone, Address, Status) VALUES 
(1, 'admin', 'hash_pw_1', 'System Admin', 'admin@pcstore.com', '0901000001', 'Server Room', 'Active'),
(2, 'user_hcm', 'hash_pw_2', 'Nguyen Van A', 'vana@gmail.com', '0901000002', 'Quan 1, HCM', 'Active'),
(3, 'user_hn', 'hash_pw_3', 'Tran Thi B', 'thib@yahoo.com', '0901000003', 'Cau Giay, HN', 'Active'),
(4, 'user_dn', 'hash_pw_4', 'Le Van C', 'vanc@outlook.com', '0901000004', 'Hai Chau, DN', 'Blocked'),
(5, 'user_vip', 'hash_pw_5', 'Pham Minh Vip', 'vip@tech.com', '0901000005', 'Landmark 81, HCM', 'Active');
SET IDENTITY_INSERT Users OFF;

-- ---------------- CATEGORIES ----------------
SET IDENTITY_INSERT Categories ON;
INSERT INTO Categories (Id, CategoryName, Description) VALUES 
(1, 'CPU', 'Vi xử lý trung tâm'),
(2, 'GPU', 'Card đồ họa rời'),
(3, 'RAM', 'Bộ nhớ trong'),
(4, 'SSD', 'Ổ cứng thể rắn'),
(5, 'Mainboard', 'Bo mạch chủ');
SET IDENTITY_INSERT Categories OFF;

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
SET IDENTITY_INSERT Products ON;
INSERT INTO Products (Id, ProductName, SupplierId, CategoryId, Description, CurrentPrice, Quantity) VALUES 
(1, 'Core i9-14900K', 1, 1, 'Intel Gen 14 Flagship', 600.00, 50),
(2, 'Core i5-13600K', 1, 1, 'Intel Mid-range King', 300.00, 100),
(3, 'RTX 4090 OC', 2, 2, 'Top Tier Gaming GPU', 1800.00, 10),
(4, 'RTX 4070 Ti', 2, 2, 'High Performance GPU', 800.00, 25),
(5, 'Samsung 990 Pro 1TB', 3, 4, 'Fastest NVMe SSD', 150.00, 200),
(6, 'ROG Maximus Z790', 4, 5, 'Extreme Mainboard', 500.00, 15);
SET IDENTITY_INSERT Products OFF;

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