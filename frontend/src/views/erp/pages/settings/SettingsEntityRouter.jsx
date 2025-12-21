import { useParams } from 'react-router-dom';

import Alert from '@mui/material/Alert';

import SettingsEntityPage from './SettingsEntityPage';
import { settingsEntities } from './settingsConfig';

export default function SettingsEntityRouter() {
  const { entity } = useParams();
  const config = settingsEntities[entity];

  if (!config) {
    return <Alert severity="warning">Settings module not configured.</Alert>;
  }

  return <SettingsEntityPage entity={entity} config={config} />;
}
