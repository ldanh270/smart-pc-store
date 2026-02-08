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
   DROP TABLES (safe order)
========================= */
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
IF OBJECT_ID('Users','U') IS NOT NULL DROP TABLE Users
GO

/* =========================
   Users (pure ERD)
========================= */
CREATE TABLE Users (
    Id            INT PRIMARY KEY,
    Username      NVARCHAR(255),
    PasswordHash  NVARCHAR(255),

    FullName      NVARCHAR(255),
    Email         NVARCHAR(255),
    Phone         NVARCHAR(255),
    Address       NVARCHAR(255),
    Status        NVARCHAR(255),

    CreatedAt     DATETIME
);
GO

/* =========================
   CATEGORIES
========================= */
CREATE TABLE Categories (
    Id            INT PRIMARY KEY,
    CategoryName  NVARCHAR(255),
    Description   NVARCHAR(255)
);
GO

/* =========================
   SUPPLIERS
========================= */
CREATE TABLE Suppliers (
    Id            INT PRIMARY KEY,
    SupplierName  NVARCHAR(255),
    ContactInfo   NVARCHAR(255),
    LeadTimeDays  INT
);
GO

/* =========================
   PRODUCTS
========================= */
CREATE TABLE Products (
    Id            INT PRIMARY KEY,
    ProductName   NVARCHAR(255),
    SupplierId    INT,
    CategoryId    INT,
    Description   NVARCHAR(255),
    CurrentPrice  DECIMAL(18,2),
    Quantity      INT
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
    Id            INT PRIMARY KEY,
    SupplierId    INT,
    ProductId     INT,
    ImportPrice   DECIMAL(18,2),
    EffectiveDate DATE
);
GO
/*
ALTER TABLE SupplierPriceHistories
ADD CONSTRAINT FK_SupplierPriceHistories_Suppliers
FOREIGN KEY (SupplierId) REFERENCES Suppliers(Id);
GO

ALTER TABLE SupplierPriceHistories
ADD CONSTRAINT FK_SupplierPriceHistories_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO
*/
/* =========================
   PURCHASE ORDERS
========================= */
CREATE TABLE PurchaseOrders (
    Id         INT PRIMARY KEY,
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
    Id        INT PRIMARY KEY,
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
    Id              INT PRIMARY KEY,
    ProductId       INT,
    QuantityChange  INT,
    TransactionType NVARCHAR(255),
    TransactionDate DATETIME
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
    Id         INT PRIMARY KEY,
    UserId INT,
    CreatedAt  DATETIME
);
GO

ALTER TABLE Carts
ADD CONSTRAINT FK_Carts_Users
FOREIGN KEY (UserId) REFERENCES Users(Id);
GO

CREATE TABLE CartItems (
    Id        INT PRIMARY KEY,
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
    Id          INT PRIMARY KEY,
    UserId      INT,
    OrderDate   DATETIME,
    Status      NVARCHAR(255),
    TotalAmount DECIMAL(18,2)
);
GO

ALTER TABLE Orders
ADD CONSTRAINT FK_Orders_Users
FOREIGN KEY (UserId) REFERENCES Users(Id);
GO

CREATE TABLE OrderItems (
    Id        INT PRIMARY KEY,
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
    Id            INT PRIMARY KEY,
    OrderId       INT,
    PaymentMethod NVARCHAR(255),
    PaymentStatus NVARCHAR(255),
    PaymentDate   DATETIME
);
GO
/*
ALTER TABLE Payments
ADD CONSTRAINT FK_Payments_Orders
FOREIGN KEY (OrderId) REFERENCES Orders(Id);
GO
*/
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
    Id            INT PRIMARY KEY,
    ProductId     INT,
    ForecastDate  DATE,
    PredictedPrice DECIMAL(18,2)
);
GO
/*
ALTER TABLE PriceForecasts
ADD CONSTRAINT FK_PriceForecasts_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO
*/

CREATE TABLE DemandForecasts (
    Id               INT PRIMARY KEY,
    ProductId        INT,
    ForecastDate     DATE,
    PredictedQuantity INT
);
GO
/*
ALTER TABLE DemandForecasts
ADD CONSTRAINT FK_DemandForecasts_Products
FOREIGN KEY (ProductId) REFERENCES Products(Id);
GO
*/
