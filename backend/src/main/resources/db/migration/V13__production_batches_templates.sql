-- Extend process templates with codes, outputs and enable flag
ALTER TABLE process_templates
    ADD COLUMN code VARCHAR(100),
    ADD COLUMN output_item_id BIGINT NULL,
    ADD COLUMN output_uom_id BIGINT NULL,
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE process_templates
    ADD CONSTRAINT uk_process_template_code UNIQUE (code),
    ADD CONSTRAINT fk_process_template_output_item FOREIGN KEY (output_item_id) REFERENCES items(id),
    ADD CONSTRAINT fk_process_template_output_uom FOREIGN KEY (output_uom_id) REFERENCES uoms(id);

-- Inputs defined at template level (BOM-like)
CREATE TABLE process_template_inputs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    uom_id BIGINT NOT NULL,
    default_qty DECIMAL(14,3) NOT NULL,
    is_optional BOOLEAN NOT NULL DEFAULT FALSE,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT fk_pti_template FOREIGN KEY (template_id) REFERENCES process_templates(id),
    CONSTRAINT fk_pti_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_pti_uom FOREIGN KEY (uom_id) REFERENCES uoms(id)
);

-- Ordered process steps
CREATE TABLE process_template_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    step_no INT NOT NULL,
    step_name VARCHAR(150) NOT NULL,
    step_type ENUM('CONSUME','PROCESS','PRODUCE','QUALITY') NOT NULL DEFAULT 'PROCESS',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT fk_pts_template FOREIGN KEY (template_id) REFERENCES process_templates(id)
);

-- Production batches refreshed to work off templates directly
ALTER TABLE production_batches
    MODIFY COLUMN production_order_id BIGINT NULL,
    ADD COLUMN template_id BIGINT NULL,
    ADD COLUMN planned_output_qty DECIMAL(14,3) DEFAULT 0,
    ADD COLUMN uom_id BIGINT NULL,
    ADD COLUMN remarks VARCHAR(500),
    MODIFY COLUMN status VARCHAR(30) NOT NULL DEFAULT 'DRAFT';

ALTER TABLE production_batches
    ADD CONSTRAINT fk_pb_template FOREIGN KEY (template_id) REFERENCES process_templates(id),
    ADD CONSTRAINT fk_pb_uom FOREIGN KEY (uom_id) REFERENCES uoms(id);

-- Inputs issued to a batch
CREATE TABLE production_batch_inputs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    uom_id BIGINT NOT NULL,
    issued_qty DECIMAL(14,3) NOT NULL,
    source_type ENUM('GODOWN','WIP') NOT NULL,
    source_ref_id BIGINT NULL,
    source_godown_id BIGINT NULL,
    issued_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT fk_pbi_batch FOREIGN KEY (batch_id) REFERENCES production_batches(id),
    CONSTRAINT fk_pbi_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_pbi_uom FOREIGN KEY (uom_id) REFERENCES uoms(id),
    CONSTRAINT fk_pbi_godown FOREIGN KEY (source_godown_id) REFERENCES godowns(id)
);

-- Runtime copy of steps for a batch
CREATE TABLE production_batch_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    step_no INT NOT NULL,
    step_name VARCHAR(150) NOT NULL,
    status ENUM('PENDING','DONE','SKIPPED') NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT fk_pbs_batch FOREIGN KEY (batch_id) REFERENCES production_batches(id)
);

-- Outputs generated from a batch (either WIP or FG)
CREATE TABLE production_batch_outputs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    uom_id BIGINT NOT NULL,
    produced_qty DECIMAL(14,3) NOT NULL,
    consumed_qty DECIMAL(14,3) NOT NULL DEFAULT 0,
    output_type ENUM('WIP','FG') NOT NULL,
    destination_godown_id BIGINT NULL,
    produced_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT fk_pbo_batch FOREIGN KEY (batch_id) REFERENCES production_batches(id),
    CONSTRAINT fk_pbo_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_pbo_uom FOREIGN KEY (uom_id) REFERENCES uoms(id),
    CONSTRAINT fk_pbo_godown FOREIGN KEY (destination_godown_id) REFERENCES godowns(id)
);

-- Light-weight movement table to complement stock ledger and track WIP
CREATE TABLE inventory_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    txn_type ENUM('PROD_ISSUE','PROD_OUTPUT','TRANSFER') NOT NULL,
    ref_type VARCHAR(100),
    ref_id BIGINT,
    item_id BIGINT NOT NULL,
    qty_in DECIMAL(14,3) NOT NULL DEFAULT 0,
    qty_out DECIMAL(14,3) NOT NULL DEFAULT 0,
    uom_id BIGINT NOT NULL,
    location_type ENUM('GODOWN','WIP') NOT NULL,
    location_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT fk_inv_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_inv_uom FOREIGN KEY (uom_id) REFERENCES uoms(id)
);

CREATE INDEX idx_inv_movements_ref ON inventory_movements (ref_type, ref_id);
CREATE INDEX idx_inv_movements_location ON inventory_movements (location_type, location_id);
