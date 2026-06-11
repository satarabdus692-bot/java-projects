package shopmanagement.ui;

import shopmanagement.dao.ProductDAO;
import shopmanagement.model.Product;
import shopmanagement.util.Utils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {

    private final ProductDAO      dao = ProductDAO.getInstance();
    private JTable                table;
    private DefaultTableModel     model;
    private JTextField            tfSearch;

    // Form fields
    private JTextField  tfName, tfPrice, tfQuantity;
    private JComboBox<String> cbCategory;
    private JTextArea   taDesc;
    private JButton     btnSave, btnDelete, btnClear;
    private JLabel      formTitle;
    private int         editId = -1;

    public ProductPanel() {
        setBackground(Utils.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        build();
        loadTable(dao.getAll());
    }

    private void build() {
        // ── Top bar ──────────────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setBackground(Utils.BG_DARK);
        top.setBorder(BorderFactory.createEmptyBorder(28, 28, 16, 28));

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Inventory Management");
        title.setFont(Utils.FONT_TITLE); title.setForeground(Utils.TEXT_PRIMARY);
        JLabel sub = new JLabel("Add, edit, and manage your product catalog");
        sub.setFont(Utils.FONT_BODY); sub.setForeground(Utils.TEXT_MUTED);
        titleArea.add(title);
        titleArea.add(Box.createVerticalStrut(3));
        titleArea.add(sub);
        top.add(titleArea, BorderLayout.WEST);

        tfSearch = Utils.darkField();
        tfSearch.setPreferredSize(new Dimension(220, 34));
        tfSearch.setToolTipText("Search by name or category");
        tfSearch.addActionListener(e -> doSearch());

        JButton btnS = Utils.accentButton("Search", Utils.ACCENT_TEAL);
        btnS.setForeground(Utils.BG_DARK);
        btnS.addActionListener(e -> doSearch());

        JButton btnAll = Utils.accentButton("Show All", Utils.BG_FIELD);
        btnAll.setForeground(Utils.TEXT_PRIMARY);
        btnAll.addActionListener(e -> { tfSearch.setText(""); loadTable(dao.getAll()); });

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRow.setOpaque(false);
        searchRow.add(Utils.darkLabel("Search:"));
        searchRow.add(tfSearch);
        searchRow.add(btnS);
        searchRow.add(btnAll);
        top.add(searchRow, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────────────────────
        String[] cols = {"ID", "Product Name", "Category", "Price", "Qty", "Last Updated"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Utils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(0).setCellRenderer(Utils.centeredRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(Utils.rightRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(Utils.centeredRenderer());

        // Qty renderer — colour low stock rows amber/red
        table.getColumnModel().getColumn(4).setCellRenderer(new Utils.StripedRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel && val instanceof Integer) {
                    int qty = (Integer) val;
                    if      (qty <= 5)  setForeground(Utils.ACCENT_RED);
                    else if (qty <= 10) setForeground(Utils.ACCENT_AMBER);
                    else                setForeground(Utils.TEXT_PRIMARY);
                }
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelect();
        });

        JScrollPane scroll = Utils.darkScroll(table);

        // ── Form panel ───────────────────────────────────────────────────────────
        JPanel form = buildForm();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, form);
        split.setResizeWeight(0.65);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(Utils.BG_DARK);
        split.setDividerLocation(760);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Utils.BG_DARK);
        center.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        center.add(split, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Utils.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        p.setPreferredSize(new Dimension(310, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(5, 0, 5, 0);

        formTitle = new JLabel("Add New Product");
        formTitle.setFont(Utils.FONT_HEADER);
        formTitle.setForeground(Utils.ACCENT_TEAL);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        p.add(formTitle, g);

        JSeparator sep = new JSeparator();
        sep.setForeground(Utils.BORDER_COLOR);
        g.gridy = 1; p.add(sep, g);
        g.gridwidth = 1;

        tfName     = Utils.darkField();
        tfPrice    = Utils.darkField();
        tfQuantity = Utils.darkField();
        taDesc     = Utils.darkArea(3);

        // Category combo (editable so user can type new categories)
        cbCategory = new JComboBox<>();
        cbCategory.setEditable(true);
        cbCategory.setBackground(Utils.BG_FIELD);
        cbCategory.setForeground(Utils.TEXT_PRIMARY);
        cbCategory.setFont(Utils.FONT_BODY);
        refreshCategories();

        int row = 2;
        row = addRow(p, g, row, "Product Name *", tfName);
        row = addRow(p, g, row, "Category *",     cbCategory);
        row = addRow(p, g, row, "Price ($) *",    tfPrice);
        row = addRow(p, g, row, "Quantity *",      tfQuantity);

        g.gridwidth = 2; g.gridx = 0; g.gridy = row;
        g.insets = new Insets(8, 0, 2, 0);
        p.add(Utils.darkLabel("Description"), g); row++;

        g.gridy = row; g.insets = new Insets(0, 0, 0, 0);
        JScrollPane dScroll = Utils.darkScroll(taDesc);
        dScroll.setPreferredSize(new Dimension(0, 75));
        p.add(dScroll, g); row++;

        // Buttons
        btnSave   = Utils.accentButton("Save",   Utils.ACCENT_TEAL);
        btnDelete = Utils.accentButton("Delete", Utils.ACCENT_RED);
        btnClear  = Utils.accentButton("Clear",  Utils.BG_FIELD);
        btnSave.setForeground(Utils.BG_DARK);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setEnabled(false);
        btnClear.setForeground(Utils.TEXT_PRIMARY);

        btnSave.addActionListener(e -> save());
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clearForm());

        JPanel btnRow = new JPanel(new GridLayout(1, 3, 6, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnSave); btnRow.add(btnDelete); btnRow.add(btnClear);

        g.gridy = row; g.insets = new Insets(16, 0, 0, 0);
        p.add(btnRow, g);

        return p;
    }

    private void refreshCategories() {
        String current = cbCategory.isEditable()
            ? (String) cbCategory.getEditor().getItem()
            : (String) cbCategory.getSelectedItem();
        cbCategory.removeAllItems();
        dao.getCategories().forEach(cbCategory::addItem);
        if (current != null && !current.isEmpty()) {
            cbCategory.setSelectedItem(current);
        }
    }

    private void save() {
        String name     = tfName.getText().trim();
        String cat      = (cbCategory.getEditor().getItem() != null
                           ? cbCategory.getEditor().getItem().toString() : "").trim();
        String priceStr = tfPrice.getText().trim();
        String qtyStr   = tfQuantity.getText().trim();

        if (name.isEmpty() || cat.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
            Utils.error(this, "Fields marked * are required."); return;
        }
        double price; int qty;
        try {
            price = Double.parseDouble(priceStr);
            qty   = Integer.parseInt(qtyStr);
            if (price < 0 || qty < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            Utils.error(this, "Price must be a positive number.\nQuantity must be a positive integer.");
            return;
        }

        Product pr = new Product(editId < 0 ? 0 : editId,
            name, cat, price, qty, taDesc.getText().trim());

        if (editId < 0) {
            if (dao.insert(pr)) Utils.info(this, "Product added successfully!");
            else Utils.error(this, "Failed to add product.");
        } else {
            if (dao.update(pr)) Utils.info(this, "Product updated successfully!");
            else Utils.error(this, "Failed to update product.");
        }
        clearForm();
        loadTable(dao.getAll());
        refreshCategories();
    }

    private void delete() {
        if (editId < 0) return;
        if (Utils.confirm(this, "Delete this product permanently?")) {
            dao.delete(editId);
            clearForm();
            loadTable(dao.getAll());
        }
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        Product p = dao.findById(id);
        if (p == null) return;
        editId = id;
        tfName.setText(p.getName());
        cbCategory.setSelectedItem(p.getCategory());
        tfPrice.setText(String.valueOf(p.getPrice()));
        tfQuantity.setText(String.valueOf(p.getQuantity()));
        taDesc.setText(p.getDescription());
        formTitle.setText("Edit Product #" + id);
        btnSave.setText("Update");
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        editId = -1;
        tfName.setText("");
        cbCategory.setSelectedIndex(cbCategory.getItemCount() > 0 ? 0 : -1);
        if (cbCategory.isEditable()) ((JTextField) cbCategory.getEditor().getEditorComponent()).setText("");
        tfPrice.setText("");
        tfQuantity.setText("");
        taDesc.setText("");
        formTitle.setText("Add New Product");
        btnSave.setText("Save");
        btnDelete.setEnabled(false);
        table.clearSelection();
    }

    private void doSearch() {
        String kw = tfSearch.getText().trim();
        loadTable(kw.isEmpty() ? dao.getAll() : dao.search(kw));
    }

    private void loadTable(List<Product> list) {
        model.setRowCount(0);
        list.forEach(p -> model.addRow(new Object[]{
            p.getId(), p.getName(), p.getCategory(),
            Utils.fmt(p.getPrice()),
            p.getQuantity(),
            formatDate(p.getUpdatedAt())}));
    }

    private String formatDate(String ts) {
        if (ts == null || ts.isEmpty()) return "";
        return ts.length() >= 16 ? ts.substring(0, 16) : ts;
    }

    private int addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridwidth = 2; g.gridx = 0; g.gridy = row;
        g.insets = new Insets(8, 0, 2, 0);
        p.add(Utils.darkLabel(label), g); row++;
        g.gridy = row; g.insets = new Insets(0, 0, 0, 0);
        p.add(field, g);
        return row + 1;
    }
}
