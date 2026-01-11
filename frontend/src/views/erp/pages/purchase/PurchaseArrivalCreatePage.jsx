import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import CompanyField from 'components/common/CompanyField';

export default function PurchaseArrivalCreatePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [header, setHeader] = useState({
    purchaseOrderId: '',
    weighbridgeTicketId: '',
    brokerId: '',
    godownId: ''
  });
  const [purchaseOrders, setPurchaseOrders] = useState([]);
  const [purchaseOrderMap, setPurchaseOrderMap] = useState({});
  const [tickets, setTickets] = useState([]);
  const [ticketMap, setTicketMap] = useState({});
  const [grossAmount, setGrossAmount] = useState(0);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [charges, setCharges] = useState([]);
  const [chargeTypes, setChargeTypes] = useState([]);
  const [expenseParties, setExpenseParties] = useState([]);
  const [brokers, setBrokers] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [grnId, setGrnId] = useState('');

  useEffect(() => {
    apiClient
      .get('/api/purchase-orders')
      .then((res) => {
        const payload = res.data?.content || res.data || [];
        setPurchaseOrders(payload);
        const lookup = payload.reduce((acc, po) => {
          acc[String(po.id)] = po;
          return acc;
        }, {});
        setPurchaseOrderMap(lookup);
      })
      .catch(() => {
        setPurchaseOrders([]);
        setPurchaseOrderMap({});
      });
    apiClient.get('/api/weighbridge/tickets')
      .then((res) => {
        const payload = res.data || [];
        setTickets(payload);
        const lookup = payload.reduce((acc, ticket) => {
          acc[String(ticket.id)] = ticket;
          return acc;
        }, {});
        setTicketMap(lookup);
      })
      .catch(() => {
        setTickets([]);
        setTicketMap({});
      });
    apiClient.get('/api/deduction-charge-types').then((res) => setChargeTypes(res.data || [])).catch(() => setChargeTypes([]));
    apiClient.get('/api/expense-parties').then((res) => setExpenseParties(res.data || [])).catch(() => setExpenseParties([]));
    apiClient.get('/api/brokers').then((res) => setBrokers(res.data || [])).catch(() => setBrokers([]));
    apiClient.get('/api/vehicles').then((res) => setVehicles(res.data || [])).catch(() => setVehicles([]));
    apiClient.get('/api/suppliers').then((res) => setSuppliers(res.data || [])).catch(() => setSuppliers([]));
  }, []);

  useEffect(() => {
    const poId = searchParams.get('poId');
    const ticketId = searchParams.get('ticketId');
    const nextGrnId = searchParams.get('grnId');
    const godownId = searchParams.get('godownId');
    if (nextGrnId) {
      setGrnId(nextGrnId);
    }
    if (godownId) {
      setHeader((prev) => ({ ...prev, godownId }));
    }
    if (poId) {
      handlePurchaseOrderChange(poId);
      setHeader((prev) => ({ ...prev, purchaseOrderId: poId }));
    }
    if (ticketId) {
      handleTicketChange(ticketId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  useEffect(() => {
    if (!grnId) return;
    apiClient
      .get(`/api/grn/${grnId}`)
      .then((response) => {
        const payload = response.data;
        if (!payload) return;
        if (payload.purchaseOrderId) {
          handlePurchaseOrderChange(payload.purchaseOrderId);
          setHeader((prev) => ({ ...prev, purchaseOrderId: payload.purchaseOrderId }));
        }
        if (payload.weighbridgeTicketId) {
          handleTicketChange(payload.weighbridgeTicketId);
        }
        if (payload.godownId) {
          setHeader((prev) => ({ ...prev, godownId: payload.godownId }));
        }
      })
      .catch(() => null);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [grnId]);

  useEffect(() => {
    if (!header.purchaseOrderId) return;
    const po = purchaseOrderMap[String(header.purchaseOrderId)];
    if (po) {
      setGrossAmount(Number(po.totalAmount || 0));
    }
  }, [header.purchaseOrderId, purchaseOrderMap]);

  const handleTicketChange = async (ticketId) => {
    setHeader((prev) => ({ ...prev, weighbridgeTicketId: ticketId }));
    if (!ticketId) return;
    const ticket = ticketMap[String(ticketId)];
    if (ticket?.poId) {
      handlePurchaseOrderChange(ticket.poId);
      setHeader((prev) => ({ ...prev, purchaseOrderId: ticket.poId }));
    }
  };

  const getChargeType = (id) => chargeTypes.find((ct) => ct.id === id);

  const updateCharge = (index, patch) => {
    setCharges((prev) => {
      const next = [...prev];
      const existing = next[index] || {};
      const merged = { ...existing, ...patch };
      const type = merged.chargeTypeId ? getChargeType(Number(merged.chargeTypeId)) : null;
      const calcType = merged.calcType || type?.defaultCalcType || '';
      const rate = merged.rate ?? type?.defaultRate ?? '';
      const shouldRecalc =
        patch.rate !== undefined ||
        patch.calcType !== undefined ||
        patch.chargeTypeId !== undefined;
      if (shouldRecalc && patch.amount === undefined) {
        if (rate && calcType === 'PERCENT') {
          merged.amount = ((grossAmount * Number(rate)) / 100).toFixed(2);
        } else if (rate) {
          merged.amount = Number(rate).toFixed(2);
        } else {
          merged.amount = '';
        }
      }
      if (merged.isDeduction === undefined && type) {
        merged.isDeduction = type.isDeduction;
      }
      next[index] = merged;
      return next;
    });
  };

  const addCharge = () => setCharges((prev) => [...prev, { key: Date.now() }]);
  const removeCharge = (index) =>
    setCharges((prev) => {
      const next = [...prev];
      next.splice(index, 1);
      return next;
    });

  const netPayable = useMemo(() => {
    const chargeAdd = charges.reduce((sum, charge) => {
      if (!charge.amount) return sum;
      return sum + (charge.isDeduction ? 0 : Number(charge.amount));
    }, 0);
    const chargeDeduct = charges.reduce((sum, charge) => {
      if (!charge.amount) return sum;
      return sum + (charge.isDeduction ? Number(charge.amount) : 0);
    }, 0);
    return grossAmount + chargeAdd - chargeDeduct;
  }, [grossAmount, charges]);

  const handlePurchaseOrderChange = async (poId) => {
    setHeader((prev) => ({ ...prev, purchaseOrderId: poId }));
    if (!poId) {
      setGrossAmount(0);
      return;
    }
    const po = purchaseOrderMap[String(poId)];
    setGrossAmount(Number(po?.totalAmount || 0));
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const filteredCharges = charges.filter((charge) => charge.chargeTypeId);
      const payloadCharges = filteredCharges.map((charge) => {
        const calcType = charge.calcType || null;
        const rate = charge.rate !== '' && charge.rate !== undefined ? Number(charge.rate) : null;
        let amount = charge.amount !== '' && charge.amount !== undefined ? Number(charge.amount) : null;
        if (amount === null && calcType === 'PERCENT' && rate !== null) {
          amount = Number(((grossAmount * rate) / 100).toFixed(2));
        }
        return {
          chargeTypeId: Number(charge.chargeTypeId),
          calcType,
          rate,
          amount,
          isDeduction: charge.isDeduction ?? null,
          payablePartyType: charge.payablePartyType || 'SUPPLIER',
          payablePartyId: charge.payablePartyId ? Number(charge.payablePartyId) : null,
          remarks: charge.remarks || ''
        };
      });
      const payload = {
        purchaseOrderId: Number(header.purchaseOrderId),
        weighbridgeTicketId: header.weighbridgeTicketId ? Number(header.weighbridgeTicketId) : null,
        brokerId: header.brokerId ? Number(header.brokerId) : null,
        godownId: Number(header.godownId),
        charges: payloadCharges
      };
      await apiClient.post('/api/purchase-arrivals', payload);
      if (grnId) {
        navigate(`/purchase/purchase-invoice?grnId=${grnId}`);
      } else {
        navigate(`/purchase/purchase-invoice?poId=${payload.purchaseOrderId}`);
      }
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save purchase arrival.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="New Purchase Arrival"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/arrival' }, { label: 'New' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
            Save Arrival
          </Button>
        }
      />
      <Stack spacing={3}>
        {error && <Typography color="error">{error}</Typography>}
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <CompanyField />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Purchase Order"
              value={header.purchaseOrderId}
              onChange={(event) => handlePurchaseOrderChange(event.target.value)}
            >
              <MenuItem value="">Select PO</MenuItem>
              {purchaseOrders.map((po) => (
                <MenuItem key={po.id} value={po.id}>
                  {po.poNo}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField fullWidth label="Gross Amount" value={grossAmount} InputProps={{ readOnly: true }} />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Weighbridge Ticket"
              value={header.weighbridgeTicketId}
              onChange={(event) => handleTicketChange(event.target.value)}
            >
              <MenuItem value="">Select Ticket</MenuItem>
              {tickets.map((ticket) => (
                <MenuItem key={ticket.id} value={ticket.id}>
                  {ticket.serialNo}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Broker"
              value={header.brokerId}
              onChange={(event) => setHeader((prev) => ({ ...prev, brokerId: event.target.value }))}
            >
              <MenuItem value="">Select Broker</MenuItem>
              {brokers.map((broker) => (
                <MenuItem key={broker.id} value={broker.id}>
                  {broker.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <MasterAutocomplete
              label="Godown"
              endpoint="/api/godowns"
              value={header.godownId}
              onChange={(nextValue) => setHeader((prev) => ({ ...prev, godownId: nextValue }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="Select godown"
              required
            />
          </Grid>
        </Grid>
        <Divider />
        <Stack spacing={2}>
          <Typography variant="h5">Charges & Deductions</Typography>
          <Stack spacing={2}>
            {charges.map((charge, index) => {
              const typeInfo = getChargeType(Number(charge.chargeTypeId));
              return (
                <Grid container spacing={2} key={charge.key || index}>
                  <Grid size={{ xs: 12, md: 3 }}>
                    <TextField
                      select
                      fullWidth
                      label="Type"
                      value={charge.chargeTypeId || ''}
                      onChange={(event) => updateCharge(index, { chargeTypeId: Number(event.target.value) })}
                    >
                      <MenuItem value="">Select</MenuItem>
                      {chargeTypes.map((ct) => (
                        <MenuItem key={ct.id} value={ct.id}>
                          {ct.name} ({ct.isDeduction ? 'Deduction' : 'Charge'})
                        </MenuItem>
                      ))}
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, md: 2 }}>
                    <TextField
                      select
                      fullWidth
                      label="Calc Type"
                      value={charge.calcType || typeInfo?.defaultCalcType || ''}
                      onChange={(event) => updateCharge(index, { calcType: event.target.value })}
                    >
                      <MenuItem value="">Default</MenuItem>
                      <MenuItem value="FLAT">Flat</MenuItem>
                      <MenuItem value="PERCENT">Percent</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, md: 2 }}>
                    <TextField
                      fullWidth
                      type="number"
                      label="Rate / Value"
                      value={charge.rate ?? typeInfo?.defaultRate ?? ''}
                      onChange={(event) => updateCharge(index, { rate: event.target.value })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 2 }}>
                    <TextField
                      fullWidth
                      type="number"
                      label="Amount"
                      value={charge.amount || ''}
                      onChange={(event) => updateCharge(index, { amount: event.target.value })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 3 }}>
                    <TextField
                      select
                      fullWidth
                      label="Payable Type"
                      value={charge.payablePartyType || 'SUPPLIER'}
                      onChange={(event) => updateCharge(index, { payablePartyType: event.target.value, payablePartyId: '' })}
                    >
                      <MenuItem value="SUPPLIER">Supplier</MenuItem>
                      <MenuItem value="BROKER">Broker</MenuItem>
                      <MenuItem value="VEHICLE">Vehicle</MenuItem>
                      <MenuItem value="EXPENSE">Expense Party</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, md: 3 }}>
                    <TextField
                      select
                      fullWidth
                      label="Payee"
                      value={charge.payablePartyId || ''}
                      onChange={(event) => updateCharge(index, { payablePartyId: event.target.value })}
                    >
                      <MenuItem value="">Select</MenuItem>
                      {(charge.payablePartyType || 'SUPPLIER') === 'SUPPLIER' &&
                        suppliers.map((supplier) => (
                          <MenuItem key={supplier.id} value={supplier.id}>
                            {supplier.name}
                          </MenuItem>
                        ))}
                      {(charge.payablePartyType || 'SUPPLIER') === 'BROKER' &&
                        brokers.map((broker) => (
                          <MenuItem key={broker.id} value={broker.id}>
                            {broker.name}
                          </MenuItem>
                        ))}
                      {(charge.payablePartyType || 'SUPPLIER') === 'VEHICLE' &&
                        vehicles.map((vehicle) => (
                          <MenuItem key={vehicle.id} value={vehicle.id}>
                            {vehicle.vehicleNo}
                          </MenuItem>
                        ))}
                      {(charge.payablePartyType || 'SUPPLIER') === 'EXPENSE' &&
                        expenseParties.map((party) => (
                          <MenuItem key={party.id} value={party.id}>
                            {party.name}
                          </MenuItem>
                        ))}
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, md: 3 }}>
                    <TextField
                      fullWidth
                      label="Remarks"
                      value={charge.remarks || ''}
                      onChange={(event) => updateCharge(index, { remarks: event.target.value })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 2 }}>
                    <Button color="error" onClick={() => removeCharge(index)}>
                      Remove
                    </Button>
                  </Grid>
                </Grid>
              );
            })}
            <Button variant="outlined" onClick={addCharge}>
              Add Charge / Deduction
            </Button>
          </Stack>
        </Stack>
        <Divider />
        <Stack direction="row" justifyContent="flex-end">
          <Typography variant="h6">Net Payable: {netPayable.toFixed(2)}</Typography>
        </Stack>
      </Stack>
    </MainCard>
  );
}
