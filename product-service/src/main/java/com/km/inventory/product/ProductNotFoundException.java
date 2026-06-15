
package com.km.inventory.product;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(Long id) {
        super("Product", id);
    }
}
