# RFQ flow

## Statuses and supplier states

- RFQ: `DRAFT` → `SUBMITTED` → `PARTIALLY_AWARDED` / `AWARDED` → `CLOSED` (or `REJECTED`).
- Supplier invite on an RFQ: `DRAFT` / `SUBMITTED` while quotes are being prepared, then `AWARDED` for winners, and `CLOSED_NOT_AWARDED` when the RFQ is fully awarded without that supplier.

## Allowed actions by RFQ status

- **DRAFT**: edit header/lines/suppliers, save, submit.
- **SUBMITTED**: capture supplier quotes, submit supplier quotes, compare & award, or close/cancel RFQ.
- **PARTIALLY_AWARDED**: view existing awards/POs, award remaining quantities, or cancel remaining balance.
- **AWARDED**: view generated POs and close the RFQ.
- **CLOSED/REJECTED**: read-only.

## API touch points

- `POST /api/rfq` – create RFQ with header, lines, and supplier set.
- `PUT /api/rfq/{id}` – edit while `DRAFT`.
- `POST /api/rfq/{id}/submit` – move to `SUBMITTED`.
- `PUT /api/rfq/{id}/suppliers/{supplierId}/quotes` – save a supplier draft quote (qty/rate/delivery/remarks per line plus optional payment terms override).
- `POST /api/rfq/{id}/suppliers/{supplierId}/quotes/submit` – submit a supplier quote (`SUBMITTED`).
- `POST /api/rfq/{id}/award` – award payload (partial or full):

```json
{
  "supplierAwards": [
    {
      "supplierId": 10,
      "allocations": [
        { "rfqLineId": 101, "awardQty": 5, "awardRate": 120, "deliveryDate": "2024-08-01" },
        { "rfqLineId": 102, "awardQty": 2 }
      ]
    },
    {
      "supplierId": 12,
      "allocations": [
        { "rfqLineId": 101, "awardQty": 3, "awardRate": 118 }
      ]
    }
  ]
}
```

Validations: every awarded line must belong to the RFQ; suppliers must be invited and have a `SUBMITTED` quote; total awarded per line cannot exceed requested quantity. Award rates/dates default from submitted quotes when omitted. POs are created (or appended) per supplier/RFQ with awarded quantities and rates. Non-awarded suppliers are closed only when the RFQ is fully awarded.

- `POST /api/rfq/{id}/close` – close without awarding; requires a closure reason and marks non-awarded suppliers as `CLOSED_NOT_AWARDED`.

## Manual smoke checklist (happy path)

1. **Create RFQ** with two suppliers and at least one line (`POST /api/rfq`) → expect `201`/`200` and `status=DRAFT`.
2. **Submit RFQ** (`POST /api/rfq/{id}/submit`) → expect `status=SUBMITTED`.
3. **Capture supplier quotes** for both suppliers (`PUT /api/rfq/{id}/suppliers/{supplierId}/quotes` then submit) → expect supplier status `SUBMITTED`.
4. **Award partially** via `/api/rfq/{id}/award` with split allocations → expect `status=PARTIALLY_AWARDED`, supplier PO(s) created, remaining qty > 0.
5. **Award remaining** in a second call → expect `status=AWARDED`, non-winning suppliers `CLOSED_NOT_AWARDED`, PO ids returned.
6. **Close RFQ** (`POST /api/rfq/{id}/close`) → expect `status=CLOSED`, no new PO created.

Record the HTTP status and response snippets for each step to confirm persistence and workflow.
