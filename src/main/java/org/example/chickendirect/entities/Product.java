package org.example.chickendirect.entities;

import org.example.chickendirect.enums.ProductStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 1)

    @Column(name = "product_id")
    private long productId;

    private String name;
    private String description;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false)
    private ProductStatus productStatus;
    private int quantity;
    @Column(nullable = false)
    private String unit;

    @OneToMany(mappedBy = "product")
    private List<OrderProduct> orderProduct;

    public Product(String name, String description, BigDecimal price, ProductStatus productStatus, int quantity, List<OrderProduct> orderProduct) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.productStatus = productStatus;
        this.quantity = quantity;
        this.orderProduct = orderProduct;
    }

    public Product(String name, String description, BigDecimal price, ProductStatus productStatus, int quantity, String unit) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.productStatus = productStatus;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Product() {
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(ProductStatus productStatus) {
        this.productStatus = productStatus;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<OrderProduct> getOrderProduct() {
        return orderProduct;
    }

    public void setOrderProduct(List<OrderProduct> orderProduct) {
        this.orderProduct = orderProduct;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", productStatus=" + productStatus +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", orderProduct=" + orderProduct +
                '}';
    }
}
