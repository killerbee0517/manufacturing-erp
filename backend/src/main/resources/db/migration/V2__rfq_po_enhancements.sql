ALTER TABLE rfq
  ADD COLUMN supplier_id BIGINT NULL,
  ADD COLUMN rfq_date DATE NULL,
  ADD COLUMN remarks VARCHAR(255) NULL;

ALTER TABLE rfq_lines
  ADD COLUMN uom_id BIGINT NULL,
  ADD COLUMN rate_expected DECIMAL(14,2) NULL,
  ADD COLUMN remarks VARCHAR(255) NULL;

ALTER TABLE rfq_vendor_quotes
  ADD COLUMN quoted_rate DECIMAL(14,2) NULL,
  ADD COLUMN quoted_qty DECIMAL(14,3) NULL,
  ADD COLUMN remarks VARCHAR(255) NULL,
  ADD COLUMN status VARCHAR(20) NULL;

ALTER TABLE purchase_orders
  ADD COLUMN po_date DATE NULL,
  ADD COLUMN remarks VARCHAR(255) NULL,
  ADD COLUMN total_amount DECIMAL(14,2) NULL;

ALTER TABLE po_lines
  ADD COLUMN uom_id BIGINT NULL,
  ADD COLUMN amount DECIMAL(14,2) NULL,
  ADD COLUMN remarks VARCHAR(255) NULL;

CREATE INDEX idx_rfq_supplier_id ON rfq (supplier_id);
CREATE INDEX idx_rfq_lines_rfq_id ON rfq_lines (rfq_id);
CREATE INDEX idx_rfq_lines_item_id ON rfq_lines (item_id);
CREATE INDEX idx_rfq_vendor_quotes_rfq_id ON rfq_vendor_quotes (rfq_id);
CREATE INDEX idx_rfq_vendor_quotes_supplier_id ON rfq_vendor_quotes (supplier_id);
CREATE INDEX idx_purchase_orders_supplier_id ON purchase_orders (supplier_id);
CREATE INDEX idx_po_lines_po_id ON po_lines (purchase_order_id);
CREATE INDEX idx_po_lines_item_id ON po_lines (item_id);
