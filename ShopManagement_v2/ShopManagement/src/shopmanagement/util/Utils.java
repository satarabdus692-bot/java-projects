package shopmanagement.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class Utils {

    // ── Palette ──────────────────────────────────────────────────────────────────
    public static final Color BG_DARK      = new Color(0x0F1923);
    public static final Color BG_CARD      = new Color(0x1A2535);
    public static final Color BG_FIELD     = new Color(0x243147);
    public static final Color ACCENT_TEAL  = new Color(0x00D4B4);
    public static final Color ACCENT_AMBER = new Color(0xFFB347);
    public static final Color ACCENT_RED   = new Color(0xFF5C6E);
    public static final Color ACCENT_BLUE  = new Color(0x4A9EFF);
    public static final Color TEXT_PRIMARY = new Color(0xEFF2F7);
    public static final Color TEXT_MUTED   = new Color(0x7A8BA5);
    public static final Color BORDER_COLOR = new Color(0x2D4060);
    public static final Color ROW_ALT      = new Color(0x1E2F45);
    public static final Color ROW_SEL      = new Color(0x1A4060);

    // ── Fonts ─────────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_NUM    = new Font("Segoe UI", Font.BOLD,  26);

    private static final NumberFormat CURRENCY =
        NumberFormat.getCurrencyInstance(Locale.US);

    private Utils() {}

    public static String fmt(double v) { return CURRENCY.format(v); }

    public static double parseMoney(String s) {
        try { return Double.parseDouble(s.replaceAll("[^\\d.]", "")); }
        catch (NumberFormatException e) { return -1; }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────────
    public static void info(Component p, String msg) {
        JOptionPane.showMessageDialog(p, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void error(Component p, String msg) {
        JOptionPane.showMessageDialog(p, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static boolean confirm(Component p, String msg) {
        return JOptionPane.showConfirmDialog(p, msg, "Confirm",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // ── Table helpers ─────────────────────────────────────────────────────────────
    public static void styleTable(JTable t) {
        t.setBackground(BG_CARD);
        t.setForeground(TEXT_PRIMARY);
        t.setFont(FONT_BODY);
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(ROW_SEL);
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setBackground(BG_DARK);
        t.getTableHeader().setForeground(ACCENT_TEAL);
        t.getTableHeader().setFont(FONT_HEADER);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_TEAL));
        t.setDefaultRenderer(Object.class, new StripedRenderer());
    }

    public static DefaultTableCellRenderer centeredRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    public static DefaultTableCellRenderer rightRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        return r;
    }

    /** Alternating-row renderer */
    public static class StripedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            if (sel) {
                setBackground(ROW_SEL);
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                setForeground(TEXT_PRIMARY);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }
    }

    // ── Component builders ────────────────────────────────────────────────────────
    public static JTextField darkField() {
        JTextField f = new JTextField();
        f.setBackground(BG_FIELD);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_TEAL);
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }

    public static JTextArea darkArea(int rows) {
        JTextArea a = new JTextArea(rows, 0);
        a.setBackground(BG_FIELD);
        a.setForeground(TEXT_PRIMARY);
        a.setCaretColor(ACCENT_TEAL);
        a.setFont(FONT_BODY);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return a;
    }

    public static JLabel darkLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUTED);
        l.setFont(FONT_BODY);
        return l;
    }

    public static JButton accentButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        // Choose text colour automatically: dark text on light bg, white on dark bg
        int brightness = (bg.getRed() * 299 + bg.getGreen() * 587 + bg.getBlue() * 114) / 1000;
        b.setForeground(brightness > 160 ? new Color(0x0F1923) : Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return b;
    }

    public static JScrollPane darkScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        sp.getHorizontalScrollBar().setBackground(BG_DARK);
        return sp;
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    /** Styled combo box */
    public static JComboBox<String> darkCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(BG_FIELD);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(FONT_BODY);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setBackground(selected ? ROW_SEL : BG_FIELD);
                setForeground(TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                return this;
            }
        });
        return cb;
    }
}
