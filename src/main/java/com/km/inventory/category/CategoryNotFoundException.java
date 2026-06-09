package com.km.inventory.category;

import com.km.inventory.common.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {
    public CategoryNotFoundException(Long id){
        super("Category", id);
    }
}
