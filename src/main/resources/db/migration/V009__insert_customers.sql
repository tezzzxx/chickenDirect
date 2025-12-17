INSERT INTO customer (customer_id, name, phone_number, email)
VALUES
    (nextval('customer_seq'), 'Alice Johnson', '90224535', 'alice@example.com'),
    (nextval('customer_seq'), 'Bob Smith', '93224536', 'bob@example.com'),
    (nextval('customer_seq'), 'Charlie Brown', '41224537', 'charlie@example.com');