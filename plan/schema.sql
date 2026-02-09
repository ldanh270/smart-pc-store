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
    FullName      NVARCHAR(255),
    Email         NVARCHAR(255),
    Phone         NVARCHAR(255),
    Address       NVARCHAR(255),
    Status        NVARCHAR(255),
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
    Description  NVARCHAR(255)
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