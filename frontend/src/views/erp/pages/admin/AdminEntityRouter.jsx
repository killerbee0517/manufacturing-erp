import { useParams } from 'react-router-dom';

import Alert from '@mui/material/Alert';

import ModulePage from 'views/erp/ModulePage';
import { adminEntities } from './adminConfig';

export default function AdminEntityRouter() {
  const { entity } = useParams();
  const config = adminEntities[entity];

  if (!config) {
    return <Alert severity="warning">Admin module not configured.</Alert>;
  }

  return <ModulePage config={config} />;
}
