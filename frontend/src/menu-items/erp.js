// assets
import {
  IconDashboard,
  IconUsers,
  IconShoppingCart,
  IconFileInvoice,
  IconPackage,
  IconReportAnalytics,
  IconSettings,
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
  IconSettings,
  IconBuildingFactory2,
  IconClipboardList,
  IconBuildingWarehouse
};

// ==============================|| ERP MENU ITEMS ||============================== //

const devToolsItem = import.meta.env.DEV
  ? {
      id: 'dev-tools',
      title: 'Dev Tools',
      type: 'item',
      url: '/dev-tools',
      icon: icons.IconClipboardList
    }
  : null;

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
      children: [
        { id: 'masters-suppliers', title: 'Supplier Management', type: 'item', url: '/masters/suppliers' },
        { id: 'masters-customers', title: 'Customer Management', type: 'item', url: '/masters/customers' },
        { id: 'masters-banks', title: 'Bank Details', type: 'item', url: '/masters/banks' },
        { id: 'masters-vehicles', title: 'Vehicle Management', type: 'item', url: '/masters/vehicles' },
        { id: 'masters-godowns', title: 'Godown Management', type: 'item', url: '/masters/godowns' },
        { id: 'masters-items', title: 'Item Master', type: 'item', url: '/masters/items' },
        { id: 'masters-uoms', title: 'UOM Master', type: 'item', url: '/masters/uoms' },
        { id: 'masters-brokers', title: 'Broker Master', type: 'item', url: '/masters/brokers' },
        { id: 'masters-charge-types', title: 'Charges & Deductions', type: 'item', url: '/masters/charge-types' },
        { id: 'masters-expense-parties', title: 'Payee Parties', type: 'item', url: '/masters/expense-parties' },
        { id: 'admin-users', title: 'User Management', type: 'item', url: '/admin/users' },
        { id: 'admin-roles', title: 'Role Management', type: 'item', url: '/admin/roles' }
      ]
    },
    {
      id: 'purchase',
      title: 'Purchase',
      type: 'collapse',
      icon: icons.IconShoppingCart,
      children: [
        { id: 'purchase-rfq', title: 'RFQ', type: 'item', url: '/purchase/rfq' },
        { id: 'purchase-po', title: 'Purchase Order', type: 'item', url: '/purchase/po' },
        { id: 'purchase-weighbridge', title: 'Weighbridge In', type: 'item', url: '/purchase/weighbridge-in' },
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
      children: [
        { id: 'sales-order', title: 'Sales Order', type: 'item', url: '/sales/sales-order' },
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
      children: [
        { id: 'production-templates', title: 'Process Templates', type: 'item', url: '/production/templates' },
        { id: 'production-batches', title: 'Batches', type: 'item', url: '/production/batches' },
        { id: 'production-wip', title: 'WIP Stock', type: 'item', url: '/production/wip-stock' },
        { id: 'production-cost-summary', title: 'Cost Summary', type: 'item', url: '/production/cost-summary' }
      ]
    },
    {
      id: 'accounts',
      title: 'Accounts',
      type: 'collapse',
      icon: icons.IconReportAnalytics,
      children: [
        { id: 'accounts-ledgers', title: 'Ledgers', type: 'item', url: '/accounts/ledgers' }
      ]
    },
    {
      id: 'reports',
      title: 'Reports',
      type: 'collapse',
      icon: icons.IconReportAnalytics,
      children: [
        { id: 'reports-ledger', title: 'Ledger', type: 'item', url: '/reports/ledger' },
        { id: 'reports-outstanding', title: 'Outstanding', type: 'item', url: '/reports/outstanding' },
        { id: 'reports-ageing', title: 'Ageing', type: 'item', url: '/reports/ageing' }
      ]
    },
    ...(devToolsItem ? [devToolsItem] : []),
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
