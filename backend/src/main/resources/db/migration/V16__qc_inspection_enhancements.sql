-- Add richer QC inspection fields and per-line tracking
ALTER TABLE qc_inspections
  ADD COLUMN IF NOT EXISTS grn_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS inspector_user_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS sample_qty DECIMAL(14,3) NULL,
  ADD COLUMN IF NOT EXISTS sample_uom_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS method VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS remarks VARCHAR(255) NULL;

UPDATE qc_inspections qi
JOIN grn_lines gl ON qi.grn_line_id = gl.id
SET qi.grn_id = gl.grn_id
WHERE qi.grn_line_id IS NOT NULL AND qi.grn_id IS NULL;

CREATE TABLE IF NOT EXISTS qc_inspection_lines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  qc_inspection_id BIGINT,
  grn_line_id BIGINT,
  received_qty DECIMAL(14,3),
  accepted_qty DECIMAL(14,3),
  rejected_qty DECIMAL(14,3),
  reason VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(100) NOT NULL DEFAULT 'system',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
  CONSTRAINT fk_qc_inspection_lines_inspection FOREIGN KEY (qc_inspection_id) REFERENCES qc_inspections(id),
  CONSTRAINT fk_qc_inspection_lines_grn_line FOREIGN KEY (grn_line_id) REFERENCES grn_lines(id)
);

CREATE INDEX IF NOT EXISTS idx_qc_inspection_lines_inspection ON qc_inspection_lines (qc_inspection_id);
CREATE INDEX IF NOT EXISTS idx_qc_inspections_grn ON qc_inspections (grn_id);
