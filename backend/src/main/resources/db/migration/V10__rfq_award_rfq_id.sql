ALTER TABLE rfq_awards
  ADD COLUMN rfq_id BIGINT NULL;

UPDATE rfq_awards a
JOIN rfq_lines l ON l.id = a.rfq_line_id
SET a.rfq_id = l.rfq_id;

ALTER TABLE rfq_awards
  MODIFY COLUMN rfq_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_rfq_awards_rfq FOREIGN KEY (rfq_id) REFERENCES rfq(id);

CREATE INDEX idx_rfq_awards_rfq ON rfq_awards (rfq_id);
