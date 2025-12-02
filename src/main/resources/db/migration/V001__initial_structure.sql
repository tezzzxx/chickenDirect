CREATE TABLE customer(
    customer_id BIGINT PRIMARY KEY,
    name VARCHAR (150),
    phone_number VARCHAR (30),
    email VARCHAR (75)
)

CREATE TABLE product(
    product_id BIGINT PRIMARY KEY ,
    name VARCHAR (150),
    description VARCHAR (800),
    price DECIMAL,
    status VARCHAR (25),
    quantity INT
)

CREATE TABLE order (
    order_id BIGINT PRIMARY KEY ,
    customer_id BIGINT,
    address_id BIGINT,
    order_date DATE,
    total_sum INT,
    shipping_charge INT,
    status VARCHAR (30),
    FOREIGN KEY (customer_id) references customer(customer_id),
    FOREIGN KEY (address_id) references customer_address(address_id)
)

CREATE TABLE address(
    address_id BIGINT PRIMARY KEY,
    customer_id BIGINT,
    apartment_number VARCHAR(15),
    address VARCHAR (150),
    zip_code VARCHAR (30),
    city VARCHAR (40),
    country VARCHAR (50),
    FOREIGN KEY (customer_id) references customer(customer_id)
)

CREATE TABLE order_product(
    order_product_id BIGINT PRIMARY KEY,
    order_id BIGINT,
    product_id BIGINT,
    quantity INT,
    unit_price INT,
    FOREIGN KEY (order_id) references order(order_id),
    FOREIGN KEY (product_id) references product(product_id)
)


create sequence customer_seq increment by 1 start with 1;
create sequence order_seq increment by 1 start with 1;
create sequence address_seq increment by 1 start with 1;
create sequence address_seq increment by 1 start with 1;
create sequence order_product_seq increment by 1 start with 1;