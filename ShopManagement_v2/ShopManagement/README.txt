╔══════════════════════════════════════════════════════════════╗
║         SHOPMANAGER PRO — SETUP INSTRUCTIONS                 ║
║         NetBeans + MySQL Edition                             ║
╚══════════════════════════════════════════════════════════════╝

QUICK START (3 steps)
─────────────────────

STEP 1 — Download MySQL JDBC Connector
  • Visit: https://dev.mysql.com/downloads/connector/j/
  • Choose "Platform Independent" from the OS dropdown
  • Download the ZIP file
  • Extract it and find:  mysql-connector-j-8.x.x.jar
  • Copy that .jar file into the  lib/  folder of this project
  • Rename it to exactly:  mysql-connector-j-8.3.0.jar
    (OR edit nbproject/project.properties line:
     file.reference.mysql-connector=lib/YOUR-ACTUAL-FILENAME.jar)

STEP 2 — Configure your MySQL credentials
  • Open:  src/shopmanagement/db/DatabaseManager.java
  • Edit lines 11–15:

        private static final String DB_HOST = "localhost";
        private static final String DB_PORT = "3306";
        private static final String DB_NAME = "shopmanagement";  ← database name (auto-created)
        private static final String DB_USER = "root";            ← your MySQL username
        private static final String DB_PASS = "1234";            ← your MySQL password

  • Make sure MySQL Server is running.
  • The database "shopmanagement" and all tables are created AUTOMATICALLY
    on first run — you do NOT need to run any SQL scripts.

STEP 3 — Open in NetBeans & Add Library
  • File → Open Project → select this folder
  • Right-click project → Properties → Libraries → Add JAR/Folder
  • Add the mysql-connector-j-8.x.x.jar from the lib/ folder
  • Clean & Build (Shift+F11), then Run (F6)


FEATURES
────────
  • Dashboard    — live stats, low-stock alerts
  • Inventory    — add / edit / delete products, search, category filter
  • Point of Sale — cart, qty control, change calculation, receipt print
  • Sales History — all transactions with receipt viewer
  • Reports       — printable Inventory & Sales reports (Java2D, no extra libs)


TROUBLESHOOTING
───────────────
  "Driver not found"
    → The mysql-connector jar is not in lib/ or not added to NetBeans Libraries

  "Cannot connect to MySQL"
    → MySQL server is not running, or DB_USER/DB_PASS are wrong in DatabaseManager.java

  "Access denied for user 'root'"
    → Change DB_USER and DB_PASS to match your MySQL installation


TESTED WITH
───────────
  • NetBeans 18+ / 19+
  • Java JDK 17 or 21
  • MySQL 8.0 / 8.1 / 8.2 / 8.3
  • mysql-connector-j 8.x.x
