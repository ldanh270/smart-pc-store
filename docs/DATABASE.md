# Database Schema Documentation

## Overview

The database uses **PostgreSQL** with **14 normalized tables** designed according to **Third Normal Form (3NF)** principles. The schema supports both B2C (Business-to-Consumer) and B2B (Business-to-Business) operations.

## Entity Relationship Diagram

```
┌──────────────┐         ┌──────────────┐
│    Users     │────────<│   Sessions   │
│              │  1    N │              │
└──────┬───────┘         └──────────────┘
       │                           
       │ 1                         
       │                           
       │ N                         
┌──────▼───────┐         ┌──────────────────┐
│    Carts     │────────<│   CartItems      │
│              │  1    N │                  │
└──────────────┘         └────────┬─────────┘
       │                          │
       │ 1                        │ N
       │                          │
       │                          │ 1
┌──────▼───────┐         ┌────────▼──────────┐
│    Orders    │         │    Products       │
│              │         │                   │
└──────┬───────┘         └────────┬──────────┘
       │                          │
       │ 1                        │ N
       │                          │
       │ N                        │ 1
┌──────▼──────────┐     ┌─────────▼──────────┐
│   OrderDetails  │────>│   Categories       │
│                 │     │   (Hierarchical)   │
└──────┬──────────┘     └────────────────────┘
       │                          
       │ N                        
       │                          
       │ 1                        
┌──────▼──────────┐     ┌────────────────────┐
│   Payments      │     │     Suppliers      │
│                 │     │                    │
└─────────────────┘     └────────┬───────────┘
                                 │
                                 │ 1
                                 │
                                 │ N
                        ┌────────▼───────────┐
                        │  PurchaseOrders    │
                        │                    │
                        └────────┬───────────┘
                                 │
                                 │ 1
                                 │
                                 │ N
                        ┌────────▼─────────────┐
                        │ PurchaseOrderItems   │
                        │                      │
                        └──────────────────────┘
                                 │
                                 │ N
                                 │
                                 │ 1
                        ┌────────▼──────────────┐
                        │ InventoryTransactions │
                        │                       │
                        └───────────────────────┘
```

## Tables

### 1. Users

Stores user accounts and authentication information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique user identifier |
| username | VARCHAR(255) | UNIQUE, NOT NULL | Login username |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| display_name | VARCHAR(255) | | User's display name |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Email address |
| phone | VARCHAR(20) | | Phone number |
| address | TEXT | | Shipping address |
| status | VARCHAR(30) | DEFAULT 'ACTIVE' | Account status (ACTIVE, INACTIVE, BANNED) |
| role | VARCHAR(30) | DEFAULT 'CUSTOMER' | User role (ADMIN, STAFF, CUSTOMER) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation time |

**Indexes:**
- `idx_users_username` on `username`
- `idx_users_email` on `email`
- `idx_users_role` on `role`

---

### 2. Sessions

Manages refresh tokens for authentication.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Session identifier |
| user_id | UUID | FOREIGN KEY → Users.id | Owner of the session |
| refresh_token | VARCHAR(64) | UNIQUE, NOT NULL | UUID refresh token |
| expired_at | TIMESTAMP | NOT NULL | Token expiration time |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Session creation time |

**Indexes:**
- `idx_sessions_refresh_token` on `refresh_token`
- `idx_sessions_user_id` on `user_id`
- `idx_sessions_expired_at` on `expired_at`

**Constraints:**
- `FK_sessions_user` FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE

---

### 3. Categories

Product categories with hierarchical structure.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INT | PRIMARY KEY AUTO_INCREMENT | Category identifier |
| category_name | VARCHAR(255) | NOT NULL | Category name |
| description | TEXT | | Category description |
| image_url | VARCHAR(500) | | Category image URL |
| status | BOOLEAN | DEFAULT true | Active status |
| parent_id | INT | FOREIGN KEY → Categories.id | Parent category (NULL for root) |

**Indexes:**
- `idx_categories_parent_id` on `parent_id`
- `idx_categories_status` on `status`

**Constraints:**
- `FK_categories_parent` FOREIGN KEY (parent_id) REFERENCES Categories(id) ON DELETE SET NULL

**Example Hierarchy:**
```
Computer Components (parent_id: NULL)
├── CPU (parent_id: 1)
├── GPU (parent_id: 1)
├── RAM (parent_id: 1)
└── Storage (parent_id: 1)
    ├── SSD (parent_id: 5)
    └── HDD (parent_id: 5)
```

---

### 4. Suppliers

Supplier information for procurement.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INT | PRIMARY KEY AUTO_INCREMENT | Supplier identifier |
| supplier_name | VARCHAR(255) | NOT NULL | Supplier name |
| contact_info | TEXT | | Contact information (address, phone, email) |
| lead_time_days | INT | DEFAULT 7 | Expected delivery time in days |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation time |

**Indexes:**
- `idx_suppliers_name` on `supplier_name`

---

### 5. Products

Product catalog with inventory information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INT | PRIMARY KEY AUTO_INCREMENT | Product identifier |
| product_name | VARCHAR(255) | NOT NULL | Product name |
| description | TEXT | | Detailed description |
| price | DECIMAL(18,2) | NOT NULL | Selling price |
| stock | INT | DEFAULT 0 | Available quantity |
| image_url | VARCHAR(500) | | Product image URL |
| category_id | INT | FOREIGN KEY → Categories.id | Product category |
| supplier_id | INT | FOREIGN KEY → Suppliers.id | Primary supplier |
| status | VARCHAR(30) | DEFAULT 'AVAILABLE' | Product status (AVAILABLE, OUT_OF_STOCK, DISCONTINUED) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Product creation time |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update time |

**Indexes:**
- `idx_products_category_id` on `category_id`
- `idx_products_supplier_id` on `supplier_id`
- `idx_products_status` on `status`
- `idx_products_name` on `product_name` (for search)

**Constraints:**
- `FK_products_category` FOREIGN KEY (category_id) REFERENCES Categories(id)
- `FK_products_supplier` FOREIGN KEY (supplier_id) REFERENCES Suppliers(id)
- `CHK_products_price` CHECK (price >= 0)
- `CHK_products_stock` CHECK (stock >= 0)

---

### 6. Carts

Shopping carts for users.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Cart identifier |
| user_id | UUID | UNIQUE, FOREIGN KEY → Users.id | Cart owner (one cart per user) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Cart creation time |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update time |

**Indexes:**
- `idx_carts_user_id` on `user_id`

**Constraints:**
- `FK_carts_user` FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE

---

### 7. CartItems

Items in shopping carts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Cart item identifier |
| cart_id | UUID | FOREIGN KEY → Carts.id | Parent cart |
| product_id | INT | FOREIGN KEY → Products.id | Product reference |
| quantity | INT | NOT NULL | Item quantity |
| price_at_add | DECIMAL(18,2) | NOT NULL | Price snapshot at add time |
| added_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Time added to cart |

**Indexes:**
- `idx_cart_items_cart_id` on `cart_id`
- `idx_cart_items_product_id` on `product_id`

**Constraints:**
- `FK_cart_items_cart` FOREIGN KEY (cart_id) REFERENCES Carts(id) ON DELETE CASCADE
- `FK_cart_items_product` FOREIGN KEY (product_id) REFERENCES Products(id)
- `UQ_cart_product` UNIQUE (cart_id, product_id) - Prevent duplicate products in same cart
- `CHK_cart_items_quantity` CHECK (quantity > 0)

---

### 8. Orders

Customer orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Order identifier |
| user_id | UUID | FOREIGN KEY → Users.id | Customer who placed order |
| order_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Order creation time |
| status | VARCHAR(30) | DEFAULT 'PENDING' | Order status |
| total_amount | DECIMAL(18,2) | NOT NULL | Total order amount |
| shipping_address | TEXT | NOT NULL | Delivery address |
| shipping_phone | VARCHAR(20) | NOT NULL | Contact phone |
| notes | TEXT | | Customer notes |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last status update |

**Valid Status Values:**
- PENDING: Order placed, awaiting processing
- PROCESSING: Being prepared
- CONFIRMED: Confirmed and ready to ship
- SHIPPING: Out for delivery
- DELIVERED: Delivered to customer
- COMPLETED: Transaction completed
- CANCELLED: Order cancelled

**Indexes:**
- `idx_orders_user_id` on `user_id`
- `idx_orders_status` on `status`
- `idx_orders_order_date` on `order_date`

**Constraints:**
- `FK_orders_user` FOREIGN KEY (user_id) REFERENCES Users(id)
- `CHK_orders_amount` CHECK (total_amount >= 0)

---

### 9. OrderDetails

Line items in orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Order detail identifier |
| order_id | UUID | FOREIGN KEY → Orders.id | Parent order |
| product_id | INT | FOREIGN KEY → Products.id | Product ordered |
| quantity | INT | NOT NULL | Quantity ordered |
| unit_price | DECIMAL(18,2) | NOT NULL | Price per unit at order time |
| subtotal | DECIMAL(18,2) | NOT NULL | Line item total (quantity * unit_price) |

**Indexes:**
- `idx_order_details_order_id` on `order_id`
- `idx_order_details_product_id` on `product_id`

**Constraints:**
- `FK_order_details_order` FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
- `FK_order_details_product` FOREIGN KEY (product_id) REFERENCES Products(id)
- `CHK_order_details_quantity` CHECK (quantity > 0)
- `CHK_order_details_subtotal` CHECK (subtotal >= 0)

---

### 10. Payments

Payment information for orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Payment identifier |
| order_id | UUID | UNIQUE, FOREIGN KEY → Orders.id | Associated order (one payment per order) |
| payment_method | VARCHAR(50) | NOT NULL | Payment method used |
| payment_status | VARCHAR(30) | DEFAULT 'PENDING' | Payment status |
| amount | DECIMAL(18,2) | NOT NULL | Payment amount |
| transaction_id | VARCHAR(255) | | External transaction ID from payment gateway |
| payment_date | TIMESTAMP | | Payment completion time |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation time |

**Payment Methods:**
- COD: Cash on Delivery
- BANK_TRANSFER: Bank transfer
- E_WALLET: E-wallet (MoMo, ZaloPay, VNPay)
- CREDIT_CARD: Credit/Debit card

**Payment Statuses:**
- PENDING: Awaiting payment
- COMPLETED: Payment successful
- FAILED: Payment failed
- REFUNDED: Payment refunded

**Indexes:**
- `idx_payments_order_id` on `order_id`
- `idx_payments_status` on `payment_status`
- `idx_payments_transaction_id` on `transaction_id`

**Constraints:**
- `FK_payments_order` FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
- `CHK_payments_amount` CHECK (amount >= 0)

---

### 11. PurchaseOrders

Orders placed to suppliers for inventory replenishment.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Purchase order identifier |
| supplier_id | INT | FOREIGN KEY → Suppliers.id | Supplier |
| order_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | PO creation date |
| expected_delivery_date | DATE | | Expected delivery date |
| status | VARCHAR(30) | DEFAULT 'PENDING' | PO status |
| total_cost | DECIMAL(18,2) | NOT NULL | Total cost |
| notes | TEXT | | Internal notes |
| created_by | UUID | FOREIGN KEY → Users.id | User who created PO |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation time |

**PO Statuses:**
- PENDING: Awaiting supplier confirmation
- CONFIRMED: Confirmed by supplier
- RECEIVED: Goods received
- CANCELLED: PO cancelled

**Indexes:**
- `idx_po_supplier_id` on `supplier_id`
- `idx_po_status` on `status`
- `idx_po_order_date` on `order_date`

**Constraints:**
- `FK_po_supplier` FOREIGN KEY (supplier_id) REFERENCES Suppliers(id)
- `FK_po_created_by` FOREIGN KEY (created_by) REFERENCES Users(id)

---

### 12. PurchaseOrderItems

Line items in purchase orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | PO item identifier |
| purchase_order_id | UUID | FOREIGN KEY → PurchaseOrders.id | Parent PO |
| product_id | INT | FOREIGN KEY → Products.id | Product ordered |
| quantity | INT | NOT NULL | Quantity ordered |
| unit_cost | DECIMAL(18,2) | NOT NULL | Cost per unit |
| subtotal | DECIMAL(18,2) | NOT NULL | Line item total |

**Indexes:**
- `idx_po_items_po_id` on `purchase_order_id`
- `idx_po_items_product_id` on `product_id`

**Constraints:**
- `FK_po_items_po` FOREIGN KEY (purchase_order_id) REFERENCES PurchaseOrders(id) ON DELETE CASCADE
- `FK_po_items_product` FOREIGN KEY (product_id) REFERENCES Products(id)

---

### 13. InventoryTransactions

Audit trail for all inventory movements.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Transaction identifier |
| product_id | INT | FOREIGN KEY → Products.id | Product affected |
| transaction_type | VARCHAR(30) | NOT NULL | Type of transaction |
| quantity_change | INT | NOT NULL | Quantity change (+ or -) |
| reference_id | UUID | | Reference to order/PO |
| reference_type | VARCHAR(50) | | Type of reference (ORDER, PURCHASE_ORDER) |
| notes | TEXT | | Transaction notes |
| created_by | UUID | FOREIGN KEY → Users.id | User who performed action |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Transaction time |

**Transaction Types:**
- PURCHASE_IN: Stock added from purchase order
- ORDER_OUT: Stock removed for customer order
- ADJUSTMENT: Manual adjustment
- RETURN: Customer return

**Indexes:**
- `idx_inv_trans_product_id` on `product_id`
- `idx_inv_trans_type` on `transaction_type`
- `idx_inv_trans_created_at` on `created_at`
- `idx_inv_trans_reference` on `(reference_type, reference_id)`

**Constraints:**
- `FK_inv_trans_product` FOREIGN KEY (product_id) REFERENCES Products(id)
- `FK_inv_trans_created_by` FOREIGN KEY (created_by) REFERENCES Users(id)

**Purpose**: 
- Complete audit trail of inventory changes
- Reconciliation and reporting
- Fraud detection and compliance

---

### 14. RevenueDaily

Aggregated daily revenue for analytics.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INT | PRIMARY KEY AUTO_INCREMENT | Record identifier |
| date | DATE | UNIQUE, NOT NULL | Date |
| total_revenue | DECIMAL(18,2) | DEFAULT 0 | Total revenue for the day |
| order_count | INT | DEFAULT 0 | Number of orders |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | Last update time |

**Indexes:**
- `idx_revenue_date` on `date`

**Purpose**: Pre-aggregated data for fast analytics and reporting.

---

## Relationships Summary

```
Users 1──N Sessions (One user can have multiple active sessions)
Users 1──1 Carts (One user has one cart)
Carts 1──N CartItems (One cart contains multiple items)
Products N──1 Categories (Many products belong to one category)
Products N──1 Suppliers (Many products from one supplier)
Users 1──N Orders (One user can place multiple orders)
Orders 1──N OrderDetails (One order contains multiple products)
Orders 1──1 Payments (One order has one payment)
Suppliers 1──N PurchaseOrders (One supplier receives multiple POs)
PurchaseOrders 1──N PurchaseOrderItems (One PO contains multiple products)
Products 1──N InventoryTransactions (Track all stock changes for a product)
Categories 1──N Categories (Self-referencing for hierarchy)
```

## Database Files

- **Schema DDL**: `plan/schema.sql` - Complete table definitions
- **Sample Data**: `plan/data.sql` - Test data for development
- **Persistence Config**: `src/main/resources/META-INF/persistence.xml`

## Performance Considerations

### Indexes

Strategic indexes on frequently queried columns:
- Foreign keys for join performance
- Status fields for filtering
- Timestamp fields for date range queries
- Unique constraints for data integrity

### Query Optimization

- Use JOIN instead of N+1 queries
- Implement pagination for large result sets
- Use EXPLAIN ANALYZE to identify slow queries
- Regular VACUUM and ANALYZE for PostgreSQL maintenance

### Connection Pooling

Configure appropriate connection pool size:
```xml
<property name="hibernate.hikari.maximumPoolSize" value="20"/>
<property name="hibernate.hikari.minimumIdle" value="5"/>
```

## Backup Strategy

### Recommended Backup Schedule

- **Full backup**: Daily at off-peak hours
- **Incremental backup**: Every 6 hours
- **Transaction log backup**: Every hour
- **Retention**: Keep 30 days of backups

### Backup Commands

```bash
# Full backup
pg_dump -U postgres -d smart_pc_store > backup_$(date +%Y%m%d).sql

# Backup with compression
pg_dump -U postgres -d smart_pc_store | gzip > backup_$(date +%Y%m%d).sql.gz

# Restore from backup
psql -U postgres -d smart_pc_store < backup_20260309.sql
```

## Migration Strategy

### Version Control

- Use Flyway or Liquibase for database migrations
- Store migrations in `src/main/resources/db/migration/`
- Follow naming convention: `V{version}__{description}.sql`

### Example Migration

```sql
-- V1__initial_schema.sql
CREATE TABLE users (...);

-- V2__add_user_status.sql
ALTER TABLE users ADD COLUMN status VARCHAR(30) DEFAULT 'ACTIVE';

-- V3__create_index_users_email.sql
CREATE INDEX idx_users_email ON users(email);
```

---

## Related Documentation

- [Installation Guide](./INSTALLATION.md)
- [API Documentation](./API_DOCUMENTATION.md)
- [Architecture Guide](./ARCHITECTURE.md)
- [Security Guide](./SECURITY.md)
