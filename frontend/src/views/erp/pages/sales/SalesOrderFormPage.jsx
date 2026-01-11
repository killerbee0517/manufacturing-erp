import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

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

import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import PageHeader from 'components/common/PageHeader';
import MainCard from 'ui-component/cards/MainCard';
import CompanyField from 'components/common/CompanyField';

const newLine = () => ({
  key: (globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`),
  id: null,
  itemId: '',
  uomId: '',
  quantity: '',
  rate: ''
});

export default function SalesOrderFormPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);
  const [saving, setSaving] = useState(false);
  const [header, setHeader] = useState({
    soNo: '',
    customerId: '',
    orderDate: new Date().toISOString().slice(0, 10),
    status: 'DRAFT',
    narration: ''
  });
  const [lines, setLines] = useState([newLine()]);

  const payload = useMemo(
    () => ({
      soNo: header.soNo || null,
      customerId: header.customerId ? Number(header.customerId) : null,
      orderDate: header.orderDate || null,
      status: header.status,
      narration: header.narration,
      lines: lines
        .filter((line) => line.itemId && line.uomId)
        .map((line) => ({
          id: line.id,
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          quantity: Number(line.quantity || 0),
          rate: Number(line.rate || 0)
        }))
    }),
    [header, lines]
  );

  useEffect(() => {
    if (!isEdit) return;
    apiClient.get(`/api/sales-orders/${id}`).then((response) => {
      const data = response.data;
      setHeader({
        soNo: data.soNo || '',
        customerId: data.customerId || '',
        orderDate: data.orderDate || '',
        status: data.status || 'DRAFT',
        narration: data.narration || ''
      });
      setLines(
        (data.lines || []).map((line) => ({
          key: (globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`),
          id: line.id,
          itemId: line.itemId,
          uomId: line.uomId,
          quantity: line.quantity,
          rate: line.rate
        }))
      );
    });
  }, [id, isEdit]);

  const updateLine = (index, field, value) => {
    setLines((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
  };

  const addLine = () => setLines((prev) => [...prev, newLine()]);

  const removeLine = (index) => {
    setLines((prev) => (prev.length <= 1 ? prev : prev.filter((_, idx) => idx !== index)));
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      if (isEdit) {
        await apiClient.put(`/api/sales-orders/${id}`, payload);
      } else {
        await apiClient.post('/api/sales-orders', payload);
      }
      navigate('/sales/sales-order');
    } finally {
      setSaving(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title={isEdit ? 'Edit Sales Order' : 'Create Sales Order'}
        breadcrumbs={[
          { label: 'Sales', to: '/sales/sales-order' },
          { label: isEdit ? 'Edit' : 'New' }
        ]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
            Save
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <CompanyField />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Sales Order No"
              value={header.soNo}
              onChange={(event) => setHeader((prev) => ({ ...prev, soNo: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Customer"
              endpoint="/api/customers"
              value={header.customerId}
              onChange={(nextValue) => setHeader((prev) => ({ ...prev, customerId: nextValue }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Search customers"
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="Order Date"
              value={header.orderDate || ''}
              onChange={(event) => setHeader((prev) => ({ ...prev, orderDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Status"
              value={header.status}
              onChange={(event) => setHeader((prev) => ({ ...prev, status: event.target.value }))}
            >
              <MenuItem value="DRAFT">Draft</MenuItem>
              <MenuItem value="POSTED">Posted</MenuItem>
              <MenuItem value="CLOSED">Closed</MenuItem>
            </TextField>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              multiline
              minRows={2}
              label="Narration"
              value={header.narration}
              onChange={(event) => setHeader((prev) => ({ ...prev, narration: event.target.value }))}
            />
          </Grid>
        </Grid>
        <Divider />
        <Stack spacing={1}>
          <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Typography variant="h5">Line Items</Typography>
            <Button size="small" variant="outlined" onClick={addLine}>
              Add Line
            </Button>
          </Stack>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Item</TableCell>
                <TableCell>UOM</TableCell>
                <TableCell>Qty</TableCell>
                <TableCell>Rate</TableCell>
                <TableCell>Amount</TableCell>
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
                      onChange={(value) => updateLine(index, 'itemId', value)}
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
                      onChange={(value) => updateLine(index, 'uomId', value)}
                      optionLabelKey="code"
                      optionValueKey="id"
                      placeholder="Search UOM"
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={line.quantity}
                      onChange={(event) => updateLine(index, 'quantity', event.target.value)}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={line.rate}
                      onChange={(event) => updateLine(index, 'rate', event.target.value)}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{Number(line.quantity || 0) * Number(line.rate || 0) || ''}</TableCell>
                  <TableCell align="right">
                    <Button size="small" color="error" onClick={() => removeLine(index)}>
                      Remove
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Stack>
      </Stack>
    </MainCard>
  );
}
