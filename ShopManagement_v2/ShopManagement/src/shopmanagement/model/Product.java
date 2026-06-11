package shopmanagement.model;

public class Product {
    private int    id;
    private String name;
    private String category;
    private double price;
    private int    quantity;
    private String description;
    private String createdAt;
    private String updatedAt;

    public Product() {}
    public Product(int id, String name, String category, double price, int quantity, String description) {
        this.id = id; this.name = name; this.category = category;
        this.price = price; this.quantity = quantity; this.description = description;
    }

    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }
    public String getName()             { return name; }
    public void   setName(String n)     { this.name = n; }
    public String getCategory()         { return category; }
    public void   setCategory(String c) { this.category = c; }
    public double getPrice()            { return price; }
    public void   setPrice(double p)    { this.price = p; }
    public int    getQuantity()         { return quantity; }
    public void   setQuantity(int q)    { this.quantity = q; }
    public String getDescription()      { return description; }
    public void   setDescription(String d) { this.description = d; }
    public String getCreatedAt()        { return createdAt; }
    public void   setCreatedAt(String s){ this.createdAt = s; }
    public String getUpdatedAt()        { return updatedAt; }
    public void   setUpdatedAt(String s){ this.updatedAt = s; }

    @Override public String toString() { return name; }
}
