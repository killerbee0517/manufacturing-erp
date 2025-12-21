ALTER TABLE rfq
  ADD COLUMN payment_terms VARCHAR(255) NULL,
  ADD COLUMN narration VARCHAR(255) NULL,
  ADD COLUMN closure_reason VARCHAR(255) NULL;

ALTER TABLE rfq_lines
  ADD COLUMN broker_id BIGINT NULL;

ALTER TABLE purchase_orders
  ADD COLUMN delivery_date DATE NULL,
  ADD COLUMN supplier_invoice_no VARCHAR(255) NULL,
  ADD COLUMN purchase_ledger VARCHAR(255) NULL,
  ADD COLUMN current_ledger_balance DECIMAL(14,2) NULL,
  ADD COLUMN rfq_id BIGINT NULL;

ALTER TABLE weighbridge_tickets
  ADD COLUMN date_out DATE NULL,
  ADD COLUMN time_out TIME NULL;

ALTER TABLE grn
  ADD COLUMN purchase_order_id BIGINT NULL,
  ADD COLUMN item_id BIGINT NULL,
  ADD COLUMN uom_id BIGINT NULL,
  ADD COLUMN narration VARCHAR(255) NULL,
  ADD COLUMN first_weight DECIMAL(14,3) NULL,
  ADD COLUMN second_weight DECIMAL(14,3) NULL,
  ADD COLUMN net_weight DECIMAL(14,3) NULL,
  ADD COLUMN quantity DECIMAL(14,3) NULL;

ALTER TABLE grn_lines
  ADD COLUMN uom_id BIGINT NULL;

CREATE INDEX idx_rfq_lines_broker_id ON rfq_lines (broker_id);
CREATE INDEX idx_purchase_orders_rfq_id ON purchase_orders (rfq_id);
CREATE INDEX idx_grn_purchase_order_id ON grn (purchase_order_id);
CREATE INDEX idx_grn_item_id ON grn (item_id);
CREATE INDEX idx_grn_uom_id ON grn (uom_id);
