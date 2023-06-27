package com.soaresdev.productorderapi.repositories;

import com.soaresdev.productorderapi.entities.Order;
import com.soaresdev.productorderapi.entities.OrderItem;
import com.soaresdev.productorderapi.entities.Product;
import com.soaresdev.productorderapi.entities.pk.OrderItemPK;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemPK> {
    @Transactional
    void deleteById_OrderAndId_Product(Order order, Product product);

    boolean existsById_OrderAndId_Product(Order order, Product product);
}