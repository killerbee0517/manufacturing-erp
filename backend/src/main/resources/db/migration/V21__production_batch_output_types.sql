ALTER TABLE production_batch_outputs
  MODIFY COLUMN output_type ENUM('WIP','FG','BYPRODUCT') NOT NULL;
