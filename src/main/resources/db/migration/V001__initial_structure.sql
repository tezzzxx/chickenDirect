CREATE TABLE customer(
    customer_id primary-key bigint,
    name varchar (150),
    phone_number varchar (30),
    email varchar (75),
)

CREATE TABLE product(
    product_id primary-key bigint,
    name varchar (150),
    description varchar (800),
    price decimal,
    status varchar (25),
    quantity int,
)

CREATE TABLE order (
    order_id primary-key bigint,
    customer_id bigint,
    address_id bigint,
    order_date date,
    total_sum int,
    shipping_charge int,
    status varchar (30),
    FOREIGN KEY (customer_id) references customer(customer_id),
    FOREIGN KEY (address_id) references customer_address(address_id),
)

CREATE TABLE address(
    address_id primary-key bigint,
    customer_id bigint,
    apartment_number varchar(15),
    address varchar (150),
    zip_code varchar (30),
    city varchar (40),
    country varchar (50),
    FOREIGN KEY (customer_id) references customer(customer_id),
)


create sequence customer_seq increment by 1 start with 1;
create sequence order_seq increment by 1 start with 1;
create sequence address_seq increment by 1 start with 1;
create sequence address_seq increment by 1 start with 1;