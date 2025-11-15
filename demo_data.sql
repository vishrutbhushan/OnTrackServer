INSERT INTO platform (user_id, platform_name, platform_rating, is_deleted, create_user, update_user, create_time, update_time)
VALUES
    (1, 'Amazon', 4.8, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Flipkart', 4.5, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'eBay', 4.3, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Myntra', 4.6, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Ajio', 4.4, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW());

INSERT INTO category (user_id, category_name, is_deleted, create_user, update_user, create_time, update_time)
VALUES
    (1, 'Electronics', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Clothing', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Books', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Home & Garden', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Sports & Outdoors', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Beauty & Personal Care', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Food & Groceries', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 'Toys & Games', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW());


INSERT INTO vendor (vendor_name, vendor_rating, is_deleted, create_user, update_user, create_time, update_time)
VALUES
    ('Amazon India', 4.9, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Flipkart Direct', 4.7, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Myntra Store', 4.6, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Apple India', 4.8, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Sony Store', 4.7, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Nike Official', 4.5, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Adidas India', 4.4, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Samsung Store', 4.8, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Realme India', 4.6, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('OnePlus Store', 4.7, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW());


INSERT INTO logistic_provider (provider_name, provider_rating, is_deleted, create_user, update_user, create_time, update_time)
VALUES
    ('FedEx', 4.7, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('DHL Express', 4.8, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Blue Dart', 4.6, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('DTDC', 4.4, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Delhivery', 4.5, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Shiprocket', 4.3, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Ecom Express', 4.4, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    ('Amazon Logistics', 4.7, FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW());

INSERT INTO orders (user_id, platform_id, category_id, vendor_id, logistic_provider_id, order_id, price, quantity, product_link, order_date, delivery_date, rating, shipment_status, is_deleted, create_user, update_user, create_time, update_time)
VALUES
    (1, 1, 1, 1, 1, 'AMZ-2025-001', 45999.00, 1, 'https://www.amazon.in/MacBook-M1-13.3-inch-laptop', '2025-11-04 14:30:00', '2025-11-08 18:00:00', NULL, 'ordered', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 1, 1, 4, 8, 'AMZ-2025-002', 129999.00, 1, 'https://www.amazon.in/Apple-iPhone-15-Pro-Max', '2025-11-05 10:15:00', '2025-11-10 12:00:00', NULL, 'ordered', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 2, 2, 2, 5, 'FKT-2025-001', 1999.00, 2, 'https://www.flipkart.com/puma-t-shirt', '2025-11-03 16:45:00', '2025-11-07 19:00:00', NULL, 'ordered', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW());


INSERT INTO orders (user_id, platform_id, category_id, vendor_id, logistic_provider_id, order_id, price, quantity, product_link, order_date, delivery_date, rating, shipment_status, is_deleted, create_user, update_user, create_time, update_time)
VALUES
    (1, 4, 1, 5, 2, 'SONY-2025-001', 24999.00, 1, 'https://www.sony.com/electronics/headphones', '2025-11-02 11:00:00', '2025-11-07 15:00:00', NULL, 'shipped', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 1, 3, 1, 1, 'AMZ-2025-003', 599.00, 3, 'https://www.amazon.in/books-fiction', '2025-10-31 09:30:00', '2025-11-06 18:00:00', NULL, 'shipped', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 3, 2, 6, 3, 'EBY-2025-001', 5999.00, 1, 'https://www.ebay.com/nike-shoes', '2025-11-01 13:20:00', '2025-11-08 20:00:00', NULL, 'shipped', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW());


INSERT INTO orders (user_id, platform_id, category_id, vendor_id, logistic_provider_id, order_id, price, quantity, product_link, order_date, delivery_date, rating, shipment_status, is_deleted, create_user, update_user, create_time, update_time)
VALUES
    (1, 1, 1, 9, 4, 'AMZ-2025-004', 18999.00, 1, 'https://www.amazon.in/realme-smartphone', '2025-10-29 15:10:00', '2025-11-05 22:00:00', NULL, 'out_of_delivery', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 2, 4, 2, 5, 'FKT-2025-002', 2499.00, 1, 'https://www.flipkart.com/home-decor', '2025-10-28 10:45:00', '2025-11-05 21:00:00', NULL, 'out_of_delivery', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 5, 6, 1, 7, 'AJO-2025-001', 3999.00, 2, 'https://www.ajio.com/beauty-products', '2025-10-27 12:00:00', '2025-11-05 23:59:00', NULL, 'out_of_delivery', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW());


INSERT INTO orders (user_id, platform_id, category_id, vendor_id, logistic_provider_id, order_id, price, quantity, product_link, order_date, delivery_date, rating, shipment_status, is_deleted, create_user, update_user, create_time, update_time)
VALUES
    (1, 1, 1, 8, 8, 'AMZ-2025-005', 42999.00, 1, 'https://www.amazon.in/samsung-tv', '2025-10-20 08:30:00', '2025-11-02 18:00:00', 4.8, 'delivered', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 4, 2, 10, 1, 'OPL-2025-001', 34999.00, 1, 'https://www.oneplus.com/phone', '2025-10-18 14:20:00', '2025-11-01 19:00:00', 4.5, 'delivered', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 2, 7, 1, 5, 'FKT-2025-003', 799.00, 5, 'https://www.flipkart.com/groceries', '2025-10-15 11:15:00', '2025-10-29 20:00:00', 4.2, 'delivered', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 1, 5, 1, 3, 'AMZ-2025-006', 8999.00, 1, 'https://www.amazon.in/sports-equipment', '2025-10-12 16:40:00', '2025-10-25 18:00:00', 4.7, 'delivered', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 3, 2, 7, 2, 'EBY-2025-002', 1299.00, 3, 'https://www.ebay.com/clothing', '2025-10-10 09:50:00', '2025-10-23 21:00:00', 3.9, 'delivered', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 5, 1, 5, 6, 'AJO-2025-002', 15999.00, 1, 'https://www.ajio.com/electronics', '2025-10-08 13:25:00', '2025-10-20 19:00:00', 4.6, 'delivered', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW());


INSERT INTO orders (user_id, platform_id, category_id, vendor_id, logistic_provider_id, order_id, price, quantity, product_link, order_date, delivery_date, rating, shipment_status, is_deleted, create_user, update_user, create_time, update_time)
VALUES
    (1, 1, 1, 1, 1, 'AMZ-2025-007', 25000.00, 1, 'https://www.amazon.in/cancelled-product', '2025-10-05 10:30:00', NULL, NULL, 'cancelled', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW()),
    (1, 2, 3, 2, 5, 'FKT-2025-004', 2999.00, 1, 'https://www.flipkart.com/cancelled-book', '2025-10-03 15:45:00', NULL, NULL, 'cancelled', FALSE, 'vishrut.bhushan.dev@gmail.com', 'vishrut.bhushan.dev@gmail.com', NOW(), NOW());

