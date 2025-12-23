import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

export default function PurchaseArrivalCreatePage() {
  const navigate = useNavigate();
  const [header, setHeader] = useState({
    purchaseOrderId: '',
    weighbridgeTicketId: '',
    godownId: '',
    unloadingCharges: '',
    deductions: '',
    tdsPercent: ''
  });
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [grossAmount, setGrossAmount] = useState(0);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiClient
      .get('/api/purchase-orders')
      .then((res) => {
        const payload = res.data?.content || res.data || [];
        setPurchaseOrders(payload);
      })
      .catch(() => setPurchaseOrders([]));
    apiClient.get('/api/weighbridge/tickets').then((res) => setTickets(res.data || [])).catch(() => setTickets([]));
  }, []);

  const netPayable = useMemo(() => {
    const unloadingCharges = Number(header.unloadingCharges || 0);
    const deductions = Number(header.deductions || 0);
    const tdsPercent = Number(header.tdsPercent || 0);
    const tdsAmount = (grossAmount * tdsPercent) / 100;
    return grossAmount + unloadingCharges - deductions - tdsAmount;
  }, [grossAmount, header.unloadingCharges, header.deductions, header.tdsPercent]);

  const handlePurchaseOrderChange = async (poId) => {
    setHeader((prev) => ({ ...prev, purchaseOrderId: poId }));
    if (!poId) {
      setGrossAmount(0);
      return;
    }
    const response = await apiClient.get(`/api/purchase-orders/${poId}`);
    setGrossAmount(Number(response.data?.totalAmount || 0));
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = {
        purchaseOrderId: Number(header.purchaseOrderId),
        weighbridgeTicketId: header.weighbridgeTicketId ? Number(header.weighbridgeTicketId) : null,
        godownId: Number(header.godownId),
        unloadingCharges: header.unloadingCharges ? Number(header.unloadingCharges) : 0,
        deductions: header.deductions ? Number(header.deductions) : 0,
        tdsPercent: header.tdsPercent ? Number(header.tdsPercent) : 0
      };
      const response = await apiClient.post('/api/purchase-arrivals', payload);
      navigate(`/purchase/arrival/${response.data.id}`);
    } finally {
      setSaving(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="New Purchase Arrival"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/arrival' }, { label: 'New' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
            Save Arrival
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Purchase Order"
              value={header.purchaseOrderId}
              onChange={(event) => handlePurchaseOrderChange(event.target.value)}
            >
              <MenuItem value="">Select PO</MenuItem>
              {purchaseOrders.map((po) => (
                <MenuItem key={po.id} value={po.id}>
                  {po.poNo}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField fullWidth label="Gross Amount" value={grossAmount} InputProps={{ readOnly: true }} />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Weighbridge Ticket"
              value={header.weighbridgeTicketId}
              onChange={(event) => setHeader((prev) => ({ ...prev, weighbridgeTicketId: event.target.value }))}
            >
              <MenuItem value="">None</MenuItem>
              {tickets.map((ticket) => (
                <MenuItem key={ticket.id} value={ticket.id}>
                  {ticket.serialNo}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Godown"
              endpoint="/api/godowns"
              value={header.godownId}
              onChange={(nextValue) => setHeader((prev) => ({ ...prev, godownId: nextValue }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Select godown"
              required
            />
          </Grid>
        </Grid>
        <Divider />
        <Stack spacing={2}>
          <Typography variant="h5">Charges & Deductions</Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField
                fullWidth
                type="number"
                label="Unloading Charges"
                value={header.unloadingCharges}
                onChange={(event) => setHeader((prev) => ({ ...prev, unloadingCharges: event.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField
                fullWidth
                type="number"
                label="Deductions"
                value={header.deductions}
                onChange={(event) => setHeader((prev) => ({ ...prev, deductions: event.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField
                fullWidth
                type="number"
                label="TDS %"
                value={header.tdsPercent}
                onChange={(event) => setHeader((prev) => ({ ...prev, tdsPercent: event.target.value }))}
              />
            </Grid>
          </Grid>
        </Stack>
        <Divider />
        <Stack direction="row" justifyContent="flex-end">
          <Typography variant="h6">Net Payable: {netPayable.toFixed(2)}</Typography>
        </Stack>
      </Stack>
    </MainCard>
  );
}
