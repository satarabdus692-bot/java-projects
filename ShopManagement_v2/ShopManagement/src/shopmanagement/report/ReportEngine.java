package shopmanagement.report;

import shopmanagement.dao.ProductDAO;
import shopmanagement.dao.SaleDAO;
import shopmanagement.model.Product;
import shopmanagement.model.Sale;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.*;
import java.util.List;

/**
 * Pure-Java PDF/print report engine using Java2D printing API.
 * Generates Inventory Report and Sales Report as printable documents.
 */
public class ReportEngine {

    // A4 at 72 dpi
    private static final int PW = 595;
    private static final int PH = 842;

    // Brand colors
    private static final Color C_BG      = new Color(0x0F1923);
    private static final Color C_CARD    = new Color(0x1A2535);
    private static final Color C_TEAL    = new Color(0x00D4B4);
    private static final Color C_AMBER   = new Color(0xFFB347);
    private static final Color C_TEXT    = new Color(0xEFF2F7);
    private static final Color C_MUTED   = new Color(0x7A8BA5);
    private static final Color C_BORDER  = new Color(0x2D4060);
    private static final Color C_ROW1    = new Color(0x1A2535);
    private static final Color C_ROW2    = new Color(0x1E2F45);
    private static final Color C_RED     = new Color(0xFF5C6E);

    // ── Inventory Report ──────────────────────────────────────────────────────────

    public static void printInventoryReport() {
        List<Product> products = ProductDAO.getInstance().getAll();
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        paper.setSize(PW * 2.83, PH * 2.83);
        paper.setImageableArea(40 * 2.83, 40 * 2.83, (PW - 80) * 2.83, (PH - 80) * 2.83);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            double scale = pageFormat.getImageableWidth() / (PW - 80.0);
            g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g.scale(scale, scale);
            drawInventoryReport(g, products);
            return Printable.PAGE_EXISTS;
        }, pf);

        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException e) { throw new RuntimeException(e); }
        }
    }

    private static void drawInventoryReport(Graphics2D g, List<Product> products) {
        int x = 0, y = 0, W = PW - 80;

        // Header bar
        g.setColor(C_BG);
        g.fillRect(x - 5, y, W + 10, 60);
        g.setColor(C_TEAL);
        g.fillRect(x - 5, y + 56, W + 10, 4);

        g.setColor(C_TEAL);
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("SHOP MANAGEMENT", x + 4, y + 22);
        g.setColor(C_MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g.drawString("INVENTORY REPORT", x + 4, y + 38);

        g.setColor(C_TEXT);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g.drawString("Generated: " + new java.util.Date(), W - 160, y + 35);
        y += 75;

        // Stats row
        int[] lowQtys = {0};
        double[] totalVal = {0};
        products.forEach(p -> {
            if (p.getQuantity() <= 10) lowQtys[0]++;
            totalVal[0] += p.getPrice() * p.getQuantity();
        });
        drawStatCard(g, x, y, 150, 45, "Total Products", String.valueOf(products.size()), C_TEAL);
        drawStatCard(g, x + 160, y, 150, 45, "Low Stock Items", String.valueOf(lowQtys[0]), C_RED);
        drawStatCard(g, x + 320, y, 150, 45, "Inventory Value", String.format("$%.2f", totalVal[0]), C_AMBER);
        y += 60;

        // Table header
        int[] cols = {x, x + 30, x + 155, x + 245, x + 310, x + 365};
        String[] heads = {"#", "Product Name", "Category", "Price", "Qty", "Value"};
        g.setColor(C_BG);
        g.fillRect(x, y, W, 22);
        g.setColor(C_TEAL);
        g.setFont(new Font("Segoe UI", Font.BOLD, 9));
        for (int i = 0; i < heads.length; i++) g.drawString(heads[i], cols[i] + 3, y + 15);
        g.setColor(C_TEAL);
        g.fillRect(x, y + 20, W, 1);
        y += 24;

        // Rows
        int row = 0;
        for (Product p : products) {
            if (y > PH - 120) break;
            Color bg = (row++ % 2 == 0) ? C_ROW1 : C_ROW2;
            g.setColor(bg);
            g.fillRect(x, y, W, 18);

            boolean low = p.getQuantity() <= 10;
            g.setColor(low ? C_RED : C_TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString(String.valueOf(p.getId()), cols[0] + 3, y + 13);
            g.drawString(truncate(p.getName(), 18), cols[1] + 3, y + 13);
            g.drawString(truncate(p.getCategory(), 12), cols[2] + 3, y + 13);
            g.drawString(String.format("$%.2f", p.getPrice()), cols[3] + 3, y + 13);
            g.setColor(low ? C_RED : C_AMBER);
            g.drawString(String.valueOf(p.getQuantity()), cols[4] + 3, y + 13);
            g.setColor(C_TEXT);
            g.drawString(String.format("$%.2f", p.getPrice() * p.getQuantity()), cols[5] + 3, y + 13);
            if (low) {
                g.setColor(C_RED);
                g.setFont(new Font("Segoe UI", Font.BOLD, 7));
                g.drawString("LOW", cols[4] + 18, y + 13);
            }
            y += 19;
        }

        // Footer
        g.setColor(C_BORDER);
        g.fillRect(x, PH - 100, W, 1);
        g.setColor(C_MUTED);
        g.setFont(new Font("Segoe UI", Font.ITALIC, 8));
        g.drawString("Shop Management System  •  Confidential", x, PH - 90);
    }

    // ── Sales Report ──────────────────────────────────────────────────────────────

    public static void printSalesReport() {
        List<Sale> sales = SaleDAO.getInstance().getAll();
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        paper.setSize(PW * 2.83, PH * 2.83);
        paper.setImageableArea(40 * 2.83, 40 * 2.83, (PW - 80) * 2.83, (PH - 80) * 2.83);
        pf.setPaper(paper); pf.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            double scale = pageFormat.getImageableWidth() / (PW - 80.0);
            g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g.scale(scale, scale);
            drawSalesReport(g, sales);
            return Printable.PAGE_EXISTS;
        }, pf);

        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException e) { throw new RuntimeException(e); }
        }
    }

    private static void drawSalesReport(Graphics2D g, List<Sale> sales) {
        int x = 0, y = 0, W = PW - 80;

        // Header
        g.setColor(C_BG);
        g.fillRect(x - 5, y, W + 10, 60);
        g.setColor(C_AMBER);
        g.fillRect(x - 5, y + 56, W + 10, 4);
        g.setColor(C_AMBER);
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("SHOP MANAGEMENT", x + 4, y + 22);
        g.setColor(C_MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g.drawString("SALES REPORT", x + 4, y + 38);
        g.setColor(C_TEXT);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g.drawString("Generated: " + new java.util.Date(), W - 160, y + 35);
        y += 75;

        double totalRev = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        drawStatCard(g, x, y, 150, 45, "Total Sales", String.valueOf(sales.size()), C_TEAL);
        drawStatCard(g, x + 160, y, 150, 45, "Total Revenue", String.format("$%.2f", totalRev), C_AMBER);
        double avg = sales.isEmpty() ? 0 : totalRev / sales.size();
        drawStatCard(g, x + 320, y, 150, 45, "Avg. Sale Value", String.format("$%.2f", avg), C_TEAL);
        y += 60;

        // Table
        int[] cols = {x, x + 45, x + 185, x + 270, x + 355};
        String[] heads = {"Sale#", "Date", "Items", "Total", "Paid"};
        g.setColor(C_BG);
        g.fillRect(x, y, W, 22);
        g.setColor(C_AMBER);
        g.setFont(new Font("Segoe UI", Font.BOLD, 9));
        for (int i = 0; i < heads.length; i++) g.drawString(heads[i], cols[i] + 3, y + 15);
        g.fillRect(x, y + 20, W, 1);
        y += 24;

        int row = 0;
        for (Sale s : sales) {
            if (y > PH - 120) break;
            g.setColor(row++ % 2 == 0 ? C_ROW1 : C_ROW2);
            g.fillRect(x, y, W, 18);
            g.setColor(C_TEXT);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString("#" + s.getId(), cols[0] + 3, y + 13);
            g.drawString(s.getSaleDate() != null ? s.getSaleDate().substring(0, 16) : "", cols[1] + 3, y + 13);
            g.drawString(String.valueOf(s.getItems().size()), cols[2] + 3, y + 13);
            g.setColor(C_AMBER);
            g.drawString(String.format("$%.2f", s.getTotalAmount()), cols[3] + 3, y + 13);
            g.setColor(C_TEXT);
            g.drawString(String.format("$%.2f", s.getAmountPaid()), cols[4] + 3, y + 13);
            y += 19;
        }

        // Totals
        g.setColor(C_TEAL);
        g.fillRect(x, y + 4, W, 1);
        g.setColor(C_AMBER);
        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g.drawString(String.format("TOTAL REVENUE:  $%.2f", totalRev), x + 3, y + 20);

        // Footer
        g.setColor(C_BORDER);
        g.fillRect(x, PH - 100, W, 1);
        g.setColor(C_MUTED);
        g.setFont(new Font("Segoe UI", Font.ITALIC, 8));
        g.drawString("Shop Management System  •  Confidential", x, PH - 90);
    }

    // ── Receipt printer ────────────────────────────────────────────────────────────

    public static void printReceipt(Sale sale) {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf  = job.defaultPage();
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            drawReceipt(g, sale, (int) pageFormat.getImageableWidth());
            return Printable.PAGE_EXISTS;
        }, pf);
        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException e) { throw new RuntimeException(e); }
        }
    }

    private static void drawReceipt(Graphics2D g, Sale sale, int W) {
        int x = 10, y = 10;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W + 20, 600);

        g.setColor(C_BG);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        String title = "SHOP MANAGEMENT";
        g.drawString(title, (W - g.getFontMetrics().stringWidth(title)) / 2, y + 14);
        y += 22;
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String sub = "Sale Receipt";
        g.drawString(sub, (W - g.getFontMetrics().stringWidth(sub)) / 2, y + 12);
        y += 18;

        String sep = "-".repeat(38);
        g.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g.drawString(sep, x, y + 10); y += 14;

        g.drawString("Date: " + (sale.getSaleDate() != null ? sale.getSaleDate().substring(0, 16) : ""), x, y + 10); y += 14;
        g.drawString("Sale #: " + sale.getId(), x, y + 10); y += 14;
        g.drawString(sep, x, y + 10); y += 16;

        g.setFont(new Font("Monospaced", Font.BOLD, 9));
        g.drawString(String.format("%-20s %5s %8s", "Item", "Qty", "Amount"), x, y + 10); y += 14;
        g.drawString(sep, x, y + 2); y += 10;

        g.setFont(new Font("Monospaced", Font.PLAIN, 9));
        for (Sale.SaleItem item : sale.getItems()) {
            g.drawString(truncate(item.getProductName(), 20), x, y + 10);
            g.drawString(String.format("%5d %8.2f", item.getQuantity(), item.getSubtotal()), x + 150, y + 10);
            y += 13;
        }
        g.setFont(new Font("Monospaced", Font.BOLD, 9));
        g.drawString(sep, x, y + 4); y += 14;
        g.drawString(String.format("%-20s %8.2f", "TOTAL:", sale.getTotalAmount()), x, y + 10); y += 14;
        g.drawString(String.format("%-20s %8.2f", "PAID:", sale.getAmountPaid()), x, y + 10); y += 14;
        g.drawString(String.format("%-20s %8.2f", "CHANGE:", sale.getChangeGiven()), x, y + 10); y += 18;
        g.setFont(new Font("Monospaced", Font.PLAIN, 9));
        String thanks = "Thank you for shopping!";
        g.drawString(thanks, (W - g.getFontMetrics().stringWidth(thanks)) / 2, y + 10);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private static void drawStatCard(Graphics2D g, int x, int y, int w, int h,
                                     String label, String value, Color accent) {
        g.setColor(C_CARD);
        g.fill(new RoundRectangle2D.Float(x, y, w, h, 8, 8));
        g.setColor(accent);
        g.fill(new RoundRectangle2D.Float(x, y, 4, h, 4, 4));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        g.setColor(C_MUTED);
        g.drawString(label.toUpperCase(), x + 10, y + 14);
        g.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g.setColor(accent);
        g.drawString(value, x + 10, y + 36);
    }

    private static String truncate(String s, int max) {
        return s == null ? "" : (s.length() <= max ? s : s.substring(0, max - 1) + "\u2026");
    }
}
