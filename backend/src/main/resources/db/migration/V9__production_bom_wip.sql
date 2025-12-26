CREATE TABLE bom_header (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  finished_item_id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  version VARCHAR(50),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (finished_item_id) REFERENCES items(id)
);

CREATE TABLE bom_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  bom_id BIGINT NOT NULL,
  component_item_id BIGINT NOT NULL,
  uom_id BIGINT NOT NULL,
  qty_per_unit DECIMAL(14,3) NOT NULL,
  scrap_percent DECIMAL(5,2),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (bom_id) REFERENCES bom_header(id),
  FOREIGN KEY (component_item_id) REFERENCES items(id),
  FOREIGN KEY (uom_id) REFERENCES uoms(id)
);

ALTER TABLE production_orders
  ADD COLUMN bom_id BIGINT,
  ADD COLUMN finished_item_id BIGINT,
  ADD COLUMN uom_id BIGINT,
  MODIFY COLUMN order_no VARCHAR(50) NOT NULL,
  MODIFY COLUMN item_id BIGINT NULL;

ALTER TABLE production_orders
  ADD CONSTRAINT fk_po_bom FOREIGN KEY (bom_id) REFERENCES bom_header(id),
  ADD CONSTRAINT fk_po_finished_item FOREIGN KEY (finished_item_id) REFERENCES items(id),
  ADD CONSTRAINT fk_po_uom FOREIGN KEY (uom_id) REFERENCES uoms(id);

UPDATE production_orders SET finished_item_id = item_id WHERE finished_item_id IS NULL;

ALTER TABLE process_runs
  MODIFY COLUMN process_step_id BIGINT NULL,
  ADD COLUMN step_name VARCHAR(150),
  ADD COLUMN started_at TIMESTAMP NULL,
  ADD COLUMN ended_at TIMESTAMP NULL;

ALTER TABLE process_run_consumptions
  ADD COLUMN source_godown_id BIGINT NULL,
  ADD COLUMN source_run_output_id BIGINT NULL,
  ADD COLUMN source_type ENUM('GODOWN','WIP') NOT NULL DEFAULT 'GODOWN',
  ADD COLUMN rate DECIMAL(14,4) NULL,
  ADD COLUMN amount DECIMAL(14,2) NULL,
  ADD CONSTRAINT fk_prc_source_run_output FOREIGN KEY (source_run_output_id) REFERENCES process_run_outputs(id);

UPDATE process_run_consumptions SET source_godown_id = godown_id WHERE source_godown_id IS NULL;

ALTER TABLE process_run_outputs
  ADD COLUMN dest_godown_id BIGINT NULL,
  ADD COLUMN output_type ENUM('WIP','FG','BYPRODUCT') NOT NULL DEFAULT 'WIP',
  ADD COLUMN rate DECIMAL(14,4) NULL,
  ADD COLUMN amount DECIMAL(14,2) NULL,
  ADD COLUMN consumed_qty DECIMAL(14,3) NOT NULL DEFAULT 0;

UPDATE process_run_outputs SET dest_godown_id = godown_id WHERE dest_godown_id IS NULL;

ALTER TABLE production_batches
  ADD COLUMN start_date DATE NULL,
  ADD COLUMN end_date DATE NULL;
