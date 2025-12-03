CREATE TABLE customer(
    customer_id BIGINT PRIMARY KEY,
    name VARCHAR (150),
    phone_number VARCHAR (30),
    email VARCHAR (75)
);

CREATE TABLE address(
    address_id BIGINT PRIMARY KEY,
    customer_id BIGINT REFERENCES customer(customer_id),
    apartment_number VARCHAR(15),
    address VARCHAR (150),
    zip_code VARCHAR (30),
    city VARCHAR (40),
    country VARCHAR (50)
);

CREATE TABLE product(
    product_id BIGINT PRIMARY KEY ,
    name VARCHAR (150),
    description VARCHAR (800),
    price DECIMAL,
    status VARCHAR (25),
    quantity INT
);

CREATE TABLE customer_order (
    order_id BIGINT PRIMARY KEY ,
    customer_id BIGINT REFERENCES customer(customer_id),
    address_id BIGINT REFERENCES address(address_id),
    order_date DATE,
    total_sum INT,
    shipping_charge INT,
    status VARCHAR (30)
);

CREATE TABLE order_product(
    order_product_id BIGINT PRIMARY KEY,
    order_id BIGINT REFERENCES customer_order(order_id),
    product_id BIGINT REFERENCES product(product_id),
    quantity INT,
    unit_price INT
);

create sequence customer_seq increment by 1 start with 1;
create sequence order_seq increment by 1 start with 1;
create sequence address_seq increment by 1 start with 1;
create sequence product_seq increment by 1 start with 1;
create sequence order_product_seq increment by 1 start with 1;