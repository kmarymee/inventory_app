package com.km.inventory.category;

import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    // CREATE
    public CategoryResponse createCategory(CategoryRequest request) {
        Category newCategory = new Category(request.getName());
        return toResponse(repository.save(newCategory));
    }

    // READ
    public List<CategoryResponse> getAllCategories() {
        return repository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        Category found = repository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException(id));
        return toResponse(found);
    }

    // UPDATE
    public CategoryResponse updateCategoryById(Long id, CategoryRequest updated) {
        Category toUpdate = repository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException(id));

        toUpdate.setName(updated.getName());

        return toResponse(repository.save(toUpdate));
    }

    // DELETE
    public void deleteCategoryById(Long id) {
        repository.deleteById(id);
    }


    // DTO Translation 
    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());

        return response;
    }

}

