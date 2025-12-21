import { useEffect, useState } from 'react';
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
  brokerId: '',
  quantity: '',
  rateExpected: '',
  remarks: ''
});

export default function RfqCreatePage() {
  const navigate = useNavigate();
  const [header, setHeader] = useState({
    rfqNo: '',
    supplierId: '',
    rfqDate: new Date().toISOString().slice(0, 10),
    paymentTerms: '',
    narration: ''
  });
  const [lines, setLines] = useState([emptyLine()]);
  const [suppliers, setSuppliers] = useState([]);
  const [items, setItems] = useState([]);
  const [uoms, setUoms] = useState([]);
  const [brokers, setBrokers] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiClient.get('/api/suppliers').then((res) => setSuppliers(res.data || [])).catch(() => setSuppliers([]));
    apiClient.get('/api/items').then((res) => setItems(res.data || [])).catch(() => setItems([]));
    apiClient.get('/api/uoms').then((res) => setUoms(res.data || [])).catch(() => setUoms([]));
    apiClient.get('/api/brokers').then((res) => setBrokers(res.data || [])).catch(() => setBrokers([]));
  }, []);

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

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = {
        rfqNo: header.rfqNo,
        supplierId: Number(header.supplierId),
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
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="RFQ No"
              value={header.rfqNo}
              onChange={(event) => setHeader((prev) => ({ ...prev, rfqNo: event.target.value }))}
              placeholder="Auto-generated"
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
                      select
                      value={line.brokerId}
                      onChange={(event) => updateLine(index, 'brokerId', event.target.value)}
                      fullWidth
                    >
                      <MenuItem value="">None</MenuItem>
                      {brokers.map((broker) => (
                        <MenuItem key={broker.id} value={broker.id}>
                          {broker.name}
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
                      value={line.rateExpected}
                      onChange={(event) => updateLine(index, 'rateExpected', event.target.value)}
                    />
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
