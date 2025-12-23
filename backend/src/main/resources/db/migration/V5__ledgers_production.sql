CREATE TABLE ledgers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  type VARCHAR(30) NOT NULL,
  reference_type VARCHAR(30),
  reference_id BIGINT,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE vouchers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  voucher_no VARCHAR(50) NOT NULL,
  voucher_date DATE NOT NULL,
  narration VARCHAR(255),
  reference_type VARCHAR(50),
  reference_id BIGINT,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE voucher_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  voucher_id BIGINT NOT NULL,
  ledger_id BIGINT NOT NULL,
  dr_amount DECIMAL(14,2) NOT NULL,
  cr_amount DECIMAL(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
  FOREIGN KEY (ledger_id) REFERENCES ledgers(id)
);

ALTER TABLE suppliers ADD COLUMN ledger_id BIGINT;
ALTER TABLE suppliers ADD CONSTRAINT fk_suppliers_ledger FOREIGN KEY (ledger_id) REFERENCES ledgers(id);

ALTER TABLE customers ADD COLUMN ledger_id BIGINT;
ALTER TABLE customers ADD CONSTRAINT fk_customers_ledger FOREIGN KEY (ledger_id) REFERENCES ledgers(id);

ALTER TABLE stock_ledger ADD COLUMN from_godown_id BIGINT;
ALTER TABLE stock_ledger ADD COLUMN to_godown_id BIGINT;

CREATE TABLE process_templates (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE process_steps (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(255),
  sequence_no INT NOT NULL,
  source_godown_id BIGINT,
  dest_godown_id BIGINT,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (template_id) REFERENCES process_templates(id),
  FOREIGN KEY (source_godown_id) REFERENCES godowns(id),
  FOREIGN KEY (dest_godown_id) REFERENCES godowns(id)
);

CREATE TABLE production_orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(50) NOT NULL,
  template_id BIGINT,
  item_id BIGINT,
  planned_qty DECIMAL(14,3) NOT NULL,
  order_date DATE,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (template_id) REFERENCES process_templates(id),
  FOREIGN KEY (item_id) REFERENCES items(id)
);

CREATE TABLE production_batches (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  batch_no VARCHAR(50) NOT NULL,
  production_order_id BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL,
  started_at TIMESTAMP,
  completed_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (production_order_id) REFERENCES production_orders(id)
);

CREATE TABLE process_runs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  production_batch_id BIGINT NOT NULL,
  process_step_id BIGINT NOT NULL,
  run_date DATE NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (production_batch_id) REFERENCES production_batches(id),
  FOREIGN KEY (process_step_id) REFERENCES process_steps(id)
);

CREATE TABLE process_run_consumptions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  process_run_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  uom_id BIGINT NOT NULL,
  godown_id BIGINT,
  quantity DECIMAL(14,3) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (process_run_id) REFERENCES process_runs(id),
  FOREIGN KEY (item_id) REFERENCES items(id),
  FOREIGN KEY (uom_id) REFERENCES uoms(id),
  FOREIGN KEY (godown_id) REFERENCES godowns(id)
);

CREATE TABLE process_run_outputs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  process_run_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  uom_id BIGINT NOT NULL,
  godown_id BIGINT,
  quantity DECIMAL(14,3) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (process_run_id) REFERENCES process_runs(id),
  FOREIGN KEY (item_id) REFERENCES items(id),
  FOREIGN KEY (uom_id) REFERENCES uoms(id),
  FOREIGN KEY (godown_id) REFERENCES godowns(id)
);
