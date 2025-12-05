package internal.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_product_seq")
    @SequenceGenerator(name = "order_product_seq", sequenceName = "order_product_seq", allocationSize = 1)
    private long order_product_id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal quantity;
    private BigDecimal unit_price;

    public OrderProduct() {
    }

    public OrderProduct(Order order, Product product, BigDecimal quantity, BigDecimal unit_price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unit_price = unit_price;
    }

    public long getOrder_product_id() {
        return order_product_id;
    }

    public void setOrder_product_id(long order_product_id) {
        this.order_product_id = order_product_id;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(BigDecimal unit_price) {
        this.unit_price = unit_price;
    }

    @Override
    public String toString() {
        return "OrderProduct{" +
                "order_product_id=" + order_product_id +
                ", order=" + order +
                ", product=" + product +
                ", quantity=" + quantity +
                ", unit_price=" + unit_price +
                '}';
    }
}
