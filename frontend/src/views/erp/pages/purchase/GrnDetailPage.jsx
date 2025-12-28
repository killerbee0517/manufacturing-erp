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

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

export default function GrnDetailPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [grn, setGrn] = useState(null);
  const [loading, setLoading] = useState(true);
  const [godownId, setGodownId] = useState('');
  const [narration, setNarration] = useState('');
  const [saving, setSaving] = useState(false);
  const [posting, setPosting] = useState(false);

  const fetchGrn = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`/api/grn/${id}`);
      setGrn(response.data);
      setGodownId(response.data.godownId || '');
      setNarration(response.data.narration || '');
    } catch {
      setGrn(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGrn();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const isDraft = useMemo(() => grn?.status === 'DRAFT', [grn]);

  const handleSave = async () => {
    setSaving(true);
    try {
      await apiClient.put(`/api/grn/${id}`, {
        godownId: godownId || null,
        narration
      });
      await fetchGrn();
    } finally {
      setSaving(false);
    }
  };

  const handlePost = async () => {
    setPosting(true);
    try {
      await apiClient.post(`/api/grn/${id}/post`);
      await fetchGrn();
    } finally {
      setPosting(false);
    }
  };

  if (!grn) {
    return (
      <MainCard>
        <PageHeader title="GRN Detail" breadcrumbs={[{ label: 'Purchase', to: '/purchase/grn' }, { label: 'Detail' }]} />
        <Typography>{loading ? 'Loading...' : 'GRN not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`GRN ${grn.grnNo}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/grn' }, { label: 'GRN Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate('/purchase/grn')}>
              Back to GRN
            </Button>
            <Button variant="contained" color="secondary" onClick={handleSave} disabled={!isDraft || saving}>
              Save
            </Button>
            <Button variant="contained" color="primary" onClick={handlePost} disabled={!isDraft || posting || !godownId}>
              Post GRN
            </Button>
          </Stack>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Supplier</Typography>
            <Typography>{grn.supplierName || grn.supplierId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Purchase Order</Typography>
            <Typography>{grn.purchaseOrderNo || grn.purchaseOrderId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Weighbridge Ticket</Typography>
            <Typography>{grn.weighbridgeSerialNo || grn.weighbridgeTicketId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">GRN Date</Typography>
            <Typography>{grn.grnDate || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Gross Weight</Typography>
            <Typography>{grn.firstWeight ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Tare Weight</Typography>
            <Typography>{grn.secondWeight ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Net Weight</Typography>
            <Typography>{grn.netWeight ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Status</Typography>
            <Typography>{grn.status}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Godown</Typography>
            <MasterAutocomplete
              label="Godown"
              endpoint="/api/godowns"
              value={godownId}
              onChange={setGodownId}
              disabled={!isDraft}
              placeholder="Select godown"
              optionLabelKey="name"
              optionValueKey="id"
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Narration"
              value={narration}
              onChange={(event) => setNarration(event.target.value)}
              disabled={!isDraft}
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
                <TableCell>Expected Qty</TableCell>
                <TableCell>Received Qty</TableCell>
                <TableCell>Accepted Qty</TableCell>
                <TableCell>Rejected Qty</TableCell>
                <TableCell>Rate</TableCell>
                <TableCell>Amount</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {grn.lines?.map((line) => (
                <TableRow key={line.id}>
                  <TableCell>{line.itemName || line.itemId || '-'}</TableCell>
                  <TableCell>{line.expectedQty ?? '-'}</TableCell>
                  <TableCell>{line.receivedQty ?? '-'}</TableCell>
                  <TableCell>{line.acceptedQty ?? '-'}</TableCell>
                  <TableCell>{line.rejectedQty ?? '-'}</TableCell>
                  <TableCell>{line.rate ?? '-'}</TableCell>
                  <TableCell>{line.amount ?? '-'}</TableCell>
                </TableRow>
              ))}
              {!grn.lines?.length && (
                <TableRow>
                  <TableCell colSpan={7}>No line items.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Stack>
      </Stack>
    </MainCard>
  );
}
