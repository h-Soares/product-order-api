package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Payment;
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
    @Modifying
    @Query("DELETE FROM Payment p WHERE p.id = :uuid")
    void deleteByUUID(UUID uuid);
}