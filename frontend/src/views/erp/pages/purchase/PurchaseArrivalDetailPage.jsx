import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';

export default function PurchaseArrivalDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [arrival, setArrival] = useState(null);
  const [loading, setLoading] = useState(false);
  const [poMap, setPoMap] = useState({});
  const [godownMap, setGodownMap] = useState({});
  const [ticketMap, setTicketMap] = useState({});
  const [chargeTypeMap, setChargeTypeMap] = useState({});
  const [brokerMap, setBrokerMap] = useState({});

  useEffect(() => {
    setLoading(true);
    apiClient
      .get(`/api/purchase-arrivals/${id}`)
      .then((response) => setArrival(response.data))
      .finally(() => setLoading(false));

    apiClient
      .get('/api/purchase-orders')
      .then((response) => {
        const payload = response.data?.content || response.data || [];
        const lookup = payload.reduce((acc, po) => {
          acc[po.id] = po.poNo;
          return acc;
        }, {});
        setPoMap(lookup);
      })
      .catch(() => setPoMap({}));

    apiClient
      .get('/api/godowns')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, godown) => {
          acc[godown.id] = godown.name;
          return acc;
        }, {});
        setGodownMap(lookup);
      })
      .catch(() => setGodownMap({}));

    apiClient
      .get('/api/weighbridge/tickets')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, ticket) => {
          acc[ticket.id] = ticket.serialNo;
          return acc;
        }, {});
        setTicketMap(lookup);
      })
      .catch(() => setTicketMap({}));

    apiClient
      .get('/api/deduction-charge-types')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, type) => {
          acc[type.id] = type.name;
          return acc;
        }, {});
        setChargeTypeMap(lookup);
      })
      .catch(() => setChargeTypeMap({}));

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
  }, [id]);

  if (!arrival) {
    return (
      <MainCard>
        <Typography>{loading ? 'Loading...' : 'Purchase arrival not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`Purchase Arrival ${arrival.id}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/arrival' }, { label: 'Arrival Detail' }]}
        actions={
          <Button variant="outlined" onClick={() => navigate('/purchase/arrival')}>
            Back to Arrivals
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Purchase Order</Typography>
            <Typography>{poMap[arrival.purchaseOrderId] || arrival.purchaseOrderId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Weighbridge Ticket</Typography>
            <Typography>{ticketMap[arrival.weighbridgeTicketId] || arrival.weighbridgeTicketId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Godown</Typography>
            <Typography>{godownMap[arrival.godownId] || arrival.godownId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Broker</Typography>
            <Typography>{brokerMap[arrival.brokerId] || arrival.brokerName || arrival.brokerId || '-'}</Typography>
          </Grid>
        </Grid>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Gross Amount</Typography>
            <Typography>{arrival.grossAmount ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Unloading Charges</Typography>
            <Typography>{arrival.unloadingCharges ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Deductions</Typography>
            <Typography>{arrival.deductions ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">TDS %</Typography>
            <Typography>{arrival.tdsPercent ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Net Payable</Typography>
            <Typography>{arrival.netPayable ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Brokerage Amount</Typography>
            <Typography>{arrival.brokerageAmount ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Created</Typography>
            <Typography>{arrival.createdAt ? new Date(arrival.createdAt).toLocaleString() : '-'}</Typography>
          </Grid>
        </Grid>
        <Divider />
        <Stack spacing={1}>
          <Typography variant="h5">Charges & Deductions</Typography>
          {arrival.charges?.length ? (
            arrival.charges.map((charge) => (
              <Grid container spacing={2} key={charge.id}>
                <Grid size={{ xs: 12, md: 4 }}>
                  <Typography variant="subtitle2">Type</Typography>
                  <Typography>{chargeTypeMap[charge.chargeTypeId] || charge.chargeTypeId}</Typography>
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                  <Typography variant="subtitle2">Amount</Typography>
                  <Typography>
                    {charge.amount} {charge.isDeduction ? '(Deduction)' : ''}
                  </Typography>
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                  <Typography variant="subtitle2">Payable To</Typography>
                  <Typography>{charge.payablePartyType || '-'}</Typography>
                </Grid>
              </Grid>
            ))
          ) : (
            <Typography color="text.secondary">No charges added.</Typography>
          )}
        </Stack>
      </Stack>
    </MainCard>
  );
}
