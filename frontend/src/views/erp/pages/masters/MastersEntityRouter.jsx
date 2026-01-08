import { Navigate, useParams } from 'react-router-dom';

import Alert from '@mui/material/Alert';

import ModulePage from 'views/erp/ModulePage';
import BankMasterPage from 'views/erp/pages/masters/BankMasterPage';
import PartyMasterPage from 'views/erp/pages/masters/PartyMasterPage';
import { mastersEntities } from './mastersConfig';

const legacyPartyRoutes = new Set(['suppliers', 'customers', 'brokers', 'expense-parties']);

export default function MastersEntityRouter() {
  const { entity } = useParams();
  if (entity === 'parties') {
    return <PartyMasterPage />;
  }
  if (entity === 'banks') {
    return <BankMasterPage />;
  }
  if (legacyPartyRoutes.has(entity)) {
    return <Navigate to="/masters/parties" replace />;
  }
  const config = mastersEntities[entity];

  if (!config) {
    return <Alert severity="warning">Master module not configured.</Alert>;
  }

  return <ModulePage config={config} />;
}
