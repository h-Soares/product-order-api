package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Order;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Override
    @Nonnull
    @EntityGraph(attributePaths = {"client", "items", "payment"}) //To improve SQL query performance
    Page<Order> findAll(Pageable pageable);
}