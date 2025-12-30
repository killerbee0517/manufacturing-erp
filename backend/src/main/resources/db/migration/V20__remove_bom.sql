ALTER TABLE production_orders
  DROP COLUMN IF EXISTS bom_id;

DROP TABLE IF EXISTS bom_lines;
DROP TABLE IF EXISTS bom_header;
