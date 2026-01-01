import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import Alert from '@mui/material/Alert';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

export default function WeighbridgeCreatePage() {
  const navigate = useNavigate();
  const [header, setHeader] = useState({
    serialNo: '',
    poId: '',
    vehicleId: '',
    itemId: '',
    dateIn: new Date().toISOString().slice(0, 10),
    timeIn: new Date().toISOString().slice(11, 16),
    grossWeight: '',
    unloadedWeight: '',
    secondDate: new Date().toISOString().slice(0, 10),
    secondTime: new Date().toISOString().slice(11, 16)
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [poInfo, setPoInfo] = useState(null);
  const [itemMap, setItemMap] = useState({});
  const [uomMap, setUomMap] = useState({});

  const netWeight = useMemo(() => {
    const gross = Number(header.grossWeight || 0);
    const unloaded = Number(header.unloadedWeight || 0);
    return gross - unloaded;
  }, [header.grossWeight, header.unloadedWeight]);

  const canSave = useMemo(() => {
    const poId = Number(header.poId);
    const vehicleId = Number(header.vehicleId);
    const grossWeight = Number(header.grossWeight);
    return Number.isFinite(poId) && poId > 0
      && Number.isFinite(vehicleId) && vehicleId > 0
      && header.dateIn
      && header.timeIn
      && Number.isFinite(grossWeight) && grossWeight > 0;
  }, [header.poId, header.vehicleId, header.dateIn, header.timeIn, header.grossWeight]);

  useEffect(() => {
    apiClient
      .get('/api/items')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, item) => {
          acc[item.id] = item.name;
          return acc;
        }, {});
        setItemMap(lookup);
      })
      .catch(() => setItemMap({}));
    apiClient
      .get('/api/uoms')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, uom) => {
          acc[uom.id] = uom.code;
          return acc;
        }, {});
        setUomMap(lookup);
      })
      .catch(() => setUomMap({}));
  }, []);

  const formatAmount = (quantity, rate) => {
    const qty = Number(quantity);
    const rateNum = Number(rate);
    if (!Number.isFinite(qty) || !Number.isFinite(rateNum)) {
      return '-';
    }
    const amount = qty * rateNum;
    return Number.isFinite(amount) ? amount.toFixed(2) : '-';
  };

  const handlePoChange = async (poId) => {
    setHeader((prev) => ({ ...prev, poId, supplierId: '' }));
    if (!poId) {
      setPoInfo(null);
      return;
    }
    const response = await apiClient.get(`/api/purchase-orders/${poId}`);
    const po = response.data;
    setPoInfo(po);
    setHeader((prev) => ({
      ...prev,
      supplierId: po.supplierId || '',
      itemId: po.lines?.length === 1 ? po.lines[0].itemId : ''
    }));
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const poId = Number(header.poId);
      const vehicleId = Number(header.vehicleId);
      const grossWeight = Number(header.grossWeight);
      if (!Number.isFinite(poId) || poId <= 0) {
        setError('Select a purchase order.');
        return;
      }
      if (!Number.isFinite(vehicleId) || vehicleId <= 0) {
        setError('Select a vehicle.');
        return;
      }
      if (!header.dateIn || !header.timeIn) {
        setError('Date and time in are required.');
        return;
      }
      if (!Number.isFinite(grossWeight) || grossWeight <= 0) {
        setError('Gross weight must be a positive number.');
        return;
      }
      setError('');
      const payload = {
        serialNo: header.serialNo || undefined,
        poId,
        vehicleId,
        dateIn: header.dateIn,
        timeIn: header.timeIn,
        grossWeight
      };
      const response = await apiClient.post('/api/weighbridge/tickets', payload);
      const shouldUnload = header.unloadedWeight !== '' && header.unloadedWeight !== null && !Number.isNaN(Number(header.unloadedWeight));
      if (shouldUnload && response.data?.id) {
        await apiClient.put(`/api/weighbridge/tickets/${response.data.id}/unload`, {
          poId,
          vehicleId,
          secondDate: header.secondDate || null,
          secondTime: header.secondTime || null,
          unloadedWeight: Number(header.unloadedWeight)
        });
      }
      navigate('/purchase/weighbridge-in');
    } catch (err) {
      setError(err?.message || 'Failed to save weighbridge ticket.');
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
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving || !canSave}>
            Save Entry
          </Button>
        }
      />
      <Stack spacing={3}>
        {error && <Alert severity="error">{error}</Alert>}
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
              required
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
              onChange={(nextValue) => {
                setHeader((prev) => ({ ...prev, vehicleId: nextValue }));
                setError('');
              }}
              optionLabelKey="vehicleNo"
              optionValueKey="id"
              placeholder="Search vehicles"
              required
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Supplier"
              value={poInfo?.supplierName || ''}
              InputProps={{ readOnly: true }}
              placeholder="Auto from PO"
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Product"
              value={
                poInfo?.lines?.length === 1
                  ? itemMap[poInfo.lines[0].itemId] || poInfo.lines[0].itemId
                  : poInfo?.lines?.length
                    ? 'Multiple items'
                    : ''
              }
              InputProps={{ readOnly: true }}
              placeholder="Auto from PO"
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
              onChange={(event) => {
                setHeader((prev) => ({ ...prev, grossWeight: event.target.value }));
                setError('');
              }}
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
              Supplier: {poInfo.supplierName || poInfo.supplierId || '-'} | Lines: {poInfo.lines?.length || 0}
            </Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Item</TableCell>
                  <TableCell>UOM</TableCell>
                  <TableCell>Qty</TableCell>
                  <TableCell>Rate</TableCell>
                  <TableCell>Amount</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(poInfo.lines || []).map((line) => (
                  <TableRow key={line.id}>
                    <TableCell>{itemMap[line.itemId] || line.itemId}</TableCell>
                    <TableCell>{uomMap[line.uomId] || line.uomId || '-'}</TableCell>
                    <TableCell>{line.quantity ?? '-'}</TableCell>
                    <TableCell>{line.rate ?? '-'}</TableCell>
                    <TableCell>{line.amount ?? formatAmount(line.quantity, line.rate)}</TableCell>
                  </TableRow>
                ))}
                {!poInfo.lines?.length && (
                  <TableRow>
                    <TableCell colSpan={5}>No PO lines</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
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
