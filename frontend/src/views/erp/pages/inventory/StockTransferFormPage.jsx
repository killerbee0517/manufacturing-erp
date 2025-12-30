import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

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

import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import PageHeader from 'components/common/PageHeader';
import MainCard from 'ui-component/cards/MainCard';

const newLine = () => ({
  key: (globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`),
  id: null,
  itemId: '',
  uomId: '',
  qty: ''
});

export default function StockTransferFormPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);
  const [saving, setSaving] = useState(false);
  const [posting, setPosting] = useState(false);
  const [header, setHeader] = useState({
    transferNo: '',
    fromGodownId: '',
    toGodownId: '',
    transferDate: new Date().toISOString().slice(0, 10),
    status: 'DRAFT',
    narration: ''
  });
  const [lines, setLines] = useState([newLine()]);

  const payload = useMemo(
    () => ({
      transferNo: isEdit ? header.transferNo || null : null,
      fromGodownId: header.fromGodownId ? Number(header.fromGodownId) : null,
      toGodownId: header.toGodownId ? Number(header.toGodownId) : null,
      transferDate: header.transferDate || null,
      narration: header.narration,
      lines: lines
        .filter((line) => line.itemId && line.uomId)
        .map((line) => ({
          id: line.id,
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          qty: Number(line.qty || 0)
        }))
    }),
    [header, lines, isEdit]
  );

  const load = async () => {
    if (!isEdit) return;
    const response = await apiClient.get(`/api/stock-transfers/${id}`);
    const data = response.data;
    setHeader({
      transferNo: data.transferNo || '',
      fromGodownId: data.fromGodownId || '',
      toGodownId: data.toGodownId || '',
      transferDate: data.transferDate || '',
      status: data.status || 'DRAFT',
      narration: data.narration || ''
    });
    setLines(
      (data.lines || []).map((line) => ({
        key: (globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`),
        id: line.id,
        itemId: line.itemId,
        uomId: line.uomId,
        qty: line.qty
      }))
    );
  };

  useEffect(() => {
    load();
  }, [id, isEdit]);

  const updateLine = (index, field, value) => {
    setLines((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
  };

  const addLine = () => setLines((prev) => [...prev, newLine()]);
  const removeLine = (index) => setLines((prev) => (prev.length <= 1 ? prev : prev.filter((_, idx) => idx !== index)));

  const handleSave = async () => {
    setSaving(true);
    try {
      if (isEdit) {
        await apiClient.put(`/api/stock-transfers/${id}`, payload);
      } else {
        await apiClient.post('/api/stock-transfers', payload);
      }
      navigate('/inventory/stock-transfer');
    } finally {
      setSaving(false);
    }
  };

  const handlePost = async () => {
    if (!isEdit) return;
    setPosting(true);
    try {
      await apiClient.post('/api/stock-transfers/post', { id: Number(id) });
      await load();
    } finally {
      setPosting(false);
    }
  };

  const disableEditing = header.status === 'POSTED';

  return (
    <MainCard>
      <PageHeader
        title={isEdit ? 'Edit Stock Transfer' : 'Create Stock Transfer'}
        breadcrumbs={[
          { label: 'Inventory', to: '/inventory/stock-transfer' },
          { label: isEdit ? 'Edit' : 'New' }
        ]}
        actions={
          <Stack direction="row" spacing={1}>
            {isEdit && (
              <Button variant="outlined" color="success" onClick={handlePost} disabled={disableEditing || posting}>
                Post
              </Button>
            )}
            <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving || disableEditing}>
              Save
            </Button>
          </Stack>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          {isEdit && (
            <Grid size={{ xs: 12, md: 4 }}>
              <TextField fullWidth label="Transfer No" value={header.transferNo} disabled />
            </Grid>
          )}
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="From Godown"
              endpoint="/api/godowns"
              value={header.fromGodownId}
              onChange={(value) => setHeader((prev) => ({ ...prev, fromGodownId: value }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Select godown"
              disabled={disableEditing}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="To Godown"
              endpoint="/api/godowns"
              value={header.toGodownId}
              onChange={(value) => setHeader((prev) => ({ ...prev, toGodownId: value }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Select godown"
              disabled={disableEditing}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="Transfer Date"
              value={header.transferDate || ''}
              onChange={(event) => setHeader((prev) => ({ ...prev, transferDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
              disabled={disableEditing}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              multiline
              minRows={2}
              label="Narration"
              value={header.narration}
              onChange={(event) => setHeader((prev) => ({ ...prev, narration: event.target.value }))}
              disabled={disableEditing}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField fullWidth label="Status" value={header.status} disabled />
          </Grid>
        </Grid>
        <Divider />
        <Stack spacing={1}>
          <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Typography variant="h5">Line Items</Typography>
            <Button size="small" variant="outlined" onClick={addLine} disabled={disableEditing}>
              Add Line
            </Button>
          </Stack>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Item</TableCell>
                <TableCell>UOM</TableCell>
                <TableCell>Quantity</TableCell>
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
                      disabled={disableEditing}
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
                      disabled={disableEditing}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={line.qty}
                      onChange={(event) => updateLine(index, 'qty', event.target.value)}
                      size="small"
                      disabled={disableEditing}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button size="small" color="error" onClick={() => removeLine(index)} disabled={disableEditing}>
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
