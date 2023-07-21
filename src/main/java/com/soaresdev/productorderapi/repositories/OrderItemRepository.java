package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.OrderItem;
import com.soaresdev.productorderapi.entities.pk.OrderItemPK;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemPK> {
    @Transactional
    void deleteById_OrderIdAndId_ProductId(UUID order_uuid, UUID product_uuid);

    boolean existsById_OrderIdAndId_ProductId(UUID order_uuid, UUID product_uuid);
    OrderItem findById_OrderIdAndId_ProductId(UUID order_uuid, UUID product_uuid);
}