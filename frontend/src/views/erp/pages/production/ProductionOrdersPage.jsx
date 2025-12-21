import { useEffect, useState } from 'react';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import FormDrawer from 'components/common/FormDrawer';
import EntitySelect from 'components/common/EntitySelect';
import { productionApi } from 'api/production';

export default function ProductionOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [formValues, setFormValues] = useState({ fgItem: null, qty: '', date: '' });

  const columns = [
    { field: 'orderNo', headerName: 'Order No' },
    { field: 'fgItem', headerName: 'FG Item' },
    { field: 'qty', headerName: 'Quantity' },
    { field: 'date', headerName: 'Date' },
    { field: 'status', headerName: 'Status' }
  ];

  const loadOrders = () => {
    setLoading(true);
    productionApi
      .listOrders()
      .then((response) => setOrders(response.data || []))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    await productionApi.createOrder({
      orderNo: `PO-${Date.now().toString().slice(-4)}`,
      fgItem: formValues.fgItem?.name || formValues.fgItem?.label || '',
      qty: formValues.qty,
      date: formValues.date
    });
    setDrawerOpen(false);
    setFormValues({ fgItem: null, qty: '', date: '' });
    loadOrders();
  };

  return (
    <MainCard>
      <PageHeader
        title="Production Orders"
        breadcrumbs={[{ label: 'Production' }, { label: 'Production Orders' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => setDrawerOpen(true)}>
            Create Order
          </Button>
        }
      />
      <DataTable columns={columns} rows={orders} loading={loading} emptyMessage="No production orders yet." />
      <FormDrawer open={drawerOpen} title="New Production Order" onClose={() => setDrawerOpen(false)} onSubmit={handleSubmit} submitLabel="Create">
        <Grid container spacing={2}>
          <Grid size={{ xs: 12 }}>
            <EntitySelect
              label="Finished Good Item"
              endpoint="/settings/stock-items"
              value={formValues.fgItem}
              onChange={(value) => setFormValues((prev) => ({ ...prev, fgItem: value }))}
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
              label="Date"
              type="date"
              value={formValues.date}
              onChange={(event) => setFormValues((prev) => ({ ...prev, date: event.target.value }))}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
        </Grid>
      </FormDrawer>
    </MainCard>
  );
}
