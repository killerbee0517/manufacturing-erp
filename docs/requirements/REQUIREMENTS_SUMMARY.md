# Requirements Summary

## Modules
- Masters (Items, UOM, warehouses, quality grades, brokers, deductions/charges, parties, routes/executives)
- Purchase
- Sales
- Inventory
- Production
- Accounting/Ledger
- Reports
- Settings/Admin

## Key Workflows
- RFQ -> PO -> Weighbridge (first weight) -> QC (accept/reject) -> GRN/Unload -> Purchase Invoice -> Payment
- Sales Order -> Delivery -> Invoice (multi-series) -> Collection
- Production (multi-step, multi-input/multi-output, WIP chaining; by-products to stock)

## Multi-Company Scoping
- Parent group with multiple subsidiaries; new companies can be added.
- Users are assigned to one/more companies; all data is strictly company-scoped.
- Company selection required at login; all APIs validate active company via `X-Company-Id`.

## Ledger + PDC Rules
- Labour/process charges post to expense parties' ledgers (party role: EXPENSE).
- Brokerage posts to broker ledger and does not reduce supplier ledger.
- Post-dated cheques appear in ledger statement but do not affect balance until cleared; on clearance, post ledger entries.

## TDS Automation Rules
- Maintain TDS section/rate/threshold by party or item category.
- Auto-calculate TDS on applicable invoices/payments and post to TDS payable ledger.
- Include TDS deductions in payment vouchers and in TDS report.

## Deductions/Charges/Brokerage
- Configurable deductions/charges master; selectable per purchase invoice.
- Each deduction line posts to configured ledger/party.
- Broker can be attached to purchases; commission calculated separately from supplier balance.

## Production/Purchase Notes
- Purchase accepts gunny/plastic bags; cutting paddy bags generates empty bag stock items.
- Packing finished goods consumes packing materials.
- By-products (dust/chaff/broken/husk/etc) captured as stock at each stage.
- Loads can be rejected or accepted with quality splits (e.g., 1st/2nd quality); rates can be finalized later at payment stage.
- Process flows exist per company: rice (Mothers Rice Mill), wheat (Mothers Agro Foods), rice flour (Mothers Food Products), spices (MAS Foods & Spices).

## Integrations (Pluggable)
- Sales Focus app, POIS machine, weighbridge, biomatrix attendance, fleet management.
- E-invoice and e-way bill integrations to be automation-ready after invoice creation.

## Reports
- Required templates in `docs/report-samples/` plus standard accounting and stock reports.
