package com.soaresdev.productorderapi.entities;

import com.soaresdev.productorderapi.entities.pk.OrderItemPK;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "tb_order_product")
public class OrderItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private final OrderItemPK id = new OrderItemPK();
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false)
    private BigDecimal productPriceRecord;

    public OrderItem() {
    }

    public OrderItem(Order order, Product product, Integer quantity) {
        this.id.setOrder(order);
        this.id.setProduct(product);
        this.quantity = quantity;
        this.productPriceRecord = product.getPrice();
    }

    public Order getOrder() {
        return id.getOrder();
    }

    public void setOrder(Order order) {
        id.setOrder(order);
    }

    public Product getProduct() {
        return id.getProduct();
    }

    public void setProduct(Product product) {
        id.setProduct(product);
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getProductPriceRecord() {
        return productPriceRecord;
    }

    public BigDecimal getSubTotal() {
        return productPriceRecord.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}