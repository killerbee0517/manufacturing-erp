# Purchase arrival with charges & deductions

This flow links weighbridge unloading → GRN → purchase arrival postings.

1. **Weighbridge In**: create the first weight against a PO and vehicle using `/purchase/weighbridge-in/new`. After unloading, record the second weight on the edit screen. The unload call hits `PUT /api/weighbridge/tickets/{id}/unload`, which computes net weight and creates a draft GRN.
2. **GRN**: on `/purchase/grn/new`, selecting the weighbridge ticket auto-loads the PO, supplier, items, and first/second/net weights. Only the Godown is editable before confirming.
3. **Purchase Arrival**: `/purchase/arrival/new` lets you select the PO or weighbridge ticket. Add **Charges & Deductions** lines from the master. Each line supports flat/percent, amount override, deduction flag, payable party type (SUPPLIER/BROKER/VEHICLE/EXPENSE), payable party, and remarks.

## Ledgers & vouchers

When saving a purchase arrival:

* Base posting debits the Purchase ledger with the PO total.
* Unloading charges debit `Unloading Expense`; deductions credit `Purchase Deductions`; TDS credits `TDS Payable`.
* Charges/Deductions lines:
  * Amount derives from rate + calc type (percent uses PO total).
  * If `isDeduction` is true, the amount is **credited** to the payable party ledger; otherwise it is **debited**.
* Supplier ledger is credited with the net payable (gross + charges – deductions – TDS).
* Voucher remains balanced via `VoucherService`.

### Masters

* `deduction_charge_types`: configure code, name, default calc type/rate, enabled, and whether it is a deduction.
* `expense_parties`: generic payees (broker/vehicle/expense) with their own ledgers.

### Minimal manual test

1. Create a PO with lines.
2. Create weighbridge gross against the PO; unload with second weight.
3. Open GRN create, choose the weighbridge ticket, select Godown, and save/confirm.
4. Create a purchase arrival for the PO or ticket; add charge/deduction lines; save.
5. Check supplier/payee ledger balances via `/api/ledgers/{id}/balance` to see net payable and party postings.
