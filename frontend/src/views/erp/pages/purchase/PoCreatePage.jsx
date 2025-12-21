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

const emptyLine = () => ({
  key: Date.now() + Math.random(),
  itemId: '',
  uomId: '',
  quantity: '',
  rate: '',
  amount: '',
  remarks: ''
});

export default function PoCreatePage() {
  const navigate = useNavigate();
  const [header, setHeader] = useState({
    poNo: '',
    supplierId: '',
    poDate: new Date().toISOString().slice(0, 10),
    remarks: ''
  });
  const [lines, setLines] = useState([emptyLine()]);
  const [suppliers, setSuppliers] = useState([]);
  const [items, setItems] = useState([]);
  const [uoms, setUoms] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiClient.get('/api/suppliers').then((res) => setSuppliers(res.data || [])).catch(() => setSuppliers([]));
    apiClient.get('/api/items').then((res) => setItems(res.data || [])).catch(() => setItems([]));
    apiClient.get('/api/uoms').then((res) => setUoms(res.data || [])).catch(() => setUoms([]));
  }, []);

  const updateLine = (index, key, value) => {
    setLines((prev) => {
      const next = [...prev];
      const updated = { ...next[index], [key]: value };
      const qty = Number(updated.quantity || 0);
      const rate = Number(updated.rate || 0);
      updated.amount = qty && rate ? (qty * rate).toFixed(2) : '';
      next[index] = updated;
      return next;
    });
  };

  const handleAddLine = () => {
    setLines((prev) => [...prev, emptyLine()]);
  };

  const handleRemoveLine = (index) => {
    setLines((prev) => (prev.length <= 1 ? prev : prev.filter((_, idx) => idx !== index)));
  };

  const totalAmount = useMemo(
    () => lines.reduce((sum, line) => sum + Number(line.amount || 0), 0),
    [lines]
  );

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = {
        poNo: header.poNo,
        supplierId: Number(header.supplierId),
        poDate: header.poDate,
        remarks: header.remarks,
        lines: lines.map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          quantity: Number(line.quantity),
          rate: Number(line.rate),
          amount: line.amount ? Number(line.amount) : null,
          remarks: line.remarks
        }))
      };
      const response = await apiClient.post('/api/purchase-orders', payload);
      navigate(`/purchase/purchase-order/${response.data.id}`);
    } finally {
      setSaving(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="Create Purchase Order"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/purchase-order' }, { label: 'Create' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
            Save
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="PO No"
              value={header.poNo}
              onChange={(event) => setHeader((prev) => ({ ...prev, poNo: event.target.value }))}
            />
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
              type="date"
              label="PO Date"
              value={header.poDate}
              onChange={(event) => setHeader((prev) => ({ ...prev, poDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Remarks"
              value={header.remarks}
              onChange={(event) => setHeader((prev) => ({ ...prev, remarks: event.target.value }))}
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
                <TableCell>Qty</TableCell>
                <TableCell>Rate</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Remarks</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lines.map((line, index) => (
                <TableRow key={line.key}>
                  <TableCell>
                    <TextField
                      select
                      value={line.itemId}
                      onChange={(event) => updateLine(index, 'itemId', event.target.value)}
                      fullWidth
                    >
                      {items.map((item) => (
                        <MenuItem key={item.id} value={item.id}>
                          {item.name}
                        </MenuItem>
                      ))}
                    </TextField>
                  </TableCell>
                  <TableCell>
                    <TextField
                      select
                      value={line.uomId}
                      onChange={(event) => updateLine(index, 'uomId', event.target.value)}
                      fullWidth
                    >
                      {uoms.map((uom) => (
                        <MenuItem key={uom.id} value={uom.id}>
                          {uom.code}
                        </MenuItem>
                      ))}
                    </TextField>
                  </TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={line.quantity}
                      onChange={(event) => updateLine(index, 'quantity', event.target.value)}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={line.rate}
                      onChange={(event) => updateLine(index, 'rate', event.target.value)}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField value={line.amount} InputProps={{ readOnly: true }} />
                  </TableCell>
                  <TableCell>
                    <TextField
                      value={line.remarks}
                      onChange={(event) => updateLine(index, 'remarks', event.target.value)}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button color="error" onClick={() => handleRemoveLine(index)}>
                      Remove
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <Button variant="outlined" onClick={handleAddLine} sx={{ alignSelf: 'flex-start' }}>
            Add Line
          </Button>
        </Stack>
        <Divider />
        <Stack direction="row" justifyContent="flex-end">
          <Typography variant="h6">Total: {totalAmount.toFixed(2)}</Typography>
        </Stack>
      </Stack>
    </MainCard>
  );
}
