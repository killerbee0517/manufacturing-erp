SET @has_rfq_company := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'rfq'
    AND COLUMN_NAME = 'company_id'
);

SET @rfq_add_company_sql := IF(
  @has_rfq_company = 0,
  'ALTER TABLE rfq ADD COLUMN company_id BIGINT',
  'SELECT 1'
);

PREPARE rfq_add_company_stmt FROM @rfq_add_company_sql;
EXECUTE rfq_add_company_stmt;
DEALLOCATE PREPARE rfq_add_company_stmt;

SET @has_rfq_lines_company := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'rfq_lines'
    AND COLUMN_NAME = 'company_id'
);

SET @rfq_lines_add_company_sql := IF(
  @has_rfq_lines_company = 0,
  'ALTER TABLE rfq_lines ADD COLUMN company_id BIGINT',
  'SELECT 1'
);

PREPARE rfq_lines_add_company_stmt FROM @rfq_lines_add_company_sql;
EXECUTE rfq_lines_add_company_stmt;
DEALLOCATE PREPARE rfq_lines_add_company_stmt;

SET @has_rfq_supplier_quotes_company := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'rfq_supplier_quotes'
    AND COLUMN_NAME = 'company_id'
);

SET @rfq_supplier_quotes_add_company_sql := IF(
  @has_rfq_supplier_quotes_company = 0,
  'ALTER TABLE rfq_supplier_quotes ADD COLUMN company_id BIGINT',
  'SELECT 1'
);

PREPARE rfq_supplier_quotes_add_company_stmt FROM @rfq_supplier_quotes_add_company_sql;
EXECUTE rfq_supplier_quotes_add_company_stmt;
DEALLOCATE PREPARE rfq_supplier_quotes_add_company_stmt;

SET @has_rfq_awards_company := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'rfq_awards'
    AND COLUMN_NAME = 'company_id'
);

SET @rfq_awards_add_company_sql := IF(
  @has_rfq_awards_company = 0,
  'ALTER TABLE rfq_awards ADD COLUMN company_id BIGINT',
  'SELECT 1'
);

PREPARE rfq_awards_add_company_stmt FROM @rfq_awards_add_company_sql;
EXECUTE rfq_awards_add_company_stmt;
DEALLOCATE PREPARE rfq_awards_add_company_stmt;

SET @has_rfq_quote_header_company := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'rfq_quote_header'
    AND COLUMN_NAME = 'company_id'
);

SET @rfq_quote_header_add_company_sql := IF(
  @has_rfq_quote_header_company = 0,
  'ALTER TABLE rfq_quote_header ADD COLUMN company_id BIGINT',
  'SELECT 1'
);

PREPARE rfq_quote_header_add_company_stmt FROM @rfq_quote_header_add_company_sql;
EXECUTE rfq_quote_header_add_company_stmt;
DEALLOCATE PREPARE rfq_quote_header_add_company_stmt;

SET @has_rfq_supplier_col := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'rfq'
    AND COLUMN_NAME = 'supplier_id'
);

SET @rfq_supplier_backfill_sql := IF(
  @has_rfq_supplier_col > 0,
  'UPDATE rfq r
     JOIN suppliers s ON r.supplier_id = s.id
     JOIN parties p ON s.party_id = p.id
   SET r.company_id = p.company_id
   WHERE r.company_id IS NULL AND p.company_id IS NOT NULL',
  'SELECT 1'
);

PREPARE rfq_supplier_backfill_stmt FROM @rfq_supplier_backfill_sql;
EXECUTE rfq_supplier_backfill_stmt;
DEALLOCATE PREPARE rfq_supplier_backfill_stmt;

UPDATE rfq r
JOIN rfq_supplier_quotes qs ON qs.rfq_id = r.id
JOIN suppliers s ON qs.supplier_id = s.id
JOIN parties p ON s.party_id = p.id
SET r.company_id = p.company_id
WHERE r.company_id IS NULL AND p.company_id IS NOT NULL;

UPDATE rfq r
SET r.company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE r.company_id IS NULL
  AND EXISTS (SELECT 1 FROM companies WHERE code = 'PARENT');

UPDATE rfq_lines l
JOIN rfq r ON l.rfq_id = r.id
SET l.company_id = r.company_id
WHERE l.company_id IS NULL AND r.company_id IS NOT NULL;

UPDATE rfq_lines l
SET l.company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE l.company_id IS NULL
  AND EXISTS (SELECT 1 FROM companies WHERE code = 'PARENT');

UPDATE rfq_supplier_quotes q
JOIN rfq r ON q.rfq_id = r.id
SET q.company_id = r.company_id
WHERE q.company_id IS NULL AND r.company_id IS NOT NULL;

UPDATE rfq_supplier_quotes q
SET q.company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE q.company_id IS NULL
  AND EXISTS (SELECT 1 FROM companies WHERE code = 'PARENT');

UPDATE rfq_awards a
JOIN rfq r ON a.rfq_id = r.id
SET a.company_id = r.company_id
WHERE a.company_id IS NULL AND r.company_id IS NOT NULL;

UPDATE rfq_awards a
SET a.company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE a.company_id IS NULL
  AND EXISTS (SELECT 1 FROM companies WHERE code = 'PARENT');

UPDATE rfq_quote_header h
JOIN rfq r ON h.rfq_id = r.id
SET h.company_id = r.company_id
WHERE h.company_id IS NULL AND r.company_id IS NOT NULL;

UPDATE rfq_quote_header h
SET h.company_id = (SELECT id FROM companies WHERE code = 'PARENT')
WHERE h.company_id IS NULL
  AND EXISTS (SELECT 1 FROM companies WHERE code = 'PARENT');

ALTER TABLE rfq
  MODIFY company_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_rfq_company FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE rfq_lines
  MODIFY company_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_rfq_lines_company FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE rfq_supplier_quotes
  MODIFY company_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_rfq_supplier_quotes_company FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE rfq_awards
  MODIFY company_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_rfq_awards_company FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE rfq_quote_header
  MODIFY company_id BIGINT NOT NULL,
  ADD CONSTRAINT fk_rfq_quote_header_company FOREIGN KEY (company_id) REFERENCES companies(id);
