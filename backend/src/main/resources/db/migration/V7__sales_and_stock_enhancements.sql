-- Extend sales orders with order date and narration
ALTER TABLE sales_orders ADD COLUMN order_date DATE;
ALTER TABLE sales_orders ADD COLUMN narration VARCHAR(255);
ALTER TABLE sales_orders ALTER COLUMN status SET DEFAULT 'DRAFT';

-- Enhance sales order lines with UOM and rate
ALTER TABLE so_lines ADD COLUMN uom_id BIGINT;
ALTER TABLE so_lines ADD CONSTRAINT fk_so_lines_uom FOREIGN KEY (uom_id) REFERENCES uoms(id);

-- Enhance sales invoice lines for stock postings
ALTER TABLE sales_invoice_lines ADD COLUMN uom_id BIGINT;
ALTER TABLE sales_invoice_lines ADD COLUMN godown_id BIGINT;
ALTER TABLE sales_invoice_lines ADD COLUMN rate DECIMAL(14,2) DEFAULT 0;
ALTER TABLE sales_invoice_lines ADD CONSTRAINT fk_sales_invoice_lines_uom FOREIGN KEY (uom_id) REFERENCES uoms(id);
ALTER TABLE sales_invoice_lines ADD CONSTRAINT fk_sales_invoice_lines_godown FOREIGN KEY (godown_id) REFERENCES godowns(id);

-- Stock transfer header and lines
CREATE TABLE stock_transfer_header (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  transfer_no VARCHAR(50) NOT NULL,
  from_godown_id BIGINT NOT NULL,
  to_godown_id BIGINT NOT NULL,
  transfer_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  narration VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (from_godown_id) REFERENCES godowns(id),
  FOREIGN KEY (to_godown_id) REFERENCES godowns(id)
);

CREATE TABLE stock_transfer_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  header_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  uom_id BIGINT NOT NULL,
  qty DECIMAL(14,3) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (header_id) REFERENCES stock_transfer_header(id),
  FOREIGN KEY (item_id) REFERENCES items(id),
  FOREIGN KEY (uom_id) REFERENCES uoms(id)
);

CREATE INDEX idx_stock_transfer_lines_header ON stock_transfer_lines(header_id);
CREATE INDEX idx_stock_transfer_lines_item ON stock_transfer_lines(item_id);

-- Strengthen stock ledger as single source of truth
ALTER TABLE stock_ledger ADD COLUMN qty_in DECIMAL(14,3) NOT NULL DEFAULT 0;
ALTER TABLE stock_ledger ADD COLUMN qty_out DECIMAL(14,3) NOT NULL DEFAULT 0;
ALTER TABLE stock_ledger ADD COLUMN rate DECIMAL(14,2) DEFAULT 0;
ALTER TABLE stock_ledger ADD COLUMN amount DECIMAL(14,2) DEFAULT 0;
ALTER TABLE stock_ledger ADD COLUMN godown_id BIGINT;
ALTER TABLE stock_ledger ADD COLUMN batch_id BIGINT;

CREATE INDEX idx_stock_ledger_doc ON stock_ledger(doc_type, doc_id);
CREATE INDEX idx_stock_ledger_item_godown ON stock_ledger(item_id, godown_id);
CREATE INDEX idx_stock_ledger_posted_at ON stock_ledger(posted_at);

-- Backfill quantities into new columns for existing data
UPDATE stock_ledger SET qty_in = quantity WHERE txn_type = 'IN' AND (qty_in = 0 OR qty_in IS NULL);
UPDATE stock_ledger SET qty_out = quantity WHERE txn_type = 'OUT' AND (qty_out = 0 OR qty_out IS NULL);
UPDATE stock_ledger SET qty_out = quantity WHERE txn_type = 'MOVE' AND from_godown_id IS NOT NULL AND (qty_out = 0 OR qty_out IS NULL);
UPDATE stock_ledger SET qty_in = quantity WHERE txn_type = 'MOVE' AND to_godown_id IS NOT NULL AND (qty_in = 0 OR qty_in IS NULL);
