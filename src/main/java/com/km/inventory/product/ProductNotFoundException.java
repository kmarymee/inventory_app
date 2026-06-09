package com.km.inventory.product;

import com.km.inventory.common.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(Long id) {
        super("Product", id);
    }
}
