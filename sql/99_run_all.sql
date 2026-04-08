SET SERVEROUTPUT ON
SET DEFINE OFF

PROMPT ===== ONLINE SHOPPING PORTAL PROJECT START =====

@/tmp/sql/00_drop_objects.sql
@/tmp/sql/01_schema.sql
@/tmp/sql/02_seed_data.sql
@/tmp/sql/03_views_and_queries.sql
@/tmp/sql/04_packages_and_triggers.sql
@/tmp/sql/05_demo_blocks.sql

PROMPT ===== PROJECT SETUP COMPLETE =====
