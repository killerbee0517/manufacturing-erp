import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

export default function GrnCreatePage() {
  const navigate = useNavigate();
  const [header, setHeader] = useState({
    supplierId: '',
    purchaseOrderId: '',
    weighbridgeTicketId: '',
    grnDate: new Date().toISOString().slice(0, 10),
    itemId: '',
    uomId: '',
    quantity: '',
    firstWeight: '',
    secondWeight: '',
    narration: ''
  });
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiClient.get('/api/purchase-orders').then((res) => {
      const payload = res.data?.content || res.data || [];
      setPurchaseOrders(payload);
    }).catch(() => setPurchaseOrders([]));
    apiClient.get('/api/weighbridge/tickets').then((res) => setTickets(res.data || [])).catch(() => setTickets([]));
  }, []);

  const netWeight = useMemo(() => {
    const first = Number(header.firstWeight || 0);
    const second = Number(header.secondWeight || 0);
    if (!first && !second) return 0;
    return Math.abs(second - first);
  }, [header.firstWeight, header.secondWeight]);

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = {
        supplierId: Number(header.supplierId),
        purchaseOrderId: header.purchaseOrderId ? Number(header.purchaseOrderId) : null,
        weighbridgeTicketId: header.weighbridgeTicketId ? Number(header.weighbridgeTicketId) : null,
        grnDate: header.grnDate,
        itemId: Number(header.itemId),
        uomId: Number(header.uomId),
        quantity: Number(header.quantity),
        firstWeight: header.firstWeight ? Number(header.firstWeight) : null,
        secondWeight: header.secondWeight ? Number(header.secondWeight) : null,
        netWeight,
        narration: header.narration
      };
      await apiClient.post('/api/grn', payload);
      navigate('/purchase/grn');
    } finally {
      setSaving(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="Create GRN"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/grn' }, { label: 'Create' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
            Save GRN
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Supplier / Party"
              endpoint="/api/suppliers"
              value={header.supplierId}
              onChange={(nextValue) => setHeader((prev) => ({ ...prev, supplierId: nextValue }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Search suppliers"
              required
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="GRN Date"
              value={header.grnDate}
              onChange={(event) => setHeader((prev) => ({ ...prev, grnDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Purchase Order"
              value={header.purchaseOrderId}
              onChange={(event) => setHeader((prev) => ({ ...prev, purchaseOrderId: event.target.value }))}
            >
              <MenuItem value="">None</MenuItem>
              {purchaseOrders.map((po) => (
                <MenuItem key={po.id} value={po.id}>
                  {po.poNo}
                </MenuItem>
              ))}
            </TextField>
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
                  {ticket.ticketNo}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
        </Grid>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Item"
              endpoint="/api/items"
              value={header.itemId}
              onChange={(nextValue) => setHeader((prev) => ({ ...prev, itemId: nextValue }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Search items"
              required
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="UOM"
              endpoint="/api/uoms"
              value={header.uomId}
              onChange={(nextValue) => setHeader((prev) => ({ ...prev, uomId: nextValue }))}
              optionLabelKey="code"
              optionValueKey="id"
              placeholder="Search UOMs"
              required
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="Quantity"
              value={header.quantity}
              onChange={(event) => setHeader((prev) => ({ ...prev, quantity: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="1st Weight"
              value={header.firstWeight}
              onChange={(event) => setHeader((prev) => ({ ...prev, firstWeight: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="2nd Weight"
              value={header.secondWeight}
              onChange={(event) => setHeader((prev) => ({ ...prev, secondWeight: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Net Weight"
              value={netWeight}
              InputProps={{ readOnly: true }}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Narration"
              value={header.narration}
              onChange={(event) => setHeader((prev) => ({ ...prev, narration: event.target.value }))}
            />
          </Grid>
        </Grid>
      </Stack>
    </MainCard>
  );
}
