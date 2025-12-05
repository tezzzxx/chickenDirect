package internal.entities;

import internal.enums.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "order_seq", allocationSize = 1)
    private long order_id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    private LocalDate date;
    private BigDecimal total_sum;
    private BigDecimal shipping_charge;
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> items = new ArrayList<>();

    public Order() {
    }

    public Order(Customer customer, Address address, LocalDate date, BigDecimal total_sum, BigDecimal shipping_charge, OrderStatus orderStatus, List<OrderProduct> items) {
        this.customer = customer;
        this.address = address;
        this.date = date;
        this.total_sum = total_sum;
        this.shipping_charge = shipping_charge;
        this.orderStatus = orderStatus;
        this.items = items;
    }

    public Order(Customer customer, Address address, LocalDate date, BigDecimal total_sum, BigDecimal shipping_charge, OrderStatus orderStatus) {
        this.customer = customer;
        this.address = address;
        this.date = date;
        this.total_sum = total_sum;
        this.shipping_charge = shipping_charge;
        this.orderStatus = orderStatus;
    }

    public long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(long order_id) {
        this.order_id = order_id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getTotal_sum() {
        return total_sum;
    }

    public void setTotal_sum(BigDecimal total_sum) {
        this.total_sum = total_sum;
    }

    public BigDecimal getShipping_charge() {
        return shipping_charge;
    }

    public void setShipping_charge(BigDecimal shipping_charge) {
        this.shipping_charge = shipping_charge;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<OrderProduct> getItems() {
        return items;
    }

    public void setItems(List<OrderProduct> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "order_id=" + order_id +
                ", customer=" + customer +
                ", address=" + address +
                ", date=" + date +
                ", total_sum=" + total_sum +
                ", shipping_charge=" + shipping_charge +
                ", orderStatus=" + orderStatus +
                ", items=" + items +
                '}';
    }
}
