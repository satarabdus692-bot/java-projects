package shopmanagement.ui;

import shopmanagement.dao.ProductDAO;
import shopmanagement.dao.SaleDAO;
import shopmanagement.model.Product;
import shopmanagement.model.Sale;
import shopmanagement.report.ReportEngine;
import shopmanagement.util.Utils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SalePanel extends JPanel {

    private final ProductDAO pDao = ProductDAO.getInstance();
    private final SaleDAO    sDao = SaleDAO.getInstance();
    private final MainFrame  owner;
    private Sale currentSale = new Sale();

    // Product browser
    private JTable            prodTable;
    private DefaultTableModel prodModel;
    private JTextField        tfSearch;
    private JSpinner          spinner;

    // Cart
    private JTable            cartTable;
    private DefaultTableModel cartModel;

    // Payment
    private JLabel     lblTotal, lblChange;
    private JTextField tfPaid;

    public SalePanel(MainFrame owner) {
        this.owner = owner;
        setBackground(Utils.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        build();
        loadProducts(pDao.getAll());
    }

    /** Called by MainFrame after a sale to refresh the product list */
    public void reloadProducts() {
        loadProducts(pDao.getAll());
    }

    private void build() {
        // ── Page title ───────────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Utils.BG_DARK);
        top.setBorder(BorderFactory.createEmptyBorder(28, 28, 16, 28));
        JLabel title = new JLabel("Point of Sale");
        title.setFont(Utils.FONT_TITLE); title.setForeground(Utils.TEXT_PRIMARY);
        JLabel sub = new JLabel("Select products and process checkout");
        sub.setFont(Utils.FONT_BODY); sub.setForeground(Utils.TEXT_MUTED);
        JPanel titleCol = new JPanel();
        titleCol.setOpaque(false);
        titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
        titleCol.add(title); titleCol.add(Box.createVerticalStrut(3)); titleCol.add(sub);
        top.add(titleCol, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        // ── Split ────────────────────────────────────────────────────────────────
        JPanel left  = buildProductBrowser();
        JPanel right = buildCart();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.56);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(Utils.BG_DARK);
        split.setDividerLocation(640);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Utils.BG_DARK);
        body.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        body.add(split, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    private JPanel buildProductBrowser() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        // Search bar
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        tfSearch = Utils.darkField();
        tfSearch.addActionListener(e -> doSearch());
        tfSearch.setToolTipText("Search products");

        JButton btnS = Utils.accentButton("Search", Utils.ACCENT_TEAL);
        btnS.setForeground(Utils.BG_DARK); btnS.addActionListener(e -> doSearch());
        JButton btnAll = Utils.accentButton("All", Utils.BG_FIELD);
        btnAll.setForeground(Utils.TEXT_PRIMARY);
        btnAll.addActionListener(e -> { tfSearch.setText(""); loadProducts(pDao.getAll()); });

        bar.add(tfSearch, BorderLayout.CENTER);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false); btnRow.add(btnS); btnRow.add(btnAll);
        bar.add(btnRow, BorderLayout.EAST);
        p.add(bar, BorderLayout.NORTH);

        // Product table
        String[] cols = {"ID", "Product", "Category", "Price", "Stock"};
        prodModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        prodTable = new JTable(prodModel);
        Utils.styleTable(prodTable);
        prodTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        prodTable.getColumnModel().getColumn(0).setMaxWidth(55);
        prodTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        prodTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        prodTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        prodTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        prodTable.getColumnModel().getColumn(3).setCellRenderer(Utils.rightRenderer());
        prodTable.getColumnModel().getColumn(4).setCellRenderer(Utils.centeredRenderer());
        p.add(Utils.darkScroll(prodTable), BorderLayout.CENTER);

        // Add-to-cart row
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        addRow.setBackground(Utils.BG_CARD);
        addRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Utils.BORDER_COLOR));
        addRow.add(Utils.darkLabel("Qty:"));
        spinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spinner.setPreferredSize(new Dimension(75, 32));
        try {
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
            editor.getTextField().setBackground(Utils.BG_FIELD);
            editor.getTextField().setForeground(Utils.TEXT_PRIMARY);
            editor.getTextField().setCaretColor(Utils.ACCENT_TEAL);
        } catch (Exception ignored) {}
        addRow.add(spinner);

        JButton btnAdd = Utils.accentButton("+ Add to Cart", Utils.ACCENT_TEAL);
        btnAdd.setForeground(Utils.BG_DARK);
        btnAdd.addActionListener(e -> addToCart());
        addRow.add(btnAdd);

        // double-click row also adds to cart
        prodTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) addToCart();
            }
        });

        p.add(addRow, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildCart() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Utils.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER_COLOR),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel cartLbl = new JLabel("  Shopping Cart");
        cartLbl.setFont(Utils.FONT_HEADER); cartLbl.setForeground(Utils.TEXT_PRIMARY);
        p.add(cartLbl, BorderLayout.NORTH);

        String[] cols = {"Product", "Unit Price", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        Utils.styleTable(cartTable);
        cartTable.setBackground(Utils.BG_DARK);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(Utils.rightRenderer());
        cartTable.getColumnModel().getColumn(2).setCellRenderer(Utils.centeredRenderer());
        cartTable.getColumnModel().getColumn(3).setCellRenderer(Utils.rightRenderer());
        p.add(Utils.darkScroll(cartTable), BorderLayout.CENTER);

        p.add(buildPaymentPanel(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildPaymentPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Utils.BG_DARK);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Utils.BORDER_COLOR),
            BorderFactory.createEmptyBorder(14, 0, 0, 0)));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(4, 0, 4, 0);

        lblTotal  = new JLabel(Utils.fmt(0), SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotal.setForeground(Utils.ACCENT_TEAL);

        tfPaid = Utils.darkField();
        tfPaid.setHorizontalAlignment(JTextField.RIGHT);
        tfPaid.setText("0.00");
        tfPaid.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        lblChange = new JLabel(Utils.fmt(0), SwingConstants.RIGHT);
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblChange.setForeground(Utils.ACCENT_AMBER);

        tfPaid.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateChange(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
        });

        addPayRow(p, g, 0, "Total:",        lblTotal);
        addPayRow(p, g, 1, "Amount Paid:",  tfPaid);
        addPayRow(p, g, 2, "Change:",       lblChange);

        JButton btnRemove   = Utils.accentButton("Remove Item",  Utils.ACCENT_RED);
        JButton btnClear    = Utils.accentButton("Clear Cart",   Utils.BG_FIELD);
        JButton btnCheckout = Utils.accentButton("  Checkout  ", Utils.ACCENT_TEAL);
        btnRemove.setForeground(Color.WHITE);
        btnClear.setForeground(Utils.TEXT_PRIMARY);
        btnCheckout.setForeground(Utils.BG_DARK);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 15));

        btnRemove.addActionListener(e -> removeItem());
        btnClear.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> checkout());

        g.gridy = 3; g.gridx = 0; g.gridwidth = 2;
        g.insets = new Insets(10, 0, 4, 0);
        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 0));
        row1.setOpaque(false); row1.add(btnRemove); row1.add(btnClear);
        p.add(row1, g);

        g.gridy = 4; g.insets = new Insets(4, 0, 0, 0);
        btnCheckout.setPreferredSize(new Dimension(0, 46));
        p.add(btnCheckout, g);

        return p;
    }

    private void addToCart() {
        int row = prodTable.getSelectedRow();
        if (row < 0) { Utils.error(this, "Select a product first."); return; }
        int pid = (int) prodModel.getValueAt(row, 0);
        Product prod = pDao.findById(pid);
        if (prod == null) return;

        int qty = (int) spinner.getValue();
        int alreadyInCart = currentSale.getItems().stream()
            .filter(i -> i.getProductId() == pid)
            .mapToInt(Sale.SaleItem::getQuantity).sum();

        if (alreadyInCart + qty > prod.getQuantity()) {
            Utils.error(this, "Not enough stock! Available: "
                + (prod.getQuantity() - alreadyInCart));
            return;
        }

        for (Sale.SaleItem item : currentSale.getItems()) {
            if (item.getProductId() == pid) {
                item.setQuantity(item.getQuantity() + qty);
                refreshCart();
                return;
            }
        }
        currentSale.addItem(new Sale.SaleItem(pid, prod.getName(), prod.getPrice(), qty));
        refreshCart();
    }

    private void removeItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { Utils.error(this, "Select a cart item to remove."); return; }
        if (Utils.confirm(this, "Remove this item from cart?")) {
            currentSale.removeItem(row);
            refreshCart();
        }
    }

    private void clearCart() {
        if (!currentSale.getItems().isEmpty()
                && Utils.confirm(this, "Clear the entire cart?")) {
            currentSale = new Sale();
            refreshCart();
        }
    }

    private void checkout() {
        if (currentSale.getItems().isEmpty()) {
            Utils.error(this, "Cart is empty."); return;
        }
        double paid = Utils.parseMoney(tfPaid.getText());
        if (paid < 0) {
            Utils.error(this, "Please enter a valid amount paid."); return;
        }
        if (paid < currentSale.getTotalAmount()) {
            Utils.error(this, "Amount paid must be at least "
                + Utils.fmt(currentSale.getTotalAmount())); return;
        }
        currentSale.setAmountPaid(paid);

        // Deduct stock
        for (Sale.SaleItem item : currentSale.getItems()) {
            Product prod = pDao.findById(item.getProductId());
            if (prod != null) {
                prod.setQuantity(prod.getQuantity() - item.getQuantity());
                pDao.update(prod);
            }
        }
        sDao.saveSale(currentSale);

        Sale completedSale = currentSale;
        String msg = String.format(
            "Sale Completed!\n\n"
            + "Total   : %s\n"
            + "Paid    : %s\n"
            + "Change  : %s\n\n"
            + "Thank you!",
            Utils.fmt(completedSale.getTotalAmount()),
            Utils.fmt(completedSale.getAmountPaid()),
            Utils.fmt(completedSale.getChangeGiven()));

        int choice = JOptionPane.showOptionDialog(this, msg, "Sale Complete",
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
            new String[]{"Print Receipt", "Close"}, "Close");

        if (choice == 0) {
            Sale forPrint = completedSale;
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() {
                    ReportEngine.printReceipt(forPrint); return null;
                }
            }.execute();
        }

        currentSale = new Sale();
        refreshCart();
        loadProducts(pDao.getAll());
        tfPaid.setText("0.00");
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        currentSale.getItems().forEach(i -> cartModel.addRow(new Object[]{
            i.getProductName(), Utils.fmt(i.getUnitPrice()),
            i.getQuantity(), Utils.fmt(i.getSubtotal())}));
        currentSale.recalc();
        lblTotal.setText(Utils.fmt(currentSale.getTotalAmount()));
        updateChange();
    }

    private void updateChange() {
        double paid   = Utils.parseMoney(tfPaid.getText());
        double change = paid < 0 ? 0 : Math.max(0, paid - currentSale.getTotalAmount());
        lblChange.setText(Utils.fmt(change));
        lblChange.setForeground(paid >= currentSale.getTotalAmount()
            ? Utils.ACCENT_AMBER : Utils.ACCENT_RED);
    }

    private void doSearch() {
        String kw = tfSearch.getText().trim();
        loadProducts(kw.isEmpty() ? pDao.getAll() : pDao.search(kw));
    }

    public void loadProducts(java.util.List<Product> list) {
        prodModel.setRowCount(0);
        list.forEach(p -> prodModel.addRow(new Object[]{
            p.getId(), p.getName(), p.getCategory(),
            Utils.fmt(p.getPrice()), p.getQuantity()}));
    }

    private void addPayRow(JPanel p, GridBagConstraints g,
                            int row, String label, JComponent field) {
        g.gridwidth = 1; g.weightx = 0.35; g.gridx = 0; g.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(Utils.FONT_HEADER); l.setForeground(Utils.TEXT_MUTED);
        p.add(l, g);
        g.gridx = 1; g.weightx = 0.65;
        p.add(field, g);
    }
}
