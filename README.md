# Online Shopping Portal in Oracle SQL and Java

This project implements your DBSL mini project as an Oracle database-backed shopping portal with:

- separate `ADMIN` and `CUSTOMER` logins
- a normalized schema with integrity constraints
- basic and complex SQL queries
- PL/SQL package, procedures, functions, cursors, anonymous blocks, exception handling, and triggers
- a Java Swing client with a light blue and black interface

## Folder Layout

- `sql/99_run_all.sql`: run this to build the entire database project
- `sql/01_schema.sql`: tables, constraints, indexes, sequences
- `sql/02_seed_data.sql`: sample data and login accounts
- `sql/03_views_and_queries.sql`: views and sample SQL queries
- `sql/04_packages_and_triggers.sql`: PL/SQL package and triggers
- `sql/05_demo_blocks.sql`: anonymous blocks and cursor demos
- `src/`: Java Swing UI source code

## Demo Logins

- Admin:
  - username: `admin01`
  - password: `Admin@123`
- Customer:
  - username: `cust01`
  - password: `Cust@123`
- Second customer:
  - username: `cust02`
  - password: `Cust@123`

## Run the Oracle SQL Project

1. Open Oracle SQL Developer.
2. Connect to your Oracle schema.
3. Open [`99_run_all.sql`](/Users/saanvi/Desktop/dbsl_miniproject/online_shopping_oracle/sql/99_run_all.sql).
4. Enable `DBMS Output` for your connection.
5. Run the script.
6. Verify created objects under Tables, Views, Sequences, Triggers, and Packages.

## Run the Java UI with Maven (macOS)

1. Edit [`OracleConnectionManager.java`](/Users/saanvi/Desktop/dbsl_miniproject/online_shopping_oracle/src/com/dbsl/shop/db/OracleConnectionManager.java) and set:
   - `DB_URL`
   - `DB_USER`
   - `DB_PASSWORD`
2. From Terminal:

```bash
cd /Users/saanvi/Desktop/dbsl_miniproject/online_shopping_oracle
mvn clean compile
mvn exec:java
```

This project uses Maven to download `ojdbc11` automatically from Maven Central, so no manual JAR setup is required.

Optional useful commands:

```bash
# Run without tests (there are no tests currently)
mvn clean package -DskipTests

# Run the app directly in one command
mvn -DskipTests compile exec:java
```

## UI Flow

- `LoginFrame`: separate customer login page and admin login page
- `CustomerDashboardFrame`: browse products, search catalog, add to cart, place orders, view order history
- `AdminDashboardFrame`: manage products, inspect users, see sales report

## Notes

- Passwords are stored as `STANDARD_HASH(..., 'SHA256')` values in Oracle, so the UI never compares plain text directly in Java.
- The project is intentionally academic and self-contained. In a production system you would add better secret management, richer authorization, and stronger error logging.
