package com.soaresdev.productorderapi.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soaresdev.productorderapi.entities.OrderItem;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

public class OrderItemDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer quantity;
    private BigDecimal productPriceRecord;
    private BigDecimal subTotal;
    @JsonProperty("product")
    private ProductDTO productDTO;

    public OrderItemDTO() {
    }

    public OrderItemDTO(OrderItem orderItem) {
        this.quantity = orderItem.getQuantity();
        this.productPriceRecord = orderItem.getProductPriceRecord();
        this.subTotal = orderItem.getSubTotal();
        this.productDTO = new ProductDTO(orderItem.getProduct());
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

    public void setProductPriceRecord(BigDecimal productPriceRecord) {
        this.productPriceRecord = productPriceRecord;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public ProductDTO getProductDTO() {
        return productDTO;
    }

    public void setProductDTO(ProductDTO productDTO) {
        this.productDTO = productDTO;
    }
}