import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

const toLine = (line) => ({
  key: `${line.id}-${Date.now()}`,
  itemId: line.itemId,
  uomId: line.uomId,
  quantity: line.quantity,
  weight: line.weight || line.quantity
});

export default function GrnCreatePage() {
  const navigate = useNavigate();
  const [header, setHeader] = useState({
    purchaseOrderId: '',
    supplierId: '',
    weighbridgeTicketId: '',
    godownId: '',
    grnDate: new Date().toISOString().slice(0, 10),
    firstWeight: '',
    secondWeight: '',
    narration: ''
  });
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [lines, setLines] = useState([]);
  const [saving, setSaving] = useState(false);
  const [supplierMap, setSupplierMap] = useState({});
  const [itemMap, setItemMap] = useState({});
  const [uomMap, setUomMap] = useState({});

  useEffect(() => {
    apiClient
      .get('/api/purchase-orders')
      .then((res) => {
        const payload = res.data?.content || res.data || [];
        setPurchaseOrders(payload);
      })
      .catch(() => setPurchaseOrders([]));
    apiClient.get('/api/weighbridge/tickets').then((res) => setTickets(res.data || [])).catch(() => setTickets([]));

    apiClient
      .get('/api/suppliers')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, supplier) => {
          acc[supplier.id] = supplier.name;
          return acc;
        }, {});
        setSupplierMap(lookup);
      })
      .catch(() => setSupplierMap({}));

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

  const netWeight = useMemo(() => {
    const first = Number(header.firstWeight || 0);
    const second = Number(header.secondWeight || 0);
    if (!first && !second) return 0;
    return Math.abs(second - first);
  }, [header.firstWeight, header.secondWeight]);

  const handlePurchaseOrderChange = async (poId) => {
    setHeader((prev) => ({ ...prev, purchaseOrderId: poId }));
    if (!poId) {
      setLines([]);
      setHeader((prev) => ({ ...prev, supplierId: '' }));
      return;
    }
    const response = await apiClient.get(`/api/purchase-orders/${poId}`);
    const po = response.data;
    setHeader((prev) => ({ ...prev, supplierId: po.supplierId || '' }));
    setLines((po.lines || []).map(toLine));
  };

  const handleTicketChange = async (ticketId) => {
    setHeader((prev) => ({ ...prev, weighbridgeTicketId: ticketId }));
    if (!ticketId) return;
    const response = await apiClient.get(`/api/weighbridge/tickets/${ticketId}`);
    const ticket = response.data;
    if (ticket.poId) {
      await handlePurchaseOrderChange(ticket.poId);
      setHeader((prev) => ({
        ...prev,
        purchaseOrderId: ticket.poId,
        supplierId: ticket.supplierId || prev.supplierId
      }));
    }
    setHeader((prev) => ({
      ...prev,
      firstWeight: ticket.grossWeight || '',
      secondWeight: ticket.unloadedWeight || '',
      grnDate: prev.grnDate
    }));
  };

  const updateLine = (index, key, value) => {
    setLines((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [key]: value };
      return next;
    });
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = {
        purchaseOrderId: Number(header.purchaseOrderId),
        weighbridgeTicketId: header.weighbridgeTicketId ? Number(header.weighbridgeTicketId) : null,
        godownId: Number(header.godownId),
        grnDate: header.grnDate,
        firstWeight: header.firstWeight ? Number(header.firstWeight) : null,
        secondWeight: header.secondWeight ? Number(header.secondWeight) : null,
        netWeight,
        narration: header.narration,
        lines: lines.map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          quantity: Number(line.quantity),
          weight: line.weight ? Number(line.weight) : null
        }))
      };
      const response = await apiClient.post('/api/grn', payload);
      navigate(`/purchase/grn/${response.data.id}`);
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
            <TextField
              fullWidth
              label="Supplier"
              value={supplierMap[header.supplierId] || ''}
              InputProps={{ readOnly: true }}
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
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Weighbridge Ticket"
              value={header.weighbridgeTicketId}
              onChange={(event) => handleTicketChange(event.target.value)}
            >
              <MenuItem value="">None</MenuItem>
              {tickets.map((ticket) => (
                <MenuItem key={ticket.id} value={ticket.id}>
                  {ticket.serialNo}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
        </Grid>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="1st Weight"
              value={header.firstWeight}
              onChange={(event) => setHeader((prev) => ({ ...prev, firstWeight: event.target.value }))}
              InputProps={{ readOnly: Boolean(header.weighbridgeTicketId) }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="2nd Weight"
              value={header.secondWeight}
              onChange={(event) => setHeader((prev) => ({ ...prev, secondWeight: event.target.value }))}
              InputProps={{ readOnly: Boolean(header.weighbridgeTicketId) }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField fullWidth label="Net Weight" value={netWeight} InputProps={{ readOnly: true }} />
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
        <Divider />
        <Stack spacing={1}>
          <Typography variant="h5">Line Items</Typography>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Item</TableCell>
                <TableCell>UOM</TableCell>
                <TableCell>Quantity</TableCell>
                <TableCell>Weight</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lines.map((line, index) => (
                <TableRow key={line.key}>
                  <TableCell>{itemMap[line.itemId] || line.itemId}</TableCell>
                  <TableCell>{uomMap[line.uomId] || line.uomId}</TableCell>
                  <TableCell>
                    <TextField type="number" value={line.quantity} InputProps={{ readOnly: true }} />
                  </TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={line.weight}
                      onChange={(event) => updateLine(index, 'weight', event.target.value)}
                      placeholder="Optional"
                      InputProps={{ readOnly: Boolean(header.weighbridgeTicketId) }}
                    />
                  </TableCell>
                </TableRow>
              ))}
              {!lines.length && (
                <TableRow>
                  <TableCell colSpan={4}>Select a purchase order to load line items.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Stack>
      </Stack>
    </MainCard>
  );
}
