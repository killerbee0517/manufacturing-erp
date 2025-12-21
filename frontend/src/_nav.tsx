import React from 'react'
import CIcon from '@coreui/icons-react'
import {
  cilSpeedometer,
  cilFactory,
  cilUser,
  cilCart,
  cilClipboard,
  cilSwapHorizontal,
  cilStorage,
  cilNotes,
  cilSpreadsheet
} from '@coreui/icons'
import { CNavGroup, CNavItem, CNavTitle } from '@coreui/react'

const nav = [
  {
    component: CNavItem,
    name: 'Dashboard',
    to: '/dashboard',
    end: true,
    icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />
  },
  {
    component: CNavTitle,
    name: 'Masters'
  },
  {
    component: CNavGroup,
    name: 'Masters',
    icon: <CIcon icon={cilFactory} customClassName="nav-icon" />,
    items: [
      {
        component: CNavItem,
        name: 'Suppliers',
        to: '/masters/suppliers'
      },
      {
        component: CNavItem,
        name: 'Items',
        to: '/masters/items'
      },
      {
        component: CNavItem,
        name: 'Locations',
        to: '/masters/locations'
      },
      {
        component: CNavItem,
        name: 'Vehicles',
        to: '/masters/vehicles'
      },
      {
        component: CNavItem,
        name: 'Brokers',
        to: '/masters/brokers'
      },
      {
        component: CNavItem,
        name: 'Users / Roles',
        to: '/masters/users-roles'
      },
      {
        component: CNavItem,
        name: 'TDS Rules',
        to: '/masters/tds-rules'
      }
    ]
  },
  {
    component: CNavTitle,
    name: 'Purchase'
  },
  {
    component: CNavGroup,
    name: 'Purchase',
    icon: <CIcon icon={cilCart} customClassName="nav-icon" />,
    items: [
      {
        component: CNavItem,
        name: 'RFQ',
        to: '/purchase/rfq'
      },
      {
        component: CNavItem,
        name: 'Purchase Orders',
        to: '/purchase/po'
      },
      {
        component: CNavItem,
        name: 'Weighbridge In',
        to: '/purchase/weighbridge-in'
      },
      {
        component: CNavItem,
        name: 'GRN',
        to: '/purchase/grn'
      },
      {
        component: CNavItem,
        name: 'QC',
        to: '/purchase/qc'
      },
      {
        component: CNavItem,
        name: 'Purchase Invoice',
        to: '/purchase/purchase-invoice'
      },
      {
        component: CNavItem,
        name: 'Debit Note',
        to: '/purchase/debit-note'
      }
    ]
  },
  {
    component: CNavTitle,
    name: 'Sales'
  },
  {
    component: CNavGroup,
    name: 'Sales',
    icon: <CIcon icon={cilClipboard} customClassName="nav-icon" />,
    items: [
      {
        component: CNavItem,
        name: 'Sales Orders',
        to: '/sales/so'
      },
      {
        component: CNavItem,
        name: 'Weighbridge Out',
        to: '/sales/weighbridge-out'
      },
      {
        component: CNavItem,
        name: 'Delivery',
        to: '/sales/delivery'
      },
      {
        component: CNavItem,
        name: 'Sales Invoice',
        to: '/sales/sales-invoice'
      }
    ]
  },
  {
    component: CNavTitle,
    name: 'Inventory'
  },
  {
    component: CNavGroup,
    name: 'Inventory',
    icon: <CIcon icon={cilStorage} customClassName="nav-icon" />,
    items: [
      {
        component: CNavItem,
        name: 'Stock On Hand',
        to: '/inventory/stock-on-hand'
      },
      {
        component: CNavItem,
        name: 'Stock Ledger',
        to: '/inventory/stock-ledger'
      },
      {
        component: CNavItem,
        name: 'Stock Transfer',
        to: '/inventory/stock-transfer'
      }
    ]
  },
  {
    component: CNavTitle,
    name: 'Quick Links'
  },
  {
    component: CNavItem,
    name: 'Users & Roles',
    to: '/masters/users-roles',
    icon: <CIcon icon={cilUser} customClassName="nav-icon" />
  },
  {
    component: CNavItem,
    name: 'TDS Rules',
    to: '/masters/tds-rules',
    icon: <CIcon icon={cilNotes} customClassName="nav-icon" />
  },
  {
    component: CNavItem,
    name: 'Stock Transfer',
    to: '/inventory/stock-transfer',
    icon: <CIcon icon={cilSwapHorizontal} customClassName="nav-icon" />
  },
  {
    component: CNavItem,
    name: 'Reports (Stub)',
    to: '/reports/tds',
    icon: <CIcon icon={cilSpreadsheet} customClassName="nav-icon" />
  }
]

export default nav
