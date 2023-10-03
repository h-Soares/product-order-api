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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public Page<ProductDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductDTO::new);
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

    @Transactional
    public ProductDTO updateByUUID(String uuid, ProductInsertDTO productInsertDTO) {
        Product product = getProduct(uuid);
        modelMapper.map(productInsertDTO, product);
        product = productRepository.save(product);
        return new ProductDTO(product);
    }

    @Transactional
    public ProductDTO addCategoryByUUID(String product_uuid, ProductCategoryInsertDTO productCategoryInsertDTO) {
        UUID insertDTOCategoryUuid = UUID.fromString(productCategoryInsertDTO.getCategory_uuid());
        ifCategoryNotExistsThrowsException(insertDTOCategoryUuid);

        Category category = categoryRepository.getReferenceById(insertDTOCategoryUuid);
        Product product = getProduct(product_uuid);

        if(product.getCategories().contains(category))
            throw new EntityExistsException("Category already exists in this product");

        product.getCategories().add(category);
        product = productRepository.save(product);
        return new ProductDTO(product);
    }

    @Transactional
    public ProductDTO removeCategoryByUUID(String product_uuid, ProductCategoryInsertDTO productCategoryInsertDTO) {
        UUID insertDTOCategoryUuid = UUID.fromString(productCategoryInsertDTO.getCategory_uuid());
        ifCategoryNotExistsThrowsException(insertDTOCategoryUuid);

        Category category = categoryRepository.getReferenceById(insertDTOCategoryUuid);
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

    private void ifCategoryNotExistsThrowsException(UUID categoryUuid) {
        if(!categoryRepository.existsById(categoryUuid))
            throw new EntityNotFoundException("Category not found");
    }
}