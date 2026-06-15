package com.km.inventory.product;

public class InvalidCategoryException extends RuntimeException {
    public InvalidCategoryException(Long categoryId) {
        super("Category with id " + categoryId + " does not exist.");
    }
}
