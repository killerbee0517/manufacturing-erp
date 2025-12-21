import { useState } from 'react';

import Alert from '@mui/material/Alert';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import { devApi } from 'api/dev';

export default function DevToolsPage() {
  const [message, setMessage] = useState('');

  const handleSeedSettings = async () => {
    await devApi.seedSettings();
    setMessage('Settings seeded successfully.');
  };

  const handleSeedTransactions = async () => {
    await devApi.seedDemoTransactions();
    setMessage('Demo transactions seeded successfully.');
  };

  return (
    <MainCard>
      <PageHeader title="Dev Tools" breadcrumbs={[{ label: 'Dev Tools' }]} />
      <Stack spacing={2}>
        <Button variant="contained" color="secondary" onClick={handleSeedSettings}>
          Seed Settings
        </Button>
        <Button variant="outlined" onClick={handleSeedTransactions}>
          Seed Demo Transactions
        </Button>
        {message && <Alert severity="success">{message}</Alert>}
      </Stack>
    </MainCard>
  );
}
