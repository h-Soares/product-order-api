package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.CategoryDTO;
import com.soaresdev.productorderapi.entities.Category;
import com.soaresdev.productorderapi.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream().map(CategoryDTO::new).toList(); /* TODO: page */
    }

    public CategoryDTO findByUUID(String uuid) {
        Optional<Category> optionalCategory = categoryRepository.findById(UUID.fromString(uuid));
        return new CategoryDTO(optionalCategory.get()); /* TODO: handle exception */
    }
}