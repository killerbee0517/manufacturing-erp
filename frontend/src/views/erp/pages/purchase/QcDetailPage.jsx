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
import Alert from '@mui/material/Alert';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import apiClient from 'api/client';

const mapLines = (lines) =>
  (lines || []).map((line) => ({
    poLineId: line.poLineId,
    itemName: line.itemName || line.itemId || '-',
    uomCode: line.uomCode || '-',
    receivedQty: line.receivedQty ?? '',
    acceptedQty: line.acceptedQty ?? '',
    rejectedQty: line.rejectedQty ?? '',
    reason: line.reason || ''
  }));

export default function QcDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [qc, setQc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [approving, setApproving] = useState(false);
  const [rejecting, setRejecting] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    sampleQty: '',
    sampleUomId: '',
    method: '',
    remarks: ''
  });
  const [lines, setLines] = useState([]);

  const isEditable = useMemo(() => qc?.status !== 'APPROVED', [qc?.status]);

  const loadQc = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`/api/qc/inspections/${id}`);
      const payload = response.data;
      setQc(payload);
      setForm({
        sampleQty: payload.sampleQty ?? '',
        sampleUomId: payload.sampleUomId ?? '',
        method: payload.method || '',
        remarks: payload.remarks || ''
      });
      setLines(mapLines(payload.lines));
      setError('');
    } catch {
      setQc(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadQc();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const updateLine = (index, key, value) => {
    setLines((prev) => {
      const next = [...prev];
      const current = next[index] || {};
      const updated = { ...current, [key]: value };
      if (key === 'rejectedQty') {
        const received = Number(updated.receivedQty || 0);
        const rejected = Number(updated.rejectedQty || 0);
        const accepted = Math.max(0, received - rejected);
        updated.acceptedQty = Number.isFinite(accepted) ? accepted : 0;
      }
      next[index] = updated;
      return next;
    });
  };

  const buildPayload = () => ({
    sampleQty: form.sampleQty === '' ? null : Number(form.sampleQty),
    sampleUomId: form.sampleUomId ? Number(form.sampleUomId) : null,
    method: form.method || null,
    remarks: form.remarks || null,
    lines: lines.map((line) => ({
      poLineId: line.poLineId,
      receivedQty: Number(line.receivedQty || 0),
      acceptedQty: Number(line.acceptedQty || 0),
      rejectedQty: Number(line.rejectedQty || 0),
      reason: line.reason || null
    }))
  });

  const handleApprove = async () => {
    setApproving(true);
    try {
      await apiClient.put(`/api/qc/inspections/${id}`, buildPayload());
      const response = await apiClient.post(`/api/qc/inspections/${id}/approve`);
      const grnId = response.data?.grnId;
      if (grnId) {
        navigate(`/purchase/grn/${grnId}`);
      } else {
        await loadQc();
      }
    } catch (err) {
      setError(err?.message || 'Failed to approve QC inspection.');
    } finally {
      setApproving(false);
    }
  };

  const handleReject = async () => {
    setRejecting(true);
    try {
      await apiClient.post(`/api/qc/inspections/${id}/reject`);
      await loadQc();
    } catch (err) {
      setError(err?.message || 'Failed to reject QC inspection.');
    } finally {
      setRejecting(false);
    }
  };

  if (!qc) {
    return (
      <MainCard>
        <PageHeader title="QC Detail" breadcrumbs={[{ label: 'Purchase', to: '/purchase/qc' }, { label: 'Detail' }]} />
        <Typography>{loading ? 'Loading...' : 'QC inspection not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title="QC Detail"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/qc' }, { label: 'QC Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            {qc.grnId && (
              <Button variant="outlined" onClick={() => navigate(`/purchase/grn/${qc.grnId}`)}>
                View GRN
              </Button>
            )}
            <Button variant="contained" color="primary" onClick={handleApprove} disabled={!isEditable || approving}>
              Approve
            </Button>
            <Button variant="outlined" color="error" onClick={handleReject} disabled={!isEditable || rejecting}>
              Reject
            </Button>
          </Stack>
        }
      />
      <Stack spacing={3}>
        {error && <Alert severity="error">{error}</Alert>}
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">PO</Typography>
            <Typography>{qc.purchaseOrderId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Weighbridge Ticket</Typography>
            <Typography>{qc.weighbridgeTicketId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Status</Typography>
            <Typography>{qc.status || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Inspection Date</Typography>
            <Typography>{qc.inspectionDate || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <TextField
              fullWidth
              type="number"
              label="Sample Qty"
              value={form.sampleQty}
              onChange={(event) => setForm((prev) => ({ ...prev, sampleQty: event.target.value }))}
              disabled={!isEditable}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <MasterAutocomplete
              label="Sample UOM"
              endpoint="/api/uoms"
              value={form.sampleUomId}
              onChange={(nextValue) => setForm((prev) => ({ ...prev, sampleUomId: nextValue }))}
              optionLabelKey="code"
              optionValueKey="id"
              disabled={!isEditable}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <TextField
              fullWidth
              label="Method"
              value={form.method}
              onChange={(event) => setForm((prev) => ({ ...prev, method: event.target.value }))}
              disabled={!isEditable}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <TextField
              fullWidth
              label="Remarks"
              value={form.remarks}
              onChange={(event) => setForm((prev) => ({ ...prev, remarks: event.target.value }))}
              disabled={!isEditable}
            />
          </Grid>
        </Grid>
        <Divider />
        <Stack spacing={1}>
          <Typography variant="h5">Items</Typography>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Item</TableCell>
                <TableCell>UOM</TableCell>
                <TableCell>Received</TableCell>
                <TableCell>Accepted</TableCell>
                <TableCell>Rejected</TableCell>
                <TableCell>Reason</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lines.map((line, index) => (
                <TableRow key={line.poLineId || index}>
                  <TableCell>{line.itemName}</TableCell>
                  <TableCell>{line.uomCode}</TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={line.receivedQty}
                      InputProps={{ readOnly: true }}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={line.acceptedQty}
                      InputProps={{ readOnly: true }}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      type="number"
                      value={line.rejectedQty}
                      onChange={(event) => updateLine(index, 'rejectedQty', event.target.value)}
                      disabled={!isEditable}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      value={line.reason}
                      onChange={(event) => updateLine(index, 'reason', event.target.value)}
                      disabled={!isEditable}
                    />
                  </TableCell>
                </TableRow>
              ))}
              {!lines.length && (
                <TableRow>
                  <TableCell colSpan={6}>No QC lines.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Stack>
      </Stack>
    </MainCard>
  );
}
