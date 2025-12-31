# Multi-company Party & Ledger Model

This module introduces company-scoped masters with a unified Party model so the same entity can act as a supplier, customer, or broker.

## Core tables

- `companies`: parent/subsidiary hierarchy.
- `user_companies`: maps users to companies with an optional primary company.
- `banks`: one bank per company; bank ledger accounts are auto-created.
- `parties`: shared master for suppliers/customers/brokers with a single `party_code` per company.
- `party_roles`: assigns one or more roles to a party with role-specific settings (credit period, broker commission config).
- `ledger_accounts` and `ledger_entries`: lightweight, company-scoped ledgers for parties and banks.
- `pdc_register`: post-dated cheque register keyed by company with links to party, role type, and bank ledger.

## API highlights

- `X-Company-Id` header is required for authenticated ERP endpoints and is validated against `user_companies`.
- `/api/companies/my` returns the companies available to the logged-in user for UI selection.
- `/api/banks` and `/api/parties` are company-scoped, support pagination/search, and expose `/autocomplete` endpoints for fast dropdowns.

## Frontend behavior

- After login, the UI fetches `/api/companies/my`; if multiple options exist it routes to **/select-company** so the user can choose the active company.
- The selected company ID is stored locally and sent on every request via the `X-Company-Id` header.

## Ledger & PDC notes

- Party role creation auto-provisions ledger accounts per role (supplier/customer/broker); banks auto-provision bank ledger accounts.
- `ledger_entries` are available for future postings (e.g., brokerage, PDC clearances); balances can be derived from `debit - credit` per account.
- `pdc_register` captures PAYABLE/RECEIVABLE flows per party-role; ledger posting on clearance can debit/credit the linked party/bank accounts.
