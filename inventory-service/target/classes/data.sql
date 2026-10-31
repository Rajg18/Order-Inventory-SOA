-- Blinkit-style Quick Commerce Grocery Seed Data (14 items across 7 categories)

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 1, 'Fresh Alphonso Mangoes (1kg)', 249.00, 'https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80', 25, 25, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 1);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 2, 'Farm Fresh Broccoli (500g)', 65.00, 'https://images.unsplash.com/photo-1459411621453-7b03977f4bfc?auto=format&fit=crop&w=600&q=80', 30, 30, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 2);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 3, 'Amul Taaza Homogenised Toned Milk (1L)', 72.00, 'https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=600&q=80', 50, 50, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 3);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 4, 'Mother Dairy Classic Paneer (200g)', 95.00, 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=600&q=80', 20, 20, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 4);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 5, 'Coca-Cola Zero Sugar Can (330ml)', 40.00, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=600&q=80', 40, 40, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 5);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 6, 'Raw Pressery Tender Coconut Water (200ml)', 60.00, 'https://images.unsplash.com/photo-1525385133512-2f3bdd039054?auto=format&fit=crop&w=600&q=80', 15, 15, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 6);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 7, 'Doritos Cheese Supreme Nachos (150g)', 50.00, 'https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=600&q=80', 35, 35, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 7);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 8, 'Haldirams Bhujia Sev (400g)', 110.00, 'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80', 25, 25, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 8);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 9, 'Maggi 2-Minute Masala Noodles (Pack of 4)', 56.00, 'https://images.unsplash.com/photo-1612927601601-6638404737ce?auto=format&fit=crop&w=600&q=80', 60, 60, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 9);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 10, 'Kelloggs Real Almond & Honey Muesli (400g)', 280.00, 'https://images.unsplash.com/photo-1517456793572-1d8efd6dc135?auto=format&fit=crop&w=600&q=80', 18, 18, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 10);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 11, 'Surf Excel Matic Top Load Liquid Detergent (1L)', 230.00, 'https://images.unsplash.com/photo-1583947215259-38e31be8751f?auto=format&fit=crop&w=600&q=80', 22, 22, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 11);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 12, 'Vim Dishwash Gel Lemon (750ml)', 155.00, 'https://images.unsplash.com/photo-1585670270608-b4be4fb880ed?auto=format&fit=crop&w=600&q=80', 30, 30, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 12);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 13, 'Dettol Original Instant Hand Sanitizer (200ml)', 99.00, 'https://images.unsplash.com/photo-1584744982491-665216d95f8b?auto=format&fit=crop&w=600&q=80', 45, 45, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 13);

INSERT INTO products (id, product_name, price, image_url, total_quantity, available_quantity, reserved_quantity)
SELECT 14, 'Colgate MaxFresh Peppermint Toothpaste (150g)', 115.00, 'https://images.unsplash.com/photo-1559591937-e62fb330bc1f?auto=format&fit=crop&w=600&q=80', 40, 40, 0
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = 14);
