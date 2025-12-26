import { useState } from 'react';
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
  brokerId: '',
  quantity: '',
  rateExpected: '',
  remarks: ''
});

export default function RfqCreatePage() {
  const navigate = useNavigate();
  const [header, setHeader] = useState({
    rfqDate: new Date().toISOString().slice(0, 10),
    paymentTerms: '',
    narration: ''
  });
  const [supplierInput, setSupplierInput] = useState('');
  const [suppliers, setSuppliers] = useState([]);
  const [lines, setLines] = useState([emptyLine()]);
  const [saving, setSaving] = useState(false);

  const getLineAmount = (line) => {
    const quantity = Number(line.quantity);
    const rate = Number(line.rateExpected);
    if (!Number.isFinite(quantity) || !Number.isFinite(rate)) {
      return '';
    }
    const amount = quantity * rate;
    return amount ? amount.toFixed(2) : '';
  };

  const updateLine = (index, key, value) => {
    setLines((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [key]: value };
      return next;
    });
  };

  const handleAddLine = () => {
    setLines((prev) => [...prev, emptyLine()]);
  };

  const handleRemoveLine = (index) => {
    setLines((prev) => (prev.length <= 1 ? prev : prev.filter((_, idx) => idx !== index)));
  };

  const addSupplier = (supplierId, label) => {
    if (!supplierId) return;
    setSuppliers((prev) => {
      if (prev.find((s) => s.id === supplierId)) return prev;
      return [...prev, { id: supplierId, name: label }];
    });
    setSupplierInput('');
  };

  const removeSupplier = (supplierId) => {
    setSuppliers((prev) => prev.filter((s) => s.id !== supplierId));
  };

  const handleSave = async () => {
    if (!suppliers.length) {
      alert('Please add at least one supplier before saving.');
      return;
    }
    setSaving(true);
    try {
      const payload = {
        supplierIds: suppliers.map((s) => Number(s.id)),
        rfqDate: header.rfqDate,
        paymentTerms: header.paymentTerms,
        narration: header.narration,
        lines: lines.map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          brokerId: line.brokerId ? Number(line.brokerId) : null,
          quantity: Number(line.quantity),
          rateExpected: line.rateExpected ? Number(line.rateExpected) : null,
          remarks: line.remarks
        }))
      };
      const response = await apiClient.post('/api/rfq', payload);
      navigate(`/purchase/rfq/${response.data.id}`);
    } finally {
      setSaving(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="Create RFQ"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/rfq' }, { label: 'Create' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
            Save
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Stack spacing={1}>
              <Typography variant="h6">Suppliers</Typography>
              <MasterAutocomplete
                label="Add Supplier"
                endpoint="/api/suppliers"
                value={supplierInput}
                onChange={(nextValue, option) => {
                  setSupplierInput(nextValue);
                  addSupplier(nextValue, option?.name);
                }}
                optionLabelKey="name"
                optionValueKey="id"
                placeholder="Search suppliers"
              />
              <Stack direction="row" spacing={1} flexWrap="wrap">
                {suppliers.map((supplier) => (
                  <Button key={supplier.id} variant="outlined" onClick={() => removeSupplier(supplier.id)}>
                    {supplier.name || supplier.id} ✕
                  </Button>
                ))}
              </Stack>
            </Stack>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="RFQ Date"
              value={header.rfqDate}
              onChange={(event) => setHeader((prev) => ({ ...prev, rfqDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Payment Terms"
              value={header.paymentTerms}
              onChange={(event) => setHeader((prev) => ({ ...prev, paymentTerms: event.target.value }))}
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
                <TableCell>Broker</TableCell>
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
                    <MasterAutocomplete
                      label="Broker"
                      endpoint="/api/brokers"
                      value={line.brokerId}
                      onChange={(nextValue) => updateLine(index, 'brokerId', nextValue)}
                      optionLabelKey="name"
                      optionValueKey="id"
                      placeholder="Search brokers"
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
                      value={line.rateExpected}
                      onChange={(event) => updateLine(index, 'rateExpected', event.target.value)}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField value={getLineAmount(line)} InputProps={{ readOnly: true }} />
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
      </Stack>
    </MainCard>
  );
}
