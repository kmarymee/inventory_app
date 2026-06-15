package com.km.inventory.product;

import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final CategoryClient categoryClient;

    public ProductService(ProductRepository repository, CategoryClient categoryClient){
        this.repository = repository;
        this.categoryClient = categoryClient;
    }

    public List<ProductResponse> getAllProducts() {
        return repository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    //Instead of just taking a raw Product, we'll take a ProductRequest and assemble the Product here.
    // Then we'll deliver a ProductResponse based off of what we've done.

    public ProductResponse createProduct(ProductRequest request){
        
        //Check if the category exists via the Category Client
        if (!categoryClient.categoryExists(request.getCategoryId())) {
            throw new InvalidCategoryException(request.getCategoryId());
        }
        
        
        //Assemble the product based off of fields in the request
        Product product = new Product(


            request.getName(), 
            request.getPrice(), 
            request.getQuantity(),
            request.getCategoryId()
        );

        //Let's now save the product, then convert the Product object to a response using our tool, and then return it
        return toResponse(repository.save(product));

    }

    public ProductResponse getProductById(Long id){
        Product found = repository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        
        return toResponse(found);
    }

    public void deleteProductById(Long id){
        repository.deleteById(id);
    }

    // Update is now refactored to take requests, and produce responses instead of raw objects.
    public ProductResponse updateProduct(Long id, ProductRequest request){
        
        //Check if the category exists via the Category Client
        if (!categoryClient.categoryExists(request.getCategoryId())) {
            throw new InvalidCategoryException(request.getCategoryId());
        }
        

        Product toUpdate = repository.findById(id)
            .orElseThrow(()-> new ProductNotFoundException(id));

        toUpdate.setName(request.getName());
        toUpdate.setPrice(request.getPrice());
        toUpdate.setQuantity(request.getQuantity());
        toUpdate.setCategoryId(request.getCategoryId());
        

        
        //Save the update, convert the Product to a ProductResponse, and return
        return toResponse(repository.save(toUpdate));
    }


    // DTO Translation helpers

    // This will use a Product object to fill out the fields of a response object.
    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        response.setCategoryId(product.getCategoryId());
        
        return response;
    }

}
