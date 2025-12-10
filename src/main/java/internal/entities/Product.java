package internal.entities;

import internal.enums.ProductStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 1)

    private long product_id;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus productStatus;
    private int quantity;

    @OneToMany(mappedBy = "product")
    private List<OrderProduct> order_product;

    public Product(String name, String description, BigDecimal price, ProductStatus productStatus, int quantity, List<OrderProduct> order_product) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.productStatus = productStatus;
        this.quantity = quantity;
        this.order_product = order_product;
    }

    public Product(String name, String description, BigDecimal price, ProductStatus productStatus, int quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.productStatus = productStatus;
        this.quantity = quantity;
    }

    public Product() {
    }

    public long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(long product_id) {
        this.product_id = product_id;
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

    public void setProductStatus(ProductStatus orderStatus) {
        this.productStatus = orderStatus;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<OrderProduct> getOrder_product() {
        return order_product;
    }

    public void setOrder_product(List<OrderProduct> order_product) {
        this.order_product = order_product;
    }

    @Override
    public String toString() {
        return "Product{" +
                "product_id=" + product_id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", productStatus=" + productStatus +
                ", quantity=" + quantity +
                ", order_product=" + order_product +
                '}';
    }
}
