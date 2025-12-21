# Manufacturing ERP (Food Products)

Manufacturing ERP MVP for food products (paddy/rice/flour/maida) with Spring Boot + React (Berry dashboard).

## Repository Structure

```
/manufacturing-erp
  /backend
  /frontend
  docker-compose.yml
  README.md
```

## Prerequisites

- Docker + Docker Compose
- Java 21
- Maven 3.9+
- Node 18+

## Quick Start

### 1) Start DB + Backend (Docker)

```bash
docker compose up --build
```

Backend will be available at `http://localhost:8080`.

### 2) Run Backend locally (optional)

```bash
cd backend
mvn spring-boot:run
```

### 3) Run Frontend locally

```bash
cd frontend
npm install
npm run dev
```

Frontend will be available at `http://localhost:5173`.

## Frontend UI

The frontend uses the Berry free admin dashboard template with ERP-specific routes and placeholders.

### Environment variables

- `VITE_API_BASE_URL` (default: `http://localhost:8080`)
- `VITE_APP_BASE_NAME` (default: `/`)

Example:

```bash
cd frontend
cp .env.example .env
```

Open `http://localhost:5173` and sign in to access the sidebar navigation.

## Default Credentials

- Username: `admin`
- Password: `admin123`

## API Docs

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Seed Data

- Admin user
- Sample supplier + tax profile
- Sample TDS rule (Section 194Q)
- Sample item (Raw Paddy) + UOM KG
- Sample broker + commission rule
- Core locations: `QC_HOLD`, `UNRESTRICTED`

## Modules (MVP)

- Purchase: Weighbridge In, GRN, QC Inspection, Purchase Invoice (with TDS)
- Sales: Sales Invoice with broker commission
- Inventory: Stock ledger + stock transfer
- Masters: Suppliers, Items, Locations, Brokers, Users/Roles, TDS Rules
- Reports: TDS Register stub

## Frontend Structure

- Menu config: `frontend/src/menu-items/erp.js`
- Routes: `frontend/src/routes/MainRoutes.jsx`
- ERP placeholder page template: `frontend/src/views/erp/ModulePage.jsx`

## Testing

```bash
cd backend
mvn test
```

```bash
cd frontend
npm run build
```
