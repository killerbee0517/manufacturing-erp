CREATE TABLE banks (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  branch VARCHAR(150),
  acc_no VARCHAR(100),
  ifsc VARCHAR(30),
  swift_code VARCHAR(50),
  type VARCHAR(50),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL
);

ALTER TABLE suppliers
  ADD COLUMN address VARCHAR(255),
  ADD COLUMN state VARCHAR(100),
  ADD COLUMN country VARCHAR(100),
  ADD COLUMN pin_code VARCHAR(20),
  ADD COLUMN gst_no VARCHAR(30),
  ADD COLUMN contact VARCHAR(50),
  ADD COLUMN email VARCHAR(150),
  ADD COLUMN bank_id BIGINT,
  ADD COLUMN supplier_type VARCHAR(20),
  ADD COLUMN credit_period INT,
  ADD CONSTRAINT fk_suppliers_bank FOREIGN KEY (bank_id) REFERENCES banks(id);

ALTER TABLE customers
  ADD COLUMN address VARCHAR(255),
  ADD COLUMN state VARCHAR(100),
  ADD COLUMN country VARCHAR(100),
  ADD COLUMN pin_code VARCHAR(20),
  ADD COLUMN pan VARCHAR(20),
  ADD COLUMN gst_no VARCHAR(30),
  ADD COLUMN contact VARCHAR(50),
  ADD COLUMN email VARCHAR(150),
  ADD COLUMN bank_id BIGINT,
  ADD COLUMN credit_period INT,
  ADD CONSTRAINT fk_customers_bank FOREIGN KEY (bank_id) REFERENCES banks(id);

ALTER TABLE vehicles
  ADD COLUMN vehicle_type VARCHAR(50),
  ADD COLUMN registration_date DATE;

ALTER TABLE godowns
  ADD COLUMN location VARCHAR(150);
