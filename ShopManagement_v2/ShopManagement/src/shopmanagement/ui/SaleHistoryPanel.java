package shopmanagement.ui;

import shopmanagement.dao.SaleDAO;
import shopmanagement.model.Sale;
import shopmanagement.report.ReportEngine;
import shopmanagement.util.Utils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SaleHistoryPanel extends JPanel {

    private final SaleDAO dao = SaleDAO.getInstance();
    private JTable            table;
    private DefaultTableModel model;
    private JTextArea         receiptArea;
    private JLabel            lblSummary;

    public SaleHistoryPanel() {
        setBackground(Utils.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        build();
        refresh();
    }

    private void build() {
        // Top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Utils.BG_DARK);
        top.setBorder(BorderFactory.createEmptyBorder(28, 28, 16, 28));
        JLabel title = new JLabel("Sales History");
        title.setFont(Utils.FONT_TITLE); title.setForeground(Utils.TEXT_PRIMARY);
        top.add(title, BorderLayout.WEST);

        JButton btnRefresh = Utils.accentButton("↻  Refresh", Utils.BG_FIELD);
        btnRefresh.setForeground(Utils.ACCENT_TEAL);
        btnRefresh.addActionListener(e -> refresh());

        JButton btnPrint = Utils.accentButton("🖨  Print Report", Utils.ACCENT_AMBER);
        btnPrint.setForeground(Utils.BG_DARK);
        btnPrint.addActionListener(e -> new SwingWorker<Void,Void>() {
            protected Void doInBackground() { ReportEngine.printSalesReport(); return null; }
        }.execute());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false); btnRow.add(btnRefresh); btnRow.add(btnPrint);
        top.add(btnRow, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Main body
        String[] cols = {"Sale #", "Date & Time", "Items", "Total", "Paid", "Change"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        Utils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(0).setCellRenderer(Utils.centeredRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(Utils.centeredRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(Utils.rightRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(Utils.rightRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(Utils.rightRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showReceipt();
        });

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(Utils.FONT_MONO);
        receiptArea.setBackground(Utils.BG_DARK);
        receiptArea.setForeground(Utils.TEXT_PRIMARY);
        receiptArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        receiptArea.setText("\n   ← Select a sale to view its receipt");
        receiptArea.setForeground(Utils.TEXT_MUTED);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            Utils.darkScroll(table), Utils.darkScroll(receiptArea));
        split.setResizeWeight(0.6); split.setDividerSize(4);
        split.setBorder(null); split.setBackground(Utils.BG_DARK);

        // Summary bar
        lblSummary = new JLabel("  Loading...");
        lblSummary.setFont(Utils.FONT_BODY); lblSummary.setForeground(Utils.TEXT_MUTED);
        lblSummary.setBackground(Utils.BG_CARD);
        lblSummary.setOpaque(true);
        lblSummary.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Utils.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 28, 10, 28)));

        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(Utils.BG_DARK);
        body.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        body.add(split, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        add(lblSummary, BorderLayout.SOUTH);
    }

    public void refresh() {
        List<Sale> sales = dao.getAll();
        model.setRowCount(0);
        sales.forEach(s -> model.addRow(new Object[]{
            "#" + s.getId(), s.getSaleDate() != null ? s.getSaleDate().substring(0, 16) : "",
            s.getItems().size(), Utils.fmt(s.getTotalAmount()),
            Utils.fmt(s.getAmountPaid()), Utils.fmt(s.getChangeGiven())}));
        lblSummary.setText(String.format(
            "   %d sales  •  Total Revenue: %s  •  Today: %s",
            dao.getTotalCount(), Utils.fmt(dao.getTotalRevenue()), Utils.fmt(dao.getTodayRevenue())));
        receiptArea.setText("\n   ← Select a sale to view its receipt");
        receiptArea.setForeground(Utils.TEXT_MUTED);
    }

    private void showReceipt() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String cellVal = (String) model.getValueAt(row, 0);
        int saleId = Integer.parseInt(cellVal.replace("#","").trim());
        Sale sale = dao.findById(saleId);
        if (sale == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║       SHOP MANAGEMENT SYSTEM         ║\n");
        sb.append("║            SALE RECEIPT              ║\n");
        sb.append("╚══════════════════════════════════════╝\n\n");
        sb.append(String.format("  Sale #    : %d%n", sale.getId()));
        sb.append(String.format("  Date      : %s%n", sale.getSaleDate() != null ? sale.getSaleDate().substring(0,16) : ""));
        sb.append("\n  ──────────────────────────────────────\n");
        sb.append(String.format("  %-22s %5s %8s%n", "Product", "Qty", "Amount"));
        sb.append("  ──────────────────────────────────────\n");
        for (Sale.SaleItem item : sale.getItems()) {
            sb.append(String.format("  %-22s %5d  %s%n",
                truncate(item.getProductName(), 22),
                item.getQuantity(), Utils.fmt(item.getSubtotal())));
        }
        sb.append("  ══════════════════════════════════════\n");
        sb.append(String.format("  %-22s        %s%n", "TOTAL:",  Utils.fmt(sale.getTotalAmount())));
        sb.append(String.format("  %-22s        %s%n", "PAID:",   Utils.fmt(sale.getAmountPaid())));
        sb.append(String.format("  %-22s        %s%n", "CHANGE:", Utils.fmt(sale.getChangeGiven())));
        sb.append("\n       Thank you for your purchase!\n");

        receiptArea.setForeground(Utils.TEXT_PRIMARY);
        receiptArea.setText(sb.toString());
        receiptArea.setCaretPosition(0);
    }

    private String truncate(String s, int max) {
        return s == null ? "" : (s.length() <= max ? s : s.substring(0, max-1) + "…");
    }
}
