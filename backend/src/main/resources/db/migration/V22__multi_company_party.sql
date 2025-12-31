CREATE TABLE companies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  parent_company_id BIGINT,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  gst_no VARCHAR(30),
  pan VARCHAR(20),
  address VARCHAR(255),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (parent_company_id) REFERENCES companies(id)
);

CREATE TABLE user_companies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  company_id BIGINT NOT NULL,
  primary_company BOOLEAN NOT NULL DEFAULT FALSE,
  role_scope VARCHAR(50),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT uq_user_company UNIQUE (user_id, company_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (company_id) REFERENCES companies(id)
);

ALTER TABLE banks
  ADD COLUMN company_id BIGINT,
  ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
  ADD CONSTRAINT fk_banks_company FOREIGN KEY (company_id) REFERENCES companies(id);

CREATE TABLE parties (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  party_code VARCHAR(50) NOT NULL,
  name VARCHAR(150) NOT NULL,
  address VARCHAR(255),
  state VARCHAR(100),
  country VARCHAR(100),
  pincode VARCHAR(20),
  pan VARCHAR(20),
  gst_no VARCHAR(30),
  contact VARCHAR(50),
  email VARCHAR(150),
  bank_id BIGINT,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT uq_party_code_per_company UNIQUE (company_id, party_code),
  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (bank_id) REFERENCES banks(id)
);

CREATE TABLE party_roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  party_id BIGINT NOT NULL,
  role_type VARCHAR(20) NOT NULL,
  credit_period_days INT,
  supplier_type VARCHAR(20),
  broker_commission_type VARCHAR(20),
  broker_commission_rate DECIMAL(14,4),
  brokerage_paid_by VARCHAR(20),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT uq_party_role UNIQUE (company_id, party_id, role_type),
  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (party_id) REFERENCES parties(id)
);

CREATE TABLE ledger_accounts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  ledger_type VARCHAR(30) NOT NULL,
  party_id BIGINT,
  bank_id BIGINT,
  name VARCHAR(150) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT uq_ledger_type_party UNIQUE (company_id, ledger_type, party_id),
  CONSTRAINT uq_ledger_bank UNIQUE (company_id, bank_id, ledger_type),
  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (party_id) REFERENCES parties(id),
  FOREIGN KEY (bank_id) REFERENCES banks(id)
);

CREATE TABLE ledger_entries (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  ledger_account_id BIGINT NOT NULL,
  txn_date DATE NOT NULL,
  txn_type VARCHAR(50) NOT NULL,
  ref_table VARCHAR(50),
  ref_id BIGINT,
  debit DECIMAL(14,2) NOT NULL DEFAULT 0,
  credit DECIMAL(14,2) NOT NULL DEFAULT 0,
  narration VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (ledger_account_id) REFERENCES ledger_accounts(id),
  INDEX idx_ledger_entries_account_date (ledger_account_id, txn_date)
);

CREATE TABLE pdc_register (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id BIGINT NOT NULL,
  pdc_no VARCHAR(50) NOT NULL,
  party_id BIGINT NOT NULL,
  party_role_type VARCHAR(20) NOT NULL,
  direction VARCHAR(20) NOT NULL,
  bank_ledger_account_id BIGINT NOT NULL,
  cheque_number VARCHAR(100) NOT NULL,
  cheque_date DATE NOT NULL,
  amount DECIMAL(14,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  remarks VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT uq_pdc_no_per_company UNIQUE (company_id, pdc_no),
  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (party_id) REFERENCES parties(id),
  FOREIGN KEY (bank_ledger_account_id) REFERENCES ledger_accounts(id)
);

INSERT INTO companies (parent_company_id, code, name, gst_no, pan, address, active, created_at, created_by, updated_at, updated_by)
VALUES (NULL, 'PARENT', 'Parent Company', NULL, NULL, NULL, TRUE, CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed');

INSERT INTO user_companies (user_id, company_id, primary_company, role_scope, created_at, created_by, updated_at, updated_by)
SELECT u.id, c.id, TRUE, NULL, CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed'
FROM users u CROSS JOIN companies c
WHERE u.username = 'admin' AND c.code = 'PARENT' AND NOT EXISTS (
  SELECT 1 FROM user_companies uc WHERE uc.user_id = u.id AND uc.company_id = c.id
);

UPDATE banks
SET company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE company_id IS NULL;
