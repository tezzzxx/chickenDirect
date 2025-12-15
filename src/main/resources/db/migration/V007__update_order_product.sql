ALTER TABLE order_product
ADD COLUMN customer_id BIGINT;

ALTER TABLE order_product
ADD CONSTRAINT fk_order_product_customer
FOREIGN KEY (customer_id) REFERENCES customer(customer_id);