package internal.entities;

import jakarta.persistence.*;

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

    private long quntity;
    private long unit_price;

    public OrderProduct() {
    }

    public OrderProduct(Order order, Product product, long quntity, long unit_price) {
        this.order = order;
        this.product = product;
        this.quntity = quntity;
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

    public long getQuntity() {
        return quntity;
    }

    public void setQuntity(long quntity) {
        this.quntity = quntity;
    }

    public long getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(long unit_price) {
        this.unit_price = unit_price;
    }

    @Override
    public String toString() {
        return "OrderProduct{" +
                "order_product_id=" + order_product_id +
                ", order=" + order +
                ", product=" + product +
                ", quntity=" + quntity +
                ", unit_price=" + unit_price +
                '}';
    }
}
