## UI Audit - manufacturing-erp

### Run results (local)
- Frontend: `npm run dev` started Vite on `http://localhost:3001/` because `3000` was in use. No runtime error logged.
- Backend: `mvn spring-boot:run` failed to start because port `8080` was already in use.

### Tech stack
- Frontend: React (Vite) + Berry (MUI) + React Router + Axios
- Backend: Spring Boot 3.2 + MariaDB + Flyway

### Route inventory
| Route | Component | Module | Behavior |
| --- | --- | --- | --- |
| /login | frontend/src/views/pages/authentication/Login.jsx | Auth | Login only |
| /dashboard | frontend/src/views/dashboard/Default/index.jsx | Dashboard | View-only (metrics via API) |
| /masters/suppliers | frontend/src/views/erp/ModulePage.jsx | Masters | Partial (List + Create) |
| /masters/items | frontend/src/views/erp/ModulePage.jsx | Masters | Partial (List + Create) |
| /masters/locations | frontend/src/views/erp/ModulePage.jsx | Masters | Partial (List + Create) |
| /masters/vehicles | frontend/src/views/erp/ModulePage.jsx | Masters | Partial (List + Create) |
| /masters/customers | frontend/src/views/erp/ModulePage.jsx | Masters | Partial (List + Create) |
| /masters/brokers | frontend/src/views/erp/ModulePage.jsx | Masters | Partial (List + Create) |
| /masters/users | frontend/src/views/erp/ModulePage.jsx | Masters | Partial (List + Create) |
| /masters/tds-rules | frontend/src/views/erp/ModulePage.jsx | Masters | Partial (List + Create) |
| /purchase/rfq | frontend/src/views/erp/ModulePage.jsx | Purchase | Partial (List + Create) |
| /purchase/po | frontend/src/views/erp/ModulePage.jsx | Purchase | Partial (List + Create) |
| /purchase/weighbridge-in | frontend/src/views/erp/ModulePage.jsx | Purchase | Partial (List + Create) |
| /purchase/grn | frontend/src/views/erp/ModulePage.jsx | Purchase | Partial (List + Create) |
| /purchase/qc | frontend/src/views/erp/ModulePage.jsx | Purchase | Partial (List + Create) |
| /purchase/purchase-invoice | frontend/src/views/erp/ModulePage.jsx | Purchase | Partial (List + Create) |
| /purchase/debit-note | frontend/src/views/erp/ModulePage.jsx | Purchase | Partial (List + Create) |
| /sales/so | frontend/src/views/erp/ModulePage.jsx | Sales | Partial (List + Create) |
| /sales/weighbridge-out | frontend/src/views/erp/ModulePage.jsx | Sales | Partial (List + Create) |
| /sales/delivery | frontend/src/views/erp/ModulePage.jsx | Sales | Partial (List + Create) |
| /sales/sales-invoice | frontend/src/views/erp/ModulePage.jsx | Sales | Partial (List + Create) |
| /inventory/stock-on-hand | frontend/src/views/erp/ModulePage.jsx | Inventory | View-only (List) |
| /inventory/stock-ledger | frontend/src/views/erp/ModulePage.jsx | Inventory | View-only (List) |
| /inventory/stock-transfer | frontend/src/views/erp/ModulePage.jsx | Inventory | Partial (List + Create) |
| /reports | frontend/src/views/erp/ModulePage.jsx | Reports | View-only (no API) |
| /settings | frontend/src/views/erp/ModulePage.jsx | Settings | View-only (no API) |

### RFQ deep check
Status: List ✅, View ❌, Create ✅, Edit ❌, Submit/Approve ❌
- List: wired to GET `/api/rfq` via `frontend/src/views/erp/moduleConfig.js` and `frontend/src/views/erp/ModulePage.jsx`.
- View: no route or component; missing `GET /api/rfq/{id}` and UI link.
- Create: form fields exist in `frontend/src/views/erp/moduleConfig.js` and POST `/api/rfq` works.
- Edit: no route, no edit form, no PUT/PATCH endpoint.
- Submit/Approve: no UI action or backend workflow endpoints.
- Filters/search: UI fields exist but are not wired to API or local filtering.

### Purchase Order deep check
Status: List ✅, View ❌, Create ✅ (header only), Edit ❌, Workflow ❌
- List: wired to GET `/api/purchase-orders`.
- View: no route or component; missing `GET /api/purchase-orders/{id}`.
- Create: form captures header only (poNo, supplierId, status). No line items.
- Edit: no route, no edit form, no PUT/PATCH endpoint.
- Workflow: no approve/print/receive actions in UI or backend endpoints.
- Data model: `po_lines` table exists, but there are no DTOs/controllers to create lines.

### API wiring audit
Frontend API usage (`frontend/src/views/erp/ModulePage.jsx`, `frontend/src/views/dashboard/Default/index.jsx`, `frontend/src/views/pages/auth-forms/AuthLogin.jsx`):
- POST `/api/auth/login`
- GET `/actuator/health`
- GET `/api/metrics/dashboard`
- GET/POST per module config:
  - `/api/suppliers`, `/api/items`, `/api/locations`, `/api/vehicles`, `/api/customers`, `/api/brokers`
  - `/api/users`, `/api/tds-rules`, `/api/uoms`, `/api/roles`
  - `/api/rfq`, `/api/purchase-orders`
  - `/api/weighbridge/tickets`, `/api/grn`, `/api/qc/inspections`, `/api/purchase-invoices`, `/api/debit-notes`
  - `/api/sales-orders`, `/api/deliveries`, `/api/sales-invoices`
  - `/api/stock-ledger`, `/api/stock-transfers`

Backend endpoints confirmed (Spring controllers in `backend/src/main/java/com/manufacturing/erp/controller`):
- List + Create only for RFQ and Purchase Orders.
- No GET by id, PUT/PATCH, or workflow actions for RFQ/PO.
- No RFQ lines/quotes endpoints; no PO lines endpoints.

Required API contracts (missing)
- RFQ
  - `GET /api/rfq/{id}` -> RFQ header + lines + vendor quotes
  - `PUT /api/rfq/{id}` -> update header + lines
  - `POST /api/rfq/{id}/submit` -> status transition to SUBMITTED
  - `POST /api/rfq/{id}/approve` -> status transition to APPROVED
  - DTOs for `rfq_lines` and `rfq_vendor_quotes`
- Purchase Orders
  - `GET /api/purchase-orders/{id}` -> header + lines
  - `PUT /api/purchase-orders/{id}` -> update header + lines
  - `POST /api/purchase-orders/{id}/approve` (optional workflow)
  - DTOs for `po_lines`

### Missing UI actions checklist
- RFQ: no view details, no edit, no submit/approve buttons.
- PO: no view details, no edit, no line items grid, no workflow actions.
- Filters/search on list pages are not wired to API or local state.
- Dashboard KPIs depend on `/api/metrics/dashboard`; backend must be running to show real values.

### Missing API checklist
- RFQ: missing view/update/workflow/lines endpoints and DTOs.
- PO: missing view/update/workflow/line items endpoints and DTOs.

### Run instructions
1) Frontend: `cd frontend && npm install && npm run dev`
2) Backend: `cd backend && mvn spring-boot:run` (ensure port `8080` is free)
