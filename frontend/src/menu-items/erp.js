// assets
import {
  IconDashboard,
  IconUsers,
  IconShoppingCart,
  IconFileInvoice,
  IconPackage,
  IconReportAnalytics,
  IconBuildingFactory2,
  IconClipboardList,
  IconBuildingWarehouse
} from '@tabler/icons-react';

// constant
const icons = {
  IconDashboard,
  IconUsers,
  IconShoppingCart,
  IconFileInvoice,
  IconPackage,
  IconReportAnalytics,
  IconBuildingFactory2,
  IconClipboardList,
  IconBuildingWarehouse
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
      icon: icons.IconBuildingWarehouse,
      roles: ['ADMIN', 'HEAD'],
      children: [
        { id: 'masters-parties', title: 'Party Master', type: 'item', url: '/masters/parties' },
        { id: 'masters-companies', title: 'Company Master', type: 'item', url: '/masters/companies' },
        { id: 'masters-banks', title: 'Bank Details', type: 'item', url: '/masters/banks' },
        { id: 'masters-vehicles', title: 'Vehicle Management', type: 'item', url: '/masters/vehicles' },
        { id: 'masters-godowns', title: 'Godown Management', type: 'item', url: '/masters/godowns' },
        { id: 'masters-items', title: 'Item Master', type: 'item', url: '/masters/items' },
        { id: 'masters-uoms', title: 'UOM Master', type: 'item', url: '/masters/uoms' },
        { id: 'masters-charge-types', title: 'Charges & Deductions', type: 'item', url: '/masters/charge-types' },
        { id: 'admin-users', title: 'User Management', type: 'item', url: '/admin/users', roles: ['ADMIN'] },
        { id: 'admin-roles', title: 'Role Management', type: 'item', url: '/admin/roles', roles: ['ADMIN'] }
      ]
    },
    {
      id: 'purchase',
      title: 'Purchase',
      type: 'collapse',
      icon: icons.IconShoppingCart,
      roles: ['ADMIN', 'HEAD', 'PURCHASE', 'STORE', 'QC', 'FINANCE'],
      children: [
        { id: 'purchase-rfq', title: 'RFQ', type: 'item', url: '/purchase/rfq' },
        { id: 'purchase-po', title: 'Purchase Order', type: 'item', url: '/purchase/po' },
        { id: 'purchase-weighbridge', title: 'Weighbridge In', type: 'item', url: '/purchase/weighbridge-in' },
        { id: 'purchase-qc', title: 'QC Inspection', type: 'item', url: '/purchase/qc' },
        { id: 'purchase-grn', title: 'GRN', type: 'item', url: '/purchase/grn' },
        { id: 'purchase-arrival', title: 'Purchase Arrival', type: 'item', url: '/purchase/arrival' },
        { id: 'purchase-invoice', title: 'Purchase Invoice', type: 'item', url: '/purchase/purchase-invoice' },
        { id: 'purchase-debit', title: 'Debit Note', type: 'item', url: '/purchase/debit-note' }
      ]
    },
    {
      id: 'inventory',
      title: 'Inventory',
      type: 'collapse',
      icon: icons.IconPackage,
      roles: ['ADMIN', 'HEAD', 'STORE', 'PRODUCTION', 'PURCHASE'],
      children: [
        { id: 'inventory-stock-on-hand', title: 'Stock On Hand', type: 'item', url: '/inventory/stock-on-hand' },
        { id: 'inventory-stock-transfer', title: 'Stock Transfer', type: 'item', url: '/inventory/stock-transfer' },
        { id: 'inventory-stock-ledger', title: 'Stock Ledger', type: 'item', url: '/inventory/stock-ledger' }
      ]
    },
    {
      id: 'sales',
      title: 'Sales',
      type: 'collapse',
      icon: icons.IconFileInvoice,
      roles: ['ADMIN', 'HEAD', 'SALES'],
      children: [
        { id: 'sales-order', title: 'Sales Order', type: 'item', url: '/sales/sales-order' },
        { id: 'sales-attendance', title: 'Sales Attendance', type: 'item', url: '/sales/attendance' },
        { id: 'sales-delivery', title: 'Delivery Note', type: 'item', url: '/sales/delivery-note' },
        { id: 'sales-tax-invoice', title: 'Tax Invoice', type: 'item', url: '/sales/tax-invoice' },
        { id: 'sales-credit-note', title: 'Credit Note', type: 'item', url: '/sales/credit-note' },
        { id: 'sales-receipt', title: 'Receipt', type: 'item', url: '/sales/receipt' }
      ]
    },
    {
      id: 'production',
      title: 'Production',
      type: 'collapse',
      icon: icons.IconBuildingFactory2,
      roles: ['ADMIN', 'HEAD', 'PRODUCTION'],
      children: [
        { id: 'production-templates', title: 'Process Templates', type: 'item', url: '/production/templates' },
        { id: 'production-batches', title: 'Batches', type: 'item', url: '/production/batches' },
        { id: 'production-runs', title: 'Production Runs', type: 'item', url: '/production/runs' },
        { id: 'production-wip', title: 'WIP Stock', type: 'item', url: '/production/wip-stock' },
        { id: 'production-cost-summary', title: 'Cost Summary', type: 'item', url: '/production/cost-summary' }
      ]
    },
    {
      id: 'accounts',
      title: 'Accounts',
      type: 'collapse',
      icon: icons.IconReportAnalytics,
      roles: ['ADMIN', 'HEAD', 'FINANCE'],
      children: [
        { id: 'accounts-ledgers', title: 'Ledgers', type: 'item', url: '/accounts/ledgers' },
        { id: 'accounts-payments', title: 'Payments', type: 'item', url: '/accounts/payments' }
      ]
    },
    {
      id: 'reports',
      title: 'Reports',
      type: 'collapse',
      icon: icons.IconReportAnalytics,
      roles: ['ADMIN', 'HEAD', 'FINANCE', 'VIEWER'],
      children: [
        { id: 'reports-ledger', title: 'Ledger', type: 'item', url: '/reports/ledger' },
        { id: 'reports-outstanding', title: 'Outstanding', type: 'item', url: '/reports/outstanding' },
        { id: 'reports-ageing', title: 'Ageing', type: 'item', url: '/reports/ageing' },
        { id: 'reports-payments', title: 'Payment Reports', type: 'item', url: '/reports/payments' },
        { id: 'reports-module', title: 'Reports Module', type: 'item', url: '/reports/module' }
      ]
    }
  ]
};

export default erp;
