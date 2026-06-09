package com.km.inventory.category;


import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    
    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }


    // CREATE
    @PostMapping
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest category) {
        return service.createCategory(category);
    }


    // READ
    @GetMapping
    public List<CategoryResponse> getAllCategories(){
        return service.getAllCategories();
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable Long id) {
        return service.getCategoryById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public CategoryResponse updateCategoryById(@PathVariable Long id, @Valid @RequestBody CategoryRequest category) {
        return service.updateCategoryById(id, category);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable Long id) {
        service.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }
}

