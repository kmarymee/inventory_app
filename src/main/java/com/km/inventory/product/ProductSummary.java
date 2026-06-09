package com.km.inventory.product;

public class ProductSummary {
    private Long id;
    private String name;
    private double price;


    public static ProductSummary fromProduct(Product product) {
        ProductSummary summary = new ProductSummary();

        summary.setId(product.getId());
        summary.setName(product.getName());
        summary.setPrice(product.getPrice());
        return summary;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

}
