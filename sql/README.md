# FlashMart SQL Scripts

Run these scripts in order when preparing a local MySQL database:

```powershell
mysql -u root -p < sql/01_schema.sql
mysql -u root -p flashmart < sql/02_demo_data.sql
```

`01_schema.sql` creates the database and all tables used by the current monolith.

`02_demo_data.sql` inserts basic users, products, details, images, reviews, and a sample cart.

If you already created the database before order payment deadlines were added, run this migration once:

```powershell
mysql -u root -p flashmart < sql/03_order_payment_deadlines.sql
```

`03_order_payment_deadlines.sql` adds the order payment deadline and backend cleanup deadline columns for timeout cancellation.
