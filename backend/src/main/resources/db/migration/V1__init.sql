CREATE TABLE roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(150) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE branches (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  code VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE godowns (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  branch_id BIGINT,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE locations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  code VARCHAR(50) NOT NULL,
  location_type VARCHAR(20) NOT NULL,
  parent_id BIGINT,
  branch_id BIGINT,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (parent_id) REFERENCES locations(id),
  FOREIGN KEY (branch_id) REFERENCES branches(id)
);

CREATE TABLE uoms (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE,
  description VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  sku VARCHAR(100) NOT NULL UNIQUE,
  uom_id BIGINT,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (uom_id) REFERENCES uoms(id)
);

CREATE TABLE vehicles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  vehicle_no VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE suppliers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  code VARCHAR(50) NOT NULL UNIQUE,
  pan VARCHAR(20),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE supplier_bank_accounts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplier_id BIGINT,
  bank_name VARCHAR(150) NOT NULL,
  account_number VARCHAR(100) NOT NULL,
  ifsc VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE customers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  code VARCHAR(50) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE brokers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  code VARCHAR(50) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE broker_commission_rules (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  broker_id BIGINT,
  rate_percent DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (broker_id) REFERENCES brokers(id)
);

CREATE TABLE broker_commissions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sales_invoice_id BIGINT,
  broker_id BIGINT,
  commission_amount DECIMAL(12,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE tds_rules (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  section_code VARCHAR(20) NOT NULL,
  rate_percent DECIMAL(10,2) NOT NULL,
  threshold_amount DECIMAL(14,2) NOT NULL,
  effective_from DATE NOT NULL,
  effective_to DATE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE supplier_tax_profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplier_id BIGINT,
  default_section VARCHAR(20) NOT NULL,
  tds_applicable BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE tds_deductions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  purchase_invoice_id BIGINT,
  section_code VARCHAR(20) NOT NULL,
  tds_amount DECIMAL(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE rfq (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rfq_no VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE rfq_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rfq_id BIGINT,
  item_id BIGINT,
  quantity DECIMAL(14,3) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE rfq_vendor_quotes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rfq_id BIGINT,
  supplier_id BIGINT,
  quote_amount DECIMAL(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE purchase_orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  po_no VARCHAR(50) NOT NULL,
  supplier_id BIGINT,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE po_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  purchase_order_id BIGINT,
  item_id BIGINT,
  quantity DECIMAL(14,3) NOT NULL,
  rate DECIMAL(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE weighbridge_tickets (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ticket_no VARCHAR(50) NOT NULL,
  vehicle_no VARCHAR(50) NOT NULL,
  supplier_id BIGINT,
  item_id BIGINT,
  date_in DATE NOT NULL,
  time_in TIME NOT NULL,
  gross_weight DECIMAL(14,3) NOT NULL,
  tare_weight DECIMAL(14,3) NOT NULL,
  net_weight DECIMAL(14,3) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE weighbridge_readings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ticket_id BIGINT,
  reading_type VARCHAR(20) NOT NULL,
  weight DECIMAL(14,3) NOT NULL,
  reading_time TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  FOREIGN KEY (ticket_id) REFERENCES weighbridge_tickets(id)
);

CREATE TABLE weighbridge_ticket_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ticket_id BIGINT,
  item_id BIGINT,
  quantity DECIMAL(14,3) NOT NULL,
  rate DECIMAL(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE grn (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  grn_no VARCHAR(50) NOT NULL,
  supplier_id BIGINT,
  weighbridge_ticket_id BIGINT,
  grn_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE grn_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  grn_id BIGINT,
  item_id BIGINT,
  bag_type VARCHAR(50) NOT NULL,
  bag_count INT NOT NULL,
  quantity DECIMAL(14,3) NOT NULL,
  weight DECIMAL(14,3) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE qc_inspections (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  grn_line_id BIGINT,
  status VARCHAR(20) NOT NULL,
  inspection_date DATE NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE qc_results (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  qc_inspection_id BIGINT,
  parameter VARCHAR(100),
  result_value VARCHAR(100),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE purchase_invoices (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  invoice_no VARCHAR(50) NOT NULL,
  supplier_id BIGINT,
  invoice_date DATE NOT NULL,
  total_amount DECIMAL(14,2) NOT NULL,
  tds_amount DECIMAL(14,2) NOT NULL,
  net_payable DECIMAL(14,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE purchase_invoice_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  purchase_invoice_id BIGINT,
  item_id BIGINT,
  quantity DECIMAL(14,3) NOT NULL,
  line_amount DECIMAL(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE debit_notes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  debit_note_no VARCHAR(50) NOT NULL,
  supplier_id BIGINT,
  reason VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE debit_note_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  debit_note_id BIGINT,
  item_id BIGINT,
  amount DECIMAL(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE sales_orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  so_no VARCHAR(50) NOT NULL,
  customer_id BIGINT,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE so_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sales_order_id BIGINT,
  item_id BIGINT,
  quantity DECIMAL(14,3) NOT NULL,
  rate DECIMAL(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE deliveries (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  delivery_no VARCHAR(50) NOT NULL,
  sales_order_id BIGINT,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE delivery_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  delivery_id BIGINT,
  item_id BIGINT,
  quantity DECIMAL(14,3) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE sales_invoices (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  invoice_no VARCHAR(50) NOT NULL,
  customer_id BIGINT,
  broker_id BIGINT,
  invoice_date DATE NOT NULL,
  total_amount DECIMAL(14,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE sales_invoice_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sales_invoice_id BIGINT,
  item_id BIGINT,
  quantity DECIMAL(14,3) NOT NULL,
  line_amount DECIMAL(14,2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE stock_ledger (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_type VARCHAR(50) NOT NULL,
  doc_id BIGINT NOT NULL,
  doc_line_id BIGINT,
  txn_type VARCHAR(10) NOT NULL,
  quantity DECIMAL(14,3) NOT NULL,
  weight DECIMAL(14,3) NOT NULL,
  uom_id BIGINT,
  item_id BIGINT,
  from_location_id BIGINT,
  to_location_id BIGINT,
  status VARCHAR(20) NOT NULL,
  posted_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

CREATE TABLE batch_lots (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  lot_no VARCHAR(50) NOT NULL,
  item_id BIGINT,
  mfg_date DATE,
  exp_date DATE,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

INSERT INTO roles (name, created_at, created_by, updated_at, updated_by) VALUES
('ADMIN', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed'),
('PURCHASE', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed'),
('STORE', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed'),
('QC', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed'),
('SALES', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed'),
('FINANCE', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed'),
('VIEWER', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed');

INSERT INTO users (username, password, full_name, created_at, created_by, updated_at, updated_by)
VALUES ('admin', '{noop}admin123', 'Admin User', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ADMIN' WHERE u.username = 'admin';

INSERT INTO branches (name, code, created_at, created_by, updated_at, updated_by)
VALUES ('Main Branch', 'MAIN', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed');

INSERT INTO uoms (code, description, created_at, created_by, updated_at, updated_by)
VALUES ('KG', 'Kilogram', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed');

INSERT INTO tds_rules (section_code, rate_percent, threshold_amount, effective_from, effective_to, created_at, created_by, updated_at, updated_by)
VALUES ('194Q', 0.1, 5000000, DATE '2023-04-01', DATE '2099-12-31', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed');

INSERT INTO items (name, sku, uom_id, created_at, created_by, updated_at, updated_by)
SELECT 'Raw Paddy', 'ITEM-001', u.id, CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed' FROM uoms u WHERE u.code = 'KG';

