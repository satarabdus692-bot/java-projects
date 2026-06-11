# 🛒 ShopManager Pro

A fully functional **Shop Management & Point-of-Sale desktop application** built with Java Swing and MySQL. Developed as a semester project demonstrating real-world application architecture using DAO pattern, Singleton design, and JDBC database integration.

---

## 📸 Screenshots

### Dashboard

<img width="2880" height="1800" alt="dashboard" src="https://github.com/user-attachments/assets/5eb42d64-11c0-47e1-8c12-85e0fd7f0aa8" />


### Point of Sale
<img width="2880" height="1800" alt="point_of_sale" src="https://github.com/user-attachments/assets/f7e0b776-4437-4ea4-8626-4ce6a579a71e" />


### Sales History
<img width="2880" height="1800" alt="sales_history" src="https://github.com/user-attachments/assets/1f6a744d-8caf-4fed-a384-55a6b84f8a6d" />



### Inventory Report

<img width="1347" height="1242" alt="inventory_report" src="https://github.com/user-attachments/assets/d72be613-9630-4558-a29d-787d2ae9554c" />


---

## ✨ Features

- 📊 **Dashboard** — Live stats cards showing total products, total revenue, today's revenue, total sales, and low stock count with a real-time low stock alert table
- 📦 **Inventory Management** — Add, edit, delete, and search products with category filtering and low stock colour indicators
- 🛒 **Point of Sale** — Product browser, shopping cart, quantity control, real-time change calculation, and checkout with receipt printing
- 📋 **Sales History** — Full transaction log with per-sale receipt viewer
- 🖨️ **Print Engine** — Built-in receipt and report printing using Java's Graphics2D (no external library)
- ⚠️ **Low Stock Alerts** — Products with quantity ≤ 10 highlighted in amber/red throughout the app

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| Java SE 17 | Core language |
| Java Swing | Desktop UI framework |
| MySQL 8.x | Relational database |
| JDBC | Database connectivity |
| mysql-connector-j 9.x | MySQL JDBC driver |
| NetBeans IDE | Development environment |

---

## 🏗️ Project Architecture

```
ShopManagement/
├── src/
│   └── shopmanagement/
│       ├── Main.java                  # Entry point
│       ├── db/
│       │   └── DatabaseManager.java   # MySQL connection (Singleton)
│       ├── model/
│       │   ├── Product.java           # Product data model
│       │   └── Sale.java              # Sale + SaleItem data models
│       ├── dao/
│       │   ├── ProductDAO.java        # Product database operations
│       │   └── SaleDAO.java           # Sale database operations
│       ├── ui/
│       │   ├── MainFrame.java         # Main window + sidebar navigation
│       │   ├── DashboardPanel.java    # Stats dashboard screen
│       │   ├── ProductPanel.java      # Inventory management screen
│       │   ├── SalePanel.java         # Point of Sale screen
│       │   └── SaleHistoryPanel.java  # Sales history screen
│       ├── util/
│       │   └── Utils.java             # Shared colours, fonts, components
│       └── report/
│           └── ReportEngine.java      # Print engine (Graphics2D)
├── lib/
│   ├── mysql-connector-j-9.x.x.jar   # MySQL JDBC driver (add manually)
│   └── slf4j-stub.jar
├── screenshots/                       # App screenshots
├── nbproject/                         # NetBeans project config
└── README.md
```

---

## 🗄️ Database Schema

The database and all tables are **created automatically** on first run.

```sql
-- Products table
CREATE TABLE products (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    category    VARCHAR(100) NOT NULL,
    price       DOUBLE NOT NULL,
    quantity    INT NOT NULL DEFAULT 0,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Sales table
CREATE TABLE sales (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    sale_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount  DOUBLE NOT NULL DEFAULT 0,
    amount_paid   DOUBLE NOT NULL DEFAULT 0,
    change_given  DOUBLE NOT NULL DEFAULT 0
);

-- Sale items table
CREATE TABLE sale_items (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    sale_id      INT NOT NULL,
    product_id   INT,
    product_name VARCHAR(150),
    unit_price   DOUBLE,
    quantity     INT,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE
);
```

---

## ⚙️ Setup & Installation

### Prerequisites
- Java JDK 17 or later
- MySQL Server 8.x running locally
- NetBeans IDE 18 or later
- mysql-connector-j-9.x.x.jar

### Step 1 — Clone or extract the project
```
Unzip ShopManagement.zip to any folder
```

### Step 2 — Add MySQL JDBC Driver
1. Download from: https://dev.mysql.com/downloads/connector/j/
2. Choose **Platform Independent** ZIP and extract it
3. Copy `mysql-connector-j-9.x.x.jar` into the `lib/` folder
4. In NetBeans: right-click **Libraries** → **Add JAR/Folder** → select the jar

### Step 3 — Configure database credentials
Open `src/shopmanagement/db/DatabaseManager.java` and edit:

```java
private static final String DB_HOST = "localhost";
private static final String DB_PORT = "3306";
private static final String DB_NAME = "shopmanagement";
private static final String DB_USER = "root";       // your MySQL username
private static final String DB_PASS = "1234";       // your MySQL password
```

### Step 4 — Run the project
- Press **Shift + F11** to Clean & Build
- Press **F6** to Run

> The database `shopmanagement` and all tables are created automatically on first run. 10 sample products are also seeded automatically.

---

## 🎨 Design Patterns Used

| Pattern | Where Used | Purpose |
|---------|-----------|---------|
| Singleton | `DatabaseManager`, `ProductDAO`, `SaleDAO` | Single instance throughout app |
| DAO (Data Access Object) | `ProductDAO`, `SaleDAO` | Separates SQL from UI logic |
| MVC (loosely) | model / dao / ui layers | Clean separation of concerns |
| Observer | Swing event listeners | UI reacts to user actions |
| Transaction | `SaleDAO.saveSale()` | Atomic sale + stock update |

---

## 💡 Key Concepts Demonstrated

- **JDBC** — connecting Java to MySQL, prepared statements, result sets
- **Transactions** — saving a sale and deducting stock atomically with rollback on failure
- **Swing UI** — custom dark theme, CardLayout navigation, table renderers, split panes
- **Graphics2D printing** — drawing receipts and reports manually on paper using Java's built-in print API
- **Auto-reconnect** — database connection validity check and reconnect on every query

---

## 🚀 How a Sale Works (end to end)

```
1. User selects product → stock checked against DB
2. Item added to in-memory Sale object
3. User enters amount paid → change calculated live
4. Checkout clicked:
     → ProductDAO.update() reduces stock for each item
     → SaleDAO.saveSale() saves sale + items in one transaction
     → currentSale reset, product list refreshed
5. Optional: print receipt using ReportEngine
```

---

## 📊 Viewing Data in MySQL

```sql
-- Connect via CMD
mysql -u root -p

-- Select database
USE shopmanagement;

-- View all products
SELECT * FROM products;

-- View all sales
SELECT * FROM sales ORDER BY sale_date DESC;

-- View sale items
SELECT s.id, s.sale_date, si.product_name, si.quantity, si.unit_price
FROM sales s
JOIN sale_items si ON s.id = si.sale_id;

-- Low stock products
SELECT * FROM products WHERE quantity <= 10 ORDER BY quantity ASC;
```

---

## 👨‍💻 Author

**ABDUSSATAR**
Semester Project — Object Oriented Programming / Software Engineering


---

## 📄 License

This project is for educational purposes.

---

> Built with ❤️ using Java, MySQL, and NetBeans
