package org.example.chickendirect.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_product_seq")
    @SequenceGenerator(name = "order_product_seq", sequenceName = "order_product_seq", allocationSize = 1)
    @Column(name = "order_product_id")
    private long orderProductId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    public OrderProduct() {
    }

    public OrderProduct(Order order, Customer customer, Product product, int quantity, BigDecimal unitPrice) {
        this.order = order;
        this.customer = customer;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }


    public long getOrderProductId() {
        return orderProductId;
    }

    public void setOrderProductId(long order_product_id) {
        this.orderProductId = order_product_id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unit_price) {
        this.unitPrice = unit_price;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public String toString() {
        return "OrderProduct{" +
                "orderProductId=" + orderProductId +
                ", order=" + order +
                ", customer=" + customer +
                ", product=" + product +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
