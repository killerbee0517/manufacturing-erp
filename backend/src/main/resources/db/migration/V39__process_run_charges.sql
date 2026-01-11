CREATE TABLE process_run_charges (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  process_run_id BIGINT NOT NULL,
  charge_type_id BIGINT NOT NULL,
  calc_type VARCHAR(20) NOT NULL,
  rate DECIMAL(14,3) NULL,
  quantity DECIMAL(14,3) NULL,
  amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  is_deduction BOOLEAN NOT NULL DEFAULT FALSE,
  payable_party_type VARCHAR(20) NOT NULL,
  payable_party_id BIGINT NULL,
  remarks VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
  CONSTRAINT fk_run_charge_run FOREIGN KEY (process_run_id) REFERENCES process_runs(id),
  CONSTRAINT fk_run_charge_type FOREIGN KEY (charge_type_id) REFERENCES deduction_charge_types(id)
) ENGINE=InnoDB;
