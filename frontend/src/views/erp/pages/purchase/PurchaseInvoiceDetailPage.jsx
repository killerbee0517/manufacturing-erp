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

export default function PurchaseInvoiceDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ supplierInvoiceNo: '', invoiceDate: '', narration: '' });
  const [saving, setSaving] = useState(false);
  const [posting, setPosting] = useState(false);

  const fetchInvoice = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`/api/purchase-invoices/${id}`);
      setInvoice(response.data);
      setForm({
        supplierInvoiceNo: response.data.supplierInvoiceNo || '',
        invoiceDate: response.data.invoiceDate || '',
        narration: response.data.narration || ''
      });
    } catch {
      setInvoice(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInvoice();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const isDraft = useMemo(() => invoice?.status === 'DRAFT', [invoice]);

  const handleSave = async () => {
    setSaving(true);
    try {
      await apiClient.put(`/api/purchase-invoices/${id}`, {
        supplierInvoiceNo: form.supplierInvoiceNo || null,
        invoiceDate: form.invoiceDate || invoice.invoiceDate,
        narration: form.narration
      });
      await fetchInvoice();
    } finally {
      setSaving(false);
    }
  };

  const handlePost = async () => {
    setPosting(true);
    try {
      await apiClient.post(`/api/purchase-invoices/${id}/post`);
      await fetchInvoice();
    } finally {
      setPosting(false);
    }
  };

  if (!invoice) {
    return (
      <MainCard>
        <PageHeader
          title="Purchase Invoice Detail"
          breadcrumbs={[{ label: 'Purchase', to: '/purchase/purchase-invoice' }, { label: 'Detail' }]}
        />
        <Typography>{loading ? 'Loading...' : 'Purchase invoice not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`Purchase Invoice ${invoice.invoiceNo}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/purchase-invoice' }, { label: 'Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate('/purchase/purchase-invoice')}>
              Back to List
            </Button>
            <Button variant="contained" color="secondary" onClick={handleSave} disabled={!isDraft || saving}>
              Save
            </Button>
            <Button variant="contained" color="primary" onClick={handlePost} disabled={!isDraft || posting}>
              Post Invoice
            </Button>
          </Stack>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Supplier</Typography>
            <Typography>{invoice.supplierName || invoice.supplierId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Purchase Order</Typography>
            <Typography>{invoice.purchaseOrderNo || invoice.purchaseOrderId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">GRN</Typography>
            <Typography>{invoice.grnNo || invoice.grnId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Supplier Invoice No"
              value={form.supplierInvoiceNo}
              onChange={(event) => setForm((prev) => ({ ...prev, supplierInvoiceNo: event.target.value }))}
              disabled={!isDraft}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="Invoice Date"
              value={form.invoiceDate}
              onChange={(event) => setForm((prev) => ({ ...prev, invoiceDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
              disabled={!isDraft}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
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
          <Typography variant="h5">Line Items</Typography>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Item</TableCell>
                <TableCell>Qty</TableCell>
                <TableCell>Rate</TableCell>
                <TableCell>Amount</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {invoice.lines?.map((line) => (
                <TableRow key={line.id}>
                  <TableCell>{line.itemName || line.itemId || '-'}</TableCell>
                  <TableCell>{line.quantity ?? '-'}</TableCell>
                  <TableCell>{line.rate ?? '-'}</TableCell>
                  <TableCell>{line.amount ?? '-'}</TableCell>
                </TableRow>
              ))}
              {!invoice.lines?.length && (
                <TableRow>
                  <TableCell colSpan={4}>No line items.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Stack>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Subtotal</Typography>
            <Typography>{invoice.subtotal ?? 0}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Tax</Typography>
            <Typography>{invoice.taxTotal ?? 0}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Round Off</Typography>
            <Typography>{invoice.roundOff ?? 0}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Grand Total</Typography>
            <Typography variant="h6">{invoice.grandTotal ?? 0}</Typography>
          </Grid>
        </Grid>
      </Stack>
    </MainCard>
  );
}
