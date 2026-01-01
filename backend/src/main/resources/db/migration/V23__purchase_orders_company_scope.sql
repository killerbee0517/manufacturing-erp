ALTER TABLE purchase_orders
  ADD COLUMN company_id BIGINT;

UPDATE purchase_orders po
LEFT JOIN rfq r ON po.rfq_id = r.id
SET po.company_id = r.company_id
WHERE po.company_id IS NULL AND r.company_id IS NOT NULL;

UPDATE purchase_orders
SET company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE company_id IS NULL AND EXISTS (SELECT 1 FROM companies WHERE code = 'PARENT');

ALTER TABLE purchase_orders
  ADD CONSTRAINT fk_purchase_orders_company FOREIGN KEY (company_id) REFERENCES companies(id);

CREATE INDEX idx_purchase_orders_company_id ON purchase_orders (company_id);
