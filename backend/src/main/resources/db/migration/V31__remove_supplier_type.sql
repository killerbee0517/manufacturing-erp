SET @has_supplier_type := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'suppliers'
    AND COLUMN_NAME = 'supplier_type'
);

SET @drop_supplier_type_sql := IF(
  @has_supplier_type > 0,
  'ALTER TABLE suppliers DROP COLUMN supplier_type',
  'SELECT 1'
);

PREPARE drop_supplier_type_stmt FROM @drop_supplier_type_sql;
EXECUTE drop_supplier_type_stmt;
DEALLOCATE PREPARE drop_supplier_type_stmt;

SET @has_party_role_supplier_type := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'party_roles'
    AND COLUMN_NAME = 'supplier_type'
);

SET @drop_party_role_supplier_type_sql := IF(
  @has_party_role_supplier_type > 0,
  'ALTER TABLE party_roles DROP COLUMN supplier_type',
  'SELECT 1'
);

PREPARE drop_party_role_supplier_type_stmt FROM @drop_party_role_supplier_type_sql;
EXECUTE drop_party_role_supplier_type_stmt;
DEALLOCATE PREPARE drop_party_role_supplier_type_stmt;
