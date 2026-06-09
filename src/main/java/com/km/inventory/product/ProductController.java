package com.km.inventory.product;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service){
        this.service = service;
    }

    // READ All
    @GetMapping
    public List<ProductResponse> getAllProducts(){
        return service.getAllProducts();
    }

    // READ One
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return service.getProductById(id);
    }
    
    // UPDATE
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest updated) {

        return service.updateProduct(id, updated);
        
    }

    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        
        service.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    // CREATE 
    @PostMapping
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest product) {
        return service.createProduct(product);
    }
}
