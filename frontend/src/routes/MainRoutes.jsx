import { lazy } from 'react';
import { Navigate } from 'react-router-dom';

// project imports
import MainLayout from 'layout/MainLayout';
import Loadable from 'ui-component/Loadable';
import AuthGuard from './AuthGuard';
import { moduleConfigs } from 'views/erp/moduleConfig';

// dashboard routing
const DashboardDefault = Loadable(lazy(() => import('views/dashboard/Default')));
const ModulePage = Loadable(lazy(() => import('views/erp/ModulePage')));

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
      path: 'masters',
      children: [
        { path: 'suppliers', element: <ModulePage config={moduleConfigs.suppliers} /> },
        { path: 'items', element: <ModulePage config={moduleConfigs.items} /> },
        { path: 'locations', element: <ModulePage config={moduleConfigs.locations} /> },
        { path: 'vehicles', element: <ModulePage config={moduleConfigs.vehicles} /> },
        { path: 'customers', element: <ModulePage config={moduleConfigs.customers} /> },
        { path: 'brokers', element: <ModulePage config={moduleConfigs.brokers} /> },
        { path: 'users', element: <ModulePage config={moduleConfigs.users} /> },
        { path: 'tds-rules', element: <ModulePage config={moduleConfigs.tdsRules} /> }
      ]
    },
    {
      path: 'purchase',
      children: [
        { path: 'rfq', element: <ModulePage config={moduleConfigs.rfq} /> },
        { path: 'po', element: <ModulePage config={moduleConfigs.purchaseOrders} /> },
        { path: 'weighbridge-in', element: <ModulePage config={moduleConfigs.weighbridgeIn} /> },
        { path: 'grn', element: <ModulePage config={moduleConfigs.grn} /> },
        { path: 'qc', element: <ModulePage config={moduleConfigs.qc} /> },
        { path: 'purchase-invoice', element: <ModulePage config={moduleConfigs.purchaseInvoice} /> },
        { path: 'debit-note', element: <ModulePage config={moduleConfigs.debitNote} /> }
      ]
    },
    {
      path: 'sales',
      children: [
        { path: 'so', element: <ModulePage config={moduleConfigs.salesOrders} /> },
        { path: 'weighbridge-out', element: <ModulePage config={moduleConfigs.weighbridgeOut} /> },
        { path: 'delivery', element: <ModulePage config={moduleConfigs.delivery} /> },
        { path: 'sales-invoice', element: <ModulePage config={moduleConfigs.salesInvoice} /> }
      ]
    },
    {
      path: 'inventory',
      children: [
        { path: 'stock-on-hand', element: <ModulePage config={moduleConfigs.stockOnHand} /> },
        { path: 'stock-ledger', element: <ModulePage config={moduleConfigs.stockLedger} /> },
        { path: 'stock-transfer', element: <ModulePage config={moduleConfigs.stockTransfer} /> }
      ]
    },
    {
      path: 'reports',
      element: <ModulePage config={moduleConfigs.reports} />
    },
    {
      path: 'settings',
      element: <ModulePage config={moduleConfigs.settings} />
    },
    {
      path: '*',
      element: <Navigate to="/dashboard" replace />
    }
  ]
};

export default MainRoutes;
