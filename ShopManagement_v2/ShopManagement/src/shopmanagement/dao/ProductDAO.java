package shopmanagement.dao;

import shopmanagement.db.DatabaseManager;
import shopmanagement.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final ProductDAO INSTANCE = new ProductDAO();
    private ProductDAO() {}
    public static ProductDAO getInstance() { return INSTANCE; }

    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setPrice(rs.getDouble("price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setDescription(rs.getString("description") != null ? rs.getString("description") : "");
        // MySQL returns Timestamp, convert to string safely
        Timestamp ca = rs.getTimestamp("created_at");
        Timestamp ua = rs.getTimestamp("updated_at");
        p.setCreatedAt(ca != null ? ca.toString() : "");
        p.setUpdatedAt(ua != null ? ua.toString() : "");
        return p;
    }

    public List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        try (ResultSet rs = conn().createStatement()
                .executeQuery("SELECT * FROM products ORDER BY category, name")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> search(String kw) {
        List<Product> list = new ArrayList<>();
        String q = "SELECT * FROM products WHERE LOWER(name) LIKE ? OR LOWER(category) LIKE ? ORDER BY name";
        try (PreparedStatement ps = conn().prepareStatement(q)) {
            String like = "%" + kw.toLowerCase() + "%";
            ps.setString(1, like); ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Product findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM products WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Product p) {
        String q = "INSERT INTO products(name,category,price,quantity,description) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getQuantity());
            ps.setString(5, p.getDescription());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) p.setId(keys.getInt(1));
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Product p) {
        // Use MySQL NOW() instead of SQLite datetime('now')
        String q = "UPDATE products SET name=?, category=?, price=?, quantity=?, description=?, updated_at=NOW() WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(q)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getQuantity());
            ps.setString(5, p.getDescription());
            ps.setInt(6, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM products WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<String> getCategories() {
        List<String> list = new ArrayList<>();
        try (ResultSet rs = conn().createStatement()
                .executeQuery("SELECT DISTINCT category FROM products ORDER BY category")) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getTotalProducts() {
        try (ResultSet rs = conn().createStatement()
                .executeQuery("SELECT COUNT(*) FROM products")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    public List<Product> getLowStock(int threshold) {
        List<Product> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM products WHERE quantity <= ? ORDER BY quantity ASC")) {
            ps.setInt(1, threshold);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
