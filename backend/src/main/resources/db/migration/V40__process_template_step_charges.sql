CREATE TABLE process_template_step_charges (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  step_id BIGINT NOT NULL,
  charge_type_id BIGINT NOT NULL,
  calc_type VARCHAR(20) NOT NULL,
  rate DECIMAL(14,3) NULL,
  per_qty BOOLEAN NOT NULL DEFAULT FALSE,
  is_deduction BOOLEAN NOT NULL DEFAULT FALSE,
  payable_party_type VARCHAR(20) NOT NULL,
  payable_party_id BIGINT NULL,
  remarks VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
  CONSTRAINT fk_template_step_charge_step FOREIGN KEY (step_id) REFERENCES process_template_steps(id),
  CONSTRAINT fk_template_step_charge_type FOREIGN KEY (charge_type_id) REFERENCES deduction_charge_types(id)
) ENGINE=InnoDB;
