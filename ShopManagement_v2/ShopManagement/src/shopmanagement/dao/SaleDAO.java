package shopmanagement.dao;

import shopmanagement.db.DatabaseManager;
import shopmanagement.model.Sale;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    private static final SaleDAO INSTANCE = new SaleDAO();
    private SaleDAO() {}
    public static SaleDAO getInstance() { return INSTANCE; }

    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public boolean saveSale(Sale sale) {
        Connection c = conn();
        try {
            c.setAutoCommit(false);

            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO sales(total_amount, amount_paid, change_given) VALUES(?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setDouble(1, sale.getTotalAmount());
            ps.setDouble(2, sale.getAmountPaid());
            ps.setDouble(3, sale.getChangeGiven());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (!keys.next()) { c.rollback(); c.setAutoCommit(true); return false; }
            int saleId = keys.getInt(1);
            sale.setId(saleId);

            PreparedStatement pi = c.prepareStatement(
                "INSERT INTO sale_items(sale_id, product_id, product_name, unit_price, quantity) VALUES(?,?,?,?,?)");
            for (Sale.SaleItem item : sale.getItems()) {
                pi.setInt(1, saleId);
                pi.setInt(2, item.getProductId());
                pi.setString(3, item.getProductName());
                pi.setDouble(4, item.getUnitPrice());
                pi.setInt(5, item.getQuantity());
                pi.addBatch();
            }
            pi.executeBatch();

            c.commit();
            c.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { c.rollback(); c.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public List<Sale> getAll() {
        List<Sale> list = new ArrayList<>();
        try (ResultSet rs = conn().createStatement()
                .executeQuery("SELECT * FROM sales ORDER BY id DESC")) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("sale_date");
                String dateStr = ts != null ? ts.toString().substring(0, 16) : "";
                Sale s = new Sale(
                    rs.getInt("id"), dateStr,
                    rs.getDouble("total_amount"),
                    rs.getDouble("amount_paid"),
                    rs.getDouble("change_given"));
                list.add(s);
            }
            for (Sale s : list) loadItems(s);
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Sale findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM sales WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("sale_date");
                String dateStr = ts != null ? ts.toString().substring(0, 16) : "";
                Sale s = new Sale(
                    rs.getInt("id"), dateStr,
                    rs.getDouble("total_amount"),
                    rs.getDouble("amount_paid"),
                    rs.getDouble("change_given"));
                loadItems(s);
                return s;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private void loadItems(Sale sale) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM sale_items WHERE sale_id=?")) {
            ps.setInt(1, sale.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sale.getItems().add(new Sale.SaleItem(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getDouble("unit_price"),
                    rs.getInt("quantity")));
            }
        }
    }

    public double getTotalRevenue() {
        try (ResultSet rs = conn().createStatement()
                .executeQuery("SELECT COALESCE(SUM(total_amount), 0) FROM sales")) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    public int getTotalCount() {
        try (ResultSet rs = conn().createStatement()
                .executeQuery("SELECT COUNT(*) FROM sales")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    public double getTodayRevenue() {
        // Use MySQL CURDATE() instead of SQLite DATE('now')
        try (ResultSet rs = conn().createStatement()
                .executeQuery("SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(sale_date) = CURDATE()")) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) { return 0; }
    }
}
