import PropTypes from 'prop-types';
import { useMemo, useState } from 'react';

import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
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
import EntitySelect from 'components/common/EntitySelect';
import { vouchersApi } from 'api/vouchers';

const emptyLine = { id: Date.now(), item: '', qty: 0, rate: 0, amount: 0, tax: 5 };
const deductionFields = [
  { key: 'weightShortage', label: 'Weight Shortage' },
  { key: 'rateReduction', label: 'Rate Reduction' },
  { key: 'refractionDeduction', label: 'Refraction Deduction' },
  { key: 'looseBagPacking', label: 'Loose Bag Packing' },
  { key: 'discountPercent', label: 'Discount %' },
  { key: 'bagWeightDeduction', label: 'Bag Weight Deduction' },
  { key: 'weighmentDeduction', label: 'Weighment Deduction' },
  { key: 'freightDeduction', label: 'Freight Deduction' },
  { key: 'brokerageCommission', label: 'Brokerage Commission' },
  { key: 'haltingCharges', label: 'Halting Charges' },
  { key: 'unloadingCharges', label: 'Unloading Charges' },
  { key: 'tdsPercent', label: 'TDS %' }
];

export default function VoucherPage({ title, type }) {
  const [header, setHeader] = useState({
    voucherNo: '',
    date: new Date().toISOString().slice(0, 10),
    party: null,
    costCenter: null,
    placeOfSupply: ''
  });
  const [narration, setNarration] = useState('');
  const [lines, setLines] = useState([emptyLine]);
  const [deductions, setDeductions] = useState({
    weightShortage: 0,
    rateReduction: 0,
    refractionDeduction: 0,
    looseBagPacking: 0,
    discountPercent: 0,
    bagWeightDeduction: 0,
    weighmentDeduction: 0,
    freightDeduction: 0,
    brokerageCommission: 0,
    haltingCharges: 0,
    unloadingCharges: 0,
    tdsPercent: 0
  });
  const [previewOpen, setPreviewOpen] = useState(false);

  const totals = useMemo(() => {
    const taxable = lines.reduce((sum, line) => sum + Number(line.amount || 0), 0);
    const taxAmount = lines.reduce((sum, line) => sum + (Number(line.amount || 0) * Number(line.tax || 0)) / 100, 0);
    const grossAmount = taxable + taxAmount;
    const discount = (grossAmount * Number(deductions.discountPercent || 0)) / 100;
    const deductionsTotal = Object.entries(deductions).reduce((sum, [key, value]) => {
      if (key === 'tdsPercent' || key === 'discountPercent') return sum;
      return sum + Number(value || 0);
    }, 0) + discount;
    const tdsAmount = (grossAmount * Number(deductions.tdsPercent || 0)) / 100;
    const netPayable = grossAmount - deductionsTotal - tdsAmount;
    return {
      taxable,
      cgst: taxAmount / 2,
      sgst: taxAmount / 2,
      igst: 0,
      roundOff: 0,
      grandTotal: grossAmount,
      deductionsTotal,
      tdsAmount,
      netPayable
    };
  }, [lines, deductions]);

  const updateLine = (index, key, value) => {
    setLines((prev) => {
      const next = [...prev];
      const line = { ...next[index], [key]: value };
      line.amount = Number(line.qty || 0) * Number(line.rate || 0);
      next[index] = line;
      return next;
    });
  };

  const handleAddLine = () => {
    setLines((prev) => [...prev, { ...emptyLine, id: Date.now() + prev.length }]);
  };

  const handleRemoveLine = (index) => {
    setLines((prev) => prev.filter((_, idx) => idx !== index));
  };

  const handleSave = async (status) => {
      const payload = {
        type,
        status,
        header: {
          voucherNo: header.voucherNo,
          date: header.date,
          party: header.party?.name || header.party?.label || header.party || '',
          costCenter: header.costCenter?.name || header.costCenter?.label || header.costCenter || '',
          placeOfSupply: header.placeOfSupply,
          narration
        },
        lines,
        totals,
        deductions
      };
      await vouchersApi.create(payload);
  };

  return (
    <MainCard>
      <PageHeader title={title} breadcrumbs={[{ label: 'Vouchers' }, { label: title }]} />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <TextField
              fullWidth
              label="Voucher No"
              value={header.voucherNo}
              onChange={(event) => setHeader((prev) => ({ ...prev, voucherNo: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <TextField
              fullWidth
              type="date"
              label="Date"
              value={header.date}
              onChange={(event) => setHeader((prev) => ({ ...prev, date: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <EntitySelect
              label="Party / Ledger"
              endpoint="/settings/ledgers"
              value={header.party}
              onChange={(value) => setHeader((prev) => ({ ...prev, party: value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <EntitySelect
              label="Cost Center"
              endpoint="/settings/cost-centers"
              value={header.costCenter}
              onChange={(value) => setHeader((prev) => ({ ...prev, costCenter: value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Place of Supply (GST)"
              value={header.placeOfSupply}
              onChange={(event) => setHeader((prev) => ({ ...prev, placeOfSupply: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 8 }}>
            <TextField
              fullWidth
              label="Narration"
              value={narration}
              onChange={(event) => setNarration(event.target.value)}
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
                <TableCell>Tax %</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lines.map((line, index) => (
                <TableRow key={line.id}>
                  <TableCell>
                    <EntitySelect
                      label="Item"
                      endpoint="/settings/stock-items"
                      value={line.item ? { name: line.item, id: line.item } : null}
                      onChange={(value) => updateLine(index, 'item', value?.name || value?.label || value?.title || value?.id || '')}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      value={line.qty}
                      type="number"
                      onChange={(event) => updateLine(index, 'qty', event.target.value)}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField
                      value={line.rate}
                      type="number"
                      onChange={(event) => updateLine(index, 'rate', event.target.value)}
                    />
                  </TableCell>
                  <TableCell>
                    <TextField value={line.amount} type="number" InputProps={{ readOnly: true }} />
                  </TableCell>
                  <TableCell>
                    <TextField
                      value={line.tax}
                      type="number"
                      onChange={(event) => updateLine(index, 'tax', event.target.value)}
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
        <Divider />
        {(type === 'purchase-invoice' || type === 'debit-note') && (
          <>
            <Stack spacing={2}>
              <Typography variant="h5">Deductions</Typography>
              <Grid container spacing={2}>
                {deductionFields.map((field) => (
                  <Grid key={field.key} size={{ xs: 12, md: 4 }}>
                    <TextField
                      fullWidth
                      type="number"
                      label={field.label}
                      value={deductions[field.key]}
                      onChange={(event) =>
                        setDeductions((prev) => ({
                          ...prev,
                          [field.key]: event.target.value
                        }))
                      }
                    />
                  </Grid>
                ))}
              </Grid>
            </Stack>
            <Divider />
          </>
        )}
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Stack spacing={1}>
              <Typography variant="h5">Summary</Typography>
              <Stack direction="row" justifyContent="space-between">
                <Typography>Taxable</Typography>
                <Typography>{totals.taxable.toFixed(2)}</Typography>
              </Stack>
              <Stack direction="row" justifyContent="space-between">
                <Typography>CGST</Typography>
                <Typography>{totals.cgst.toFixed(2)}</Typography>
              </Stack>
              <Stack direction="row" justifyContent="space-between">
                <Typography>SGST</Typography>
                <Typography>{totals.sgst.toFixed(2)}</Typography>
              </Stack>
              <Stack direction="row" justifyContent="space-between">
                <Typography>IGST</Typography>
                <Typography>{totals.igst.toFixed(2)}</Typography>
              </Stack>
              <Stack direction="row" justifyContent="space-between">
                <Typography>Round Off</Typography>
                <Typography>{totals.roundOff.toFixed(2)}</Typography>
              </Stack>
              {(type === 'purchase-invoice' || type === 'debit-note') && (
                <>
                  <Stack direction="row" justifyContent="space-between">
                    <Typography>Deductions</Typography>
                    <Typography>{totals.deductionsTotal.toFixed(2)}</Typography>
                  </Stack>
                  <Stack direction="row" justifyContent="space-between">
                    <Typography>TDS</Typography>
                    <Typography>{totals.tdsAmount.toFixed(2)}</Typography>
                  </Stack>
                </>
              )}
              <Stack direction="row" justifyContent="space-between">
                <Typography variant="h6">Grand Total</Typography>
                <Typography variant="h6">{totals.grandTotal.toFixed(2)}</Typography>
              </Stack>
              {(type === 'purchase-invoice' || type === 'debit-note') && (
                <Stack direction="row" justifyContent="space-between">
                  <Typography variant="h6">Net Payable</Typography>
                  <Typography variant="h6">{totals.netPayable.toFixed(2)}</Typography>
                </Stack>
              )}
            </Stack>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Stack direction="row" spacing={2} justifyContent="flex-end" alignItems="center" sx={{ height: '100%' }}>
              <Button variant="outlined" onClick={() => setPreviewOpen(true)}>
                Print Preview
              </Button>
              <Button variant="outlined" onClick={() => handleSave('DRAFT')}>
                Save Draft
              </Button>
              <Button variant="contained" color="secondary" onClick={() => handleSave('POSTED')}>
                Post
              </Button>
            </Stack>
          </Grid>
        </Grid>
      </Stack>
      <Dialog open={previewOpen} onClose={() => setPreviewOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Print Preview</DialogTitle>
        <DialogContent>
          <Box sx={{ p: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Preview template placeholder. Integrate with actual print layout later.
            </Typography>
          </Box>
        </DialogContent>
      </Dialog>
    </MainCard>
  );
}

VoucherPage.propTypes = {
  title: PropTypes.string.isRequired,
  type: PropTypes.string.isRequired
};
