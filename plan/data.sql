/* ========================================================
   INSERT DATA (10 rows per table)
======================================================== */

-- 1. Users
INSERT INTO Users VALUES 
(1, 'anh.le', 'hash1', 'Le Duc Anh', 'anh@tech.com', '091', 'Da Nang', 'Active', '2026-01-01'),
(2, 'minh.nguyen', 'hash2', 'Nguyen Quang Minh', 'minh@test.com', '092', 'Ha Noi', 'Active', '2026-01-02'),
(3, 'hoa.pham', 'hash3', 'Pham Thi Hoa', 'hoa@sale.com', '093', 'HCM', 'Active', '2026-01-03'),
(4, 'dung.tran', 'hash4', 'Tran Anh Dung', 'dung@dev.com', '094', 'Da Nang', 'Inactive', '2026-01-04'),
(5, 'linh.vu', 'hash5', 'Vu Dieu Linh', 'linh@web.com', '095', 'Hue', 'Active', '2026-01-05'),
(6, 'bao.hoang', 'hash6', 'Hoang Gia Bao', 'bao@store.com', '096', 'Can Tho', 'Active', '2026-01-06'),
(7, 'tu.dang', 'hash7', 'Dang Thanh Tu', 'tu@data.com', '097', 'Hai Phong', 'Active', '2026-01-07'),
(8, 'ngoc.mai', 'hash8', 'Mai Hong Ngoc', 'ngoc@cloud.com', '098', 'Da Lat', 'Active', '2026-01-08'),
(9, 'thanh.le', 'hash9', 'Le Chi Thanh', 'thanh@ai.com', '099', 'Vung Tau', 'Active', '2026-01-09'),
(10, 'phuc.do', 'hash10', 'Do Hoang Phuc', 'phuc@blockchain.com', '088', 'Nha Trang', 'Active', '2026-01-10');

-- 2. Categories
INSERT INTO Categories VALUES 
(1, 'CPU', 'Processors'), (2, 'GPU', 'Graphics Cards'), (3, 'RAM', 'Memory'), (4, 'SSD', 'Storage'), (5, 'Mainboard', 'Motherboards'),
(6, 'PSU', 'Power Supply'), (7, 'Case', 'Chassis'), (8, 'Cooling', 'Fans & AIO'), (9, 'Monitor', 'Displays'), (10, 'Mouse', 'Peripherals');

-- 3. Suppliers
INSERT INTO Suppliers VALUES 
(1, 'Intel', 'intel.com', 7), (2, 'NVIDIA', 'nvidia.com', 10), (3, 'Samsung', 'samsung.com', 5), (4, 'ASUS', 'asus.com', 8), (5, 'Corsair', 'corsair.com', 6),
(6, 'Gigabyte', 'gigabyte.com', 9), (7, 'MSI', 'msi.com', 7), (8, 'Western Digital', 'wdc.com', 4), (9, 'Kingston', 'kingston.com', 5), (10, 'Logitech', 'logitech.com', 3);

-- 4. Products
INSERT INTO Products VALUES 
(1, 'Core i9-13900K', 1, 1, 'High-end CPU', 589.00, 50),
(2, 'RTX 4090', 2, 2, 'Extreme GPU', 1599.00, 20),
(3, '980 Pro 1TB', 3, 4, 'NVMe SSD', 120.00, 100),
(4, 'ROG Strix Z790', 4, 5, 'Gaming Board', 450.00, 30),
(5, 'Vengeance 32GB', 5, 3, 'DDR5 RAM', 150.00, 80),
(6, 'Aorus RTX 4080', 6, 2, 'Premium GPU', 1199.00, 15),
(7, 'MPG A850G', 7, 6, '850W Gold PSU', 160.00, 40),
(8, 'Blue 2TB', 8, 4, 'SATA SSD', 140.00, 60),
(9, 'Fury Renegade', 9, 3, 'Performance RAM', 130.00, 70),
(10, 'G502 Hero', 10, 10, 'Gaming Mouse', 50.00, 200);

-- 5. SupplierPriceHistories
INSERT INTO SupplierPriceHistories VALUES 
(1,1,1,550.00,'2026-01-01'), (2,2,2,1450.00,'2026-01-01'), (3,3,3,100.00,'2026-01-01'), (4,4,4,400.00,'2026-01-01'), (5,5,5,130.00,'2026-01-01'),
(6,6,6,1100.00,'2026-01-01'), (7,7,7,140.00,'2026-01-01'), (8,8,8,120.00,'2026-01-01'), (9,9,9,115.00,'2026-01-01'), (10,10,10,40.00,'2026-01-01');

-- 6. PurchaseOrders
INSERT INTO PurchaseOrders VALUES 
(1,1,'2026-02-01','Completed'), (2,2,'2026-02-02','Completed'), (3,3,'2026-02-03','Pending'), (4,4,'2026-02-04','Shipped'), (5,5,'2026-02-05','Completed'),
(6,6,'2026-02-06','Completed'), (7,7,'2026-02-07','Pending'), (8,8,'2026-02-08','Completed'), (9,9,'2026-02-08','Completed'), (10,10,'2026-02-08','Completed');

-- 7. PurchaseOrderItems
INSERT INTO PurchaseOrderItems VALUES 
(1,1,1,10,550.00), (2,2,2,5,1450.00), (3,3,3,50,100.00), (4,4,4,15,400.00), (5,5,5,30,130.00),
(6,6,6,10,1100.00), (7,7,7,20,140.00), (8,8,8,25,120.00), (9,9,9,40,115.00), (10,10,10,100,40.00);

-- 8. InventoryTransactions
INSERT INTO InventoryTransactions VALUES 
(1,1,10,'Import','2026-02-02'), (2,2,5,'Import','2026-02-03'), (3,1,-2,'Export','2026-02-04'), (4,3,50,'Import','2026-02-04'), (5,4,15,'Import','2026-02-05'),
(6,5,30,'Import','2026-02-06'), (7,2,-1,'Export','2026-02-07'), (8,6,10,'Import','2026-02-07'), (9,8,25,'Import','2026-02-08'), (10,10,100,'Import','2026-02-08');

-- 9. Carts
INSERT INTO Carts VALUES 
(1,1,'2026-02-01'), (2,2,'2026-02-02'), (3,3,'2026-02-03'), (4,4,'2026-02-04'), (5,5,'2026-02-05'),
(6,6,'2026-02-06'), (7,7,'2026-02-07'), (8,8,'2026-02-08'), (9,9,'2026-02-08'), (10,10,'2026-02-08');

-- 10. CartItems
INSERT INTO CartItems VALUES 
(1,1,1,1), (2,2,2,1), (3,3,3,2), (4,4,4,1), (5,5,5,4), (6,6,6,1), (7,7,7,2), (8,8,8,1), (9,9,9,2), (10,10,10,5);

-- 11. Orders
INSERT INTO Orders VALUES 
(1,1,'2026-02-01','Paid',589.00), (2,2,'2026-02-02','Paid',1599.00), (3,3,'2026-02-03','Pending',240.00), (4,4,'2026-02-04','Paid',450.00), (5,5,'2026-02-05','Cancelled',600.00),
(6,6,'2026-02-06','Paid',1199.00), (7,7,'2026-02-07','Paid',320.00), (8,8,'2026-02-08','Paid',140.00), (9,9,'2026-02-08','Paid',260.00), (10,10,'2026-02-08','Paid',250.00);

-- 12. OrderItems
INSERT INTO OrderItems VALUES 
(1,1,1,1,589.00), (2,2,2,1,1599.00), (3,3,3,2,120.00), (4,4,4,1,450.00), (5,5,5,4,150.00),
(6,6,6,1,1199.00), (7,7,7,2,160.00), (8,8,8,1,140.00), (9,9,9,2,130.00), (10,10,10,5,50.00);

-- 13. Payments
INSERT INTO Payments VALUES 
(1,1,'E-Wallet','Success','2026-02-01'), (2,2,'Bank Transfer','Success','2026-02-02'), (3,4,'Credit Card','Success','2026-02-04'), (4,6,'Bank Transfer','Success','2026-02-06'), (5,7,'Cash','Success','2026-02-07'),
(6,8,'E-Wallet','Success','2026-02-08'), (7,9,'Credit Card','Success','2026-02-08'), (8,10,'Cash','Success','2026-02-08'), (9,1, 'E-Wallet', 'Success', '2026-02-01'), (10,2, 'Bank', 'Success', '2026-02-02');

-- 14. RevenueDaily
INSERT INTO RevenueDaily VALUES 
('2026-02-01',589.00,1), ('2026-02-02',1599.00,1), ('2026-02-03',0,0), ('2026-02-04',450.00,1), ('2026-02-05',0,0),
('2026-02-06',1199.00,1), ('2026-02-07',320.00,1), ('2026-02-08',650.00,3), ('2026-02-09',0,0), ('2026-02-10',0,0);

-- 15. PriceForecasts
INSERT INTO PriceForecasts VALUES 
(1,1,'2026-03-01',570.00), (2,2,'2026-03-01',1550.00), (3,3,'2026-03-01',110.00), (4,4,'2026-03-01',430.00), (5,5,'2026-03-01',145.00),
(6,6,'2026-03-01',1150.00), (7,7,'2026-03-01',155.00), (8,8,'2026-03-01',135.00), (9,9,'2026-03-01',125.00), (10,10,'2026-03-01',45.00);

-- 16. DemandForecasts
INSERT INTO DemandForecasts VALUES 
(1,1,'2026-03-01',20), (2,2,'2026-03-01',10), (3,3,'2026-03-01',40), (4,4,'2026-03-01',15), (5,5,'2026-03-01',25),
(6,6,'2026-03-01',8), (7,7,'2026-03-01',12), (8,8,'2026-03-01',18), (9,9,'2026-03-01',22), (10,10,'2026-03-01',50);
GO