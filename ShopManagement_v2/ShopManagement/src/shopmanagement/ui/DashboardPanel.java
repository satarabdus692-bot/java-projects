package shopmanagement.ui;

import shopmanagement.dao.ProductDAO;
import shopmanagement.dao.SaleDAO;
import shopmanagement.model.Product;
import shopmanagement.util.Utils;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final MainFrame owner;
    private JLabel lblProducts, lblRevenue, lblSales, lblLowStock, lblTodayRev;
    private JPanel  lowStockPanel;

    public DashboardPanel(MainFrame owner) {
        this.owner = owner;
        setBackground(Utils.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        build();
        refresh();
    }

    private void build() {
        // ── Top bar ──────────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Utils.BG_DARK);
        topBar.setBorder(BorderFactory.createEmptyBorder(28, 28, 16, 28));

        JLabel title = new JLabel("Dashboard");
        title.setFont(Utils.FONT_TITLE); title.setForeground(Utils.TEXT_PRIMARY);
        JLabel sub = new JLabel("Welcome back — here's your shop overview");
        sub.setFont(Utils.FONT_BODY); sub.setForeground(Utils.TEXT_MUTED);

        JPanel titleCol = new JPanel(); titleCol.setOpaque(false);
        titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
        titleCol.add(title); titleCol.add(Box.createVerticalStrut(4)); titleCol.add(sub);
        topBar.add(titleCol, BorderLayout.WEST);

        JButton btnRefresh = Utils.accentButton("↻  Refresh", Utils.BG_FIELD);
        btnRefresh.setForeground(Utils.ACCENT_TEAL);
        btnRefresh.addActionListener(e -> refresh());
        JButton btnNewSale = Utils.accentButton("＋  New Sale", Utils.ACCENT_TEAL);
        btnNewSale.setForeground(Utils.BG_DARK);
        btnNewSale.addActionListener(e -> owner.goToSale());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false); btns.add(btnRefresh); btns.add(btnNewSale);
        topBar.add(btns, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Body ─────────────────────────────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout(0, 24));
        body.setBackground(Utils.BG_DARK);
        body.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));

        // Stat cards row
        lblProducts  = statValueLabel();
        lblRevenue   = statValueLabel();
        lblSales     = statValueLabel();
        lblLowStock  = statValueLabel();
        lblTodayRev  = statValueLabel();

        JPanel cards = new JPanel(new GridLayout(1, 5, 16, 0));
        cards.setOpaque(false);
        cards.add(mkCard("Total Products",  lblProducts, Utils.ACCENT_TEAL,  "📦"));
        cards.add(mkCard("Total Revenue",   lblRevenue,  Utils.ACCENT_AMBER, "💰"));
        cards.add(mkCard("Today's Revenue", lblTodayRev, Utils.ACCENT_BLUE,  "📈"));
        cards.add(mkCard("Total Sales",     lblSales,    Utils.ACCENT_TEAL,  "🛍"));
        cards.add(mkCard("Low Stock",       lblLowStock, Utils.ACCENT_RED,   "⚠"));
        body.add(cards, BorderLayout.NORTH);

        // Low-stock alert table
        lowStockPanel = new JPanel(new BorderLayout(0, 12));
        lowStockPanel.setOpaque(false);
        JLabel alertTitle = new JLabel("⚠  Low Stock Alert");
        alertTitle.setFont(Utils.FONT_HEADER); alertTitle.setForeground(Utils.ACCENT_RED);
        lowStockPanel.add(alertTitle, BorderLayout.NORTH);

        body.add(lowStockPanel, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    public void refresh() {
        ProductDAO pDao = ProductDAO.getInstance();
        SaleDAO    sDao = SaleDAO.getInstance();

        lblProducts.setText(String.valueOf(pDao.getTotalProducts()));
        lblRevenue.setText(Utils.fmt(sDao.getTotalRevenue()));
        lblTodayRev.setText(Utils.fmt(sDao.getTodayRevenue()));
        lblSales.setText(String.valueOf(sDao.getTotalCount()));

        List<Product> low = pDao.getLowStock(10);
        lblLowStock.setText(String.valueOf(low.size()));
        if (low.size() > 0) lblLowStock.setForeground(Utils.ACCENT_RED);
        else                 lblLowStock.setForeground(Utils.ACCENT_TEAL);

        // Rebuild low-stock table
        lowStockPanel.removeAll();
        JLabel alertTitle = new JLabel("⚠  Low Stock Alert  (quantity ≤ 10)");
        alertTitle.setFont(Utils.FONT_HEADER); alertTitle.setForeground(Utils.ACCENT_RED);
        lowStockPanel.add(alertTitle, BorderLayout.NORTH);

        if (low.isEmpty()) {
            JLabel ok = new JLabel("  \u2705  All products well stocked!");
            ok.setFont(Utils.FONT_BODY); ok.setForeground(Utils.ACCENT_TEAL);
            ok.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
            lowStockPanel.add(ok, BorderLayout.CENTER);
        } else {
            String[] cols = {"ID", "Product Name", "Category", "Price", "Qty"};
            javax.swing.table.DefaultTableModel tm =
                new javax.swing.table.DefaultTableModel(cols, 0) {
                    public boolean isCellEditable(int r, int c) { return false; }
                    public Class<?> getColumnClass(int c) {
                        return c == 0 || c == 4 ? Integer.class : String.class;
                    }
                };
            for (Product p : low) {
                tm.addRow(new Object[]{
                    p.getId(), p.getName(), p.getCategory(),
                    Utils.fmt(p.getPrice()), p.getQuantity()});
            }
            JTable t = new JTable(tm);
            Utils.styleTable(t);
            t.getColumnModel().getColumn(0).setPreferredWidth(45);
            t.getColumnModel().getColumn(0).setMaxWidth(60);
            t.getColumnModel().getColumn(1).setPreferredWidth(220);
            t.getColumnModel().getColumn(2).setPreferredWidth(130);
            t.getColumnModel().getColumn(3).setPreferredWidth(90);
            t.getColumnModel().getColumn(4).setPreferredWidth(60);
            t.getColumnModel().getColumn(0).setCellRenderer(Utils.centeredRenderer());
            t.getColumnModel().getColumn(3).setCellRenderer(Utils.rightRenderer());
            // Qty column — red if ≤5, amber if ≤10
            t.getColumnModel().getColumn(4).setCellRenderer(new Utils.StripedRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object val,
                        boolean sel, boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                    setHorizontalAlignment(CENTER);
                    if (!sel && val instanceof Integer) {
                        int qty = (Integer) val;
                        setForeground(qty <= 5 ? Utils.ACCENT_RED : Utils.ACCENT_AMBER);
                    }
                    return this;
                }
            });
            lowStockPanel.add(Utils.darkScroll(t), BorderLayout.CENTER);
        }
        lowStockPanel.revalidate(); lowStockPanel.repaint();
    }

    private JPanel mkCard(String label, JLabel value, Color accent, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Utils.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Utils.FONT_SMALL); lbl.setForeground(Utils.TEXT_MUTED);

        value.setFont(Utils.FONT_NUM); value.setForeground(accent);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false); top.add(lbl, BorderLayout.WEST); top.add(ico, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JLabel statValueLabel() {
        JLabel l = new JLabel("—");
        l.setFont(Utils.FONT_NUM); l.setForeground(Utils.ACCENT_TEAL); return l;
    }
}
