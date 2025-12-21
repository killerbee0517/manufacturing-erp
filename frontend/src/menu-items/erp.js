// assets
import {
  IconDashboard,
  IconUsers,
  IconShoppingCart,
  IconFileInvoice,
  IconPackage,
  IconReportAnalytics,
  IconSettings
} from '@tabler/icons-react';

// constant
const icons = {
  IconDashboard,
  IconUsers,
  IconShoppingCart,
  IconFileInvoice,
  IconPackage,
  IconReportAnalytics,
  IconSettings
};

// ==============================|| ERP MENU ITEMS ||============================== //

const erp = {
  id: 'erp',
  title: 'ERP',
  type: 'group',
  children: [
    {
      id: 'dashboard',
      title: 'Dashboard',
      type: 'item',
      url: '/dashboard',
      icon: icons.IconDashboard,
      breadcrumbs: false
    },
    {
      id: 'masters',
      title: 'Masters',
      type: 'collapse',
      icon: icons.IconUsers,
      children: [
        { id: 'masters-suppliers', title: 'Suppliers', type: 'item', url: '/masters/suppliers' },
        { id: 'masters-items', title: 'Items', type: 'item', url: '/masters/items' },
        { id: 'masters-locations', title: 'Locations (Godown/Storage)', type: 'item', url: '/masters/locations' },
        { id: 'masters-vehicles', title: 'Vehicles', type: 'item', url: '/masters/vehicles' },
        { id: 'masters-customers', title: 'Customers', type: 'item', url: '/masters/customers' },
        { id: 'masters-brokers', title: 'Brokers & Commission', type: 'item', url: '/masters/brokers' },
        { id: 'masters-users', title: 'Users & Roles', type: 'item', url: '/masters/users' },
        { id: 'masters-tds', title: 'TDS Rules', type: 'item', url: '/masters/tds-rules' }
      ]
    },
    {
      id: 'purchase',
      title: 'Purchase',
      type: 'collapse',
      icon: icons.IconShoppingCart,
      children: [
        { id: 'purchase-rfq', title: 'RFQ', type: 'item', url: '/purchase/rfq' },
        { id: 'purchase-po', title: 'Purchase Orders', type: 'item', url: '/purchase/po' },
        { id: 'purchase-weighbridge-in', title: 'Weighbridge In', type: 'item', url: '/purchase/weighbridge-in' },
        { id: 'purchase-grn', title: 'GRN', type: 'item', url: '/purchase/grn' },
        { id: 'purchase-qc', title: 'QC Inspection', type: 'item', url: '/purchase/qc' },
        { id: 'purchase-invoice', title: 'Purchase Invoice', type: 'item', url: '/purchase/purchase-invoice' },
        { id: 'purchase-debit', title: 'Debit Note', type: 'item', url: '/purchase/debit-note' }
      ]
    },
    {
      id: 'sales',
      title: 'Sales',
      type: 'collapse',
      icon: icons.IconFileInvoice,
      children: [
        { id: 'sales-so', title: 'Sales Orders', type: 'item', url: '/sales/so' },
        { id: 'sales-weighbridge-out', title: 'Weighbridge Out', type: 'item', url: '/sales/weighbridge-out' },
        { id: 'sales-delivery', title: 'Delivery', type: 'item', url: '/sales/delivery' },
        { id: 'sales-invoice', title: 'Sales Invoice', type: 'item', url: '/sales/sales-invoice' }
      ]
    },
    {
      id: 'inventory',
      title: 'Inventory',
      type: 'collapse',
      icon: icons.IconPackage,
      children: [
        { id: 'inventory-stock-on-hand', title: 'Stock On Hand', type: 'item', url: '/inventory/stock-on-hand' },
        { id: 'inventory-stock-ledger', title: 'Stock Ledger', type: 'item', url: '/inventory/stock-ledger' },
        { id: 'inventory-stock-transfer', title: 'Stock Transfer', type: 'item', url: '/inventory/stock-transfer' }
      ]
    },
    {
      id: 'reports',
      title: 'Reports',
      type: 'item',
      url: '/reports',
      icon: icons.IconReportAnalytics
    },
    {
      id: 'settings',
      title: 'Settings',
      type: 'item',
      url: '/settings',
      icon: icons.IconSettings
    }
  ]
};

export default erp;
