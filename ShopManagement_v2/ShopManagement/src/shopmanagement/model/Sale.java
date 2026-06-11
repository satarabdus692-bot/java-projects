package shopmanagement.model;

import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int            id;
    private String         saleDate;
    private List<SaleItem> items       = new ArrayList<>();
    private double         totalAmount;
    private double         amountPaid;
    private double         changeGiven;

    public Sale() {}
    public Sale(int id, String saleDate, double totalAmount, double amountPaid, double changeGiven) {
        this.id = id; this.saleDate = saleDate;
        this.totalAmount = totalAmount; this.amountPaid = amountPaid; this.changeGiven = changeGiven;
    }

    public void addItem(SaleItem item) { items.add(item); recalc(); }
    public void removeItem(int idx)    { if (idx >= 0 && idx < items.size()) { items.remove(idx); recalc(); } }
    public void recalc() {
        totalAmount = items.stream().mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
    }

    public int            getId()               { return id; }
    public void           setId(int id)         { this.id = id; }
    public String         getSaleDate()         { return saleDate; }
    public void           setSaleDate(String d) { this.saleDate = d; }
    public List<SaleItem> getItems()            { return items; }
    public double         getTotalAmount()      { return totalAmount; }
    public void           setTotalAmount(double t){ this.totalAmount = t; }
    public double         getAmountPaid()       { return amountPaid; }
    public void           setAmountPaid(double a){ this.amountPaid = a; this.changeGiven = a - totalAmount; }
    public double         getChangeGiven()      { return changeGiven; }
    public void           setChangeGiven(double c){ this.changeGiven = c; }

    public static class SaleItem {
        private int    productId;
        private String productName;
        private double unitPrice;
        private int    quantity;

        public SaleItem() {}
        public SaleItem(int productId, String productName, double unitPrice, int quantity) {
            this.productId = productId; this.productName = productName;
            this.unitPrice = unitPrice; this.quantity = quantity;
        }
        public int    getProductId()            { return productId; }
        public String getProductName()          { return productName; }
        public double getUnitPrice()            { return unitPrice; }
        public int    getQuantity()             { return quantity; }
        public void   setQuantity(int q)        { this.quantity = q; }
        public double getSubtotal()             { return unitPrice * quantity; }
    }
}
