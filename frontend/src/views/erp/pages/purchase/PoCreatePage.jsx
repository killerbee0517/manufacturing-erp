import { useMemo, useState } from 'react';
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

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

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
    supplierId: '',
    poDate: new Date().toISOString().slice(0, 10),
    deliveryDate: '',
    supplierInvoiceNo: '',
    purchaseLedger: '',
    currentLedgerBalance: 0,
    narration: ''
  });
  const [lines, setLines] = useState([emptyLine()]);
  const [saving, setSaving] = useState(false);

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
        supplierId: Number(header.supplierId),
        poDate: header.poDate,
        deliveryDate: header.deliveryDate || null,
        supplierInvoiceNo: header.supplierInvoiceNo,
        purchaseLedger: header.purchaseLedger,
        currentLedgerBalance: Number(header.currentLedgerBalance || 0),
        narration: header.narration,
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
      navigate(`/purchase/po/${response.data.id}`);
    } finally {
      setSaving(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="Create Purchase Order"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/po' }, { label: 'Create' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
            Save
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Supplier"
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
              label="PO Date"
              value={header.poDate}
              onChange={(event) => setHeader((prev) => ({ ...prev, poDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="Delivery Date"
              value={header.deliveryDate}
              onChange={(event) => setHeader((prev) => ({ ...prev, deliveryDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Supplier Invoice No"
              value={header.supplierInvoiceNo}
              onChange={(event) => setHeader((prev) => ({ ...prev, supplierInvoiceNo: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Purchase Ledger"
              value={header.purchaseLedger}
              onChange={(event) => setHeader((prev) => ({ ...prev, purchaseLedger: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Current Ledger Balance"
              value={header.currentLedgerBalance}
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
                    <MasterAutocomplete
                      label="Item"
                      endpoint="/api/items"
                      value={line.itemId}
                      onChange={(nextValue) => updateLine(index, 'itemId', nextValue)}
                      optionLabelKey="name"
                      optionValueKey="id"
                      placeholder="Search items"
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <MasterAutocomplete
                      label="UOM"
                      endpoint="/api/uoms"
                      value={line.uomId}
                      onChange={(nextValue) => updateLine(index, 'uomId', nextValue)}
                      optionLabelKey="code"
                      optionValueKey="id"
                      placeholder="Search UOMs"
                      size="small"
                    />
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
