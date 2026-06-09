package com.km.inventory.product;

import org.springframework.stereotype.Service;

import com.km.inventory.category.Category;
import com.km.inventory.category.CategoryNotFoundException;
import com.km.inventory.category.CategoryRepository;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;


    public ProductService(ProductRepository repository, CategoryRepository categoryRepository){
        this.repository = repository;
        this.categoryRepository = categoryRepository;
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
        //Assemble the product based off of fields in the request
        Product product = new Product(
            request.getName(), 
            request.getPrice(), 
            request.getQuantity()
        );
        //Now let's send the associated category id to our tool so it can attach a Category object
        applyCategory(product, request.getCategoryId());
        
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
        Product toUpdate = repository.findById(id)
            .orElseThrow(()-> new ProductNotFoundException(id));

        toUpdate.setName(request.getName());
        toUpdate.setPrice(request.getPrice());
        toUpdate.setQuantity(request.getQuantity());
        
        //Now we'll attach the appropriate Category object to the updated product
        applyCategory(toUpdate, request.getCategoryId());
        
        //Save the update, convert the Product to a ProductResponse, and return
        return toResponse(repository.save(toUpdate));
    }


    // DTO Translation helpers

    //This'll attach a Category object to a Product object
    private void applyCategory(Product product, Long categoryId) {
        if (categoryId != null) {
            //Retrieve the category object from the repository
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
            //Set the found category to the product provided
            product.setCategory(category);
        }
    }

    // This will use a Product object to fill out the fields of a response object.
    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        if (product.getCategory() != null) {
            //If the product has an assosciated category, 
            // store the basic information about it that we might need in a pinch
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        return response;
    }

}
