CREATE TABLE rfq_quote_header (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rfq_id BIGINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL,
  payment_terms_override VARCHAR(255),
  remarks VARCHAR(255),
  submitted_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT fk_rfq_quote_header_rfq FOREIGN KEY (rfq_id) REFERENCES rfq(id),
  CONSTRAINT fk_rfq_quote_header_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE rfq_quote_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  quote_header_id BIGINT NOT NULL,
  rfq_line_id BIGINT NOT NULL,
  quoted_qty DECIMAL(14,3),
  quoted_rate DECIMAL(14,2),
  delivery_date DATE,
  remarks VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  CONSTRAINT fk_rfq_quote_lines_header FOREIGN KEY (quote_header_id) REFERENCES rfq_quote_header(id),
  CONSTRAINT fk_rfq_quote_lines_rfq_line FOREIGN KEY (rfq_line_id) REFERENCES rfq_lines(id)
);

ALTER TABLE rfq_awards
  CHANGE COLUMN rate awarded_rate DECIMAL(14,2) NOT NULL;

ALTER TABLE rfq_awards
  ADD COLUMN awarded_delivery_date DATE NULL;

CREATE INDEX idx_rfq_quote_header_rfq ON rfq_quote_header (rfq_id);
CREATE INDEX idx_rfq_quote_header_supplier ON rfq_quote_header (supplier_id);
CREATE INDEX idx_rfq_quote_lines_header ON rfq_quote_lines (quote_header_id);
CREATE INDEX idx_rfq_quote_lines_rfq_line ON rfq_quote_lines (rfq_line_id);
