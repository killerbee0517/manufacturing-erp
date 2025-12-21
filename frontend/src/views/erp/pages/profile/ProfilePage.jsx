import { useState } from 'react';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';

export default function ProfilePage() {
  const [formValues, setFormValues] = useState({
    name: 'Warehouse Admin',
    email: 'admin@factory.local',
    phone: ''
  });

  return (
    <MainCard>
      <PageHeader title="My Profile" breadcrumbs={[{ label: 'Profile' }]} />
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            label="Full Name"
            value={formValues.name}
            onChange={(event) => setFormValues((prev) => ({ ...prev, name: event.target.value }))}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            label="Email"
            value={formValues.email}
            onChange={(event) => setFormValues((prev) => ({ ...prev, email: event.target.value }))}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            label="Phone"
            value={formValues.phone}
            onChange={(event) => setFormValues((prev) => ({ ...prev, phone: event.target.value }))}
          />
        </Grid>
        <Grid size={{ xs: 12 }}>
          <Button variant="contained" color="secondary">
            Update Profile
          </Button>
        </Grid>
      </Grid>
    </MainCard>
  );
}
