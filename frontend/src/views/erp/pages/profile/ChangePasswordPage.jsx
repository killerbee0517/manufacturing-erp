import { useState } from 'react';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';

export default function ChangePasswordPage() {
  const [formValues, setFormValues] = useState({ current: '', next: '', confirm: '' });

  return (
    <MainCard>
      <PageHeader title="Change Password" breadcrumbs={[{ label: 'Profile', to: '/profile' }, { label: 'Change Password' }]} />
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            label="Current Password"
            type="password"
            value={formValues.current}
            onChange={(event) => setFormValues((prev) => ({ ...prev, current: event.target.value }))}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            label="New Password"
            type="password"
            value={formValues.next}
            onChange={(event) => setFormValues((prev) => ({ ...prev, next: event.target.value }))}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            label="Confirm Password"
            type="password"
            value={formValues.confirm}
            onChange={(event) => setFormValues((prev) => ({ ...prev, confirm: event.target.value }))}
          />
        </Grid>
        <Grid size={{ xs: 12 }}>
          <Button variant="contained" color="secondary">
            Update Password
          </Button>
        </Grid>
      </Grid>
    </MainCard>
  );
}
