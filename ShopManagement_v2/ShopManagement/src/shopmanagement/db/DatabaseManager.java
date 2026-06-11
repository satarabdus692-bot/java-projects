package shopmanagement.db;

import java.sql.*;
import javax.swing.JOptionPane;

public class DatabaseManager {

    // ── MySQL Configuration ── EDIT THESE TO MATCH YOUR SETUP ──────────────────
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "shopmanagement";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";
    // ───────────────────────────────────────────────────────────────────────────

    private static final String MYSQL_URL =
        "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
        + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
        + "&createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(MYSQL_URL, DB_USER, DB_PASS);
            connection.setAutoCommit(true);
            createSchema();
            seedData();
        } catch (ClassNotFoundException e) {
            String msg = "<html><b>MySQL JDBC Driver not found!</b><br><br>"
                + "Please add <b>mysql-connector-j-8.x.x.jar</b> to the <b>lib/</b> folder,<br>"
                + "then add it to the NetBeans project Libraries.<br><br>"
                + "Download: https://dev.mysql.com/downloads/connector/j/<br>"
                + "(Choose Platform Independent ZIP, extract the .jar)</html>";
            JOptionPane.showMessageDialog(null, msg, "Driver Missing", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("MySQL Driver missing", e);
        } catch (SQLException e) {
            String msg = "<html><b>Cannot connect to MySQL!</b><br><br>"
                + "Host: " + DB_HOST + ":" + DB_PORT + "<br>"
                + "Database: " + DB_NAME + "<br>"
                + "User: " + DB_USER + "<br><br>"
                + "<b>Error:</b> " + e.getMessage() + "<br><br>"
                + "Edit <b>DatabaseManager.java</b> with correct credentials.</html>";
            JOptionPane.showMessageDialog(null, msg, "Connection Failed", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("MySQL connection failed", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    /** Returns a live connection, reconnecting if needed. */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = DriverManager.getConnection(MYSQL_URL, DB_USER, DB_PASS);
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private void createSchema() throws SQLException {
        Statement st = connection.createStatement();

        st.execute("CREATE TABLE IF NOT EXISTS products ("
                + "id          INT AUTO_INCREMENT PRIMARY KEY,"
                + "name        VARCHAR(150) NOT NULL,"
                + "category    VARCHAR(100) NOT NULL,"
                + "price       DOUBLE NOT NULL,"
                + "quantity    INT NOT NULL DEFAULT 0,"
                + "description TEXT,"
                + "created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        st.execute("CREATE TABLE IF NOT EXISTS sales ("
                + "id            INT AUTO_INCREMENT PRIMARY KEY,"
                + "sale_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "total_amount  DOUBLE NOT NULL DEFAULT 0,"
                + "amount_paid   DOUBLE NOT NULL DEFAULT 0,"
                + "change_given  DOUBLE NOT NULL DEFAULT 0"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        st.execute("CREATE TABLE IF NOT EXISTS sale_items ("
                + "id           INT AUTO_INCREMENT PRIMARY KEY,"
                + "sale_id      INT NOT NULL,"
                + "product_id   INT,"
                + "product_name VARCHAR(150),"
                + "unit_price   DOUBLE,"
                + "quantity     INT,"
                + "FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        st.close();
    }

    private void seedData() throws SQLException {
        try (ResultSet rs = connection.createStatement()
                .executeQuery("SELECT COUNT(*) FROM products")) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }

        String sql = "INSERT INTO products(name,category,price,quantity,description) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String[][] data = {
                {"Rice 1kg",       "Groceries", "2.50",  "100", "Premium basmati rice"},
                {"Cooking Oil 1L", "Groceries", "3.75",  "60",  "Sunflower cooking oil"},
                {"Bread Loaf",     "Bakery",    "1.20",  "40",  "Fresh white bread"},
                {"Milk 1L",        "Dairy",     "1.80",  "80",  "Full cream milk"},
                {"Sugar 1kg",      "Groceries", "1.50",  "75",  "Refined white sugar"},
                {"Salt 500g",      "Groceries", "0.75",  "120", "Iodized table salt"},
                {"Tea Bags 100pk", "Beverages", "4.20",  "50",  "Black tea bags"},
                {"Shampoo 400ml",  "Personal",  "5.99",  "8",   "Herbal shampoo"},
                {"Soap Bar",       "Personal",  "1.10",  "5",   "Antibacterial soap"},
                {"Biscuits 200g",  "Snacks",    "2.00",  "3",   "Digestive biscuits"},
            };
            for (String[] row : data) {
                ps.setString(1, row[0]); ps.setString(2, row[1]);
                ps.setDouble(3, Double.parseDouble(row[2]));
                ps.setInt(4, Integer.parseInt(row[3]));
                ps.setString(5, row[4]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void close() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}
