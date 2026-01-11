ALTER TABLE sales_orders ADD COLUMN company_id BIGINT;
ALTER TABLE sales_invoices ADD COLUMN company_id BIGINT;
ALTER TABLE deliveries ADD COLUMN company_id BIGINT;
ALTER TABLE stock_ledger ADD COLUMN company_id BIGINT;
ALTER TABLE stock_transfer_header ADD COLUMN company_id BIGINT;
ALTER TABLE inventory_movements ADD COLUMN company_id BIGINT;

UPDATE sales_orders
SET company_id = (SELECT id FROM companies LIMIT 1)
WHERE company_id IS NULL;

UPDATE sales_invoices
SET company_id = (SELECT id FROM companies LIMIT 1)
WHERE company_id IS NULL;

UPDATE stock_ledger
SET company_id = (SELECT id FROM companies LIMIT 1)
WHERE company_id IS NULL;

UPDATE stock_transfer_header
SET company_id = (SELECT id FROM companies LIMIT 1)
WHERE company_id IS NULL;

UPDATE inventory_movements
SET company_id = (SELECT id FROM companies LIMIT 1)
WHERE company_id IS NULL;

UPDATE deliveries d
JOIN sales_orders so ON d.sales_order_id = so.id
SET d.company_id = so.company_id
WHERE d.company_id IS NULL;

UPDATE deliveries
SET company_id = (SELECT id FROM companies LIMIT 1)
WHERE company_id IS NULL;

ALTER TABLE sales_orders MODIFY company_id BIGINT NOT NULL;
ALTER TABLE sales_invoices MODIFY company_id BIGINT NOT NULL;
ALTER TABLE deliveries MODIFY company_id BIGINT NOT NULL;
ALTER TABLE stock_ledger MODIFY company_id BIGINT NOT NULL;
ALTER TABLE stock_transfer_header MODIFY company_id BIGINT NOT NULL;
ALTER TABLE inventory_movements MODIFY company_id BIGINT NOT NULL;

ALTER TABLE sales_orders
  ADD CONSTRAINT fk_sales_orders_company FOREIGN KEY (company_id) REFERENCES companies(id);
ALTER TABLE sales_invoices
  ADD CONSTRAINT fk_sales_invoices_company FOREIGN KEY (company_id) REFERENCES companies(id);
ALTER TABLE deliveries
  ADD CONSTRAINT fk_deliveries_company FOREIGN KEY (company_id) REFERENCES companies(id);
ALTER TABLE stock_ledger
  ADD CONSTRAINT fk_stock_ledger_company FOREIGN KEY (company_id) REFERENCES companies(id);
ALTER TABLE stock_transfer_header
  ADD CONSTRAINT fk_stock_transfer_header_company FOREIGN KEY (company_id) REFERENCES companies(id);
ALTER TABLE inventory_movements
  ADD CONSTRAINT fk_inventory_movements_company FOREIGN KEY (company_id) REFERENCES companies(id);
