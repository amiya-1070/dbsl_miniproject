SET SERVEROUTPUT ON

PROMPT Inserting master data...

INSERT INTO app_user (user_id, username, password_hash, role, full_name, email, phone)
VALUES (seq_app_user.NEXTVAL, 'admin01', STANDARD_HASH('Admin@123', 'SHA256'), 'ADMIN',
        'Aarav Menon', 'admin01@shop.local', '9876543210');

INSERT INTO admin_profile (admin_id, designation, office_location)
VALUES (1, 'Store Administrator', 'Manipal Main Office');

INSERT INTO app_user (user_id, username, password_hash, role, full_name, email, phone)
VALUES (seq_app_user.NEXTVAL, 'cust01', STANDARD_HASH('Cust@123', 'SHA256'), 'CUSTOMER',
        'Diya Sharma', 'cust01@shop.local', '9988776655');

INSERT INTO customer_profile (customer_id, default_address, city, pincode, loyalty_points)
VALUES (2, '12, Blue Pearl Residency, Manipal', 'Udupi', '576104', 25);

INSERT INTO app_user (user_id, username, password_hash, role, full_name, email, phone)
VALUES (seq_app_user.NEXTVAL, 'cust02', STANDARD_HASH('Cust@123', 'SHA256'), 'CUSTOMER',
        'Rohan Pai', 'cust02@shop.local', '9900112233');

INSERT INTO customer_profile (customer_id, default_address, city, pincode, loyalty_points)
VALUES (3, '77, Lake View Road, Mangalore', 'Mangalore', '575001', 10);

INSERT INTO category (category_id, category_name, description, display_order)
VALUES (seq_category.NEXTVAL, 'Electronics', 'Smart gadgets and accessories', 1);

INSERT INTO category (category_id, category_name, description, display_order)
VALUES (seq_category.NEXTVAL, 'Fashion', 'Trending clothing and apparel', 2);

INSERT INTO category (category_id, category_name, description, display_order)
VALUES (seq_category.NEXTVAL, 'Home Essentials', 'Daily-use and kitchen products', 3);

INSERT INTO product (product_id, category_id, sku, product_name, description, unit_price, stock_qty, reorder_level, status)
VALUES (seq_product.NEXTVAL, 1, 'ELEC-1001', 'Noise Cancelling Headphones',
        'Wireless over-ear headphones with deep bass and 30-hour battery.', 4999.00, 35, 8, 'AVAILABLE');

INSERT INTO product (product_id, category_id, sku, product_name, description, unit_price, stock_qty, reorder_level, status)
VALUES (seq_product.NEXTVAL, 1, 'ELEC-1002', 'Smart Fitness Watch',
        'Water-resistant wearable with heart-rate tracking and AMOLED display.', 3299.00, 48, 10, 'AVAILABLE');

INSERT INTO product (product_id, category_id, sku, product_name, description, unit_price, stock_qty, reorder_level, status)
VALUES (seq_product.NEXTVAL, 2, 'FASH-2001', 'Classic Denim Jacket',
        'Regular fit jacket suitable for all-season casual wear.', 2199.00, 20, 5, 'AVAILABLE');

INSERT INTO product (product_id, category_id, sku, product_name, description, unit_price, stock_qty, reorder_level, status)
VALUES (seq_product.NEXTVAL, 3, 'HOME-3001', 'Ceramic Dinner Set',
        '18-piece matte finish dining set for modern kitchens.', 2799.00, 15, 4, 'AVAILABLE');

INSERT INTO product (product_id, category_id, sku, product_name, description, unit_price, stock_qty, reorder_level, status)
VALUES (seq_product.NEXTVAL, 3, 'HOME-3002', 'Air Purifier Compact',
        'Compact purifier with HEPA filtration and silent night mode.', 6499.00, 12, 3, 'AVAILABLE');

INSERT INTO shopping_cart (cart_id, customer_id)
VALUES (seq_cart.NEXTVAL, 2);

INSERT INTO shopping_cart (cart_id, customer_id)
VALUES (seq_cart.NEXTVAL, 3);

COMMIT;

PROMPT Seed data loaded.
