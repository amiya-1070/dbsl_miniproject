SET SERVEROUTPUT ON

PROMPT Creating sequences...

CREATE SEQUENCE seq_app_user START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_category START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_product START WITH 1001 INCREMENT BY 1;
CREATE SEQUENCE seq_cart START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_cart_item START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_order START WITH 5001 INCREMENT BY 1;
CREATE SEQUENCE seq_order_item START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_payment START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_invoice START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_feedback START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_product_audit START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_login_audit START WITH 1 INCREMENT BY 1;

PROMPT Creating tables...

CREATE TABLE app_user (
    user_id          NUMBER PRIMARY KEY,
    username         VARCHAR2(40) CONSTRAINT uq_app_user_username UNIQUE NOT NULL,
    password_hash    VARCHAR2(64) NOT NULL,
    role             VARCHAR2(10) NOT NULL CHECK (role IN ('ADMIN', 'CUSTOMER')),
    full_name        VARCHAR2(100) NOT NULL,
    email            VARCHAR2(120) CONSTRAINT uq_app_user_email UNIQUE NOT NULL,
    phone            VARCHAR2(15),
    status           VARCHAR2(10) DEFAULT 'ACTIVE' NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED')),
    created_at       DATE DEFAULT SYSDATE NOT NULL,
    last_login       DATE,
    CONSTRAINT chk_app_user_email CHECK (REGEXP_LIKE(email, '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'))
);

CREATE TABLE admin_profile (
    admin_id         NUMBER PRIMARY KEY,
    designation      VARCHAR2(60) NOT NULL,
    office_location  VARCHAR2(80),
    CONSTRAINT fk_admin_profile_user FOREIGN KEY (admin_id) REFERENCES app_user(user_id) ON DELETE CASCADE
);

CREATE TABLE customer_profile (
    customer_id      NUMBER PRIMARY KEY,
    default_address  VARCHAR2(200) NOT NULL,
    city             VARCHAR2(60) NOT NULL,
    pincode          VARCHAR2(6) NOT NULL,
    loyalty_points   NUMBER DEFAULT 0 NOT NULL CHECK (loyalty_points >= 0),
    CONSTRAINT fk_customer_profile_user FOREIGN KEY (customer_id) REFERENCES app_user(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_customer_pincode CHECK (REGEXP_LIKE(pincode, '^[0-9]{6}$'))
);

CREATE TABLE category (
    category_id      NUMBER PRIMARY KEY,
    category_name    VARCHAR2(60) CONSTRAINT uq_category_name UNIQUE NOT NULL,
    description      VARCHAR2(250),
    display_order    NUMBER DEFAULT 1 NOT NULL CHECK (display_order > 0)
);

CREATE TABLE product (
    product_id       NUMBER PRIMARY KEY,
    category_id      NUMBER NOT NULL,
    sku              VARCHAR2(30) CONSTRAINT uq_product_sku UNIQUE NOT NULL,
    product_name     VARCHAR2(120) NOT NULL,
    description      CLOB,
    unit_price       NUMBER(10,2) NOT NULL CHECK (unit_price > 0),
    stock_qty        NUMBER DEFAULT 0 NOT NULL CHECK (stock_qty >= 0),
    reorder_level    NUMBER DEFAULT 5 NOT NULL CHECK (reorder_level >= 0),
    status           VARCHAR2(15) DEFAULT 'AVAILABLE' NOT NULL CHECK (status IN ('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED')),
    created_at       DATE DEFAULT SYSDATE NOT NULL,
    updated_at       DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE shopping_cart (
    cart_id          NUMBER PRIMARY KEY,
    customer_id      NUMBER CONSTRAINT uq_cart_customer UNIQUE NOT NULL,
    created_at       DATE DEFAULT SYSDATE NOT NULL,
    updated_at       DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customer_profile(customer_id) ON DELETE CASCADE
);

CREATE TABLE cart_item (
    cart_item_id     NUMBER PRIMARY KEY,
    cart_id          NUMBER NOT NULL,
    product_id       NUMBER NOT NULL,
    quantity         NUMBER NOT NULL CHECK (quantity > 0),
    unit_price       NUMBER(10,2) NOT NULL,
    line_total       NUMBER(12,2) NOT NULL,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES shopping_cart(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES product(product_id),
    CONSTRAINT uq_cart_item UNIQUE (cart_id, product_id)
);

CREATE TABLE customer_order (
    order_id          NUMBER PRIMARY KEY,
    order_number      VARCHAR2(20) CONSTRAINT uq_customer_order_number UNIQUE NOT NULL,
    customer_id       NUMBER NOT NULL,
    order_date        DATE DEFAULT SYSDATE NOT NULL,
    status            VARCHAR2(15) DEFAULT 'PLACED' NOT NULL CHECK (status IN ('PLACED', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    shipping_address  VARCHAR2(200) NOT NULL,
    gross_amount      NUMBER(12,2) DEFAULT 0 NOT NULL,
    discount_amount   NUMBER(12,2) DEFAULT 0 NOT NULL CHECK (discount_amount >= 0),
    tax_amount        NUMBER(12,2) DEFAULT 0 NOT NULL CHECK (tax_amount >= 0),
    total_amount      NUMBER(12,2) DEFAULT 0 NOT NULL CHECK (total_amount >= 0),
    payment_status    VARCHAR2(15) DEFAULT 'PENDING' NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    created_by        NUMBER,
    CONSTRAINT fk_customer_order_customer FOREIGN KEY (customer_id) REFERENCES customer_profile(customer_id),
    CONSTRAINT fk_customer_order_creator FOREIGN KEY (created_by) REFERENCES app_user(user_id)
);

CREATE TABLE order_item (
    order_item_id    NUMBER PRIMARY KEY,
    order_id         NUMBER NOT NULL,
    product_id       NUMBER NOT NULL,
    quantity         NUMBER NOT NULL CHECK (quantity > 0),
    unit_price       NUMBER(10,2) NOT NULL,
    line_total       NUMBER(12,2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES customer_order(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE payment (
    payment_id         NUMBER PRIMARY KEY,
    order_id           NUMBER CONSTRAINT uq_payment_order UNIQUE NOT NULL,
    payment_method     VARCHAR2(20) NOT NULL CHECK (payment_method IN ('UPI', 'CARD', 'COD', 'NETBANKING')),
    payment_reference  VARCHAR2(50),
    payment_date       DATE DEFAULT SYSDATE NOT NULL,
    amount             NUMBER(12,2) NOT NULL CHECK (amount > 0),
    payment_status     VARCHAR2(15) DEFAULT 'PAID' NOT NULL CHECK (payment_status IN ('PAID', 'FAILED', 'REFUNDED')),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES customer_order(order_id) ON DELETE CASCADE
);

CREATE TABLE invoice (
    invoice_id        NUMBER PRIMARY KEY,
    order_id          NUMBER CONSTRAINT uq_invoice_order UNIQUE NOT NULL,
    invoice_number    VARCHAR2(25) CONSTRAINT uq_invoice_number UNIQUE NOT NULL,
    invoice_date      DATE DEFAULT SYSDATE NOT NULL,
    subtotal          NUMBER(12,2) NOT NULL,
    tax_amount        NUMBER(12,2) NOT NULL,
    total_amount      NUMBER(12,2) NOT NULL,
    CONSTRAINT fk_invoice_order FOREIGN KEY (order_id) REFERENCES customer_order(order_id) ON DELETE CASCADE
);

CREATE TABLE feedback (
    feedback_id       NUMBER PRIMARY KEY,
    customer_id       NUMBER NOT NULL,
    product_id        NUMBER NOT NULL,
    rating            NUMBER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments          VARCHAR2(500),
    created_at        DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_feedback_customer FOREIGN KEY (customer_id) REFERENCES customer_profile(customer_id),
    CONSTRAINT fk_feedback_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE product_audit (
    audit_id          NUMBER PRIMARY KEY,
    product_id        NUMBER,
    action_type       VARCHAR2(10) NOT NULL,
    old_price         NUMBER(10,2),
    new_price         NUMBER(10,2),
    old_stock         NUMBER,
    new_stock         NUMBER,
    changed_by        VARCHAR2(100) DEFAULT USER NOT NULL,
    changed_at        DATE DEFAULT SYSDATE NOT NULL,
    note              VARCHAR2(250)
);

CREATE TABLE login_audit (
    audit_id          NUMBER PRIMARY KEY,
    username          VARCHAR2(40) NOT NULL,
    role_requested    VARCHAR2(10) NOT NULL,
    success_flag      CHAR(1) NOT NULL CHECK (success_flag IN ('Y', 'N')),
    audit_message     VARCHAR2(200),
    login_time        DATE DEFAULT SYSDATE NOT NULL
);

PROMPT Creating indexes...

CREATE INDEX idx_product_category_name ON product (category_id, product_name);
CREATE INDEX idx_order_customer_date ON customer_order (customer_id, order_date);
CREATE INDEX idx_order_item_product ON order_item (product_id);
CREATE INDEX idx_payment_status ON payment (payment_status, payment_date);
