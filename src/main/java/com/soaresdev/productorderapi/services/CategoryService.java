package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.CategoryDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.CategoryInsertDTO;
import com.soaresdev.productorderapi.entities.Category;
import com.soaresdev.productorderapi.repositories.CategoryRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryService(CategoryRepository categoryRepository,ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    public Page<CategoryDTO> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(CategoryDTO::new);
    }

    public CategoryDTO findByUUID(String uuid) {
        return new CategoryDTO(getCategory(uuid));
    }

    @Transactional
    public CategoryDTO insert(CategoryInsertDTO categoryInsertDTO) {
        if(categoryRepository.existsByName(categoryInsertDTO.getName()))
            throw new EntityExistsException("Category already exists");

        Category category = modelMapper.map(categoryInsertDTO, Category.class);
        category = categoryRepository.save(category);
        return new CategoryDTO(category);
    }

    @Transactional
    public void deleteByUUID(String uuid) {
        categoryRepository.deleteByUUID(getCategory(uuid).getId());
    }

    @Transactional
    public CategoryDTO updateByUUID(String uuid, CategoryInsertDTO categoryInsertDTO) {
        Category category = getCategory(uuid);
        if(!category.getName().equals(categoryInsertDTO.getName()) && categoryRepository.existsByName(categoryInsertDTO.getName()))
            throw new EntityExistsException("Category name already exists");

        category.setName(categoryInsertDTO.getName());
        category = categoryRepository.save(category);
        return new CategoryDTO(category);
    }

    private Category getCategory(String uuid) {
        return categoryRepository.findById(UUID.fromString(uuid))
               .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }
}