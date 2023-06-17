package com.soaresdev.productorderapi.services;

import com.soaresdev.productorderapi.dtos.ProductDTO;
import com.soaresdev.productorderapi.entities.Product;
import com.soaresdev.productorderapi.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream().map(ProductDTO::new).toList(); /* TODO: page */
    }

    public ProductDTO findByUUID(String uuid) {
        Optional<Product> optionalProduct = productRepository.findById(UUID.fromString(uuid));
        return new ProductDTO(optionalProduct.get()); /* TODO: handle exception */
    }
}