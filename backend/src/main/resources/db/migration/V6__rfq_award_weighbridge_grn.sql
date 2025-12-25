-- RFQ supplier invites and awards
CREATE TABLE rfq_supplier_quotes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rfq_id BIGINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  remarks VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT fk_rfq_supplier_quotes_rfq FOREIGN KEY (rfq_id) REFERENCES rfq(id),
  CONSTRAINT fk_rfq_supplier_quotes_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
) ENGINE=InnoDB;

CREATE TABLE rfq_awards (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rfq_line_id BIGINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  awarded_qty DECIMAL(14,3) NOT NULL,
  rate DECIMAL(14,2) NOT NULL,
  award_status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT fk_rfq_awards_line FOREIGN KEY (rfq_line_id) REFERENCES rfq_lines(id),
  CONSTRAINT fk_rfq_awards_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
) ENGINE=InnoDB;

CREATE INDEX idx_rfq_supplier_quotes_rfq ON rfq_supplier_quotes (rfq_id);
CREATE INDEX idx_rfq_awards_line ON rfq_awards (rfq_line_id);

-- Weighbridge enhancements
ALTER TABLE weighbridge_tickets ADD COLUMN po_id BIGINT NULL;
ALTER TABLE weighbridge_tickets ADD COLUMN vehicle_id BIGINT NULL;

CREATE INDEX idx_weighbridge_po ON weighbridge_tickets (po_id);
CREATE INDEX idx_weighbridge_vehicle ON weighbridge_tickets (vehicle_id);

-- GRN updates
ALTER TABLE grn ADD COLUMN godown_id BIGINT NULL;

ALTER TABLE grn_lines ADD COLUMN po_line_id BIGINT NULL;
ALTER TABLE grn_lines ADD COLUMN received_qty DECIMAL(14,3) NULL;
ALTER TABLE grn_lines ADD COLUMN rate DECIMAL(14,2) NULL;
ALTER TABLE grn_lines ADD COLUMN amount DECIMAL(14,2) NULL;

CREATE INDEX idx_grn_weighbridge_id ON grn (weighbridge_ticket_id);
CREATE INDEX idx_grn_lines_po_line_id ON grn_lines (po_line_id);
