ALTER TABLE purchase_invoices
  ADD COLUMN broker_id BIGINT NULL,
  ADD COLUMN brokerage_amount DECIMAL(14,2) DEFAULT 0;

CREATE TABLE IF NOT EXISTS purchase_invoice_charges (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  purchase_invoice_id BIGINT,
  charge_type_id BIGINT NULL,
  calc_type VARCHAR(20) NOT NULL,
  rate DECIMAL(14,2) DEFAULT 0,
  amount DECIMAL(14,2) DEFAULT 0,
  is_deduction BOOLEAN NOT NULL DEFAULT FALSE,
  payable_party_type VARCHAR(20) NOT NULL,
  payable_party_id BIGINT NULL,
  remarks VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT fk_purchase_invoice_charges_invoice FOREIGN KEY (purchase_invoice_id) REFERENCES purchase_invoices(id),
  CONSTRAINT fk_purchase_invoice_charges_type FOREIGN KEY (charge_type_id) REFERENCES deduction_charge_types(id)
);

CREATE INDEX idx_purchase_invoice_charges_invoice ON purchase_invoice_charges (purchase_invoice_id);
