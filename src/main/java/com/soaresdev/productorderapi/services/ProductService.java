package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.ProductDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductCategoryInsertDTO;
import com.soaresdev.productorderapi.dtos.insertDTOs.ProductInsertDTO;
import com.soaresdev.productorderapi.entities.Category;
import com.soaresdev.productorderapi.entities.Product;
import com.soaresdev.productorderapi.repositories.CategoryRepository;
import com.soaresdev.productorderapi.repositories.ProductRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream().map(ProductDTO::new).toList(); /* TODO: page */
    }

    public ProductDTO findByUUID(String uuid) {
        return new ProductDTO(getProduct(uuid));
    }

    @Transactional
    public ProductDTO insert(ProductInsertDTO productInsertDTO) {
        Product product = modelMapper.map(productInsertDTO, Product.class);
        product = productRepository.save(product);
        return new ProductDTO(product);
    }

    @Transactional
    public void deleteByUUID(String uuid) {
        productRepository.delete(getProduct(uuid));
    }

    @Transactional //TODO: price validation in ProductInsertDTO
    public ProductDTO updateByUUID(String uuid, ProductInsertDTO productInsertDTO) {
        Product product = getProduct(uuid);
        modelMapper.map(productInsertDTO, product);
        product = productRepository.save(product);
        return new ProductDTO(product);
    }

    @Transactional
    public ProductDTO addCategoryByUUID(String product_uuid, ProductCategoryInsertDTO productCategoryInsertDTO) {
        if(!categoryRepository.existsById(UUID.fromString(productCategoryInsertDTO.getCategory_uuid())))
            throw new EntityNotFoundException("Category not found");

        Category category = categoryRepository.getReferenceById(UUID.fromString(productCategoryInsertDTO.getCategory_uuid()));
        Product product = getProduct(product_uuid);

        if(product.getCategories().contains(category))
            throw new EntityExistsException("Category already exists in this product");

        product.getCategories().add(category);
        product = productRepository.save(product);
        return new ProductDTO(product);
    }

    @Transactional
    public ProductDTO removeCategoryByUUID(String product_uuid, ProductCategoryInsertDTO productCategoryInsertDTO) {
        if(!categoryRepository.existsById(UUID.fromString(productCategoryInsertDTO.getCategory_uuid())))
            throw new EntityNotFoundException("Category not found");

        Category category = categoryRepository.getReferenceById(UUID.fromString(productCategoryInsertDTO.getCategory_uuid()));
        Product product = getProduct(product_uuid);

        if(!product.getCategories().contains(category))
            throw new EntityNotFoundException("Category not found in this product");

        product.getCategories().remove(category);
        product = productRepository.save(product);
        return new ProductDTO(product);
    }

    private Product getProduct(String uuid) {
        return productRepository.findById(UUID.fromString(uuid))
               .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }
}