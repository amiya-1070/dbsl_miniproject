SET SERVEROUTPUT ON

PROMPT Demonstrating package calls and PL/SQL blocks...

DECLARE
    v_user_id    NUMBER;
    v_full_name  VARCHAR2(100);
BEGIN
    shop_portal_pkg.authenticate_user('cust01', 'Cust@123', 'CUSTOMER', v_user_id, v_full_name);
    DBMS_OUTPUT.PUT_LINE('Authenticated customer: ' || v_full_name || ' (ID=' || v_user_id || ')');
END;
/

BEGIN
    shop_portal_pkg.add_to_cart(2, 1001, 1);
    shop_portal_pkg.add_to_cart(2, 1002, 2);
    DBMS_OUTPUT.PUT_LINE('Current cart total for customer 2 = ' || shop_portal_pkg.get_cart_total(2));
END;
/

DECLARE
    v_order_id    NUMBER;
    v_invoice_id  NUMBER;
    v_total       NUMBER;
BEGIN
    shop_portal_pkg.place_order(
        p_customer_id    => 2,
        p_payment_method => 'UPI',
        p_shipping_addr  => '12, Blue Pearl Residency, Manipal',
        p_order_id       => v_order_id,
        p_invoice_id     => v_invoice_id,
        p_total_amount   => v_total
    );

    COMMIT;

    DBMS_OUTPUT.PUT_LINE('Placed order id: ' || v_order_id);
    DBMS_OUTPUT.PUT_LINE('Generated invoice id: ' || v_invoice_id);
    DBMS_OUTPUT.PUT_LINE('Order total using function: ' || shop_portal_pkg.get_order_total(v_order_id));
END;
/

BEGIN
    shop_portal_pkg.generate_sales_report(SYSDATE - 7, SYSDATE, NULL);
    DBMS_OUTPUT.PUT_LINE('Top category: ' || shop_portal_pkg.get_top_category(SYSDATE - 7, SYSDATE));
END;
/

DECLARE
    CURSOR c_low_stock IS
        SELECT product_name, stock_qty, reorder_level
        FROM product
        WHERE stock_qty <= reorder_level
        ORDER BY stock_qty;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Low stock products');
    FOR rec IN c_low_stock LOOP
        DBMS_OUTPUT.PUT_LINE(rec.product_name || ' -> stock=' || rec.stock_qty || ', reorder=' || rec.reorder_level);
    END LOOP;
END;
/
