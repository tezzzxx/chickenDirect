CREATE TABLE customer_address (
    customer_id BIGINT NOT NULL,
    address_id BIGINT NOT NULL,
    PRIMARY KEY (customer_id, address_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (address_id) REFERENCES address(address_id)
);