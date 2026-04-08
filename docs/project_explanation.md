# Project Explanation

## Why this database design

The synopsis describes an online shopping portal with authentication, product browsing, cart management, ordering, invoicing, stock updates, and admin reporting. The schema is therefore split into:

- `app_user`, `admin_profile`, `customer_profile` for role-based login and contact data
- `category`, `product` for catalog management
- `shopping_cart`, `cart_item` for pre-order shopping activity
- `customer_order`, `order_item`, `payment`, `invoice` for checkout, billing, and order history
- `feedback`, `product_audit`, `login_audit` for user feedback, auditing, and traceability

This keeps the design normalized and avoids storing repeated order, product, and user details in a single wide table.

## Why these Oracle SQL features were included

- DDL: tables, views, indexes, sequences
- DML: inserts, updates, deletes, merge-like cart logic, transactional order placement
- Constraints: primary key, foreign key, unique, check, not null
- TCL: `COMMIT`, `ROLLBACK`, `SAVEPOINT`
- DCL: included as an optional file because many student schemas cannot create users and roles
- Complex SQL: joins, `GROUP BY`, `HAVING`, `WITH`, nested subqueries, `UNION`, `INTERSECT`, `MINUS`
- PL/SQL anonymous blocks: in `05_demo_blocks.sql`
- Procedures and functions: inside `shop_portal_pkg`
- Package: groups shopping actions into one reusable module
- Cursors: explicit cursor in `generate_sales_report` and demo low-stock cursor
- Exception handling: invalid login, blocked users, empty cart, insufficient stock
- Triggers:
  - auto-calculate cart and order line totals
  - auto-reduce stock after checkout
  - audit product insert/update/delete
  - `INSTEAD OF` trigger to edit a join view

## Why the UI is split into customer and admin pages

Your abstract explicitly says the application supports both customers and administrators. Splitting the login into two dedicated pages makes the role separation obvious during demo and matches the requirement that only authorized users can access specific functionality.

## Why the theme uses light blue and black

The theme is implemented in a reusable `Theme` class. Light blue is used for panels, headers, selection states, and buttons, while black and near-black shades are used for text and frame contrast. That gives the application a clean academic-demo look without feeling plain.
