ALTER TABLE production_batch_inputs
  ADD COLUMN IF NOT EXISTS step_no INT;

ALTER TABLE production_batch_outputs
  ADD COLUMN IF NOT EXISTS step_no INT;
