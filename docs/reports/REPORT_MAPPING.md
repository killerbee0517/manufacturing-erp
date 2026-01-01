# Report Mapping

Each report lists the template file, filter parameters, and placeholder source tables to be mapped during implementation.

## daily production chart (1).xlsx
- Filters: company, date, process, product, shift
- Source tables: production_batches, production_steps, stock_movements, items

## monthly production chart (1) (1) (1).xlsx
- Filters: company, month range, process, product
- Source tables: production_batches, production_steps, stock_movements, items

## Rice Daily Summary Report- 19.08.2023 (1) (1) (1).xlsx
- Filters: company, date
- Source tables: production_batches, production_steps, stock_movements, items, quality_grades

## Mothers Rice EXCEL PURCHASE STATEMENT (1) (1) (1).xlsx
- Filters: company, date range, supplier, broker
- Source tables: purchase_orders, weighbridge_entries, qc_results, grn, purchase_invoices, parties

## Purchase Report agro (1) (2) (1).xlsx
- Filters: company, date range, supplier, item
- Source tables: purchase_orders, grn, purchase_invoices, items, parties

## Mothers Agro partywise PURCHASE contract STATEMENT (1) (1) (1).xlsx
- Filters: company, date range, party
- Source tables: purchase_contracts, purchase_orders, parties

## REPORT SPICES AND AGRO (1) (1) (1).xlsx
- Filters: company, date range, item group
- Source tables: stock_movements, items, warehouses

## Foods report as on 14-09-2023 (2) (1) (1).xlsx
- Filters: company, as_on_date
- Source tables: stock_balances, items, warehouses

## summarised bank payment report of mothers group (1) (1) (2).xlsx
- Filters: company, date range, bank
- Source tables: payment_vouchers, ledger_entries, banks, parties

## TDS REPORT (1) (1) (1).xlsx
- Filters: company, date range, party, tds_section
- Source tables: tds_deductions, ledger_entries, parties
