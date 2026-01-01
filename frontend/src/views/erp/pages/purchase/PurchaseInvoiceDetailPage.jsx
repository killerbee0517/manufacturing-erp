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
  typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : `charge-${Date.now()}-${Math.random()}`;

const newCharge = () => ({
  clientId: generateId(),
  chargeTypeId: '',
  calcType: 'FLAT',
  rate: '',
  amount: '',
  isDeduction: false,
  payablePartyType: 'EXPENSE',
  payablePartyId: '',
  remarks: ''
});

const partyEndpoint = (type) => {
  switch (type) {
    case 'SUPPLIER':
      return '/api/suppliers';
    case 'BROKER':
      return '/api/brokers';
    case 'VEHICLE':
      return '/api/vehicles';
    case 'EXPENSE':
    default:
      return '/api/expense-parties';
  }
};

export default function PurchaseInvoiceDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ supplierInvoiceNo: '', invoiceDate: '', narration: '' });
  const [brokerId, setBrokerId] = useState('');
  const [charges, setCharges] = useState([newCharge()]);
  const [saving, setSaving] = useState(false);
  const [posting, setPosting] = useState(false);

  const fetchInvoice = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`/api/purchase-invoices/${id}`);
      const payload = response.data;
      setInvoice(payload);
      setForm({
        supplierInvoiceNo: payload.supplierInvoiceNo || '',
        invoiceDate: payload.invoiceDate || '',
        narration: payload.narration || ''
      });
      setBrokerId(payload.brokerId || '');
      setCharges(
        (payload.charges || []).map((charge) => ({
          ...charge,
          clientId: generateId()
        }))
      );
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
  const baseAmount = useMemo(() => Number(invoice?.subtotal || 0), [invoice]);

  const totals = useMemo(
    () =>
      (charges || []).reduce(
        (acc, charge) => {
          const amount = Number(charge.amount || 0);
          if (charge.isDeduction) {
            acc.deductions += amount;
          } else {
            acc.additions += amount;
          }
          return acc;
        },
        { additions: 0, deductions: 0 }
      ),
    [charges]
  );

  const handleChargeChange = (index, key, value) => {
    setCharges((prev) => {
      const next = [...prev];
      const updated = { ...next[index], [key]: value };
      if (key === 'calcType' || key === 'rate') {
        const rate = Number(updated.rate || 0);
        if (updated.calcType === 'PERCENT') {
          updated.amount = ((baseAmount * rate) / 100).toFixed(2);
        } else if (updated.calcType === 'FLAT') {
          updated.amount = rate ? rate.toFixed(2) : '';
        }
      }
      if (key === 'payablePartyType') {
        updated.payablePartyId = '';
      }
      next[index] = updated;
      return next;
    });
  };

  const handleAddCharge = () => setCharges((prev) => [...prev, newCharge()]);
  const handleRemoveCharge = (clientId) => setCharges((prev) => prev.filter((charge) => charge.clientId !== clientId));

  const handleSave = async () => {
    setSaving(true);
    try {
      await apiClient.put(`/api/purchase-invoices/${id}`, {
        supplierInvoiceNo: form.supplierInvoiceNo || null,
        invoiceDate: form.invoiceDate || invoice.invoiceDate,
        narration: form.narration,
        brokerId: brokerId || null,
        charges: (charges || [])
          .filter((charge) => charge.chargeTypeId)
          .map((charge) => ({
            chargeTypeId: Number(charge.chargeTypeId),
            calcType: charge.calcType,
            rate: charge.rate === '' ? null : Number(charge.rate),
            amount: charge.amount === '' ? null : Number(charge.amount),
            isDeduction: !!charge.isDeduction,
            payablePartyType: charge.payablePartyType,
            payablePartyId: charge.payablePartyId === '' ? null : Number(charge.payablePartyId),
            remarks: charge.remarks || null
          }))
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
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Broker"
              endpoint="/api/brokers"
              value={brokerId}
              onChange={setBrokerId}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Search broker"
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
        <Stack spacing={1}>
          <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Typography variant="h5">Charges & Deductions</Typography>
            {isDraft && (
              <Button variant="outlined" onClick={handleAddCharge}>
                Add Charge
              </Button>
            )}
          </Stack>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Type</TableCell>
                <TableCell>Calc</TableCell>
                <TableCell>Rate</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Deduction</TableCell>
                <TableCell>Party Type</TableCell>
                <TableCell>Party</TableCell>
                <TableCell>Remarks</TableCell>
                {isDraft && <TableCell align="right">Actions</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {charges.map((charge, index) => (
                <TableRow key={charge.clientId}>
                  <TableCell>
                    <MasterAutocomplete
                      label=""
                      endpoint="/api/deduction-charge-types"
                      value={charge.chargeTypeId || ''}
                      onChange={(value) => handleChargeChange(index, 'chargeTypeId', value)}
                      optionLabelKey="name"
                      optionValueKey="id"
                      placeholder="Type"
                      size="small"
                      disabled={!isDraft}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      select
                      fullWidth
                      size="small"
                      value={charge.calcType || 'FLAT'}
                      onChange={(event) => handleChargeChange(index, 'calcType', event.target.value)}
                      disabled={!isDraft}
                    >
                      <MenuItem value="FLAT">Flat</MenuItem>
                      <MenuItem value="PERCENT">Percent</MenuItem>
                    </TextField>
                  </TableCell>
                  <TableCell>
                    <TextField
                      fullWidth
                      type="number"
                      size="small"
                      value={charge.rate}
                      onChange={(event) => handleChargeChange(index, 'rate', event.target.value)}
                      disabled={!isDraft}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      fullWidth
                      type="number"
                      size="small"
                      value={charge.amount}
                      onChange={(event) => handleChargeChange(index, 'amount', event.target.value)}
                      disabled={!isDraft}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      select
                      fullWidth
                      size="small"
                      value={charge.isDeduction ? 'YES' : 'NO'}
                      onChange={(event) => handleChargeChange(index, 'isDeduction', event.target.value === 'YES')}
                      disabled={!isDraft}
                    >
                      <MenuItem value="NO">No</MenuItem>
                      <MenuItem value="YES">Yes</MenuItem>
                    </TextField>
                  </TableCell>
                  <TableCell>
                    <TextField
                      select
                      fullWidth
                      size="small"
                      value={charge.payablePartyType || 'EXPENSE'}
                      onChange={(event) => handleChargeChange(index, 'payablePartyType', event.target.value)}
                      disabled={!isDraft}
                    >
                      <MenuItem value="SUPPLIER">Supplier</MenuItem>
                      <MenuItem value="BROKER">Broker</MenuItem>
                      <MenuItem value="VEHICLE">Vehicle</MenuItem>
                      <MenuItem value="EXPENSE">Expense</MenuItem>
                    </TextField>
                  </TableCell>
                  <TableCell>
                    <MasterAutocomplete
                      label=""
                      endpoint={partyEndpoint(charge.payablePartyType)}
                      value={charge.payablePartyId || ''}
                      onChange={(value) => handleChargeChange(index, 'payablePartyId', value)}
                      optionValueKey="id"
                      placeholder="Party"
                      size="small"
                      disabled={!isDraft}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      fullWidth
                      size="small"
                      value={charge.remarks || ''}
                      onChange={(event) => handleChargeChange(index, 'remarks', event.target.value)}
                      disabled={!isDraft}
                    />
                  </TableCell>
                  {isDraft && (
                    <TableCell align="right">
                      <IconButton onClick={() => handleRemoveCharge(charge.clientId)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  )}
                </TableRow>
              ))}
              {!charges.length && (
                <TableRow>
                  <TableCell colSpan={isDraft ? 9 : 8}>No charges configured.</TableCell>
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
            <Typography variant="subtitle2">Charge Additions</Typography>
            <Typography>{totals.additions.toFixed(2)}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Charge Deductions</Typography>
            <Typography>{totals.deductions.toFixed(2)}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Total Amount</Typography>
            <Typography>{invoice.totalAmount ?? 0}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">TDS</Typography>
            <Typography>{invoice.tdsAmount ?? 0}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Net Payable</Typography>
            <Typography>{invoice.netPayable ?? 0}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Brokerage</Typography>
            <Typography>{invoice.brokerageAmount ?? 0}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Status</Typography>
            <Typography>{invoice.status}</Typography>
          </Grid>
        </Grid>
      </Stack>
    </MainCard>
  );
}
