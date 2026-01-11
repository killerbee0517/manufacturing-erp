import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import IconButton from '@mui/material/IconButton';
import MenuItem from '@mui/material/MenuItem';
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
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import apiClient from 'api/client';

const generateId = () =>
  typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `alloc-${Date.now()}-${Math.random()}`;

const newAllocation = () => ({
  clientId: generateId(),
  purchaseInvoiceId: '',
  allocatedAmount: '',
  remarks: ''
});

const partyEndpoint = (type) => {
  switch (type) {
    case 'SUPPLIER':
      return '/api/suppliers';
    case 'CUSTOMER':
      return '/api/customers';
    case 'BROKER':
      return '/api/brokers';
    case 'EXPENSE':
      return '/api/expense-parties';
    default:
      return '/api/suppliers';
  }
};

export default function PaymentVoucherDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNew = id === 'new';
  const [voucher, setVoucher] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [posting, setPosting] = useState(false);
  const [clearing, setClearing] = useState(false);
  const [form, setForm] = useState({
    voucherDate: new Date().toISOString().slice(0, 10),
    partyType: 'SUPPLIER',
    partyId: '',
    paymentDirection: 'PAYABLE',
    paymentMode: 'BANK',
    bankId: '',
    amount: '',
    narration: '',
    chequeNumber: '',
    chequeDate: ''
  });
  const [allocations, setAllocations] = useState([newAllocation()]);

  const fetchVoucher = async () => {
    if (isNew) {
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const response = await apiClient.get(`/api/payment-vouchers/${id}`);
      const payload = response.data;
      setVoucher(payload);
      setForm({
        voucherDate: payload.voucherDate || new Date().toISOString().slice(0, 10),
        partyType: payload.partyType || 'SUPPLIER',
        partyId: payload.partyId || '',
        paymentDirection: payload.paymentDirection || 'PAYABLE',
        paymentMode: payload.paymentMode || 'BANK',
        bankId: payload.bankId || '',
        amount: payload.amount ?? '',
        narration: payload.narration || '',
        chequeNumber: payload.chequeNumber || '',
        chequeDate: payload.chequeDate || ''
      });
      setAllocations(
        (payload.allocations || []).map((allocation) => ({
          ...allocation,
          clientId: generateId()
        }))
      );
    } catch {
      setVoucher(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVoucher();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const isDraft = useMemo(() => (isNew ? true : voucher?.status === 'DRAFT'), [isNew, voucher]);
  const canClearPdc = useMemo(() => voucher?.status === 'PDC_ISSUED', [voucher]);
  const totalAllocated = useMemo(
    () => allocations.reduce((sum, alloc) => sum + Number(alloc.allocatedAmount || 0), 0),
    [allocations]
  );

  const handleAllocationChange = (index, key, value) => {
    setAllocations((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [key]: value };
      return next;
    });
  };

  const handleAddAllocation = () => setAllocations((prev) => [...prev, newAllocation()]);
  const handleRemoveAllocation = (clientId) => setAllocations((prev) => prev.filter((line) => line.clientId !== clientId));

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = {
        voucherDate: form.voucherDate,
        partyType: form.partyType,
        partyId: form.partyId === '' ? null : Number(form.partyId),
        paymentDirection: form.paymentDirection,
        paymentMode: form.paymentMode,
        bankId:
          form.paymentMode === 'CASH' ? null : form.bankId === '' ? null : Number(form.bankId),
        amount: form.amount === '' ? 0 : Number(form.amount),
        narration: form.narration,
        chequeNumber: form.paymentMode === 'PDC' ? form.chequeNumber : null,
        chequeDate: form.paymentMode === 'PDC' ? form.chequeDate : null,
        allocations:
          form.partyType === 'SUPPLIER'
            ? allocations
                .filter((allocation) => allocation.purchaseInvoiceId)
                .map((allocation) => ({
                  purchaseInvoiceId: Number(allocation.purchaseInvoiceId),
                  allocatedAmount: allocation.allocatedAmount === '' ? 0 : Number(allocation.allocatedAmount),
                  remarks: allocation.remarks
                }))
            : []
      };
      const response = isNew
        ? await apiClient.post('/api/payment-vouchers', payload)
        : await apiClient.put(`/api/payment-vouchers/${id}`, payload);
      navigate(`/accounts/payments/${response.data.id}`);
    } finally {
      setSaving(false);
    }
  };

  const handlePost = async () => {
    if (isNew) return;
    setPosting(true);
    try {
      await apiClient.post(`/api/payment-vouchers/${id}/post`);
      await fetchVoucher();
    } finally {
      setPosting(false);
    }
  };

  const handleClearPdc = async () => {
    setClearing(true);
    try {
      await apiClient.post(`/api/payment-vouchers/${id}/clear-pdc`);
      await fetchVoucher();
    } finally {
      setClearing(false);
    }
  };

  if (!isNew && !voucher) {
    return (
      <MainCard>
        <PageHeader title="Payment Voucher" breadcrumbs={[{ label: 'Accounts', to: '/accounts/payments' }, { label: 'Detail' }]} />
        <Typography>{loading ? 'Loading...' : 'Payment voucher not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={isNew ? 'New Payment' : `Payment ${voucher?.voucherNo}`}
        breadcrumbs={[{ label: 'Accounts', to: '/accounts/payments' }, { label: isNew ? 'New' : 'Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate('/accounts/payments')}>
              Back to List
            </Button>
            <Button variant="contained" color="secondary" onClick={handleSave} disabled={!isDraft || saving}>
              Save
            </Button>
            <Button variant="contained" color="primary" onClick={handlePost} disabled={!isDraft || posting || isNew}>
              Post
            </Button>
            <Button variant="contained" color="primary" onClick={handleClearPdc} disabled={!canClearPdc || clearing}>
              Clear PDC
            </Button>
          </Stack>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="Voucher Date"
              value={form.voucherDate}
              onChange={(event) => setForm((prev) => ({ ...prev, voucherDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
              disabled={!isDraft}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              select
              fullWidth
              label="Party Type"
              value={form.partyType}
              onChange={(event) =>
                setForm((prev) => ({ ...prev, partyType: event.target.value, partyId: '' }))
              }
              disabled={!isDraft}
            >
              <MenuItem value="SUPPLIER">Supplier</MenuItem>
              <MenuItem value="CUSTOMER">Customer</MenuItem>
              <MenuItem value="BROKER">Broker</MenuItem>
              <MenuItem value="EXPENSE">Expense Party</MenuItem>
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Party"
              endpoint={partyEndpoint(form.partyType)}
              value={form.partyId}
              onChange={(value) => setForm((prev) => ({ ...prev, partyId: value }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Search party"
              disabled={!isDraft}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              select
              fullWidth
              label="Payment Direction"
              value={form.paymentDirection}
              onChange={(event) => setForm((prev) => ({ ...prev, paymentDirection: event.target.value }))}
              disabled={!isDraft}
            >
              <MenuItem value="PAYABLE">Payable</MenuItem>
              <MenuItem value="RECEIVABLE">Refund</MenuItem>
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              select
              fullWidth
              label="Payment Mode"
              value={form.paymentMode}
              onChange={(event) => setForm((prev) => ({ ...prev, paymentMode: event.target.value, bankId: '' }))}
              disabled={!isDraft}
            >
              <MenuItem value="BANK">Bank</MenuItem>
              <MenuItem value="CASH">Cash</MenuItem>
              <MenuItem value="PDC">PDC</MenuItem>
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Bank"
              endpoint="/api/banks"
              value={form.bankId}
              onChange={(value) => setForm((prev) => ({ ...prev, bankId: value }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Select bank"
              disabled={!isDraft || form.paymentMode === 'CASH'}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="Amount"
              value={form.amount}
              onChange={(event) => setForm((prev) => ({ ...prev, amount: event.target.value }))}
              disabled={!isDraft}
            />
          </Grid>
          {form.paymentMode === 'PDC' && (
            <>
              <Grid size={{ xs: 12, md: 4 }}>
                <TextField
                  fullWidth
                  label="Cheque Number"
                  value={form.chequeNumber}
                  onChange={(event) => setForm((prev) => ({ ...prev, chequeNumber: event.target.value }))}
                  disabled={!isDraft}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <TextField
                  fullWidth
                  type="date"
                  label="Cheque Date"
                  value={form.chequeDate}
                  onChange={(event) => setForm((prev) => ({ ...prev, chequeDate: event.target.value }))}
                  InputLabelProps={{ shrink: true }}
                  disabled={!isDraft}
                />
              </Grid>
            </>
          )}
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
        {form.partyType === 'SUPPLIER' && (
          <>
            <Divider />
            <Stack spacing={1}>
              <Stack direction="row" justifyContent="space-between" alignItems="center">
                <Typography variant="h5">Invoice Allocations</Typography>
                {isDraft && (
                  <Button variant="outlined" onClick={handleAddAllocation}>
                    Add Allocation
                  </Button>
                )}
              </Stack>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Invoice</TableCell>
                    <TableCell>Amount</TableCell>
                    <TableCell>Remarks</TableCell>
                    {isDraft && <TableCell align="right">Actions</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {allocations.map((allocation, index) => (
                    <TableRow key={allocation.clientId}>
                      <TableCell>
                        <MasterAutocomplete
                          label=""
                          endpoint="/api/purchase-invoices"
                          queryParams={{ status: 'POSTED', supplierId: form.partyId || undefined }}
                          value={allocation.purchaseInvoiceId || ''}
                          onChange={(value) => handleAllocationChange(index, 'purchaseInvoiceId', value)}
                          optionLabelKey="invoiceNo"
                          optionValueKey="id"
                          placeholder="Invoice"
                          size="small"
                          disabled={!isDraft || !form.partyId}
                        />
                      </TableCell>
                      <TableCell>
                        <TextField
                          fullWidth
                          type="number"
                          size="small"
                          value={allocation.allocatedAmount}
                          onChange={(event) => handleAllocationChange(index, 'allocatedAmount', event.target.value)}
                          disabled={!isDraft}
                        />
                      </TableCell>
                      <TableCell>
                        <TextField
                          fullWidth
                          size="small"
                          value={allocation.remarks || ''}
                          onChange={(event) => handleAllocationChange(index, 'remarks', event.target.value)}
                          disabled={!isDraft}
                        />
                      </TableCell>
                      {isDraft && (
                        <TableCell align="right">
                          <IconButton onClick={() => handleRemoveAllocation(allocation.clientId)}>
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                  {!allocations.length && (
                    <TableRow>
                      <TableCell colSpan={isDraft ? 4 : 3}>No allocations.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </Stack>
          </>
        )}
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Total Allocated</Typography>
            <Typography>{totalAllocated.toFixed(2)}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Status</Typography>
            <Typography>{voucher?.status || 'DRAFT'}</Typography>
          </Grid>
        </Grid>
      </Stack>
    </MainCard>
  );
}
