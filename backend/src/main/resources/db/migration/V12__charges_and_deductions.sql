CREATE TABLE deduction_charge_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(100) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  default_calc_type VARCHAR(20) NOT NULL,
  default_rate DECIMAL(14,3) NULL,
  is_deduction BOOLEAN NOT NULL DEFAULT FALSE,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'system'
) ENGINE=InnoDB;

CREATE TABLE expense_parties (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  party_type VARCHAR(20) NOT NULL,
  ledger_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'system'
) ENGINE=InnoDB;

CREATE TABLE purchase_arrivals (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  purchase_order_id BIGINT NULL,
  weighbridge_ticket_id BIGINT NULL,
  godown_id BIGINT NULL,
  unloading_charges DECIMAL(14,2) NOT NULL DEFAULT 0,
  deductions DECIMAL(14,2) NOT NULL DEFAULT 0,
  tds_percent DECIMAL(14,2) NOT NULL DEFAULT 0,
  gross_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  net_payable DECIMAL(14,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
  CONSTRAINT fk_purchase_arrival_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
  CONSTRAINT fk_purchase_arrival_weighbridge FOREIGN KEY (weighbridge_ticket_id) REFERENCES weighbridge_tickets(id),
  CONSTRAINT fk_purchase_arrival_godown FOREIGN KEY (godown_id) REFERENCES godowns(id)
) ENGINE=InnoDB;

CREATE TABLE purchase_arrival_charges (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  purchase_arrival_id BIGINT NOT NULL,
  charge_type_id BIGINT NOT NULL,
  calc_type VARCHAR(20) NOT NULL,
  rate DECIMAL(14,3) NULL,
  amount DECIMAL(14,2) NOT NULL DEFAULT 0,
  is_deduction BOOLEAN NOT NULL DEFAULT FALSE,
  payable_party_type VARCHAR(20) NOT NULL,
  payable_party_id BIGINT NULL,
  remarks VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
  CONSTRAINT fk_purchase_arrival_charge_arrival FOREIGN KEY (purchase_arrival_id) REFERENCES purchase_arrivals(id),
  CONSTRAINT fk_purchase_arrival_charge_type FOREIGN KEY (charge_type_id) REFERENCES deduction_charge_types(id)
) ENGINE=InnoDB;

ALTER TABLE weighbridge_tickets MODIFY COLUMN tare_weight DECIMAL(14,3) NULL;
ALTER TABLE weighbridge_tickets MODIFY COLUMN net_weight DECIMAL(14,3) NULL;
