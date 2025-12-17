INSERT INTO address (address_id, apartment_number, address, zip_code, city, country)
VALUES
    (nextval('address_seq'), '12A', 'Main Street 1', '10001', 'New York', 'USA'),
    (nextval('address_seq'), '5B', 'Second Avenue 23', '20002', 'Los Angeles', 'USA'),
    (nextval('address_seq'), '7C', 'Third Boulevard 45', '30003', 'Chicago', 'USA');