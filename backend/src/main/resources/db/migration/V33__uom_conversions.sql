ALTER TABLE uoms
  ADD COLUMN base_uom_id BIGINT NULL,
  ADD COLUMN conversion_factor DECIMAL(19, 6) NULL,
  ADD CONSTRAINT fk_uoms_base_uom FOREIGN KEY (base_uom_id) REFERENCES uoms(id);
