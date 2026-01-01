ALTER TABLE qc_inspections
  ADD COLUMN IF NOT EXISTS weighbridge_ticket_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS purchase_order_id BIGINT NULL;

UPDATE qc_inspections qi
JOIN grn g ON qi.grn_id = g.id
SET qi.purchase_order_id = g.purchase_order_id,
    qi.weighbridge_ticket_id = g.weighbridge_ticket_id
WHERE qi.grn_id IS NOT NULL;

ALTER TABLE qc_inspection_lines
  ADD COLUMN IF NOT EXISTS purchase_order_line_id BIGINT NULL;

UPDATE qc_inspection_lines ql
JOIN grn_lines gl ON ql.grn_line_id = gl.id
SET ql.purchase_order_line_id = gl.po_line_id
WHERE ql.grn_line_id IS NOT NULL;

ALTER TABLE qc_inspections
  ADD CONSTRAINT fk_qc_inspections_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
  ADD CONSTRAINT fk_qc_inspections_weighbridge FOREIGN KEY (weighbridge_ticket_id) REFERENCES weighbridge_tickets(id);

ALTER TABLE qc_inspection_lines
  ADD CONSTRAINT fk_qc_inspection_lines_po_line FOREIGN KEY (purchase_order_line_id) REFERENCES po_lines(id);

CREATE INDEX idx_qc_inspections_weighbridge ON qc_inspections (weighbridge_ticket_id);
CREATE INDEX idx_qc_inspections_purchase_order ON qc_inspections (purchase_order_id);
CREATE INDEX idx_qc_inspection_lines_po_line ON qc_inspection_lines (purchase_order_line_id);
