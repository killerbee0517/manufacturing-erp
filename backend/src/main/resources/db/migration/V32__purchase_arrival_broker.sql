ALTER TABLE purchase_arrivals
  ADD COLUMN broker_id BIGINT NULL,
  ADD COLUMN brokerage_amount DECIMAL(19, 2) NULL,
  ADD CONSTRAINT fk_purchase_arrivals_broker FOREIGN KEY (broker_id) REFERENCES brokers(id);
