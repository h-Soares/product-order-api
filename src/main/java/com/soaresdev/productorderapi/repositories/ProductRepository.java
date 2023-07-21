package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Product;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Override
    @Nonnull
    @EntityGraph(attributePaths = {"categories"}) //To improve SQL query performance
    List<Product> findAll();
}