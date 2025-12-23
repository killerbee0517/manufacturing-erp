import { useEffect, useState } from 'react';

import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import { productionApi } from 'api/production';

export default function ProductionCostSummaryPage() {
  const [orders, setOrders] = useState([]);
  const [batches, setBatches] = useState([]);
  const [selection, setSelection] = useState({ orderId: '', batchId: '' });
  const [summary, setSummary] = useState(null);

  useEffect(() => {
    productionApi
      .listOrders()
      .then((response) => setOrders(response.data || []))
      .catch(() => setOrders([]));
  }, []);

  useEffect(() => {
    if (!selection.orderId) {
      setBatches([]);
      return;
    }
    productionApi
      .listBatches(selection.orderId)
      .then((response) => setBatches(response.data || []))
      .catch(() => setBatches([]));
  }, [selection.orderId]);

  useEffect(() => {
    if (!selection.batchId) {
      setSummary(null);
      return;
    }
    productionApi
      .getCostSummary(selection.batchId)
      .then((response) => setSummary(response.data))
      .catch(() => setSummary(null));
  }, [selection.batchId]);

  return (
    <MainCard>
      <PageHeader title="Cost Summary" breadcrumbs={[{ label: 'Production' }, { label: 'Cost Summary' }]} />
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            select
            label="Production Order"
            value={selection.orderId}
            onChange={(event) => setSelection({ orderId: event.target.value, batchId: '' })}
          >
            {orders.map((order) => (
              <MenuItem key={order.id} value={order.id}>
                {order.orderNo}
              </MenuItem>
            ))}
          </TextField>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            select
            label="Batch"
            value={selection.batchId}
            onChange={(event) => setSelection((prev) => ({ ...prev, batchId: event.target.value }))}
          >
            {batches.map((batch) => (
              <MenuItem key={batch.id} value={batch.id}>
                {batch.batchNo}
              </MenuItem>
            ))}
          </TextField>
        </Grid>
      </Grid>
      <Card variant="outlined">
        <CardContent>
          {summary ? (
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 4 }}>
                <Typography variant="subtitle2">Total Consumption Qty</Typography>
                <Typography variant="h6">{summary.totalConsumptionQty}</Typography>
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <Typography variant="subtitle2">Total Output Qty</Typography>
                <Typography variant="h6">{summary.totalOutputQty}</Typography>
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <Typography variant="subtitle2">Unit Cost</Typography>
                <Typography variant="h6">{summary.unitCost}</Typography>
              </Grid>
            </Grid>
          ) : (
            <Typography variant="body2" color="text.secondary">
              Select a batch to view cost summary.
            </Typography>
          )}
        </CardContent>
      </Card>
    </MainCard>
  );
}
