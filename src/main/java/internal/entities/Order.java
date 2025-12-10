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
    @Column(name = "order_id")
    private long orderId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    private LocalDate date;
    @Column(name = "total_sum")
    private BigDecimal totalSum;
    @Column(name = "shipping_charge")
    private BigDecimal shippingCharge;
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> items = new ArrayList<>();

    public Order() {
    }

    public Order(Customer customer, Address address, LocalDate date, BigDecimal totalSum, BigDecimal shippingCharge, OrderStatus orderStatus, List<OrderProduct> items) {
        this.customer = customer;
        this.address = address;
        this.date = date;
        this.totalSum = totalSum;
        this.shippingCharge = shippingCharge;
        this.orderStatus = orderStatus;
        this.items = items;
    }

    public Order(Customer customer, Address address, LocalDate date, BigDecimal totalSum, BigDecimal shippingCharge, OrderStatus orderStatus) {
        this.customer = customer;
        this.address = address;
        this.date = date;
        this.totalSum = totalSum;
        this.shippingCharge = shippingCharge;
        this.orderStatus = orderStatus;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long order_id) {
        this.orderId = order_id;
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

    public BigDecimal getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(BigDecimal total_sum) {
        this.totalSum = total_sum;
    }

    public BigDecimal getShippingCharge() {
        return shippingCharge;
    }

    public void setShippingCharge(BigDecimal shipping_charge) {
        this.shippingCharge = shipping_charge;
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
                "order_id=" + orderId +
                ", customer=" + customer +
                ", address=" + address +
                ", date=" + date +
                ", total_sum=" + totalSum +
                ", shipping_charge=" + shippingCharge +
                ", orderStatus=" + orderStatus +
                ", items=" + items +
                '}';
    }
}
