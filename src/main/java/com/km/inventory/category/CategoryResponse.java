package com.km.inventory.category;


import java.util.List;

import com.km.inventory.product.ProductSummary;

public class CategoryResponse {
    
    private Long id;
    private String name;
    private List<ProductSummary> products;

    
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
    public List<ProductSummary> getProducts() {
        return products;
    }
    public void setProducts(List<ProductSummary> products) {
        this.products = products;
    }


}
