import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Alert from '@mui/material/Alert';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Snackbar from '@mui/material/Snackbar';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import apiClient from 'api/client';
import PageHeader from 'components/common/PageHeader';
import MainCard from 'ui-component/cards/MainCard';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

export default function RfqAwardPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [rfq, setRfq] = useState(null);
  const [awards, setAwards] = useState([]);
  const [saving, setSaving] = useState(false);
  const [supplierOptions, setSupplierOptions] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
  const [quoteCache, setQuoteCache] = useState({});

  useEffect(() => {
    apiClient.get(`/api/rfq/${id}`).then((response) => {
      const data = response.data;
      setRfq(data);
      const baseSuppliers = (data.suppliers || []).map((s) => s.supplierId);
      setSupplierOptions(baseSuppliers);
      const seed = (data.lines || []).flatMap((line) => {
        return [
          {
            key: `${line.id}-0`,
            rfqLineId: line.id,
            supplierId: '',
            qty: '',
            rate: ''
          }
        ];
      });
      setAwards(seed);
    });
  }, [id]);

  const addAwardRow = (lineId) => {
    setAwards((prev) => [
      ...prev,
      { key: `${lineId}-${Date.now()}`, rfqLineId: lineId, supplierId: '', qty: '', rate: '' }
    ]);
  };

  const updateAward = (key, patch) => {
    setAwards((prev) => prev.map((award) => (award.key === key ? { ...award, ...patch } : award)));
  };

  const removeAward = (key) => {
    setAwards((prev) => prev.filter((award) => award.key !== key));
  };

  const loadQuoteForSupplier = async (supplierId) => {
    if (!supplierId) return null;
    if (quoteCache[supplierId]) return quoteCache[supplierId];
    const response = await apiClient.get(`/api/rfq/${id}/quotes/${supplierId}`);
    setQuoteCache((prev) => ({ ...prev, [supplierId]: response.data }));
    return response.data;
  };

  const handleSupplierChange = async (award, supplierId) => {
    updateAward(award.key, { supplierId });
    if (!supplierId) return;
    try {
      const quote = await loadQuoteForSupplier(supplierId);
      const quoteLine = quote?.lines?.find((line) => line.rfqLineId === award.rfqLineId);
      if (quoteLine) {
        updateAward(award.key, {
          qty: quoteLine.quotedQty ?? '',
          rate: quoteLine.quotedRate ?? ''
        });
      }
    } catch {
      // ignore quote load failures
    }
  };

  const handleSubmit = async () => {
    setSaving(true);
    try {
      const payload = {
        awards: awards.map((award) => ({
          rfqLineId: award.rfqLineId,
          supplierId: Number(award.supplierId),
          awardQty: Number(award.qty || 0),
          awardRate: Number(award.rate || 0)
        })),
        closeRemaining: false
      };
      const response = await apiClient.post(`/api/rfq/${id}/award`, payload);
      const poIds = response.data?.poIdsBySupplier;
      const poRefs = response.data?.purchaseOrders || [];
      const firstPoId = poRefs[0]?.poId || (poIds ? Object.values(poIds).flat()[0] : null);
      if (firstPoId) {
        navigate(`/purchase/po/${firstPoId}`);
      } else {
        navigate(`/purchase/rfq/${id}`);
      }
    } catch (error) {
      setSnackbar({
        open: true,
        message: error?.response?.data?.message || 'Unable to award RFQ. Please check inputs.',
        severity: 'error'
      });
    } finally {
      setSaving(false);
    }
  };

  const handleSubmitRfq = async () => {
    if (!rfq) return;
    setSubmitting(true);
    try {
      await apiClient.post(`/api/rfq/${id}/submit`);
      const response = await apiClient.get(`/api/rfq/${id}`);
      setRfq(response.data);
      setSnackbar({ open: true, message: 'RFQ submitted successfully', severity: 'success' });
    } catch (error) {
      setSnackbar({
        open: true,
        message: error?.response?.data?.message || 'Unable to submit RFQ.',
        severity: 'error'
      });
    } finally {
      setSubmitting(false);
    }
  };

  if (!rfq) {
    return (
      <MainCard>
        <Typography>Loading...</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`Award RFQ ${rfq.rfqNo}`}
        breadcrumbs={[
          { label: 'Purchase', to: '/purchase/rfq' },
          { label: `RFQ ${rfq.rfqNo}`, to: `/purchase/rfq/${id}` },
          { label: 'Award' }
        ]}
        actions={
          <Button
            variant="contained"
            color="secondary"
            onClick={handleSubmit}
            disabled={saving || !['QUOTING', 'AWARDED_PARTIAL'].includes(rfq.status)}
          >
            Confirm Awards
          </Button>
        }
      />
      <Stack spacing={2}>
        {rfq.status === 'DRAFT' && (
          <Alert
            severity="info"
            action={
              <Button color="inherit" onClick={handleSubmitRfq} disabled={submitting}>
                Submit RFQ
              </Button>
            }
          >
            Submit the RFQ before awarding.
          </Alert>
        )}
        <Typography variant="body1">
          Assign quantities and rates per supplier. Remaining quantity must not exceed requested quantity.
        </Typography>
        <Divider />
        {(rfq.lines || []).map((line) => (
          <Stack key={line.id} spacing={1}>
            <Typography variant="h6">
              {line.quantity} units for Item #{line.itemId} (UOM #{line.uomId})
            </Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Supplier</TableCell>
                  <TableCell>Quantity</TableCell>
                  <TableCell>Rate</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {awards
                  .filter((award) => award.rfqLineId === line.id)
                  .map((award) => (
                    <TableRow key={award.key}>
                      <TableCell>
                        <MasterAutocomplete
                          label="Supplier"
                          endpoint="/api/suppliers"
                          value={award.supplierId}
                          onChange={(nextValue) => handleSupplierChange(award, nextValue)}
                          optionLabelKey="name"
                          optionValueKey="id"
                          placeholder="Choose supplier"
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <TextField
                          size="small"
                          type="number"
                          value={award.qty}
                          onChange={(event) => updateAward(award.key, { qty: event.target.value })}
                        />
                      </TableCell>
                      <TableCell>
                        <TextField
                          size="small"
                          type="number"
                          value={award.rate}
                          onChange={(event) => updateAward(award.key, { rate: event.target.value })}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Button color="error" onClick={() => removeAward(award.key)}>
                          Remove
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                <TableRow>
                  <TableCell colSpan={4}>
                    <Button onClick={() => addAwardRow(line.id)}>Add supplier award</Button>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </Stack>
        ))}
      </Stack>
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </MainCard>
  );
}
