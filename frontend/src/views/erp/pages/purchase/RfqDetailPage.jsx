import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import FormControl from '@mui/material/FormControl';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormLabel from '@mui/material/FormLabel';
import Grid from '@mui/material/Grid';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import Select from '@mui/material/Select';
import Stack from '@mui/material/Stack';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
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
import RfqCloseDialog from './components/RfqCloseDialog';

export default function RfqDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [rfq, setRfq] = useState(null);
  const [loading, setLoading] = useState(false);
  const [itemMap, setItemMap] = useState({});
  const [uomMap, setUomMap] = useState({});
  const [supplierMap, setSupplierMap] = useState({});
  const [brokerMap, setBrokerMap] = useState({});
  const [closeOpen, setCloseOpen] = useState(false);
  const [closing, setClosing] = useState(false);
  const [tab, setTab] = useState('request');
  const [quoteSupplierId, setQuoteSupplierId] = useState('');
  const [quoteForm, setQuoteForm] = useState({ paymentTermsOverride: '', remarks: '', lines: [] });
  const [quoteLoading, setQuoteLoading] = useState(false);
  const [compareQuotes, setCompareQuotes] = useState({});
  const [compareLoading, setCompareLoading] = useState(false);
  const [awardAllocations, setAwardAllocations] = useState({});
  const [awardErrors, setAwardErrors] = useState({});
  const [awarding, setAwarding] = useState(false);
  const [closeRemaining, setCloseRemaining] = useState(false);
  const [closureReason, setClosureReason] = useState('');

  const isFullyAwarded = rfq?.status === 'AWARDED_FULL';
  const isQuoteLocked = isFullyAwarded || rfq?.status === 'CANCELLED';
  const hasSubmittedQuotes = useMemo(
    () => Object.values(compareQuotes || {}).some((quote) => quote.status === 'SUBMITTED'),
    [compareQuotes]
  );

  const awardedByLine = useMemo(() => {
    if (!rfq?.awards) return {};
    return rfq.awards.reduce((acc, award) => {
      if (!award.rfqLineId) return acc;
      const existing = acc[award.rfqLineId] || { total: 0, bySupplier: {} };
      const qty = Number(award.awardQty || 0);
      const safeQty = Number.isFinite(qty) ? qty : 0;
      existing.total += safeQty;
      existing.bySupplier = { ...(existing.bySupplier || {}), [award.supplierId]: safeQty };
      acc[award.rfqLineId] = existing;
      return acc;
    }, {});
  }, [rfq?.awards]);

  const remainingQtyByLine = useMemo(() => {
    if (!rfq?.lines) return {};
    return rfq.lines.reduce((acc, line) => {
      const already = awardedByLine[line.id]?.total || 0;
      acc[line.id] = Math.max(0, Number(line.quantity || 0) - Number(already));
      return acc;
    }, {});
  }, [rfq?.lines, awardedByLine]);

  const getStatusColor = (status) => {
    if (['AWARDED_FULL', 'AWARDED_PARTIAL', 'AWARDED', 'PARTIALLY_AWARDED'].includes(status)) return 'success';
    if (status === 'QUOTING') return 'info';
    if (status === 'CANCELLED') return 'error';
    return 'default';
  };

  const formatAmount = (quantity, rate) => {
    const qty = Number(quantity);
    const rateNum = Number(rate);
    if (!Number.isFinite(qty) || !Number.isFinite(rateNum)) {
      return '-';
    }
    const amount = qty * rateNum;
    return Number.isFinite(amount) ? amount.toFixed(2) : '-';
  };

  const loadMasters = () => {
    apiClient
      .get('/api/items')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, item) => {
          acc[item.id] = item.name;
          return acc;
        }, {});
        setItemMap(lookup);
      })
      .catch(() => setItemMap({}));
    apiClient
      .get('/api/uoms')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, uom) => {
          acc[uom.id] = uom.code;
          return acc;
        }, {});
        setUomMap(lookup);
      })
      .catch(() => setUomMap({}));
    apiClient
      .get('/api/suppliers')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, supplier) => {
          acc[supplier.id] = supplier.name;
          return acc;
        }, {});
        setSupplierMap(lookup);
      })
      .catch(() => setSupplierMap({}));
    apiClient
      .get('/api/brokers')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, broker) => {
          acc[broker.id] = broker.name;
          return acc;
        }, {});
        setBrokerMap(lookup);
      })
      .catch(() => setBrokerMap({}));
  };

  const loadRfq = () => {
    setLoading(true);
    apiClient
      .get(`/api/rfq/${id}`)
      .then((response) => {
        const data = response.data;
        setRfq(data);
        setAwardAllocations({});
        setAwardErrors({});
        setCloseRemaining(false);
        setClosureReason('');
        if (!quoteSupplierId && data.suppliers?.length) {
          setQuoteSupplierId(data.suppliers[0].supplierId);
        }
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRfq();
    loadMasters();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (rfq && quoteSupplierId) {
      loadQuoteDetail(quoteSupplierId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [quoteSupplierId, rfq?.id]);

  useEffect(() => {
    if (tab === 'compare' && rfq) {
      loadComparisonQuotes();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab, rfq?.id]);

  const loadQuoteDetail = async (supplierId) => {
    if (!rfq) return;
    setQuoteLoading(true);
    try {
      const response = await apiClient.get(`/api/rfq/${id}/quotes/${supplierId}`);
      const quote = response.data;
      const lines = (rfq.lines || []).map((line) => {
        const quoteLine = quote.lines?.find((ql) => ql.rfqLineId === line.id) || {};
        return {
          rfqLineId: line.id,
          quotedQty: quoteLine.quotedQty ?? line.quantity,
          quotedRate: quoteLine.quotedRate ?? line.rateExpected ?? '',
          deliveryDate: quoteLine.deliveryDate ?? '',
          remarks: quoteLine.remarks ?? ''
        };
      });
      setQuoteForm({
        paymentTermsOverride: quote.paymentTermsOverride || '',
        remarks: quote.remarks || '',
        lines
      });
    } finally {
      setQuoteLoading(false);
    }
  };

  const handleQuoteLineChange = (lineId, key, value) => {
    setQuoteForm((prev) => ({
      ...prev,
      lines: prev.lines.map((line) => (line.rfqLineId === lineId ? { ...line, [key]: value } : line))
    }));
  };

  const buildQuotePayload = () => ({
    paymentTermsOverride: quoteForm.paymentTermsOverride || null,
    remarks: quoteForm.remarks || null,
    lines: quoteForm.lines.map((line) => ({
      rfqLineId: line.rfqLineId,
      quotedQty: line.quotedQty === '' ? null : Number(line.quotedQty),
      quotedRate: line.quotedRate === '' ? null : Number(line.quotedRate),
      deliveryDate: line.deliveryDate || null,
      remarks: line.remarks || null
    }))
  });

  const saveQuote = async () => {
    if (!quoteSupplierId) return;
    setQuoteLoading(true);
    try {
      await apiClient.put(`/api/rfq/${id}/quotes/${quoteSupplierId}`, buildQuotePayload());
      loadRfq();
    } finally {
      setQuoteLoading(false);
    }
  };

  const submitQuote = async () => {
    if (!quoteSupplierId) return;
    setQuoteLoading(true);
    try {
      await apiClient.put(`/api/rfq/${id}/quotes/${quoteSupplierId}`, buildQuotePayload());
      await apiClient.post(`/api/rfq/${id}/quotes/${quoteSupplierId}/submit`);
      loadRfq();
    } finally {
      setQuoteLoading(false);
    }
  };

  const loadComparisonQuotes = async () => {
    if (!rfq?.suppliers?.length) return;
    setCompareLoading(true);
    try {
      const responses = await Promise.all(
        rfq.suppliers.map(async (supplier) => {
          try {
            const result = await apiClient.get(`/api/rfq/${id}/quotes/${supplier.supplierId}`);
            return result.data;
          } catch (error) {
            return null;
          }
        })
      );
      const map = {};
      responses
        .filter(Boolean)
        .forEach((quote) => {
          map[quote.supplierId] = quote;
        });
      setCompareQuotes(map);
      buildAwardDefaults(map);
    } finally {
      setCompareLoading(false);
    }
  };

  const buildAwardDefaults = (quotesMap) => {
    if (!rfq) return;
    const submittedQuotes = Object.values(quotesMap || {}).filter((quote) => quote.status === 'SUBMITTED');
    const next = {};
    (rfq.lines || []).forEach((line) => {
      const remaining = remainingQtyByLine[line.id] ?? line.quantity;
      const bestQuote = submittedQuotes.reduce((best, quote) => {
        const qLine = quote.lines?.find((ql) => ql.rfqLineId === line.id);
        if (!qLine || qLine.quotedRate == null) return best;
        if (!best || Number(qLine.quotedRate) < Number(best.rate)) {
          return { supplierId: quote.supplierId, rate: qLine.quotedRate };
        }
        return best;
      }, null);
      const perSupplier = {};
      submittedQuotes.forEach((quote) => {
        const qLine = quote.lines?.find((ql) => ql.rfqLineId === line.id);
        perSupplier[quote.supplierId] = {
          awardQty: bestQuote?.supplierId === quote.supplierId ? remaining : '',
          awardRate: qLine?.quotedRate ?? '',
          deliveryDate: qLine?.deliveryDate || ''
        };
      });
      next[line.id] = perSupplier;
    });
    setAwardAllocations(next);
    setAwardErrors({});
  };

  const handleAwardChange = (lineId, supplierId, key, value) => {
    setAwardAllocations((prev) => {
      const currentLine = prev[lineId] || {};
      const supplierAlloc = currentLine[supplierId] || {};
      return {
        ...prev,
        [lineId]: {
          ...currentLine,
          [supplierId]: { ...supplierAlloc, [key]: value }
        }
      };
    });
  };

  const submitAwards = async (supplierFilter) => {
    if (!rfq) return;
    const supplierAwardMap = {};
    const validationErrors = {};
    (rfq.lines || []).forEach((line) => {
      const allocations = awardAllocations[line.id] || {};
      let totalForLine = awardedByLine[line.id]?.total || 0;
      Object.entries(allocations).forEach(([supplierId, alloc]) => {
        if (supplierFilter && Number(supplierId) !== Number(supplierFilter)) {
          return;
        }
        const qtyNum = Number(alloc.awardQty);
        if (Number.isFinite(qtyNum) && qtyNum > 0) {
          totalForLine += qtyNum;
          supplierAwardMap[supplierId] = supplierAwardMap[supplierId] || [];
          supplierAwardMap[supplierId].push({
            rfqLineId: line.id,
            awardQty: qtyNum,
            awardRate: alloc.awardRate === '' || alloc.awardRate == null ? null : Number(alloc.awardRate),
            deliveryDate: alloc.deliveryDate || null
          });
        }
      });
      if (totalForLine > Number(line.quantity || 0)) {
        validationErrors[line.id] = 'Awarded quantity exceeds requested quantity';
      }
    });

    if (!Object.values(supplierAwardMap).flat().length) {
      setAwardErrors({ form: 'Enter an award quantity for at least one supplier' });
      return;
    }
    if (closeRemaining && !closureReason) {
      setAwardErrors({ form: 'Provide a closure reason when cancelling remaining quantity' });
      return;
    }
    setAwardErrors(validationErrors);
    if (Object.keys(validationErrors).length) {
      return;
    }

    const supplierAwards = Object.entries(supplierAwardMap).map(([supplierId, allocations]) => ({
      supplierId: Number(supplierId),
      allocations
    }));
    const payload = { supplierAwards, remarks: null, closeRemaining, closureReason: closeRemaining ? closureReason : null };
    setAwarding(true);
    try {
      const response = await apiClient.post(`/api/rfq/${id}/award`, payload);
      setRfq(response.data);
      setAwardAllocations({});
      setCloseRemaining(false);
      setClosureReason('');
      setTab('request');
      const poIds = response.data?.poIdsBySupplier;
      const poRefs = response.data?.purchaseOrders || [];
      const supplierPo = supplierFilter
        ? poRefs.find((po) => Number(po.supplierId) === Number(supplierFilter))
        : null;
      const supplierPoId =
        supplierPo?.poId
        || (poIds && supplierFilter ? (poIds[supplierFilter] || [])[0] : null);
      const fallbackPoId = poRefs[0]?.poId || (poIds ? Object.values(poIds).flat()[0] : null);
      const targetPoId = supplierPoId || fallbackPoId;
      if (targetPoId) {
        navigate(`/purchase/po/${targetPoId}`);
      }
    } catch (error) {
      setAwardErrors({ form: error?.response?.data?.message || 'Unable to award RFQ. Please review the inputs.' });
    } finally {
      setAwarding(false);
    }
  };

  const handleSubmit = async () => {
    await apiClient.post(`/api/rfq/${id}/submit`);
    loadRfq();
  };

  const handleClose = async (reason) => {
    setClosing(true);
    try {
      const response = await apiClient.post(`/api/rfq/${id}/cancel`, { closureReason: reason });
      setRfq(response.data);
    } finally {
      setClosing(false);
      setCloseOpen(false);
    }
  };

  if (!rfq) {
    return (
      <MainCard>
        <Typography>{loading ? 'Loading...' : 'RFQ not found.'}</Typography>
      </MainCard>
    );
  }

  const supplierOptions = rfq.suppliers || [];

  const renderQuoteStatusChips = () => (
    <Stack direction="row" spacing={1} flexWrap="wrap">
      {supplierOptions.map((supplier) => (
        <Chip
          key={supplier.supplierId}
          label={`${supplierMap[supplier.supplierId] || supplier.supplierId} (${supplier.status || 'PENDING'})`}
        />
      ))}
      {!supplierOptions.length && <Typography>-</Typography>}
    </Stack>
  );

  const renderRequestTab = () => (
    <Stack spacing={3}>
      {/** Purchase Orders generated after award */}
      {/* Using purchase order references to show numbers and open detail quickly */}
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 3 }}>
          <Typography variant="subtitle2">Suppliers</Typography>
          {renderQuoteStatusChips()}
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <Typography variant="subtitle2">RFQ Date</Typography>
          <Typography>{rfq.rfqDate || '-'}</Typography>
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <Typography variant="subtitle2">Status</Typography>
          <Chip label={rfq.status} color={getStatusColor(rfq.status)} />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <Typography variant="subtitle2">Payment Terms</Typography>
          <Typography>{rfq.paymentTerms || '-'}</Typography>
        </Grid>
        <Grid size={{ xs: 12 }}>
          <Typography variant="subtitle2">Narration</Typography>
          <Typography>{rfq.narration || '-'}</Typography>
        </Grid>
        {rfq.closureReason && (
          <Grid size={{ xs: 12 }}>
            <Typography variant="subtitle2">Closure Reason</Typography>
            <Typography>{rfq.closureReason}</Typography>
          </Grid>
        )}
      </Grid>
      {(rfq.purchaseOrders?.length || 0) > 0 && (
        <Stack spacing={1}>
          <Typography variant="h6">Generated Purchase Orders</Typography>
          <Stack direction="row" spacing={1} flexWrap="wrap">
            {(rfq.purchaseOrders || []).map((po) => (
              <Button key={`${po.supplierId}-${po.poId}`} variant="outlined" onClick={() => navigate(`/purchase/po/${po.poId}`)}>
                {supplierMap[po.supplierId] || po.supplierId} - {po.poNo || `PO #${po.poId}`}
              </Button>
            ))}
          </Stack>
        </Stack>
      )}
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
              <TableCell>Amount</TableCell>
              <TableCell>Remarks</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rfq.lines?.map((line) => (
              <TableRow key={line.id}>
                <TableCell>{itemMap[line.itemId] || line.itemId}</TableCell>
                <TableCell>{uomMap[line.uomId] || line.uomId}</TableCell>
                <TableCell>{brokerMap[line.brokerId] || line.brokerId || '-'}</TableCell>
                <TableCell>{line.quantity}</TableCell>
                <TableCell>{line.rateExpected ?? '-'}</TableCell>
                <TableCell>{formatAmount(line.quantity, line.rateExpected)}</TableCell>
                <TableCell>{line.remarks ?? '-'}</TableCell>
              </TableRow>
            ))}
            {!rfq.lines?.length && (
              <TableRow>
                <TableCell colSpan={7}>No line items</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Stack>
    </Stack>
  );

  const renderSupplierQuotesTab = () => (
    <Stack spacing={3}>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 4 }}>
          <FormControl fullWidth>
            <InputLabel id="supplier-select-label">Supplier</InputLabel>
            <Select
              labelId="supplier-select-label"
              label="Supplier"
              value={quoteSupplierId}
              onChange={(event) => setQuoteSupplierId(event.target.value)}
              disabled={!supplierOptions.length}
            >
              {supplierOptions.map((supplier) => (
                <MenuItem key={supplier.supplierId} value={supplier.supplierId}>
                  {supplierMap[supplier.supplierId] || supplier.supplierId}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>
        <Grid size={{ xs: 12, md: 8 }}>
          {renderQuoteStatusChips()}
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              label="Payment Terms Override"
              value={quoteForm.paymentTermsOverride}
              onChange={(event) => setQuoteForm((prev) => ({ ...prev, paymentTermsOverride: event.target.value }))}
              disabled={isQuoteLocked}
            />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              label="Quote Remarks"
              value={quoteForm.remarks}
              onChange={(event) => setQuoteForm((prev) => ({ ...prev, remarks: event.target.value }))}
              disabled={isQuoteLocked}
            />
        </Grid>
      </Grid>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Item</TableCell>
            <TableCell>Expected Qty</TableCell>
            <TableCell>Quoted Qty</TableCell>
            <TableCell>Quoted Rate</TableCell>
            <TableCell>Delivery Date</TableCell>
            <TableCell>Remarks</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {(quoteForm.lines || []).map((line) => (
            <TableRow key={line.rfqLineId}>
              <TableCell>{itemMap[rfq.lines.find((l) => l.id === line.rfqLineId)?.itemId] || line.rfqLineId}</TableCell>
              <TableCell>{rfq.lines.find((l) => l.id === line.rfqLineId)?.quantity ?? '-'}</TableCell>
              <TableCell>
                <TextField
                  type="number"
                  size="small"
                  value={line.quotedQty}
                  onChange={(event) => handleQuoteLineChange(line.rfqLineId, 'quotedQty', event.target.value)}
                  disabled={isQuoteLocked}
                />
              </TableCell>
              <TableCell>
                <TextField
                  type="number"
                  size="small"
                  value={line.quotedRate}
                  onChange={(event) => handleQuoteLineChange(line.rfqLineId, 'quotedRate', event.target.value)}
                  disabled={isQuoteLocked}
                />
              </TableCell>
              <TableCell>
                <TextField
                  type="date"
                  size="small"
                  value={line.deliveryDate || ''}
                  onChange={(event) => handleQuoteLineChange(line.rfqLineId, 'deliveryDate', event.target.value)}
                  disabled={isQuoteLocked}
                  InputLabelProps={{ shrink: true }}
                />
              </TableCell>
              <TableCell>
                <TextField
                  size="small"
                  value={line.remarks}
                  onChange={(event) => handleQuoteLineChange(line.rfqLineId, 'remarks', event.target.value)}
                  disabled={isQuoteLocked}
                />
              </TableCell>
            </TableRow>
          ))}
          {!quoteForm.lines.length && (
            <TableRow>
              <TableCell colSpan={6}>Select a supplier to start capturing quotes</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
      <Stack direction="row" spacing={1} justifyContent="flex-end">
        <Button variant="outlined" onClick={saveQuote} disabled={quoteLoading || isQuoteLocked || !quoteSupplierId}>
          Save Draft
        </Button>
        <Button variant="contained" color="secondary" onClick={submitQuote} disabled={quoteLoading || isQuoteLocked || !quoteSupplierId}>
          Submit Quote
        </Button>
      </Stack>
    </Stack>
  );

  const renderCompareTab = () => {
    const submittedQuotes = Object.values(compareQuotes || {}).filter((quote) => quote.status === 'SUBMITTED');
    const isSupplierFullyAwarded = (supplierId) => {
      if (isFullyAwarded) return true;
      if (!rfq?.lines?.length) return false;
      let hasQuotedLine = false;
      return rfq.lines.every((line) => {
        const qLine = compareQuotes?.[supplierId]?.lines?.find((ql) => ql.rfqLineId === line.id);
        if (!qLine || qLine.quotedQty == null) {
          return true;
        }
        hasQuotedLine = true;
        const quotedQty = Number(qLine.quotedQty || 0);
        const alreadyAwarded = awardedByLine[line.id]?.bySupplier?.[supplierId] || 0;
        return quotedQty > 0 ? alreadyAwarded >= quotedQty : true;
      }) && hasQuotedLine;
    };
    return (
      <Stack spacing={3}>
        <Typography>
          Compare submitted supplier quotes per line and capture allocations. You can split quantities across suppliers as long as the total
          does not exceed the requested quantity. Locked after a full award.
        </Typography>
        {(rfq.lines || []).map((line) => {
          const lineQuotes = submittedQuotes.map((quote) => ({
            quote,
            qLine: quote.lines?.find((ql) => ql.rfqLineId === line.id)
          }));
          const lineAllocations = awardAllocations[line.id] || {};
          const alreadyAwarded = awardedByLine[line.id]?.total || 0;
          const remaining = remainingQtyByLine[line.id] ?? line.quantity;
          const disableLineAwards = isFullyAwarded || rfq?.status === 'CANCELLED' || remaining <= 0;
          return (
            <Stack key={line.id} spacing={1}>
              <Stack direction="row" justifyContent="space-between" alignItems="center">
                <Stack>
                  <Typography variant="subtitle2">{itemMap[line.itemId] || line.itemId}</Typography>
                  <Typography variant="caption">
                    Requested: {line.quantity} | Awarded: {alreadyAwarded} | Remaining: {remaining}
                  </Typography>
                </Stack>
                {awardErrors[line.id] && (
                  <Typography variant="caption" color="error">
                    {awardErrors[line.id]}
                  </Typography>
                )}
              </Stack>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Supplier</TableCell>
                    <TableCell>Quoted Qty</TableCell>
                    <TableCell>Quoted Rate</TableCell>
                    <TableCell>Quoted Delivery</TableCell>
                    <TableCell>Award Qty</TableCell>
                    <TableCell>Award Rate</TableCell>
                    <TableCell>Award Delivery</TableCell>
                    <TableCell />
                  </TableRow>
                </TableHead>
                <TableBody>
                  {lineQuotes.length ? (
                    lineQuotes.map(({ quote, qLine }) => {
                      const allocation = lineAllocations[quote.supplierId] || {};
                      const disableAward =
                        awarding
                        || isFullyAwarded
                        || rfq?.status === 'CANCELLED'
                        || compareLoading
                        || !hasSubmittedQuotes
                        || isSupplierFullyAwarded(quote.supplierId);
                      return (
                        <TableRow key={quote.supplierId}>
                          <TableCell>{supplierMap[quote.supplierId] || quote.supplierId}</TableCell>
                          <TableCell>{qLine?.quotedQty ?? '-'}</TableCell>
                          <TableCell>{qLine?.quotedRate ?? '-'}</TableCell>
                          <TableCell>{qLine?.deliveryDate || '-'}</TableCell>
                          <TableCell>
                            <TextField
                              type="number"
                              size="small"
                              value={allocation.awardQty ?? ''}
                              onChange={(event) => handleAwardChange(line.id, quote.supplierId, 'awardQty', event.target.value)}
                              disabled={disableLineAwards}
                            />
                          </TableCell>
                          <TableCell>
                            <TextField
                              type="number"
                              size="small"
                              value={allocation.awardRate ?? ''}
                              onChange={(event) => handleAwardChange(line.id, quote.supplierId, 'awardRate', event.target.value)}
                              disabled={disableLineAwards}
                            />
                          </TableCell>
                          <TableCell>
                            <TextField
                              type="date"
                              size="small"
                              value={allocation.deliveryDate || ''}
                              onChange={(event) => handleAwardChange(line.id, quote.supplierId, 'deliveryDate', event.target.value)}
                              disabled={disableLineAwards}
                              InputLabelProps={{ shrink: true }}
                            />
                          </TableCell>
                          <TableCell align="right">
                            <Button
                              size="small"
                              variant="outlined"
                              onClick={() => submitAwards(quote.supplierId)}
                              disabled={disableAward}
                            >
                              Award Supplier
                            </Button>
                          </TableCell>
                        </TableRow>
                      );
                    })
                  ) : (
                    <TableRow>
                      <TableCell colSpan={8}>No submitted quotes yet.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </Stack>
          );
        })}
        {!rfq.lines?.length && <Typography>No lines to award</Typography>}
        <Stack spacing={1}>
          <FormControl component="fieldset">
            <FormLabel component="legend">Remaining quantity handling</FormLabel>
            <RadioGroup
              row
              value={closeRemaining ? 'cancel' : 'open'}
              onChange={(event) => setCloseRemaining(event.target.value === 'cancel')}
            >
              <FormControlLabel value="open" control={<Radio />} label="Keep RFQ open for further awards" />
              <FormControlLabel value="cancel" control={<Radio />} label="Cancel remaining quantity" />
            </RadioGroup>
          </FormControl>
          {closeRemaining && (
            <TextField
              label="Closure Reason"
              value={closureReason}
              onChange={(event) => setClosureReason(event.target.value)}
              required
            />
          )}
        </Stack>
        {awardErrors.form && (
          <Typography color="error" variant="body2">
            {awardErrors.form}
          </Typography>
        )}
      </Stack>
    );
  };

  return (
    <MainCard>
      <PageHeader
        title={`RFQ ${rfq.rfqNo}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/rfq' }, { label: 'RFQ Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate(`/purchase/rfq/${id}/edit`)} disabled={rfq.status !== 'DRAFT'}>
              Edit
            </Button>
            <Button
              variant="outlined"
              onClick={() => setTab('quotes')}
              disabled={!['QUOTING', 'AWARDED_PARTIAL', 'AWARDED_FULL'].includes(rfq.status)}
            >
              Supplier Quotes
            </Button>
            <Button
              variant="contained"
              color="secondary"
              onClick={() => setTab('compare')}
              disabled={!['QUOTING', 'AWARDED_PARTIAL'].includes(rfq.status)}
            >
              Compare &amp; Award
            </Button>
            <Button
              variant="outlined"
              disabled={rfq.status !== 'DRAFT'}
              onClick={handleSubmit}
            >
              Submit
            </Button>
            <Button
              variant="outlined"
              color="secondary"
              onClick={() => setCloseOpen(true)}
              disabled={['CANCELLED', 'AWARDED_FULL'].includes(rfq.status)}
            >
              Cancel RFQ
            </Button>
          </Stack>
        }
      />
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
        <Tabs value={tab} onChange={(_, value) => setTab(value)}>
          <Tab label="Request" value="request" />
          <Tab label="Supplier Quotes" value="quotes" />
          <Tab label="Compare & Award" value="compare" />
        </Tabs>
      </Box>
      {tab === 'request' && renderRequestTab()}
      {tab === 'quotes' && renderSupplierQuotesTab()}
      {tab === 'compare' && renderCompareTab()}
      <RfqCloseDialog open={closeOpen} onClose={() => setCloseOpen(false)} onConfirm={handleClose} loading={closing} />
    </MainCard>
  );
}
