package com.km.inventory.category;

public class CategoryNotFoundException extends ResourceNotFoundException {
    public CategoryNotFoundException(Long id){
        super("Category", id);
    }
}
