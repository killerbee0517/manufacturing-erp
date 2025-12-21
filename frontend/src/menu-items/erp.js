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
  IconClipboardList
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
  IconClipboardList
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
      id: 'settings',
      title: 'Settings',
      type: 'collapse',
      icon: icons.IconUsers,
      children: [
        { id: 'settings-company', title: 'Company', type: 'item', url: '/settings/company' },
        { id: 'settings-ledgers', title: 'Ledgers', type: 'item', url: '/settings/ledgers' },
        { id: 'settings-ledger-groups', title: 'Ledger Groups', type: 'item', url: '/settings/ledger-groups' },
        { id: 'settings-stock-items', title: 'Stock Items', type: 'item', url: '/settings/stock-items' },
        { id: 'settings-stock-groups', title: 'Stock Groups', type: 'item', url: '/settings/stock-groups' },
        { id: 'settings-uom', title: 'UOM', type: 'item', url: '/settings/uom' },
        { id: 'settings-godowns', title: 'Godowns', type: 'item', url: '/settings/godowns' },
        { id: 'settings-cost-categories', title: 'Cost Categories', type: 'item', url: '/settings/cost-categories' },
        { id: 'settings-cost-centers', title: 'Cost Centers', type: 'item', url: '/settings/cost-centers' },
        { id: 'settings-gst-hsn', title: 'GST & HSN', type: 'item', url: '/settings/gst-hsn' },
        { id: 'settings-document-series', title: 'Document Series', type: 'item', url: '/settings/document-series' }
      ]
    },
    {
      id: 'purchase',
      title: 'Purchase',
      type: 'collapse',
      icon: icons.IconShoppingCart,
      children: [
        { id: 'purchase-rfq', title: 'RFQ', type: 'item', url: '/purchase/rfq' },
        { id: 'purchase-po', title: 'Purchase Order', type: 'item', url: '/purchase/purchase-order' },
        { id: 'purchase-grn', title: 'GRN', type: 'item', url: '/purchase/grn' },
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
        { id: 'sales-order', title: 'Sales Order', type: 'item', url: '/sales/sales-order' },
        { id: 'sales-delivery', title: 'Delivery Note', type: 'item', url: '/sales/delivery-note' },
        { id: 'sales-tax-invoice', title: 'Tax Invoice', type: 'item', url: '/sales/tax-invoice' },
        { id: 'sales-credit-note', title: 'Credit Note', type: 'item', url: '/sales/credit-note' },
        { id: 'sales-receipt', title: 'Receipt', type: 'item', url: '/sales/receipt' }
      ]
    },
    {
      id: 'inventory',
      title: 'Inventory',
      type: 'collapse',
      icon: icons.IconPackage,
      children: [
        { id: 'inventory-stock-transfer', title: 'Stock Transfer', type: 'item', url: '/inventory/stock-transfer' },
        { id: 'inventory-stock-ledger', title: 'Stock Ledger', type: 'item', url: '/inventory/stock-ledger' },
        { id: 'inventory-item-balances', title: 'Item Balances', type: 'item', url: '/inventory/item-balances' }
      ]
    },
    {
      id: 'production',
      title: 'Production',
      type: 'collapse',
      icon: icons.IconBuildingFactory2,
      children: [
        { id: 'production-process-templates', title: 'Process Templates', type: 'item', url: '/production/process-templates' },
        { id: 'production-orders', title: 'Production Orders', type: 'item', url: '/production/production-orders' },
        { id: 'production-execution', title: 'Process Execution', type: 'item', url: '/production/process-execution' },
        { id: 'production-reprocess', title: 'Reprocess/Scrap', type: 'item', url: '/production/reprocess-scrap' }
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
    ...(devToolsItem ? [devToolsItem] : [])
  ]
};

export default erp;
