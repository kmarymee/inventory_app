package com.km.inventory.product;

public abstract class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName+" with id "+id+" could not be found.");
    }
    
}
