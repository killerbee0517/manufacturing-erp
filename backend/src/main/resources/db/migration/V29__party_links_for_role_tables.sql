ALTER TABLE suppliers
  ADD COLUMN party_id BIGINT,
  ADD CONSTRAINT fk_suppliers_party FOREIGN KEY (party_id) REFERENCES parties(id);

ALTER TABLE customers
  ADD COLUMN party_id BIGINT,
  ADD CONSTRAINT fk_customers_party FOREIGN KEY (party_id) REFERENCES parties(id);

ALTER TABLE brokers
  ADD COLUMN party_id BIGINT,
  ADD CONSTRAINT fk_brokers_party FOREIGN KEY (party_id) REFERENCES parties(id);

ALTER TABLE expense_parties
  ADD COLUMN party_id BIGINT,
  ADD CONSTRAINT fk_expense_parties_party FOREIGN KEY (party_id) REFERENCES parties(id);
