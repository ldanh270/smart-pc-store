USE master
GO

IF DB_ID('SMART_PC_STORE') IS NOT NULL
BEGIN
    ALTER DATABASE SMART_PC_STORE SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE SMART_PC_STORE;
END
GO

CREATE DATABASE SMART_PC_STORE
GO

USE SMART_PC_STORE
GO

/* =========================
   DROP TABLES (Safe Order)
========================= */
-- Drop bảng con trước, bảng cha sau
IF OBJECT_ID('DemandForecasts','U') IS NOT NULL DROP TABLE DemandForecasts
IF OBJECT_ID('PriceForecasts','U') IS NOT NULL DROP TABLE PriceForecasts
IF OBJECT_ID('RevenueDaily','U') IS NOT NULL DROP TABLE RevenueDaily
IF OBJECT_ID('Payments','U') IS NOT NULL DROP TABLE Payments
IF OBJECT_ID('OrderItems','U') IS NOT NULL DROP TABLE OrderItems
IF OBJECT_ID('Orders','U') IS NOT NULL DROP TABLE Orders
IF OBJECT_ID('CartItems','U') IS NOT NULL DROP TABLE CartItems
IF OBJECT_ID('Carts','U') IS NOT NULL DROP TABLE Carts
IF OBJECT_ID('InventoryTransactions','U') IS NOT NULL DROP TABLE InventoryTransactions
IF OBJECT_ID('PurchaseOrderItems','U') IS NOT NULL DROP TABLE PurchaseOrderItems
IF OBJECT_ID('PurchaseOrders','U') IS NOT NULL DROP TABLE PurchaseOrders
IF OBJECT_ID('SupplierPriceHistories','U') IS NOT NULL DROP TABLE SupplierPriceHistories
IF OBJECT_ID('Products','U') IS NOT NULL DROP TABLE Products
IF OBJECT_ID('Categories','U') IS NOT NULL DROP TABLE Categories
IF OBJECT_ID('Suppliers','U') IS NOT NULL DROP TABLE Suppliers
IF OBJECT_ID('Sessions','U') IS NOT NULL DROP TABLE Sessions
IF OBJECT_ID('Users','U') IS NOT NULL DROP TABLE Users
GO

/* =========================
   USERS
========================= */
CREATE TABLE Users (
    Id            INT PRIMARY KEY IDENTITY(1,1), -- Tự tăng từ 1
    Username      NVARCHAR(255),
    PasswordHash  NVARCHAR(255),
    DisplayName   NVARCHAR(255),
    Email         NVARCHAR(255),
    Phone         NVARCHAR(255),
    Address       NVARCHAR(255),
    Status        NVARCHAR(30),
    Role          NVARCHAR(30),
    CreatedAt     DATETIME DEFAULT GETDATE()
);
GO

/* =========================
   SESSIONS
========================= */
CREATE TABLE Sessions (
    Id           INT PRIMARY KEY IDENTITY(1,1),
    UserId       INT, -- Phải cùng kiểu INT với Users(Id)
    RefreshToken VARCHAR(64),
    ExpiredAt    DATETIME
);
GO

ALTER TABLE Sessions 
ADD CONSTRAINT FK_Users_Session
FOREIGN KEY (UserId) REFERENCES Users(Id);
GO

/* =========================
   CATEGORIES
========================= */
CREATE TABLE Categories (
    Id           INT PRIMARY KEY IDENTITY(1,1),
    CategoryName NVARCHAR(255),
    Description  NVARCHAR(255),
    ImageUrl     NVARCHAR(500),
    Status       BIT NOT NULL DEFAULT 1,
    ParentId     INT NULL
);
GO

/* =========================
   SUPPLIERS
========================= */
CREATE TABLE Suppliers (
    Id           INT PRIMARY KEY IDENTITY(1,1),
    SupplierName NVARCHAR(255),
    ContactInfo  NVARCHAR(255),
    LeadTimeDays INT
);
GO

/* =========================
   PRODUCTS
========================= */
CREATE TABLE Products (
    Id           INT PRIMARY KEY IDENTITY(1,1),
    ProductName  NVARCHAR(255),
    SupplierId   INT,
    CategoryId   INT,
    Description  NVARCHAR(255),
    CurrentPrice DECIMAL(18,2),
    Status BIT NOT NULL DEFAULT 1,
    Quantity     INT
);
GO

ALTER TABLE Products
ADD CONSTRAINT FK_Products_Suppliers
FOREIGN KEY (SupplierId) REFERENCES Suppliers(Id);
GO

ALTER TABLE Products
ADD CONSTRAINT FK_Products_Categories
FOREIGN KEY (CategoryId) REFERENCES Categories(Id);
GO

/* =========================
   SUPPLIER PRICE HISTORY
========================= */
CREATE TABLE SupplierPriceHistories (
    Id            INT PRIMARY KEY IDENTITY(1,1),
    SupplierId    INT,
    ProductId     INT,
    ImportPrice   DECIMAL(18,2),
    EffectiveDate DATE
);
GO

ALTER TABLE SupplierPriceHistories
ADD CONSTRAINT FK_SupplierPriceHistories_Suppliers
FOREIGN KEY (SupplierId) REFERENCES Suppliers(Id);
GO

ALTER TABLE SupplierPriceHistories
ADD CONSTRAINT FK_SupplierPriceHistories_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO

ALTER TABLE SupplierPriceHistories
ADD CONSTRAINT CK_SPH_ImportPrice_Positive CHECK (ImportPrice > 0);
GO

/* =========================
   PURCHASE ORDERS
========================= */
CREATE TABLE PurchaseOrders (
    Id         INT PRIMARY KEY IDENTITY(1,1),
    SupplierId INT,
    OrderDate  DATE,
    Status     NVARCHAR(255)
);
GO

ALTER TABLE PurchaseOrders
ADD CONSTRAINT FK_PurchaseOrders_Suppliers
FOREIGN KEY (SupplierId) REFERENCES Suppliers(Id);
GO

CREATE TABLE PurchaseOrderItems (
    Id        INT PRIMARY KEY IDENTITY(1,1),
    PoId      INT,
    ProductId INT,
    Quantity  INT,
    UnitPrice DECIMAL(18,2)
);
GO

ALTER TABLE PurchaseOrderItems
ADD CONSTRAINT FK_PurchaseOrderItems_PurchaseOrders
FOREIGN KEY (PoId) REFERENCES PurchaseOrders(Id);
GO

ALTER TABLE PurchaseOrderItems
ADD CONSTRAINT FK_PurchaseOrderItems_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO

/* 1) Suppliers: thêm cột mới */
IF COL_LENGTH('dbo.Suppliers', 'ComponentTypes') IS NULL
BEGIN
ALTER TABLE dbo.Suppliers
    ADD ComponentTypes NVARCHAR(255) NULL;
END
GO

IF COL_LENGTH('dbo.Suppliers', 'Status') IS NULL
BEGIN
ALTER TABLE dbo.Suppliers
    ADD Status BIT NULL;
END
GO

IF OBJECT_ID('DF_Suppliers_Status', 'D') IS NULL
BEGIN
ALTER TABLE dbo.Suppliers
    ADD CONSTRAINT DF_Suppliers_Status DEFAULT (1) FOR Status;
END
GO

ALTER TABLE dbo.Suppliers
ALTER COLUMN Status BIT NOT NULL;
GO

/* 2) PurchaseOrders: thêm cột mới */
IF COL_LENGTH('dbo.PurchaseOrders', 'ExpectedDeliveryDate') IS NULL
BEGIN
ALTER TABLE dbo.PurchaseOrders
    ADD ExpectedDeliveryDate DATE NULL;
END
GO

IF COL_LENGTH('dbo.PurchaseOrders', 'PoCode') IS NULL
BEGIN
ALTER TABLE dbo.PurchaseOrders
    ADD PoCode NVARCHAR(100) NULL;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UX_PurchaseOrders_PoCode'
      AND object_id = OBJECT_ID('dbo.PurchaseOrders')
)
BEGIN
CREATE UNIQUE INDEX UX_PurchaseOrders_PoCode
    ON dbo.PurchaseOrders(PoCode)
    WHERE PoCode IS NOT NULL;
END
GO

/* 4) GoodsReceiptNotes */
IF OBJECT_ID('dbo.GoodsReceiptNotes', 'U') IS NULL
    BEGIN
        CREATE TABLE dbo.GoodsReceiptNotes (
                                               Id INT IDENTITY(1,1) PRIMARY KEY,
                                               PoId INT NOT NULL,
                                               ReceiptDate DATE NOT NULL CONSTRAINT DF_GRN_ReceiptDate DEFAULT (CAST(GETDATE() AS DATE)),
                                               Note NVARCHAR(500) NULL,
                                               CONSTRAINT FK_GRN_PO FOREIGN KEY (PoId) REFERENCES dbo.PurchaseOrders(Id)
        );
    END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_GRN_PoId'
      AND object_id = OBJECT_ID('dbo.GoodsReceiptNotes')
)
    BEGIN
        CREATE INDEX IX_GRN_PoId ON dbo.GoodsReceiptNotes(PoId);
    END
GO

/* 5) GoodsReceiptNoteItems */
IF OBJECT_ID('dbo.GoodsReceiptNoteItems', 'U') IS NULL
    BEGIN
        CREATE TABLE dbo.GoodsReceiptNoteItems (
                                                   Id INT IDENTITY(1,1) PRIMARY KEY,
                                                   GrnId INT NOT NULL,
                                                   ProductId INT NOT NULL,
                                                   QuantityReceived INT NOT NULL,
                                                   UnitCost DECIMAL(18,2) NOT NULL,
                                                   CONSTRAINT FK_GRNI_GRN     FOREIGN KEY (GrnId) REFERENCES dbo.GoodsReceiptNotes(Id),
                                                   CONSTRAINT FK_GRNI_Product FOREIGN KEY (ProductId) REFERENCES dbo.Products(Id),
                                                   CONSTRAINT CK_GRNI_Qty_Positive CHECK (QuantityReceived > 0),
                                                   CONSTRAINT CK_GRNI_UnitCost_Positive CHECK (UnitCost > 0)
        );
    END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_GRNI_GrnId_ProductId'
      AND object_id = OBJECT_ID('dbo.GoodsReceiptNoteItems')
)
    BEGIN
        CREATE INDEX IX_GRNI_GrnId_ProductId
            ON dbo.GoodsReceiptNoteItems(GrnId, ProductId);
    END
GO

/* =========================
   INVENTORY TRANSACTIONS
========================= */
CREATE TABLE InventoryTransactions (
    Id              INT PRIMARY KEY IDENTITY(1,1),
    ProductId       INT,
    QuantityChange  INT,
    TransactionType NVARCHAR(255),
    TransactionDate DATETIME DEFAULT GETDATE()
);
GO

ALTER TABLE InventoryTransactions
ADD CONSTRAINT FK_InventoryTransactions_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO

/* =========================
   CARTS
========================= */
CREATE TABLE Carts (
    Id        INT PRIMARY KEY IDENTITY(1,1),
    UserId    INT,
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO

ALTER TABLE Carts
ADD CONSTRAINT FK_Carts_Users
FOREIGN KEY (UserId) REFERENCES Users(Id);
GO

CREATE TABLE CartItems (
    Id        INT PRIMARY KEY IDENTITY(1,1),
    CartId    INT,
    ProductId INT,
    Quantity  INT
);
GO

ALTER TABLE CartItems
ADD CONSTRAINT FK_CartItems_Carts
FOREIGN KEY (CartId) REFERENCES Carts(Id);
GO

ALTER TABLE CartItems
ADD CONSTRAINT FK_CartItems_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO

/* =========================
   ORDERS
========================= */
CREATE TABLE Orders (
    Id          INT PRIMARY KEY IDENTITY(1,1),
    UserId      INT,
    OrderDate   DATETIME DEFAULT GETDATE(),
    Status      NVARCHAR(255),
    TotalAmount DECIMAL(18,2)
);
GO

ALTER TABLE Orders
ADD CONSTRAINT FK_Orders_Users
FOREIGN KEY (UserId) REFERENCES Users(Id);
GO

CREATE TABLE OrderItems (
    Id        INT PRIMARY KEY IDENTITY(1,1),
    OrderId   INT,
    ProductId INT,
    Quantity  INT,
    UnitPrice DECIMAL(18,2)
);
GO

ALTER TABLE OrderItems
ADD CONSTRAINT FK_OrderItems_Orders
FOREIGN KEY (OrderId) REFERENCES Orders(Id);
GO

ALTER TABLE OrderItems
ADD CONSTRAINT FK_OrderItems_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO

/* =========================
   PAYMENTS
========================= */
CREATE TABLE Payments (
    Id            INT PRIMARY KEY IDENTITY(1,1),
    OrderId       INT,
    PaymentMethod NVARCHAR(255),
    PaymentStatus NVARCHAR(255),
    PaymentDate   DATETIME DEFAULT GETDATE()
);
GO

ALTER TABLE Payments
ADD CONSTRAINT FK_Payments_Orders
FOREIGN KEY (OrderId) REFERENCES Orders(Id);
GO

/* =========================
   REVENUE DAILY
========================= */
CREATE TABLE RevenueDaily (
    RevenueDate  DATE PRIMARY KEY,
    TotalRevenue DECIMAL(18,2),
    TotalOrders  INT
);
GO

/* =========================
   FORECASTS
========================= */
CREATE TABLE PriceForecasts (
    Id             INT PRIMARY KEY IDENTITY(1,1),
    ProductId      INT,
    ForecastDate   DATE,
    PredictedPrice DECIMAL(18,2)
);
GO

ALTER TABLE PriceForecasts
ADD CONSTRAINT FK_PriceForecasts_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO

CREATE TABLE DemandForecasts (
    Id                INT PRIMARY KEY IDENTITY(1,1),
    ProductId         INT,
    ForecastDate      DATE,
    PredictedQuantity INT
);
GO

ALTER TABLE DemandForecasts
ADD CONSTRAINT FK_DemandForecasts_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO