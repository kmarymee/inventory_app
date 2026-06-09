package com.km.inventory.common;

public abstract class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName+" with id "+id+" could not be found.");
    }
    
}
