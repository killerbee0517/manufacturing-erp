ALTER TABLE process_templates
  ADD COLUMN company_id BIGINT;

UPDATE process_templates
SET company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE company_id IS NULL;

ALTER TABLE process_templates
  MODIFY company_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_process_templates_company FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE production_orders
  ADD COLUMN company_id BIGINT;

UPDATE production_orders po
JOIN process_templates pt ON pt.id = po.template_id
SET po.company_id = pt.company_id
WHERE po.company_id IS NULL;

UPDATE production_orders
SET company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE company_id IS NULL;

ALTER TABLE production_orders
  MODIFY company_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_production_orders_company FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE production_batches
  ADD COLUMN company_id BIGINT;

UPDATE production_batches pb
JOIN process_templates pt ON pt.id = pb.template_id
SET pb.company_id = pt.company_id
WHERE pb.company_id IS NULL;

UPDATE production_batches
SET company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE company_id IS NULL;

ALTER TABLE production_batches
  MODIFY company_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_production_batches_company FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE process_runs
  ADD COLUMN company_id BIGINT,
  ADD COLUMN moisture_percent DECIMAL(10,4);

UPDATE process_runs pr
JOIN production_batches pb ON pb.id = pr.production_batch_id
SET pr.company_id = pb.company_id
WHERE pr.company_id IS NULL;

UPDATE process_runs
SET company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE company_id IS NULL;

ALTER TABLE process_runs
  MODIFY company_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_process_runs_company FOREIGN KEY (company_id) REFERENCES companies(id);

CREATE TABLE process_template_outputs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  uom_id BIGINT NOT NULL,
  output_type VARCHAR(20) NOT NULL,
  default_ratio DECIMAL(14,4) NOT NULL,
  notes VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT fk_pto_template FOREIGN KEY (template_id) REFERENCES process_templates(id),
  CONSTRAINT fk_pto_item FOREIGN KEY (item_id) REFERENCES items(id),
  CONSTRAINT fk_pto_uom FOREIGN KEY (uom_id) REFERENCES uoms(id)
);
