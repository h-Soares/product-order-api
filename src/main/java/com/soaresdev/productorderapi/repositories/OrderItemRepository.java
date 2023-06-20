package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.OrderItem;
import com.soaresdev.productorderapi.entities.pk.OrderItemPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemPK> {
}