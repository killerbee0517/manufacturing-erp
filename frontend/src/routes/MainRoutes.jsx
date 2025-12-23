import { lazy } from 'react';
import { Navigate } from 'react-router-dom';

// project imports
import MainLayout from 'layout/MainLayout';
import Loadable from 'ui-component/Loadable';
import AuthGuard from './AuthGuard';
// dashboard routing
const DashboardDefault = Loadable(lazy(() => import('views/dashboard/Default')));
const SettingsEntityRouter = Loadable(lazy(() => import('views/erp/pages/settings/SettingsEntityRouter')));
const MastersListPage = Loadable(lazy(() => import('views/erp/pages/masters/MastersListPage')));
const MasterFormPage = Loadable(lazy(() => import('views/erp/pages/masters/MasterFormPage')));
const MasterDetailPage = Loadable(lazy(() => import('views/erp/pages/masters/MasterDetailPage')));
const AdminEntityRouter = Loadable(lazy(() => import('views/erp/pages/admin/AdminEntityRouter')));
const RfqPage = Loadable(lazy(() => import('views/erp/pages/purchase/RfqPage')));
const RfqCreatePage = Loadable(lazy(() => import('views/erp/pages/purchase/RfqCreatePage')));
const RfqDetailPage = Loadable(lazy(() => import('views/erp/pages/purchase/RfqDetailPage')));
const RfqEditPage = Loadable(lazy(() => import('views/erp/pages/purchase/RfqEditPage')));
const PurchaseOrderPage = Loadable(lazy(() => import('views/erp/pages/purchase/PurchaseOrderPage')));
const PoCreatePage = Loadable(lazy(() => import('views/erp/pages/purchase/PoCreatePage')));
const PoDetailPage = Loadable(lazy(() => import('views/erp/pages/purchase/PoDetailPage')));
const PoEditPage = Loadable(lazy(() => import('views/erp/pages/purchase/PoEditPage')));
const WeighbridgeListPage = Loadable(lazy(() => import('views/erp/pages/purchase/WeighbridgeListPage')));
const WeighbridgeCreatePage = Loadable(lazy(() => import('views/erp/pages/purchase/WeighbridgeCreatePage')));
const WeighbridgeDetailPage = Loadable(lazy(() => import('views/erp/pages/purchase/WeighbridgeDetailPage')));
const WeighbridgeEditPage = Loadable(lazy(() => import('views/erp/pages/purchase/WeighbridgeEditPage')));
const GrnPage = Loadable(lazy(() => import('views/erp/pages/purchase/GrnPage')));
const GrnCreatePage = Loadable(lazy(() => import('views/erp/pages/purchase/GrnCreatePage')));
const GrnDetailPage = Loadable(lazy(() => import('views/erp/pages/purchase/GrnDetailPage')));
const PurchaseArrivalPage = Loadable(lazy(() => import('views/erp/pages/purchase/PurchaseArrivalPage')));
const PurchaseArrivalCreatePage = Loadable(lazy(() => import('views/erp/pages/purchase/PurchaseArrivalCreatePage')));
const PurchaseArrivalDetailPage = Loadable(lazy(() => import('views/erp/pages/purchase/PurchaseArrivalDetailPage')));
const PurchaseInvoicePage = Loadable(lazy(() => import('views/erp/pages/purchase/PurchaseInvoicePage')));
const DebitNotePage = Loadable(lazy(() => import('views/erp/pages/purchase/DebitNotePage')));
const SalesOrderPage = Loadable(lazy(() => import('views/erp/pages/sales/SalesOrderPage')));
const DeliveryNotePage = Loadable(lazy(() => import('views/erp/pages/sales/DeliveryNotePage')));
const TaxInvoicePage = Loadable(lazy(() => import('views/erp/pages/sales/TaxInvoicePage')));
const CreditNotePage = Loadable(lazy(() => import('views/erp/pages/sales/CreditNotePage')));
const ReceiptPage = Loadable(lazy(() => import('views/erp/pages/sales/ReceiptPage')));
const StockTransferPage = Loadable(lazy(() => import('views/erp/pages/inventory/StockTransferPage')));
const StockLedgerPage = Loadable(lazy(() => import('views/erp/pages/inventory/StockLedgerPage')));
const ItemBalancesPage = Loadable(lazy(() => import('views/erp/pages/inventory/ItemBalancesPage')));
const ProcessTemplatesPage = Loadable(lazy(() => import('views/erp/pages/production/ProcessTemplatesPage')));
const ProductionOrdersPage = Loadable(lazy(() => import('views/erp/pages/production/ProductionOrdersPage')));
const ProcessExecutionPage = Loadable(lazy(() => import('views/erp/pages/production/ProcessExecutionPage')));
const ReprocessScrapPage = Loadable(lazy(() => import('views/erp/pages/production/ReprocessScrapPage')));
const LedgerReportPage = Loadable(lazy(() => import('views/erp/pages/reports/LedgerReportPage')));
const OutstandingReportPage = Loadable(lazy(() => import('views/erp/pages/reports/OutstandingReportPage')));
const AgeingReportPage = Loadable(lazy(() => import('views/erp/pages/reports/AgeingReportPage')));
const ProfilePage = Loadable(lazy(() => import('views/erp/pages/profile/ProfilePage')));
const ChangePasswordPage = Loadable(lazy(() => import('views/erp/pages/profile/ChangePasswordPage')));
const DevToolsPage = Loadable(lazy(() => import('views/erp/pages/dev-tools/DevToolsPage')));

// ==============================|| MAIN ROUTING ||============================== //

const MainRoutes = {
  path: '/',
  element: (
    <AuthGuard>
      <MainLayout />
    </AuthGuard>
  ),
  children: [
    {
      path: '/',
      element: <Navigate to="/dashboard" replace />
    },
    {
      path: 'dashboard',
      element: <DashboardDefault />
    },
    {
      path: 'settings',
      children: [
        { path: '', element: <Navigate to="/settings/company" replace /> },
        { path: ':entity', element: <SettingsEntityRouter /> }
      ]
    },
    {
      path: 'masters',
      children: [
        { path: ':entity', element: <MastersListPage /> },
        { path: ':entity/new', element: <MasterFormPage mode="create" /> },
        { path: ':entity/:id', element: <MasterDetailPage /> },
        { path: ':entity/:id/edit', element: <MasterFormPage mode="edit" /> }
      ]
    },
    {
      path: 'admin',
      children: [{ path: ':entity', element: <AdminEntityRouter /> }]
    },
    {
      path: 'purchase',
      children: [
        { path: 'rfq', element: <RfqPage /> },
        { path: 'rfq/new', element: <RfqCreatePage /> },
        { path: 'rfq/:id', element: <RfqDetailPage /> },
        { path: 'rfq/:id/edit', element: <RfqEditPage /> },
        { path: 'po', element: <PurchaseOrderPage /> },
        { path: 'po/new', element: <PoCreatePage /> },
        { path: 'po/:id', element: <PoDetailPage /> },
        { path: 'po/:id/edit', element: <PoEditPage /> },
        { path: 'weighbridge-in', element: <WeighbridgeListPage /> },
        { path: 'weighbridge-in/new', element: <WeighbridgeCreatePage /> },
        { path: 'weighbridge-in/:id', element: <WeighbridgeDetailPage /> },
        { path: 'weighbridge-in/:id/edit', element: <WeighbridgeEditPage /> },
        { path: 'grn', element: <GrnPage /> },
        { path: 'grn/new', element: <GrnCreatePage /> },
        { path: 'grn/:id', element: <GrnDetailPage /> },
        { path: 'arrival', element: <PurchaseArrivalPage /> },
        { path: 'arrival/new', element: <PurchaseArrivalCreatePage /> },
        { path: 'arrival/:id', element: <PurchaseArrivalDetailPage /> },
        { path: 'purchase-invoice', element: <PurchaseInvoicePage /> },
        { path: 'debit-note', element: <DebitNotePage /> }
      ]
    },
    {
      path: 'sales',
      children: [
        { path: 'sales-order', element: <SalesOrderPage /> },
        { path: 'delivery-note', element: <DeliveryNotePage /> },
        { path: 'tax-invoice', element: <TaxInvoicePage /> },
        { path: 'credit-note', element: <CreditNotePage /> },
        { path: 'receipt', element: <ReceiptPage /> }
      ]
    },
    {
      path: 'inventory',
      children: [
        { path: 'stock-on-hand', element: <ItemBalancesPage /> },
        { path: 'stock-transfer', element: <StockTransferPage /> },
        { path: 'stock-ledger', element: <StockLedgerPage /> }
      ]
    },
    {
      path: 'production',
      children: [
        { path: 'process-templates', element: <ProcessTemplatesPage /> },
        { path: 'production-orders', element: <ProductionOrdersPage /> },
        { path: 'process-execution', element: <ProcessExecutionPage /> },
        { path: 'reprocess-scrap', element: <ReprocessScrapPage /> }
      ]
    },
    {
      path: 'reports',
      children: [
        { path: 'ledger', element: <LedgerReportPage /> },
        { path: 'outstanding', element: <OutstandingReportPage /> },
        { path: 'ageing', element: <AgeingReportPage /> }
      ]
    },
    {
      path: 'profile',
      children: [
        { path: '', element: <ProfilePage /> },
        { path: 'change-password', element: <ChangePasswordPage /> }
      ]
    },
    ...(import.meta.env.DEV
      ? [
          {
            path: 'dev-tools',
            element: <DevToolsPage />
          }
        ]
      : []),
    {
      path: 'dev-tools',
      element: <Navigate to="/dashboard" replace />
    },
    {
      path: '*',
      element: <Navigate to="/dashboard" replace />
    }
  ]
};

export default MainRoutes;
