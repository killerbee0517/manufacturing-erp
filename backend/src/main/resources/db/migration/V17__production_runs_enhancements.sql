-- Add sequencing and metadata to process runs to support multi-run batches
ALTER TABLE process_runs
    ADD COLUMN run_no INT NULL,
    ADD COLUMN step_no INT NULL,
    ADD COLUMN notes VARCHAR(500) NULL;

-- Backfill any existing rows with a default run number
UPDATE process_runs SET run_no = id WHERE run_no IS NULL;
