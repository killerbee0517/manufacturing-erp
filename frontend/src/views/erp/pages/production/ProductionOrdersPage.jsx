import { useEffect, useState } from 'react';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import FormDrawer from 'components/common/FormDrawer';
import apiClient from 'api/client';
import { productionApi } from 'api/production';

export default function ProductionOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [templates, setTemplates] = useState([]);
  const [boms, setBoms] = useState([]);
  const [items, setItems] = useState([]);
  const [uoms, setUoms] = useState([]);
  const [formValues, setFormValues] = useState({
    orderNo: '',
    templateId: '',
    bomId: '',
    finishedItemId: '',
    uomId: '',
    plannedQty: '',
    orderDate: ''
  });

  const columns = [
    { field: 'orderNo', headerName: 'Order No' },
    { field: 'templateName', headerName: 'Template' },
    { field: 'bomName', headerName: 'BOM' },
    { field: 'finishedItemName', headerName: 'Finished Item' },
    { field: 'uomCode', headerName: 'UOM' },
    { field: 'plannedQty', headerName: 'Planned Qty' },
    { field: 'orderDate', headerName: 'Order Date' },
    { field: 'status', headerName: 'Status' },
    {
      field: 'actions',
      headerName: 'Actions',
      render: (row) => (
        <Stack direction="row" spacing={1}>
          <Button size="small" variant="text" onClick={() => handleStartBatch(row.id)}>
            Start Batch
          </Button>
        </Stack>
      )
    }
  ];

  const loadOrders = () => {
    setLoading(true);
    productionApi
      .listOrders()
      .then((response) => setOrders(response.data || []))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  };

  const loadLookups = () => {
    productionApi
      .listTemplates()
      .then((response) => setTemplates(response.data || []))
      .catch(() => setTemplates([]));
    productionApi
      .listBoms()
      .then((response) => setBoms(response.data || []))
      .catch(() => setBoms([]));
    apiClient
      .get('/api/items')
      .then((response) => setItems(response.data || []))
      .catch(() => setItems([]));
    apiClient
      .get('/api/uoms')
      .then((response) => setUoms(response.data || []))
      .catch(() => setUoms([]));
  };

  useEffect(() => {
    loadOrders();
    loadLookups();
  }, []);

  const handleStartBatch = async (orderId) => {
    await productionApi.startBatch(orderId);
    loadOrders();
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    await productionApi.createOrder({
      orderNo: formValues.orderNo || `ORD-${Date.now().toString().slice(-4)}`,
      templateId: formValues.templateId || null,
      bomId: Number(formValues.bomId),
      finishedItemId: Number(formValues.finishedItemId),
      uomId: Number(formValues.uomId),
      plannedQty: Number(formValues.plannedQty),
      orderDate: formValues.orderDate || null
    });
    setDrawerOpen(false);
    setFormValues({ orderNo: '', templateId: '', bomId: '', finishedItemId: '', uomId: '', plannedQty: '', orderDate: '' });
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
            <TextField
              fullWidth
              label="Order No"
              value={formValues.orderNo}
              onChange={(event) => setFormValues((prev) => ({ ...prev, orderNo: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              select
              label="Process Template"
              value={formValues.templateId}
              onChange={(event) => setFormValues((prev) => ({ ...prev, templateId: event.target.value }))}
            >
              <MenuItem value="">None</MenuItem>
              {templates.map((template) => (
                <MenuItem key={template.id} value={template.id}>
                  {template.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              select
              label="BOM"
              value={formValues.bomId}
              onChange={(event) => setFormValues((prev) => ({ ...prev, bomId: event.target.value }))}
              required
            >
              {boms.map((bom) => (
                <MenuItem key={bom.id} value={bom.id}>
                  {bom.name} - {bom.finishedItemName}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              select
              label="Finished Item"
              value={formValues.finishedItemId}
              onChange={(event) => setFormValues((prev) => ({ ...prev, finishedItemId: event.target.value }))}
              required
            >
              {items.map((item) => (
                <MenuItem key={item.id} value={item.id}>
                  {item.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              select
              label="UOM"
              value={formValues.uomId}
              onChange={(event) => setFormValues((prev) => ({ ...prev, uomId: event.target.value }))}
              required
            >
              {uoms.map((uom) => (
                <MenuItem key={uom.id} value={uom.id}>
                  {uom.code}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Planned Quantity"
              type="number"
              value={formValues.plannedQty}
              onChange={(event) => setFormValues((prev) => ({ ...prev, plannedQty: event.target.value }))}
              required
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Order Date"
              type="date"
              value={formValues.orderDate}
              onChange={(event) => setFormValues((prev) => ({ ...prev, orderDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
        </Grid>
      </FormDrawer>
    </MainCard>
  );
}
