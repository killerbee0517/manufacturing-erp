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

export default function WeighbridgeCreatePage() {
  const navigate = useNavigate();
  const [header, setHeader] = useState({
    ticketNo: '',
    vehicleNo: '',
    supplierId: '',
    itemId: '',
    dateIn: new Date().toISOString().slice(0, 10),
    timeIn: new Date().toISOString().slice(11, 16),
    grossWeight: '',
    unloadedWeight: '',
    dateOut: '',
    timeOut: ''
  });
  const [suppliers, setSuppliers] = useState([]);
  const [items, setItems] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiClient.get('/api/suppliers').then((res) => setSuppliers(res.data || [])).catch(() => setSuppliers([]));
    apiClient.get('/api/items').then((res) => setItems(res.data || [])).catch(() => setItems([]));
    apiClient.get('/api/vehicles').then((res) => setVehicles(res.data || [])).catch(() => setVehicles([]));
  }, []);

  const netWeight = useMemo(() => {
    const gross = Number(header.grossWeight || 0);
    const unloaded = Number(header.unloadedWeight || 0);
    return gross - unloaded;
  }, [header.grossWeight, header.unloadedWeight]);

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = {
        ticketNo: header.ticketNo || undefined,
        vehicleNo: header.vehicleNo,
        supplierId: Number(header.supplierId),
        itemId: Number(header.itemId),
        dateIn: header.dateIn,
        timeIn: header.timeIn,
        grossWeight: Number(header.grossWeight),
        unloadedWeight: Number(header.unloadedWeight),
        dateOut: header.dateOut || null,
        timeOut: header.timeOut || null
      };
      await apiClient.post('/api/weighbridge/tickets', payload);
      navigate('/purchase/weighbridge-in');
    } finally {
      setSaving(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="Weighbridge In"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/weighbridge-in' }, { label: 'New Entry' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
            Save Entry
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Serial / Ticket No"
              value={header.ticketNo}
              onChange={(event) => setHeader((prev) => ({ ...prev, ticketNo: event.target.value }))}
              placeholder="Auto-generated"
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Vehicle"
              value={header.vehicleNo}
              onChange={(event) => setHeader((prev) => ({ ...prev, vehicleNo: event.target.value }))}
            >
              {vehicles.map((vehicle) => (
                <MenuItem key={vehicle.id || vehicle.vehicleNo} value={vehicle.vehicleNo || vehicle.name || vehicle.id}>
                  {vehicle.vehicleNo || vehicle.name || vehicle.registrationNo || vehicle.id}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Supplier"
              value={header.supplierId}
              onChange={(event) => setHeader((prev) => ({ ...prev, supplierId: event.target.value }))}
            >
              {suppliers.map((supplier) => (
                <MenuItem key={supplier.id} value={supplier.id}>
                  {supplier.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Product"
              value={header.itemId}
              onChange={(event) => setHeader((prev) => ({ ...prev, itemId: event.target.value }))}
            >
              {items.map((item) => (
                <MenuItem key={item.id} value={item.id}>
                  {item.name}
                </MenuItem>
              ))}
            </TextField>
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
        <Divider />
        <Stack spacing={1}>
          <Typography variant="h5">Second Weighment</Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField
                fullWidth
                type="date"
                label="Second Date"
                value={header.dateOut}
                onChange={(event) => setHeader((prev) => ({ ...prev, dateOut: event.target.value }))}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField
                fullWidth
                type="time"
                label="Second Time"
                value={header.timeOut}
                onChange={(event) => setHeader((prev) => ({ ...prev, timeOut: event.target.value }))}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
          </Grid>
        </Stack>
      </Stack>
    </MainCard>
  );
}
