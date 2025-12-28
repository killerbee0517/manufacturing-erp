import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import IconButton from '@mui/material/IconButton';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import DeleteIcon from '@mui/icons-material/Delete';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';

const generateId = () =>
  typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `line-${Date.now()}-${Math.random()}`;

const newLine = () => ({
  clientId: generateId(),
  description: '',
  baseValue: '',
  rate: '',
  amount: '',
  remarks: ''
});

export default function DebitNoteDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [note, setNote] = useState(null);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ dnDate: new Date().toISOString().slice(0, 10), narration: '' });
  const [lines, setLines] = useState([newLine()]);
  const [saving, setSaving] = useState(false);
  const [posting, setPosting] = useState(false);

  const fetchNote = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`/api/debit-notes/${id}`);
      setNote(response.data);
      setForm({
        dnDate: response.data.dnDate || new Date().toISOString().slice(0, 10),
        narration: response.data.narration || ''
      });
      setLines(
        (response.data.lines || []).map((line) => ({
          ...line,
          clientId: generateId()
        }))
      );
    } catch {
      setNote(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNote();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const isDraft = useMemo(() => note?.status === 'DRAFT', [note]);
  const totalAmount = useMemo(
    () => (lines || []).reduce((sum, line) => sum + Number(line.amount || 0), 0),
    [lines]
  );

  const handleLineChange = (index, key, value) => {
    setLines((prev) => {
      const next = [...prev];
      const updated = { ...next[index], [key]: value };
      if (key === 'baseValue' || key === 'rate') {
        const base = Number(updated.baseValue || 0);
        const rate = Number(updated.rate || 0);
        updated.amount = ((base * rate) / 100).toFixed(2);
      }
      next[index] = updated;
      return next;
    });
  };

  const handleAddLine = () => setLines((prev) => [...prev, newLine()]);
  const handleRemoveLine = (clientId) => setLines((prev) => prev.filter((line) => line.clientId !== clientId));

  const handleSave = async () => {
    setSaving(true);
    try {
      await apiClient.put(`/api/debit-notes/${id}`, {
        dnDate: form.dnDate,
        narration: form.narration,
        lines: lines.map((line) => ({
          id: line.id,
          ruleId: line.ruleId,
          description: line.description,
          baseValue: line.baseValue === '' ? null : Number(line.baseValue),
          rate: line.rate === '' ? null : Number(line.rate),
          amount: line.amount === '' ? null : Number(line.amount),
          remarks: line.remarks
        }))
      });
      await fetchNote();
    } finally {
      setSaving(false);
    }
  };

  const handlePost = async () => {
    setPosting(true);
    try {
      await apiClient.post(`/api/debit-notes/${id}/post`);
      await fetchNote();
    } finally {
      setPosting(false);
    }
  };

  if (!note) {
    return (
      <MainCard>
        <PageHeader
          title="Debit Note Detail"
          breadcrumbs={[{ label: 'Purchase', to: '/purchase/debit-note' }, { label: 'Detail' }]}
        />
        <Typography>{loading ? 'Loading...' : 'Debit note not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`Debit Note ${note.dnNo}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/debit-note' }, { label: 'Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate('/purchase/debit-note')}>
              Back to List
            </Button>
            <Button variant="contained" color="secondary" onClick={handleSave} disabled={!isDraft || saving}>
              Save
            </Button>
            <Button variant="contained" color="primary" onClick={handlePost} disabled={!isDraft || posting}>
              Post Debit Note
            </Button>
          </Stack>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Supplier</Typography>
            <Typography>{note.supplierName || note.supplierId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">PO</Typography>
            <Typography>{note.purchaseOrderNo || note.purchaseOrderId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">GRN</Typography>
            <Typography>{note.grnNo || note.grnId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Purchase Invoice</Typography>
            <Typography>{note.purchaseInvoiceNo || note.purchaseInvoiceId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="Debit Note Date"
              value={form.dnDate}
              onChange={(event) => setForm((prev) => ({ ...prev, dnDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
              disabled={!isDraft}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 8 }}>
            <TextField
              fullWidth
              label="Narration"
              value={form.narration}
              onChange={(event) => setForm((prev) => ({ ...prev, narration: event.target.value }))}
              disabled={!isDraft}
            />
          </Grid>
        </Grid>
        <Divider />
        <Stack spacing={1}>
          <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Typography variant="h5">Deduction Lines</Typography>
            {isDraft && (
              <Button variant="outlined" onClick={handleAddLine}>
                Add Line
              </Button>
            )}
          </Stack>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Description</TableCell>
                <TableCell>Base</TableCell>
                <TableCell>Rate %</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Remarks</TableCell>
                {isDraft && <TableCell align="right">Actions</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {lines.map((line, index) => (
                <TableRow key={line.clientId}>
                  <TableCell>
                    <TextField
                      fullWidth
                      value={line.description || ''}
                      onChange={(event) => handleLineChange(index, 'description', event.target.value)}
                      disabled={!isDraft}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      fullWidth
                      type="number"
                      value={line.baseValue}
                      onChange={(event) => handleLineChange(index, 'baseValue', event.target.value)}
                      disabled={!isDraft}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      fullWidth
                      type="number"
                      value={line.rate}
                      onChange={(event) => handleLineChange(index, 'rate', event.target.value)}
                      disabled={!isDraft}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      fullWidth
                      type="number"
                      value={line.amount}
                      onChange={(event) => handleLineChange(index, 'amount', event.target.value)}
                      disabled={!isDraft}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      fullWidth
                      value={line.remarks || ''}
                      onChange={(event) => handleLineChange(index, 'remarks', event.target.value)}
                      disabled={!isDraft}
                    />
                  </TableCell>
                  {isDraft && (
                    <TableCell align="right">
                      <IconButton onClick={() => handleRemoveLine(line.clientId)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  )}
                </TableRow>
              ))}
              {!lines.length && (
                <TableRow>
                  <TableCell colSpan={isDraft ? 6 : 5}>No deduction lines.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Stack>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Total Deduction</Typography>
            <Typography variant="h6">{totalAmount.toFixed(2)}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Status</Typography>
            <Typography>{note.status}</Typography>
          </Grid>
        </Grid>
      </Stack>
    </MainCard>
  );
}
