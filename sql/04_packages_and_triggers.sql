SET SERVEROUTPUT ON

CREATE OR REPLACE PACKAGE shop_portal_pkg AS
    invalid_login EXCEPTION;
    insufficient_stock EXCEPTION;
    inactive_user EXCEPTION;

    PROCEDURE authenticate_user(
        p_username   IN  app_user.username%TYPE,
        p_password   IN  VARCHAR2,
        p_role       IN  app_user.role%TYPE,
        p_user_id    OUT app_user.user_id%TYPE,
        p_full_name  OUT app_user.full_name%TYPE
    );

    PROCEDURE add_to_cart(
        p_customer_id IN customer_profile.customer_id%TYPE,
        p_product_id  IN product.product_id%TYPE,
        p_quantity    IN NUMBER
    );

    PROCEDURE update_cart_quantity(
        p_customer_id IN customer_profile.customer_id%TYPE,
        p_product_id  IN product.product_id%TYPE,
        p_quantity    IN NUMBER
    );

    FUNCTION get_cart_total(
        p_customer_id IN customer_profile.customer_id%TYPE
    ) RETURN NUMBER;

    PROCEDURE place_order(
        p_customer_id     IN  customer_profile.customer_id%TYPE,
        p_payment_method  IN  payment.payment_method%TYPE,
        p_shipping_addr   IN  customer_order.shipping_address%TYPE,
        p_order_id        OUT NUMBER,
        p_invoice_id      OUT NUMBER,
        p_total_amount    OUT NUMBER
    );

    FUNCTION get_order_total(
        p_order_id IN customer_order.order_id%TYPE
    ) RETURN NUMBER;

    FUNCTION get_top_category(
        p_start_date IN DATE,
        p_end_date   IN DATE
    ) RETURN VARCHAR2;

    PROCEDURE generate_sales_report(
        p_start_date  IN DATE,
        p_end_date    IN DATE,
        p_category_id IN NUMBER DEFAULT NULL
    );

    PROCEDURE update_product_stock(
        p_product_id IN NUMBER,
        p_new_stock  IN NUMBER,
        p_admin_id   IN NUMBER
    );
END shop_portal_pkg;
/

CREATE OR REPLACE PACKAGE BODY shop_portal_pkg AS

    PROCEDURE authenticate_user(
        p_username   IN  app_user.username%TYPE,
        p_password   IN  VARCHAR2,
        p_role       IN  app_user.role%TYPE,
        p_user_id    OUT app_user.user_id%TYPE,
        p_full_name  OUT app_user.full_name%TYPE
    ) IS
        v_status app_user.status%TYPE;
    BEGIN
        SELECT user_id, full_name, status
        INTO p_user_id, p_full_name, v_status
        FROM app_user
        WHERE username = p_username
          AND role = p_role
          AND password_hash = STANDARD_HASH(p_password, 'SHA256');

        IF v_status <> 'ACTIVE' THEN
            INSERT INTO login_audit (audit_id, username, role_requested, success_flag, audit_message)
            VALUES (seq_login_audit.NEXTVAL, p_username, p_role, 'N', 'User account is blocked');
            RAISE inactive_user;
        END IF;

        UPDATE app_user
        SET last_login = SYSDATE
        WHERE user_id = p_user_id;

        INSERT INTO login_audit (audit_id, username, role_requested, success_flag, audit_message)
        VALUES (seq_login_audit.NEXTVAL, p_username, p_role, 'Y', 'Login successful');
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            INSERT INTO login_audit (audit_id, username, role_requested, success_flag, audit_message)
            VALUES (seq_login_audit.NEXTVAL, p_username, p_role, 'N', 'Invalid username / password / role');
            RAISE invalid_login;
    END authenticate_user;

    PROCEDURE add_to_cart(
        p_customer_id IN customer_profile.customer_id%TYPE,
        p_product_id  IN product.product_id%TYPE,
        p_quantity    IN NUMBER
    ) IS
        v_cart_id     shopping_cart.cart_id%TYPE;
        v_stock_qty   product.stock_qty%TYPE;
        v_status      product.status%TYPE;
        v_exists      NUMBER;
    BEGIN
        IF p_quantity <= 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Quantity must be greater than zero.');
        END IF;

        BEGIN
            SELECT cart_id
            INTO v_cart_id
            FROM shopping_cart
            WHERE customer_id = p_customer_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_cart_id := seq_cart.NEXTVAL;
                INSERT INTO shopping_cart (cart_id, customer_id)
                VALUES (v_cart_id, p_customer_id);
        END;

        SELECT stock_qty, status
        INTO v_stock_qty, v_status
        FROM product
        WHERE product_id = p_product_id;

        IF v_status <> 'AVAILABLE' OR v_stock_qty < p_quantity THEN
            RAISE insufficient_stock;
        END IF;

        SELECT COUNT(*)
        INTO v_exists
        FROM cart_item
        WHERE cart_id = v_cart_id
          AND product_id = p_product_id;

        IF v_exists = 0 THEN
            INSERT INTO cart_item (cart_item_id, cart_id, product_id, quantity, unit_price, line_total)
            VALUES (seq_cart_item.NEXTVAL, v_cart_id, p_product_id, p_quantity, 0, 0);
        ELSE
            UPDATE cart_item
            SET quantity = quantity + p_quantity
            WHERE cart_id = v_cart_id
              AND product_id = p_product_id;
        END IF;

        UPDATE shopping_cart
        SET updated_at = SYSDATE
        WHERE cart_id = v_cart_id;
    END add_to_cart;

    PROCEDURE update_cart_quantity(
        p_customer_id IN customer_profile.customer_id%TYPE,
        p_product_id  IN product.product_id%TYPE,
        p_quantity    IN NUMBER
    ) IS
        v_cart_id shopping_cart.cart_id%TYPE;
    BEGIN
        SELECT cart_id
        INTO v_cart_id
        FROM shopping_cart
        WHERE customer_id = p_customer_id;

        IF p_quantity <= 0 THEN
            DELETE FROM cart_item
            WHERE cart_id = v_cart_id
              AND product_id = p_product_id;
        ELSE
            UPDATE cart_item
            SET quantity = p_quantity
            WHERE cart_id = v_cart_id
              AND product_id = p_product_id;
        END IF;

        UPDATE shopping_cart
        SET updated_at = SYSDATE
        WHERE cart_id = v_cart_id;
    END update_cart_quantity;

    FUNCTION get_cart_total(
        p_customer_id IN customer_profile.customer_id%TYPE
    ) RETURN NUMBER IS
        v_total NUMBER := 0;
    BEGIN
        SELECT NVL(SUM(ci.line_total), 0)
        INTO v_total
        FROM shopping_cart sc
        LEFT JOIN cart_item ci
            ON ci.cart_id = sc.cart_id
        WHERE sc.customer_id = p_customer_id;

        RETURN v_total;
    END get_cart_total;

    PROCEDURE place_order(
        p_customer_id     IN  customer_profile.customer_id%TYPE,
        p_payment_method  IN  payment.payment_method%TYPE,
        p_shipping_addr   IN  customer_order.shipping_address%TYPE,
        p_order_id        OUT NUMBER,
        p_invoice_id      OUT NUMBER,
        p_total_amount    OUT NUMBER
    ) IS
        CURSOR c_cart_items IS
            SELECT ci.product_id, ci.quantity, ci.line_total
            FROM shopping_cart sc
            JOIN cart_item ci
                ON ci.cart_id = sc.cart_id
            WHERE sc.customer_id = p_customer_id
            FOR UPDATE OF ci.quantity;

        v_gross         NUMBER := 0;
        v_tax           NUMBER := 0;
        v_invoice_no    VARCHAR2(25);
        v_order_number  VARCHAR2(20);
        v_stock_qty     product.stock_qty%TYPE;
        v_counter       NUMBER := 0;
    BEGIN
        p_order_id := seq_order.NEXTVAL;
        p_invoice_id := seq_invoice.NEXTVAL;
        v_order_number := 'ORD-' || TO_CHAR(p_order_id, 'FM000000');
        v_invoice_no := 'INV-' || TO_CHAR(p_invoice_id, 'FM000000');

        INSERT INTO customer_order (
            order_id, order_number, customer_id, shipping_address, gross_amount,
            tax_amount, total_amount, payment_status, created_by
        )
        VALUES (
            p_order_id, v_order_number, p_customer_id, p_shipping_addr, 0,
            0, 0, 'PENDING', p_customer_id
        );

        FOR rec IN c_cart_items LOOP
            v_counter := v_counter + 1;

            SELECT stock_qty
            INTO v_stock_qty
            FROM product
            WHERE product_id = rec.product_id
            FOR UPDATE;

            IF v_stock_qty < rec.quantity THEN
                RAISE insufficient_stock;
            END IF;

            INSERT INTO order_item (order_item_id, order_id, product_id, quantity, unit_price, line_total)
            VALUES (seq_order_item.NEXTVAL, p_order_id, rec.product_id, rec.quantity, 0, 0);

            v_gross := v_gross + rec.line_total;
        END LOOP;

        IF v_counter = 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Cart is empty. Add products before checkout.');
        END IF;

        v_tax := ROUND(v_gross * 0.05, 2);
        p_total_amount := v_gross + v_tax;

        UPDATE customer_order
        SET gross_amount = v_gross,
            tax_amount = v_tax,
            total_amount = p_total_amount,
            payment_status = 'PAID',
            status = 'PAID'
        WHERE order_id = p_order_id;

        INSERT INTO payment (payment_id, order_id, payment_method, payment_reference, amount, payment_status)
        VALUES (seq_payment.NEXTVAL, p_order_id, p_payment_method, 'PAY-' || TO_CHAR(p_order_id), p_total_amount, 'PAID');

        INSERT INTO invoice (invoice_id, order_id, invoice_number, subtotal, tax_amount, total_amount)
        VALUES (p_invoice_id, p_order_id, v_invoice_no, v_gross, v_tax, p_total_amount);

        DELETE FROM cart_item
        WHERE cart_id = (
            SELECT cart_id
            FROM shopping_cart
            WHERE customer_id = p_customer_id
        );

        UPDATE shopping_cart
        SET updated_at = SYSDATE
        WHERE customer_id = p_customer_id;

        UPDATE customer_profile
        SET loyalty_points = loyalty_points + FLOOR(p_total_amount / 100)
        WHERE customer_id = p_customer_id;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END place_order;

    FUNCTION get_order_total(
        p_order_id IN customer_order.order_id%TYPE
    ) RETURN NUMBER IS
        v_total NUMBER;
    BEGIN
        SELECT total_amount
        INTO v_total
        FROM customer_order
        WHERE order_id = p_order_id;

        RETURN v_total;
    END get_order_total;

    FUNCTION get_top_category(
        p_start_date IN DATE,
        p_end_date   IN DATE
    ) RETURN VARCHAR2 IS
        v_category_name category.category_name%TYPE;
    BEGIN
        SELECT category_name
        INTO v_category_name
        FROM (
            SELECT c.category_name, SUM(oi.line_total) AS revenue
            FROM customer_order o
            JOIN order_item oi
                ON oi.order_id = o.order_id
            JOIN product p
                ON p.product_id = oi.product_id
            JOIN category c
                ON c.category_id = p.category_id
            WHERE TRUNC(o.order_date) BETWEEN TRUNC(p_start_date) AND TRUNC(p_end_date)
            GROUP BY c.category_name
            ORDER BY revenue DESC
        )
        WHERE ROWNUM = 1;

        RETURN v_category_name;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 'NO SALES';
    END get_top_category;

    PROCEDURE generate_sales_report(
        p_start_date  IN DATE,
        p_end_date    IN DATE,
        p_category_id IN NUMBER DEFAULT NULL
    ) IS
        CURSOR c_sales IS
            SELECT c.category_name, COUNT(DISTINCT o.order_id) AS total_orders,
                   SUM(oi.quantity) AS units_sold, SUM(oi.line_total) AS revenue
            FROM customer_order o
            JOIN order_item oi
                ON oi.order_id = o.order_id
            JOIN product p
                ON p.product_id = oi.product_id
            JOIN category c
                ON c.category_id = p.category_id
            WHERE TRUNC(o.order_date) BETWEEN TRUNC(p_start_date) AND TRUNC(p_end_date)
              AND (p_category_id IS NULL OR p.category_id = p_category_id)
            GROUP BY c.category_name
            ORDER BY revenue DESC;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('SALES REPORT FROM ' || TO_CHAR(p_start_date, 'DD-MON-YYYY') ||
                             ' TO ' || TO_CHAR(p_end_date, 'DD-MON-YYYY'));
        FOR rec IN c_sales LOOP
            DBMS_OUTPUT.PUT_LINE(
                RPAD(rec.category_name, 20) || ' | Orders: ' || rec.total_orders ||
                ' | Units: ' || rec.units_sold || ' | Revenue: ' || rec.revenue
            );
        END LOOP;
    END generate_sales_report;

    PROCEDURE update_product_stock(
        p_product_id IN NUMBER,
        p_new_stock  IN NUMBER,
        p_admin_id   IN NUMBER
    ) IS
    BEGIN
        UPDATE product
        SET stock_qty = p_new_stock,
            updated_at = SYSDATE,
            status = CASE
                WHEN p_new_stock = 0 THEN 'OUT_OF_STOCK'
                ELSE 'AVAILABLE'
            END
        WHERE product_id = p_product_id;

        INSERT INTO product_audit (
            audit_id, product_id, action_type, new_stock, note, changed_by
        )
        VALUES (
            seq_product_audit.NEXTVAL, p_product_id, 'UPDATE', p_new_stock,
            'Stock manually adjusted by admin id ' || p_admin_id,
            USER
        );
    END update_product_stock;
END shop_portal_pkg;
/

CREATE OR REPLACE TRIGGER trg_cart_item_calc
BEFORE INSERT OR UPDATE OF quantity, product_id ON cart_item
FOR EACH ROW
DECLARE
    v_unit_price product.unit_price%TYPE;
BEGIN
    SELECT unit_price
    INTO v_unit_price
    FROM product
    WHERE product_id = :NEW.product_id;

    :NEW.unit_price := v_unit_price;
    :NEW.line_total := ROUND(v_unit_price * :NEW.quantity, 2);
END;
/

CREATE OR REPLACE TRIGGER trg_order_item_calc
BEFORE INSERT OR UPDATE OF quantity, product_id ON order_item
FOR EACH ROW
DECLARE
    v_unit_price product.unit_price%TYPE;
BEGIN
    SELECT unit_price
    INTO v_unit_price
    FROM product
    WHERE product_id = :NEW.product_id;

    :NEW.unit_price := v_unit_price;
    :NEW.line_total := ROUND(v_unit_price * :NEW.quantity, 2);
END;
/

CREATE OR REPLACE TRIGGER trg_order_item_stock
AFTER INSERT ON order_item
FOR EACH ROW
BEGIN
    UPDATE product
    SET stock_qty = stock_qty - :NEW.quantity,
        updated_at = SYSDATE,
        status = CASE
            WHEN stock_qty - :NEW.quantity <= 0 THEN 'OUT_OF_STOCK'
            ELSE 'AVAILABLE'
        END
    WHERE product_id = :NEW.product_id;
END;
/

CREATE OR REPLACE TRIGGER trg_product_audit
AFTER INSERT OR UPDATE OR DELETE ON product
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        INSERT INTO product_audit (audit_id, product_id, action_type, new_price, new_stock, note)
        VALUES (seq_product_audit.NEXTVAL, :NEW.product_id, 'INSERT', :NEW.unit_price, :NEW.stock_qty, 'New product added');
    ELSIF UPDATING THEN
        INSERT INTO product_audit (audit_id, product_id, action_type, old_price, new_price, old_stock, new_stock, note)
        VALUES (seq_product_audit.NEXTVAL, :NEW.product_id, 'UPDATE', :OLD.unit_price, :NEW.unit_price, :OLD.stock_qty, :NEW.stock_qty, 'Product details updated');
    ELSIF DELETING THEN
        INSERT INTO product_audit (audit_id, product_id, action_type, old_price, old_stock, note)
        VALUES (seq_product_audit.NEXTVAL, :OLD.product_id, 'DELETE', :OLD.unit_price, :OLD.stock_qty, 'Product removed');
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_admin_product_editor_io
INSTEAD OF INSERT OR UPDATE ON vw_admin_product_editor
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        INSERT INTO product (
            product_id, category_id, sku, product_name, description,
            unit_price, stock_qty, reorder_level, status, created_at, updated_at
        )
        VALUES (
            seq_product.NEXTVAL, :NEW.category_id, :NEW.sku, :NEW.product_name, :NEW.description,
            :NEW.unit_price, :NEW.stock_qty, :NEW.reorder_level, :NEW.status, SYSDATE, SYSDATE
        );
    ELSIF UPDATING THEN
        UPDATE product
        SET category_id = :NEW.category_id,
            sku = :NEW.sku,
            product_name = :NEW.product_name,
            description = :NEW.description,
            unit_price = :NEW.unit_price,
            stock_qty = :NEW.stock_qty,
            reorder_level = :NEW.reorder_level,
            status = :NEW.status,
            updated_at = SYSDATE
        WHERE product_id = :OLD.product_id;
    END IF;
END;
/
