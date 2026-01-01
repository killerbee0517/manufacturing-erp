CREATE TABLE IF NOT EXISTS payment_vouchers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  voucher_no VARCHAR(50) NOT NULL,
  voucher_date DATE NOT NULL,
  party_type VARCHAR(20) NOT NULL,
  party_id BIGINT NOT NULL,
  payment_direction VARCHAR(20) NOT NULL,
  payment_mode VARCHAR(20) NOT NULL,
  bank_id BIGINT NULL,
  amount DECIMAL(14,2) NOT NULL,
  narration VARCHAR(255),
  status VARCHAR(20) NOT NULL,
  cheque_number VARCHAR(50),
  cheque_date DATE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT fk_payment_vouchers_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_payment_vouchers_bank FOREIGN KEY (bank_id) REFERENCES banks(id)
);

CREATE TABLE IF NOT EXISTS payment_voucher_allocations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  payment_voucher_id BIGINT NOT NULL,
  purchase_invoice_id BIGINT NULL,
  allocated_amount DECIMAL(14,2) NOT NULL,
  remarks VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT fk_payment_allocations_voucher FOREIGN KEY (payment_voucher_id) REFERENCES payment_vouchers(id),
  CONSTRAINT fk_payment_allocations_invoice FOREIGN KEY (purchase_invoice_id) REFERENCES purchase_invoices(id)
);

CREATE INDEX idx_payment_vouchers_company_date ON payment_vouchers (company_id, voucher_date);
CREATE INDEX idx_payment_allocations_voucher ON payment_voucher_allocations (payment_voucher_id);
