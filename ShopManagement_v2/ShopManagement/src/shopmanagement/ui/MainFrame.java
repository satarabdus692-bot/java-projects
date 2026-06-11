package shopmanagement.ui;

import shopmanagement.db.DatabaseManager;
import shopmanagement.util.Utils;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private JPanel          sidebar;
    private JPanel          contentPanel;
    private CardLayout      cardLayout;
    private DashboardPanel  dashPanel;
    private ProductPanel    prodPanel;
    private SalePanel       salePanel;
    private SaleHistoryPanel histPanel;
    private JButton[]       navBtns;
    private int             activeIdx = 0;

    public MainFrame() {
        DatabaseManager.getInstance();   // init DB (shows error dialog on failure)
        applyGlobalUI();
        setTitle("ShopManager Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        buildUI();
        // Close DB on exit
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                DatabaseManager.getInstance().close();
            }
        });
    }

    private void applyGlobalUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background",            Utils.BG_DARK);
        UIManager.put("OptionPane.background",       Utils.BG_CARD);
        UIManager.put("OptionPane.messageForeground",Utils.TEXT_PRIMARY);
        UIManager.put("Button.background",           Utils.BG_FIELD);
        UIManager.put("Button.foreground",           Utils.TEXT_PRIMARY);
        UIManager.put("TextField.background",        Utils.BG_FIELD);
        UIManager.put("TextField.foreground",        Utils.TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",   Utils.ACCENT_TEAL);
        UIManager.put("TextArea.background",         Utils.BG_FIELD);
        UIManager.put("TextArea.foreground",         Utils.TEXT_PRIMARY);
        UIManager.put("ComboBox.background",         Utils.BG_FIELD);
        UIManager.put("ComboBox.foreground",         Utils.TEXT_PRIMARY);
        UIManager.put("Spinner.background",          Utils.BG_FIELD);
        UIManager.put("ScrollPane.background",       Utils.BG_CARD);
        UIManager.put("Viewport.background",         Utils.BG_CARD);
        UIManager.put("Label.foreground",            Utils.TEXT_PRIMARY);
        UIManager.put("ScrollBar.background",        Utils.BG_DARK);
        UIManager.put("ScrollBar.thumb",             Utils.BG_FIELD);
        UIManager.put("Table.background",            Utils.BG_CARD);
        UIManager.put("Table.foreground",            Utils.TEXT_PRIMARY);
        UIManager.put("TableHeader.background",      Utils.BG_DARK);
        UIManager.put("TableHeader.foreground",      Utils.ACCENT_TEAL);
        UIManager.put("SplitPane.background",        Utils.BG_DARK);
        UIManager.put("SplitPaneDivider.background", Utils.BORDER_COLOR);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Utils.BG_DARK);

        // ── Sidebar ──────────────────────────────────────────────────────────────
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(0x0A1420));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Utils.BORDER_COLOR));

        // Logo area
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(0x0A1420));
        logoPanel.setMaximumSize(new Dimension(220, 80));
        logoPanel.setPreferredSize(new Dimension(220, 80));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

        JLabel logo = new JLabel("<html><span style='color:#00D4B4;font-size:15pt;font-weight:bold;'>"
            + "&#128722;</span>"
            + "<span style='color:#EFF2F7;font-size:11pt;font-weight:bold;'> ShopManager</span></html>");
        JLabel sub = new JLabel("   Pro Edition  \u2014  MySQL");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sub.setForeground(Utils.TEXT_MUTED);

        JPanel logoText = new JPanel();
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));
        logoText.setOpaque(false);
        logoText.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        logoText.add(logo);
        logoText.add(Box.createVerticalStrut(2));
        logoText.add(sub);
        logoPanel.add(logoText, BorderLayout.CENTER);

        JPanel logoDivider = new JPanel();
        logoDivider.setBackground(Utils.BORDER_COLOR);
        logoDivider.setPreferredSize(new Dimension(220, 1));
        logoDivider.setMaximumSize(new Dimension(220, 1));

        sidebar.add(logoPanel);
        sidebar.add(logoDivider);
        sidebar.add(Box.createVerticalStrut(18));

        // Nav section label
        JLabel navLabel = navSectionLabel("NAVIGATION");
        sidebar.add(navLabel);
        sidebar.add(Box.createVerticalStrut(4));

        // Nav buttons
        String[][] navItems = {
            {"  \uD83D\uDCCA  Dashboard",      "Overview & Stats"},
            {"  \uD83D\uDCE6  Inventory",       "Products & Stock"},
            {"  \uD83D\uDED2  New Sale",         "Point of Sale"},
            {"  \uD83D\uDCCB  Sales History",    "Past Transactions"},
        };
        navBtns = new JButton[navItems.length];
        for (int i = 0; i < navItems.length; i++) {
            navBtns[i] = createNavButton(navItems[i][0], navItems[i][1], i);
            sidebar.add(navBtns[i]);
            sidebar.add(Box.createVerticalStrut(2));
        }

        sidebar.add(Box.createVerticalGlue());

        // Reports section
        JLabel repLabel = navSectionLabel("REPORTS");
        sidebar.add(repLabel);
        sidebar.add(Box.createVerticalStrut(4));

        JButton btnInventRpt = createReportBtn("  \uD83D\uDDCB\uFE0F  Inventory Report");
        JButton btnSalesRpt  = createReportBtn("  \uD83D\uDCB0  Sales Report");
        btnInventRpt.addActionListener(e -> runReport("inventory"));
        btnSalesRpt.addActionListener(e -> runReport("sales"));
        sidebar.add(btnInventRpt);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(btnSalesRpt);
        sidebar.add(Box.createVerticalStrut(16));

        // ── Content area ─────────────────────────────────────────────────────────
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Utils.BG_DARK);

        dashPanel = new DashboardPanel(this);
        prodPanel = new ProductPanel();
        salePanel = new SalePanel(this);
        histPanel = new SaleHistoryPanel();

        contentPanel.add(dashPanel, "DASHBOARD");
        contentPanel.add(prodPanel, "INVENTORY");
        contentPanel.add(salePanel, "SALE");
        contentPanel.add(histPanel, "HISTORY");

        add(sidebar,      BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        selectNav(0);
    }

    private JLabel navSectionLabel(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(Utils.TEXT_MUTED);
        l.setMaximumSize(new Dimension(220, 22));
        return l;
    }

    private JButton createNavButton(String label, String sublabel, int idx) {
        JButton btn = new JButton("<html>" + label
            + "<br><font color='#9AAFC5' size='3'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
            + sublabel + "</font></html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(Utils.TEXT_PRIMARY);
        btn.setBackground(new Color(0x0A1420));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(220, 56));
        btn.setPreferredSize(new Dimension(220, 56));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        btn.addActionListener(e -> selectNav(idx));
        return btn;
    }

    private JButton createReportBtn(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Utils.TEXT_MUTED);
        btn.setBackground(new Color(0x0A1420));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(220, 38));
        btn.setPreferredSize(new Dimension(220, 38));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        return btn;
    }

    public void selectNav(int idx) {
        activeIdx = idx;
        String[] cards = {"DASHBOARD", "INVENTORY", "SALE", "HISTORY"};
        cardLayout.show(contentPanel, cards[idx]);

        for (int i = 0; i < navBtns.length; i++) {
            boolean active = (i == idx);
            navBtns[i].setBackground(active ? new Color(0x162030) : new Color(0x0A1420));
            navBtns[i].setBorder(active
                ? BorderFactory.createMatteBorder(0, 3, 0, 0, Utils.ACCENT_TEAL)
                : BorderFactory.createEmptyBorder(0, 4, 0, 0));
        }

        if (idx == 0) dashPanel.refresh();
        if (idx == 3) histPanel.refresh();
    }

    public void goToHistory() { selectNav(3); histPanel.refresh(); }
    public void goToSale()    { selectNav(2); }
    public void refreshSaleProducts() { salePanel.reloadProducts(); }

    private void runReport(String type) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                if ("inventory".equals(type))
                    shopmanagement.report.ReportEngine.printInventoryReport();
                else
                    shopmanagement.report.ReportEngine.printSalesReport();
                return null;
            }
            @Override protected void done() {
                try { get(); }
                catch (Exception e) {
                    Utils.error(MainFrame.this, "Report error: "
                        + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
            }
        };
        worker.execute();
    }
}
