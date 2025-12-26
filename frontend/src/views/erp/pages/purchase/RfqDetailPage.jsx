import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import FormControl from '@mui/material/FormControl';
import Grid from '@mui/material/Grid';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
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

const awardedStatuses = new Set(['AWARDED', 'PARTIALLY_AWARDED']);

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
  const [awardLines, setAwardLines] = useState({});
  const [awarding, setAwarding] = useState(false);

  const isAwarded = useMemo(() => awardedStatuses.has(rfq?.status), [rfq?.status]);
  const hasSubmittedQuotes = useMemo(
    () => Object.values(compareQuotes || {}).some((quote) => quote.status === 'SUBMITTED'),
    [compareQuotes]
  );

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
    const next = {};
    (rfq.lines || []).forEach((line) => {
      const submittedQuotes = Object.values(quotesMap || {}).filter((quote) => quote.status === 'SUBMITTED');
      const bestQuote = submittedQuotes.reduce(
        (best, quote) => {
          const qLine = quote.lines?.find((ql) => ql.rfqLineId === line.id);
          if (!qLine || qLine.quotedRate == null) return best;
          if (!best || Number(qLine.quotedRate) < Number(best.rate)) {
            return {
              supplierId: quote.supplierId,
              rate: qLine.quotedRate,
              deliveryDate: qLine.deliveryDate
            };
          }
          return best;
        },
        null
      );
      next[line.id] = {
        rfqLineId: line.id,
        supplierId: bestQuote?.supplierId || '',
        quantity: line.quantity,
        rate: bestQuote?.rate || '',
        deliveryDate: bestQuote?.deliveryDate || ''
      };
    });
    setAwardLines(next);
  };

  const handleAwardLineChange = (lineId, key, value) => {
    setAwardLines((prev) => ({
      ...prev,
      [lineId]: { ...(prev[lineId] || {}), [key]: value }
    }));
  };

  const submitAwards = async () => {
    const payload = {
      awards: Object.values(awardLines || {})
        .filter((line) => line.supplierId)
        .map((line) => ({
          rfqLineId: line.rfqLineId,
          supplierId: Number(line.supplierId),
          quantity: Number(
            line.quantity === '' || line.quantity == null
              ? rfq.lines.find((l) => l.id === line.rfqLineId)?.quantity ?? 0
              : line.quantity
          ),
          rate: line.rate === '' ? null : Number(line.rate),
          deliveryDate: line.deliveryDate || null
        })),
      remarks: null
    };
    setAwarding(true);
    try {
      const response = await apiClient.post(`/api/rfq/${id}/award`, payload);
      setRfq(response.data);
      setTab('request');
    } finally {
      setAwarding(false);
    }
  };

  const handleSubmit = async () => {
    await apiClient.post(`/api/rfq/${id}/submit`);
    loadRfq();
  };

  const handleApprove = async () => {
    await apiClient.post(`/api/rfq/${id}/approve`);
    loadRfq();
  };

  const handleClose = async (reason) => {
    setClosing(true);
    try {
      const response = await apiClient.post(`/api/rfq/${id}/close`, { closureReason: reason });
      const poId = response.data?.purchaseOrderId;
      if (poId) {
        navigate(`/purchase/po/${poId}`);
        return;
      }
      loadRfq();
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
          <Chip label={rfq.status} color={awardedStatuses.has(rfq.status) ? 'success' : rfq.status === 'APPROVED' ? 'info' : 'default'} />
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
      {Object.keys(rfq.createdPoIds || {}).length > 0 && (
        <Stack spacing={1}>
          <Typography variant="h6">Generated Purchase Orders</Typography>
          <Stack direction="row" spacing={1} flexWrap="wrap">
            {Object.entries(rfq.createdPoIds).map(([supplierId, poId]) => (
              <Button key={poId} variant="outlined" onClick={() => navigate(`/purchase/po/${poId}`)}>
                {supplierMap[supplierId] || supplierId} - PO #{poId}
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
              disabled={!supplierOptions.length || isAwarded}
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
            disabled={isAwarded}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            label="Quote Remarks"
            value={quoteForm.remarks}
            onChange={(event) => setQuoteForm((prev) => ({ ...prev, remarks: event.target.value }))}
            disabled={isAwarded}
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
                  disabled={isAwarded}
                />
              </TableCell>
              <TableCell>
                <TextField
                  type="number"
                  size="small"
                  value={line.quotedRate}
                  onChange={(event) => handleQuoteLineChange(line.rfqLineId, 'quotedRate', event.target.value)}
                  disabled={isAwarded}
                />
              </TableCell>
              <TableCell>
                <TextField
                  type="date"
                  size="small"
                  value={line.deliveryDate || ''}
                  onChange={(event) => handleQuoteLineChange(line.rfqLineId, 'deliveryDate', event.target.value)}
                  disabled={isAwarded}
                  InputLabelProps={{ shrink: true }}
                />
              </TableCell>
              <TableCell>
                <TextField
                  size="small"
                  value={line.remarks}
                  onChange={(event) => handleQuoteLineChange(line.rfqLineId, 'remarks', event.target.value)}
                  disabled={isAwarded}
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
        <Button variant="outlined" onClick={saveQuote} disabled={quoteLoading || isAwarded || !quoteSupplierId}>
          Save Draft
        </Button>
        <Button variant="contained" color="secondary" onClick={submitQuote} disabled={quoteLoading || isAwarded || !quoteSupplierId}>
          Submit Quote
        </Button>
      </Stack>
    </Stack>
  );

  const renderCompareTab = () => (
    <Stack spacing={3}>
      <Typography>
        Compare submitted supplier quotes per line and select an award. Only submitted quotes can be awarded. Locked after awarding.
      </Typography>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Item</TableCell>
            <TableCell>Supplier Quotes</TableCell>
            <TableCell>Award Supplier</TableCell>
            <TableCell>Award Qty</TableCell>
            <TableCell>Award Rate</TableCell>
            <TableCell>Delivery Date</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {(rfq.lines || []).map((line) => {
            const lineQuotes = Object.values(compareQuotes || {}).map((quote) => {
              const qLine = quote.lines?.find((ql) => ql.rfqLineId === line.id);
              return { quote, qLine };
            });
            const awardLine = awardLines[line.id] || {};
            return (
              <TableRow key={line.id}>
                <TableCell>
                  <Stack>
                    <Typography variant="subtitle2">{itemMap[line.itemId] || line.itemId}</Typography>
                    <Typography variant="caption">Req Qty: {line.quantity}</Typography>
                  </Stack>
                </TableCell>
                <TableCell>
                  <Stack spacing={0.5}>
                    {lineQuotes.map(({ quote, qLine }) => (
                      <Typography key={quote.supplierId} variant="body2">
                        {supplierMap[quote.supplierId] || quote.supplierId}: {qLine?.quotedRate ?? '-'} @ {qLine?.deliveryDate || '-'} (
                        {quote.status})
                      </Typography>
                    ))}
                    {!lineQuotes.length && <Typography variant="caption">No quotes yet.</Typography>}
                  </Stack>
                </TableCell>
                <TableCell>
                  <FormControl fullWidth size="small">
                    <Select
                      value={awardLine.supplierId || ''}
                      onChange={(event) => handleAwardLineChange(line.id, 'supplierId', event.target.value)}
                      disabled={isAwarded}
                    >
                      <MenuItem value="">Select</MenuItem>
                      {Object.values(compareQuotes || {})
                        .filter((quote) => quote.status === 'SUBMITTED')
                        .map((quote) => (
                          <MenuItem key={quote.supplierId} value={quote.supplierId}>
                            {supplierMap[quote.supplierId] || quote.supplierId}
                          </MenuItem>
                        ))}
                    </Select>
                  </FormControl>
                </TableCell>
                <TableCell>
                  <TextField
                    type="number"
                    size="small"
                    value={awardLine.quantity ?? line.quantity}
                    onChange={(event) => handleAwardLineChange(line.id, 'quantity', event.target.value)}
                    disabled={isAwarded}
                  />
                </TableCell>
                <TableCell>
                  <TextField
                    type="number"
                    size="small"
                    value={awardLine.rate ?? ''}
                    onChange={(event) => handleAwardLineChange(line.id, 'rate', event.target.value)}
                    disabled={isAwarded}
                  />
                </TableCell>
                <TableCell>
                  <TextField
                    type="date"
                    size="small"
                    value={awardLine.deliveryDate || ''}
                    onChange={(event) => handleAwardLineChange(line.id, 'deliveryDate', event.target.value)}
                    disabled={isAwarded}
                    InputLabelProps={{ shrink: true }}
                  />
                </TableCell>
              </TableRow>
            );
          })}
          {!rfq.lines?.length && (
            <TableRow>
              <TableCell colSpan={6}>No lines to award</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
      <Stack direction="row" spacing={1} justifyContent="flex-end">
        <Button
          variant="contained"
          color="secondary"
          onClick={submitAwards}
          disabled={awarding || isAwarded || compareLoading || !hasSubmittedQuotes}
        >
          Award &amp; Generate PO
        </Button>
      </Stack>
    </Stack>
  );

  return (
    <MainCard>
      <PageHeader
        title={`RFQ ${rfq.rfqNo}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/rfq' }, { label: 'RFQ Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate(`/purchase/rfq/${id}/edit`)} disabled={isAwarded}>
              Edit
            </Button>
            <Button variant="outlined" color="secondary" onClick={() => setCloseOpen(true)} disabled={rfq.status === 'CLOSED'}>
              Close
            </Button>
            <Button variant="outlined" disabled={rfq.status !== 'DRAFT'} onClick={handleSubmit}>
              Submit
            </Button>
            <Button variant="contained" color="secondary" disabled={rfq.status !== 'SUBMITTED'} onClick={handleApprove}>
              Approve
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
