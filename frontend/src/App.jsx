import React from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import DefaultLayout from './layout/DefaultLayout.jsx'
import LoginPage from './pages/LoginPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import PlaceholderPage from './pages/PlaceholderPage.jsx'

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token')
  if (!token) {
    return <Navigate to="/login" replace />
  }
  return children
}

const createPlaceholder = (title, subtitle, description, formFields, tableColumns) => (
  <PlaceholderPage
    title={title}
    subtitle={subtitle}
    description={description}
    formFields={formFields}
    tableColumns={tableColumns}
  />
)

const App = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route
      path="/"
      element={
        <ProtectedRoute>
          <DefaultLayout />
        </ProtectedRoute>
      }
    >
      <Route index element={<Navigate to="dashboard" replace />} />
      <Route path="dashboard" element={<DashboardPage />} />

      <Route
        path="masters/suppliers"
        element={createPlaceholder(
          'Suppliers',
          'Masters',
          'Manage supplier onboarding, compliance, and pricing matrices.',
          [
            { label: 'Supplier Name', placeholder: 'ABC Agro Pvt Ltd' },
            { label: 'GSTIN', placeholder: '22AAAAA0000A1Z5' },
            { label: 'Status', type: 'select', options: ['Active', 'Inactive'] }
          ],
          ['Supplier', 'Location', 'Status']
        )}
      />
      <Route
        path="masters/items"
        element={createPlaceholder(
          'Items',
          'Masters',
          'Maintain raw material and finished goods masters.',
          [
            { label: 'Item Name', placeholder: 'Raw Paddy' },
            { label: 'Category', placeholder: 'Raw Material' },
            { label: 'UOM', type: 'select', options: ['KG', 'MT', 'Bag'] }
          ],
          ['Item', 'Category', 'Status']
        )}
      />
      <Route
        path="masters/locations"
        element={createPlaceholder(
          'Locations',
          'Masters',
          'Configure warehouse and yard locations for stock tracking.',
          [
            { label: 'Location Code', placeholder: 'UNRESTRICTED' },
            { label: 'Location Name', placeholder: 'Main Warehouse' },
            { label: 'Type', type: 'select', options: ['Storage', 'QC', 'Transit'] }
          ],
          ['Location', 'Type', 'Status']
        )}
      />
      <Route
        path="masters/vehicles"
        element={createPlaceholder(
          'Vehicles',
          'Masters',
          'Keep a registry of vehicle and transporter details.',
          [
            { label: 'Vehicle Number', placeholder: 'MH12AB1234' },
            { label: 'Transporter', placeholder: 'Swift Logistics' },
            { label: 'Capacity', placeholder: '20 MT' }
          ],
          ['Vehicle', 'Transporter', 'Status']
        )}
      />
      <Route
        path="masters/brokers"
        element={createPlaceholder(
          'Brokers',
          'Masters',
          'Manage broker commission slabs and agreements.',
          [
            { label: 'Broker Name', placeholder: 'Nova Brokers' },
            { label: 'Commission %', placeholder: '1.5' },
            { label: 'Status', type: 'select', options: ['Active', 'Inactive'] }
          ],
          ['Broker', 'Commission', 'Status']
        )}
      />
      <Route
        path="masters/users-roles"
        element={createPlaceholder(
          'Users & Roles',
          'Masters',
          'Provision user access, roles, and approval workflows.',
          [
            { label: 'User Name', placeholder: 'Operations Manager' },
            { label: 'Role', type: 'select', options: ['Admin', 'Supervisor', 'Clerk'] },
            { label: 'Department', placeholder: 'Procurement' }
          ],
          ['User', 'Role', 'Status']
        )}
      />
      <Route
        path="masters/tds-rules"
        element={createPlaceholder(
          'TDS Rules',
          'Masters',
          'Configure tax deduction rules and thresholds.',
          [
            { label: 'Section', placeholder: '194Q' },
            { label: 'Threshold', placeholder: '5000000' },
            { label: 'Rate (%)', placeholder: '0.1' }
          ],
          ['Section', 'Rate', 'Status']
        )}
      />

      <Route
        path="purchase/rfq"
        element={createPlaceholder(
          'Request for Quotation',
          'Purchase',
          'Issue RFQs and compare supplier quotes.',
          [
            { label: 'RFQ Number', placeholder: 'RFQ-2024-001' },
            { label: 'Supplier', placeholder: 'ABC Agro Pvt Ltd' },
            { label: 'Due Date', placeholder: '2024-08-15' }
          ],
          ['RFQ', 'Supplier', 'Status']
        )}
      />
      <Route
        path="purchase/po"
        element={createPlaceholder(
          'Purchase Orders',
          'Purchase',
          'Create and monitor purchase orders.',
          [
            { label: 'PO Number', placeholder: 'PO-2024-014' },
            { label: 'Supplier', placeholder: 'ABC Agro Pvt Ltd' },
            { label: 'Value', placeholder: '₹12,50,000' }
          ],
          ['PO', 'Supplier', 'Status']
        )}
      />
      <Route
        path="purchase/weighbridge-in"
        element={createPlaceholder(
          'Weighbridge In',
          'Purchase',
          'Record inbound vehicle weighments and lot details.',
          [
            { label: 'Ticket ID', placeholder: 'WB-IN-0043' },
            { label: 'Vehicle', placeholder: 'MH12AB1234' },
            { label: 'Net Weight', placeholder: '18.6 MT' }
          ],
          ['Ticket', 'Vehicle', 'Status']
        )}
      />
      <Route
        path="purchase/grn"
        element={createPlaceholder(
          'Goods Receipt Note',
          'Purchase',
          'Log GRN quantities and warehouse receipts.',
          [
            { label: 'GRN Number', placeholder: 'GRN-2024-021' },
            { label: 'PO Reference', placeholder: 'PO-2024-014' },
            { label: 'Received Qty', placeholder: '120 MT' }
          ],
          ['GRN', 'PO', 'Status']
        )}
      />
      <Route
        path="purchase/qc"
        element={createPlaceholder(
          'Quality Check',
          'Purchase',
          'Capture QC parameters and approvals.',
          [
            { label: 'QC Lot', placeholder: 'QC-LOT-008' },
            { label: 'Inspector', placeholder: 'R. Sharma' },
            { label: 'Moisture %', placeholder: '11.2' }
          ],
          ['QC Lot', 'Inspector', 'Status']
        )}
      />
      <Route
        path="purchase/purchase-invoice"
        element={createPlaceholder(
          'Purchase Invoice',
          'Purchase',
          'Match supplier invoices with GRN and QC.',
          [
            { label: 'Invoice Number', placeholder: 'PI-2024-112' },
            { label: 'Supplier', placeholder: 'ABC Agro Pvt Ltd' },
            { label: 'Invoice Value', placeholder: '₹13,20,000' }
          ],
          ['Invoice', 'Supplier', 'Status']
        )}
      />
      <Route
        path="purchase/debit-note"
        element={createPlaceholder(
          'Debit Note',
          'Purchase',
          'Issue debit notes for quality or quantity variance.',
          [
            { label: 'Debit Note #', placeholder: 'DN-2024-003' },
            { label: 'Reference Invoice', placeholder: 'PI-2024-112' },
            { label: 'Amount', placeholder: '₹25,000' }
          ],
          ['Debit Note', 'Invoice', 'Status']
        )}
      />

      <Route
        path="sales/so"
        element={createPlaceholder(
          'Sales Orders',
          'Sales',
          'Create and track customer orders.',
          [
            { label: 'SO Number', placeholder: 'SO-2024-031' },
            { label: 'Customer', placeholder: 'City Foods Ltd' },
            { label: 'Order Value', placeholder: '₹18,00,000' }
          ],
          ['SO', 'Customer', 'Status']
        )}
      />
      <Route
        path="sales/weighbridge-out"
        element={createPlaceholder(
          'Weighbridge Out',
          'Sales',
          'Record outbound weighments for dispatch.',
          [
            { label: 'Ticket ID', placeholder: 'WB-OUT-0074' },
            { label: 'Vehicle', placeholder: 'MH12AB3456' },
            { label: 'Net Weight', placeholder: '24.3 MT' }
          ],
          ['Ticket', 'Vehicle', 'Status']
        )}
      />
      <Route
        path="sales/delivery"
        element={createPlaceholder(
          'Delivery',
          'Sales',
          'Schedule deliveries and generate delivery notes.',
          [
            { label: 'Delivery Note', placeholder: 'DN-2024-045' },
            { label: 'SO Reference', placeholder: 'SO-2024-031' },
            { label: 'Dispatch Date', placeholder: '2024-08-20' }
          ],
          ['Delivery', 'Customer', 'Status']
        )}
      />
      <Route
        path="sales/sales-invoice"
        element={createPlaceholder(
          'Sales Invoice',
          'Sales',
          'Generate invoices and capture broker commissions.',
          [
            { label: 'Invoice Number', placeholder: 'SI-2024-090' },
            { label: 'Customer', placeholder: 'City Foods Ltd' },
            { label: 'Invoice Value', placeholder: '₹18,00,000' }
          ],
          ['Invoice', 'Customer', 'Status']
        )}
      />

      <Route
        path="inventory/stock-on-hand"
        element={createPlaceholder(
          'Stock On Hand',
          'Inventory',
          'Monitor live stock across locations.',
          [
            { label: 'Location', placeholder: 'Main Warehouse' },
            { label: 'Item', placeholder: 'Raw Paddy' },
            { label: 'Quantity', placeholder: '320 MT' }
          ],
          ['Item', 'Location', 'Status']
        )}
      />
      <Route
        path="inventory/stock-ledger"
        element={createPlaceholder(
          'Stock Ledger',
          'Inventory',
          'Review stock movements and batch balances.',
          [
            { label: 'Ledger Date', placeholder: '2024-08-01' },
            { label: 'Item', placeholder: 'Raw Paddy' },
            { label: 'Balance Qty', placeholder: '320 MT' }
          ],
          ['Batch', 'Movement', 'Status']
        )}
      />
      <Route
        path="inventory/stock-transfer"
        element={createPlaceholder(
          'Stock Transfer',
          'Inventory',
          'Move stock between locations with approval tracking.',
          [
            { label: 'From Location', placeholder: 'QC_HOLD' },
            { label: 'To Location', placeholder: 'UNRESTRICTED' },
            { label: 'Quantity', placeholder: '12 MT' }
          ],
          ['Transfer ID', 'Route', 'Status']
        )}
      />

      <Route
        path="reports/tds"
        element={createPlaceholder(
          'TDS Register',
          'Reports',
          'Reporting stub for TDS compliance views.',
          [
            { label: 'Period', placeholder: 'Apr 2024 - Jun 2024' },
            { label: 'Section', placeholder: '194Q' },
            { label: 'Export Format', type: 'select', options: ['CSV', 'XLSX'] }
          ],
          ['Section', 'Vendor', 'Status']
        )}
      />

      <Route path="*" element={<Navigate to="dashboard" replace />} />
    </Route>
  </Routes>
)

export default App
