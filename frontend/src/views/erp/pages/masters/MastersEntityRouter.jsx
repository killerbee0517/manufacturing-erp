import { useParams } from 'react-router-dom';

import Alert from '@mui/material/Alert';

import ModulePage from 'views/erp/ModulePage';
import { mastersEntities } from './mastersConfig';

export default function MastersEntityRouter() {
  const { entity } = useParams();
  const config = mastersEntities[entity];

  if (!config) {
    return <Alert severity="warning">Master module not configured.</Alert>;
  }

  return <ModulePage config={config} />;
}
