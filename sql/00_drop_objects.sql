SET SERVEROUTPUT ON

BEGIN
    FOR rec IN (
        SELECT object_name, object_type
        FROM user_objects
        WHERE object_name IN (
            'SHOP_PORTAL_PKG',
            'TRG_CART_ITEM_CALC',
            'TRG_ORDER_ITEM_CALC',
            'TRG_ORDER_ITEM_STOCK',
            'TRG_PRODUCT_AUDIT',
            'TRG_ADMIN_PRODUCT_EDITOR_IO',
            'VW_PRODUCT_CATALOG',
            'VW_CUSTOMER_ORDER_HISTORY',
            'VW_SALES_SUMMARY',
            'VW_ADMIN_PRODUCT_EDITOR',
            'LOGIN_AUDIT',
            'PRODUCT_AUDIT',
            'FEEDBACK',
            'INVOICE',
            'PAYMENT',
            'ORDER_ITEM',
            'CUSTOMER_ORDER',
            'CART_ITEM',
            'SHOPPING_CART',
            'PRODUCT',
            'CATEGORY',
            'CUSTOMER_PROFILE',
            'ADMIN_PROFILE',
            'APP_USER'
        )
        ORDER BY CASE object_type
            WHEN 'TRIGGER' THEN 1
            WHEN 'VIEW' THEN 2
            WHEN 'PACKAGE BODY' THEN 3
            WHEN 'PACKAGE' THEN 4
            ELSE 5
        END
    ) LOOP
        BEGIN
            IF rec.object_type = 'PACKAGE BODY' THEN
                NULL;
            ELSIF rec.object_type = 'PACKAGE' THEN
                EXECUTE IMMEDIATE 'DROP PACKAGE ' || rec.object_name;
            ELSE
                EXECUTE IMMEDIATE 'DROP ' || rec.object_type || ' ' || rec.object_name;
            END IF;
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('Skipped drop for ' || rec.object_type || ' ' || rec.object_name || ': ' || SQLERRM);
        END;
    END LOOP;
END;
/

BEGIN
    FOR rec IN (
        SELECT sequence_name
        FROM user_sequences
        WHERE sequence_name IN (
            'SEQ_APP_USER',
            'SEQ_CATEGORY',
            'SEQ_PRODUCT',
            'SEQ_CART',
            'SEQ_CART_ITEM',
            'SEQ_ORDER',
            'SEQ_ORDER_ITEM',
            'SEQ_PAYMENT',
            'SEQ_INVOICE',
            'SEQ_FEEDBACK',
            'SEQ_PRODUCT_AUDIT',
            'SEQ_LOGIN_AUDIT'
        )
    ) LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || rec.sequence_name;
    END LOOP;
END;
/
