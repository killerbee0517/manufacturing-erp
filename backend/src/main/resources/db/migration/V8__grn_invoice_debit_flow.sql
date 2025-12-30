-- Align GRN lines with expected/accepted quantities
ALTER TABLE grn
  ADD COLUMN received_date DATE NULL;

UPDATE grn SET received_date = grn_date WHERE received_date IS NULL;

ALTER TABLE grn_lines
  ADD COLUMN expected_qty DECIMAL(14,3) NULL,
  ADD COLUMN accepted_qty DECIMAL(14,3) NULL,
  ADD COLUMN rejected_qty DECIMAL(14,3) DEFAULT 0;

UPDATE grn_lines
SET expected_qty = quantity
WHERE expected_qty IS NULL;

UPDATE grn_lines
SET accepted_qty = COALESCE(received_qty, quantity)
WHERE accepted_qty IS NULL;

-- Purchase invoice enhancements
ALTER TABLE purchase_invoices
  ADD COLUMN supplier_invoice_no VARCHAR(255) NULL,
  ADD COLUMN po_id BIGINT NULL,
  ADD COLUMN grn_id BIGINT NULL,
  ADD COLUMN narration VARCHAR(255) NULL,
  ADD COLUMN subtotal DECIMAL(14,2) DEFAULT 0,
  ADD COLUMN tax_total DECIMAL(14,2) DEFAULT 0,
  ADD COLUMN round_off DECIMAL(14,2) DEFAULT 0,
  ADD COLUMN grand_total DECIMAL(14,2) DEFAULT 0;

UPDATE purchase_invoices
SET status = 'POSTED'
WHERE status IS NULL;

UPDATE purchase_invoices
SET subtotal = total_amount,
    grand_total = net_payable
WHERE subtotal = 0 AND total_amount IS NOT NULL;

ALTER TABLE purchase_invoice_lines
  ADD COLUMN uom_id BIGINT NULL,
  ADD COLUMN rate DECIMAL(14,2) DEFAULT 0,
  ADD COLUMN amount DECIMAL(14,2) DEFAULT 0;

UPDATE purchase_invoice_lines
SET amount = line_amount
WHERE (amount IS NULL OR amount = 0) AND line_amount IS NOT NULL;

-- Debit note enhancements
ALTER TABLE debit_notes
  ADD COLUMN purchase_invoice_id BIGINT NULL,
  ADD COLUMN po_id BIGINT NULL,
  ADD COLUMN grn_id BIGINT NULL,
  ADD COLUMN dn_date DATE NULL,
  ADD COLUMN narration VARCHAR(255) NULL,
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  ADD COLUMN total_deduction DECIMAL(14,2) DEFAULT 0;

CREATE TABLE IF NOT EXISTS debit_note_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  debit_note_id BIGINT,
  rule_id BIGINT NULL,
  description VARCHAR(255),
  base_value DECIMAL(14,2) DEFAULT 0,
  rate DECIMAL(14,2) DEFAULT 0,
  amount DECIMAL(14,2) DEFAULT 0,
  remarks VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT fk_debit_note_lines_note FOREIGN KEY (debit_note_id) REFERENCES debit_notes(id)
);

CREATE INDEX idx_purchase_invoices_grn ON purchase_invoices (grn_id);
CREATE INDEX idx_debit_notes_invoice ON debit_notes (purchase_invoice_id);
