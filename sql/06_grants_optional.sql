PROMPT Optional DCL examples. Run these only if you have DBA privileges or dedicated database users.
PROMPT Example:
PROMPT CREATE USER shop_admin IDENTIFIED BY shop_admin;
PROMPT CREATE USER shop_customer IDENTIFIED BY shop_customer;
PROMPT GRANT CREATE SESSION TO shop_admin, shop_customer;
PROMPT GRANT SELECT, INSERT, UPDATE, DELETE ON product TO shop_admin;
PROMPT GRANT EXECUTE ON shop_portal_pkg TO shop_admin, shop_customer;
