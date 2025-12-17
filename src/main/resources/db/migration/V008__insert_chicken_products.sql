INSERT INTO product (product_id, name, description, price, product_status, quantity, unit) VALUES
(nextval('product_seq'), 'Whole Chicken', 'Fresh whole chicken approx. 1.5kg', 8.99, 'IN_STOCK', 50, 'kg'),
(nextval('product_seq'), 'Chicken Breast', 'Boneless skinless chicken breast fillets', 10.50, 'IN_STOCK', 100, 'kg'),
(nextval('product_seq'), 'Chicken Thighs', 'Juicy chicken thighs perfect for grilling', 7.50, 'IN_STOCK', 80, 'kg'),
(nextval('product_seq'), 'Chicken Wings', 'Crispy wings great for snacks or parties', 6.99, 'IN_STOCK', 70, 'kg'),
(nextval('product_seq'), 'Chicken Drumsticks', 'Tender drumsticks ideal for roasting', 7.00, 'IN_STOCK', 60, 'kg'),
(nextval('product_seq'), 'Chicken Nuggets', 'Breaded chicken nuggets ready to fry', 5.99, 'IN_STOCK', 200, 'kg'),
(nextval('product_seq'), 'Chicken Sausages', 'Homemade chicken sausages with herbs', 9.50, 'PENDING_RESTOCK', 5, 'kg'),
(nextval('product_seq'), 'Chicken Liver', 'Fresh chicken liver perfect for pâté or stew', 4.50, 'PENDING_RESTOCK', 2, 'kg');