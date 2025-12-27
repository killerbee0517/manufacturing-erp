import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

export default function WeighbridgeEditPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [header, setHeader] = useState({
    serialNo: '',
    poId: '',
    vehicleId: '',
    itemId: '',
    dateIn: '',
    timeIn: '',
    grossWeight: '',
    unloadedWeight: '',
    secondDate: '',
    secondTime: ''
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [poInfo, setPoInfo] = useState(null);

  useEffect(() => {
    setLoading(true);
    apiClient
      .get(`/api/weighbridge/tickets/${id}`)
      .then((response) => {
        const ticket = response.data;
        setHeader({
          serialNo: ticket.serialNo || '',
          poId: ticket.poId || '',
          vehicleId: ticket.vehicleId || '',
          itemId: ticket.itemId || '',
          dateIn: ticket.dateIn || '',
          timeIn: ticket.timeIn || '',
          grossWeight: ticket.grossWeight ?? '',
          unloadedWeight: ticket.unloadedWeight ?? '',
          secondDate: ticket.secondDate || '',
          secondTime: ticket.secondTime || ''
        });
        if (ticket.poId) {
          handlePoChange(ticket.poId);
        }
      })
      .finally(() => setLoading(false));
  }, [id]);

  const netWeight = useMemo(() => {
    const gross = Number(header.grossWeight || 0);
    const unloaded = Number(header.unloadedWeight || 0);
    return gross - unloaded;
  }, [header.grossWeight, header.unloadedWeight]);

  const handlePoChange = async (poId) => {
    setHeader((prev) => ({ ...prev, poId }));
    if (!poId) {
      setPoInfo(null);
      return;
    }
    const response = await apiClient.get(`/api/purchase-orders/${poId}`);
    setPoInfo(response.data);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const hasUnload = header.unloadedWeight;
      if (hasUnload) {
        const payload = {
          poId: Number(header.poId),
          vehicleId: Number(header.vehicleId),
          secondDate: header.secondDate || null,
          secondTime: header.secondTime || null,
          unloadedWeight: Number(header.unloadedWeight)
        };
        await apiClient.put(`/api/weighbridge/tickets/${id}/unload`, payload);
      } else {
        const payload = {
          serialNo: header.serialNo || undefined,
          poId: Number(header.poId),
          vehicleId: Number(header.vehicleId),
          dateIn: header.dateIn,
          timeIn: header.timeIn,
          grossWeight: Number(header.grossWeight)
        };
        await apiClient.put(`/api/weighbridge/tickets/${id}`, payload);
      }
      navigate(`/purchase/weighbridge-in/${id}`);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <MainCard>
        <Typography>Loading...</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title="Edit Weighbridge Entry"
        breadcrumbs={[
          { label: 'Purchase', to: '/purchase/weighbridge-in' },
          { label: `Ticket ${id}`, to: `/purchase/weighbridge-in/${id}` },
          { label: 'Edit' }
        ]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
            Save Changes
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Purchase Order"
              endpoint="/api/purchase-orders"
              value={header.poId}
              onChange={(nextValue) => handlePoChange(nextValue)}
              optionLabelKey="poNo"
              optionValueKey="id"
              placeholder="Select PO"
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Serial / Ticket No"
              value={header.serialNo}
              onChange={(event) => setHeader((prev) => ({ ...prev, serialNo: event.target.value }))}
              placeholder="Auto-generated"
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Vehicle"
              endpoint="/api/vehicles"
              value={header.vehicleId}
              onChange={(nextValue) => setHeader((prev) => ({ ...prev, vehicleId: nextValue }))}
              optionLabelKey="vehicleNo"
              optionValueKey="id"
              placeholder="Search vehicles"
              required
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField fullWidth label="Supplier" value={poInfo?.supplierName || poInfo?.supplierId || ''} InputProps={{ readOnly: true }} />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Product"
              value={
                poInfo?.lines?.length === 1
                  ? poInfo.lines[0].itemId
                  : header.itemId
              }
              InputProps={{ readOnly: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="Date In"
              value={header.dateIn}
              onChange={(event) => setHeader((prev) => ({ ...prev, dateIn: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="time"
              label="Time In"
              value={header.timeIn}
              onChange={(event) => setHeader((prev) => ({ ...prev, timeIn: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="Gross Weight"
              value={header.grossWeight}
              onChange={(event) => setHeader((prev) => ({ ...prev, grossWeight: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="Unloaded Weight"
              value={header.unloadedWeight}
              onChange={(event) => setHeader((prev) => ({ ...prev, unloadedWeight: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="Net Weight"
              value={Number.isNaN(netWeight) ? '' : netWeight}
              InputProps={{ readOnly: true }}
            />
          </Grid>
        </Grid>
        {poInfo && (
          <Stack spacing={1}>
            <Typography variant="h5">PO Items</Typography>
            <Typography color="text.secondary">
              Supplier: {poInfo.supplierName || poInfo.supplierId} • Lines: {poInfo.lines?.length || 0}
            </Typography>
            <Grid container spacing={1}>
              {poInfo.lines?.map((line) => (
                <Grid key={line.id} size={{ xs: 12, md: 6 }}>
                  <Typography variant="subtitle2">{line.itemId}</Typography>
                  <Typography color="text.secondary">Quantity: {line.quantity}</Typography>
                </Grid>
              ))}
            </Grid>
          </Stack>
        )}
        <Divider />
        <Stack spacing={1}>
          <Typography variant="h5">Second Weighment</Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField
                fullWidth
                type="date"
                label="Second Date"
                value={header.secondDate}
                onChange={(event) => setHeader((prev) => ({ ...prev, secondDate: event.target.value }))}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField
                fullWidth
                type="time"
                label="Second Time"
                value={header.secondTime}
                onChange={(event) => setHeader((prev) => ({ ...prev, secondTime: event.target.value }))}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
          </Grid>
        </Stack>
      </Stack>
    </MainCard>
  );
}
