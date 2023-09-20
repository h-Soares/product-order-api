package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Payment;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsByOrderId(UUID uuid);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Payment p WHERE p.id = :uuid")
    void deleteByUUID(UUID uuid);

    @Override
    @Nonnull
    @EntityGraph(attributePaths = {"order"}) //To improve SQL query performance
    Page<Payment> findAll(Pageable pageable);
}