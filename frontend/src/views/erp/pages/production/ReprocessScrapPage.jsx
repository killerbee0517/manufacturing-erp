import { useEffect, useState } from 'react';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import FormDrawer from 'components/common/FormDrawer';
import apiClient from 'api/client';

export default function ReprocessScrapPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [formValues, setFormValues] = useState({ item: '', qty: '', reason: '' });

  const columns = [
    { field: 'item', headerName: 'Item' },
    { field: 'qty', headerName: 'Quantity' },
    { field: 'reason', headerName: 'Reason' }
  ];

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/production/reprocess-scrap')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    const record = { ...formValues };
    await apiClient.post('/production/reprocess-scrap', record);
    setRows((prev) => [...prev, { id: Date.now(), ...record }]);
    setDrawerOpen(false);
    setFormValues({ item: '', qty: '', reason: '' });
  };

  return (
    <MainCard>
      <PageHeader
        title="Reprocess/Scrap"
        breadcrumbs={[{ label: 'Production' }, { label: 'Reprocess/Scrap' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => setDrawerOpen(true)}>
            Add Entry
          </Button>
        }
      />
      <DataTable columns={columns} rows={rows} loading={loading} emptyMessage="No reprocess/scrap entries yet." />
      <FormDrawer open={drawerOpen} title="New Entry" onClose={() => setDrawerOpen(false)} onSubmit={handleSubmit} submitLabel="Save">
        <Grid container spacing={2}>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Item"
              value={formValues.item}
              onChange={(event) => setFormValues((prev) => ({ ...prev, item: event.target.value }))}
              required
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Quantity"
              type="number"
              value={formValues.qty}
              onChange={(event) => setFormValues((prev) => ({ ...prev, qty: event.target.value }))}
              required
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Reason"
              value={formValues.reason}
              onChange={(event) => setFormValues((prev) => ({ ...prev, reason: event.target.value }))}
              required
            />
          </Grid>
        </Grid>
      </FormDrawer>
    </MainCard>
  );
}
