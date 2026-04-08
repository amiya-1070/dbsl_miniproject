SET SERVEROUTPUT ON

PROMPT Creating views...

CREATE OR REPLACE VIEW vw_product_catalog AS
SELECT
    p.product_id,
    p.sku,
    p.product_name,
    c.category_name,
    p.unit_price,
    p.stock_qty,
    p.status,
    DBMS_LOB.SUBSTR(p.description, 200, 1) AS short_description
FROM product p
JOIN category c
    ON c.category_id = p.category_id
WHERE p.status <> 'DISCONTINUED';

CREATE OR REPLACE VIEW vw_customer_order_history AS
SELECT
    o.order_id,
    o.order_number,
    o.customer_id,
    u.full_name AS customer_name,
    o.order_date,
    o.status,
    o.payment_status,
    o.total_amount
FROM customer_order o
JOIN app_user u
    ON u.user_id = o.customer_id;

CREATE OR REPLACE VIEW vw_sales_summary AS
SELECT
    TRUNC(o.order_date) AS order_day,
    c.category_name,
    COUNT(DISTINCT o.order_id) AS orders_count,
    SUM(oi.quantity) AS units_sold,
    SUM(oi.line_total) AS revenue
FROM customer_order o
JOIN order_item oi
    ON oi.order_id = o.order_id
JOIN product p
    ON p.product_id = oi.product_id
JOIN category c
    ON c.category_id = p.category_id
WHERE o.payment_status = 'PAID'
GROUP BY TRUNC(o.order_date), c.category_name;

CREATE OR REPLACE VIEW vw_admin_product_editor AS
SELECT
    p.product_id,
    p.category_id,
    c.category_name,
    p.sku,
    p.product_name,
    p.description,
    p.unit_price,
    p.stock_qty,
    p.reorder_level,
    p.status,
    p.updated_at
FROM product p
JOIN category c
    ON c.category_id = p.category_id;

PROMPT Running sample SQL queries...

PROMPT 1. Basic retrieval of all products.
SELECT product_id, product_name, unit_price, stock_qty
FROM product
ORDER BY product_id;

PROMPT 2. Products between two prices.
SELECT product_name, unit_price
FROM product
WHERE unit_price BETWEEN 2000 AND 5000
ORDER BY unit_price;

PROMPT 3. Search by category and name.
SELECT product_name, category_name, unit_price
FROM vw_product_catalog
WHERE LOWER(category_name) = 'electronics'
   OR LOWER(product_name) LIKE '%watch%';

PROMPT 4. Aggregate category-wise stock value.
SELECT c.category_name, SUM(p.stock_qty * p.unit_price) AS inventory_value
FROM category c
JOIN product p
    ON p.category_id = c.category_id
GROUP BY c.category_name
ORDER BY inventory_value DESC;

PROMPT 5. HAVING clause for categories with more than one product.
SELECT c.category_name, COUNT(*) AS product_count
FROM category c
JOIN product p
    ON p.category_id = c.category_id
GROUP BY c.category_name
HAVING COUNT(*) > 1;

PROMPT 6. WITH clause for expensive products over average price.
WITH avg_price AS (
    SELECT AVG(unit_price) AS overall_avg
    FROM product
)
SELECT product_name, unit_price
FROM product, avg_price
WHERE product.unit_price > avg_price.overall_avg;

PROMPT 7. Nested subquery for products in the top-priced category.
SELECT product_name, unit_price
FROM product
WHERE category_id = (
    SELECT category_id
    FROM (
        SELECT category_id, AVG(unit_price) AS avg_price
        FROM product
        GROUP BY category_id
        ORDER BY avg_price DESC
    )
    WHERE ROWNUM = 1
);

PROMPT 8. EXISTS query for customers who already placed orders.
SELECT full_name, email
FROM app_user u
WHERE u.role = 'CUSTOMER'
  AND EXISTS (
        SELECT 1
        FROM customer_order o
        WHERE o.customer_id = u.user_id
  );

PROMPT 9. Set operation UNION for all active logins.
SELECT username FROM app_user WHERE role = 'ADMIN'
UNION
SELECT username FROM app_user WHERE role = 'CUSTOMER';

PROMPT 10. MINUS to show products never ordered yet.
SELECT product_id, product_name
FROM product
MINUS
SELECT p.product_id, p.product_name
FROM product p
JOIN order_item oi
    ON oi.product_id = p.product_id;

PROMPT 11. INTERSECT on products in cart and catalog.
SELECT product_id FROM cart_item
INTERSECT
SELECT product_id FROM product;

PROMPT 12. View access example.
SELECT *
FROM vw_product_catalog
ORDER BY category_name, product_name;

PROMPT 13. Transaction control example.
SAVEPOINT before_demo_update;
UPDATE product
SET unit_price = unit_price + 100
WHERE product_id = 1001;
ROLLBACK TO before_demo_update;

PROMPT 14. Order by and group by on customer history.
SELECT customer_name, COUNT(*) AS orders_placed, SUM(total_amount) AS total_spent
FROM vw_customer_order_history
GROUP BY customer_name
ORDER BY total_spent DESC NULLS LAST;
