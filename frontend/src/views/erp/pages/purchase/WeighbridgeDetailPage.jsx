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

export default function WeighbridgeDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(false);
  const [supplierMap, setSupplierMap] = useState({});
  const [itemMap, setItemMap] = useState({});
  const [vehicleMap, setVehicleMap] = useState({});
  const [poMap, setPoMap] = useState({});

  useEffect(() => {
    setLoading(true);
    apiClient
      .get(`/api/weighbridge/tickets/${id}`)
      .then((response) => setTicket(response.data))
      .finally(() => setLoading(false));

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
      .get('/api/vehicles')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, vehicle) => {
          acc[vehicle.id] = vehicle.vehicleNo;
          return acc;
        }, {});
        setVehicleMap(lookup);
      })
      .catch(() => setVehicleMap({}));

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
  }, [id]);

  if (!ticket) {
    return (
      <MainCard>
        <Typography>{loading ? 'Loading...' : 'Weighbridge ticket not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`Weighbridge Ticket ${ticket.serialNo || ticket.id}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/weighbridge-in' }, { label: 'Ticket Detail' }]}
        actions={
          <Button variant="outlined" onClick={() => navigate(`/purchase/weighbridge-in/${id}/edit`)}>
            Edit
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Serial No</Typography>
            <Typography>{ticket.serialNo || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Purchase Order</Typography>
            <Typography>{poMap[ticket.poId] || ticket.poId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Vehicle</Typography>
            <Typography>{vehicleMap[ticket.vehicleId] || ticket.vehicleId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Supplier</Typography>
            <Typography>{supplierMap[ticket.supplierId] || ticket.supplierId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Item</Typography>
            <Typography>{itemMap[ticket.itemId] || ticket.itemId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Date In</Typography>
            <Typography>{ticket.dateIn || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Time In</Typography>
            <Typography>{ticket.timeIn || '-'}</Typography>
          </Grid>
        </Grid>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Gross Weight</Typography>
            <Typography>{ticket.grossWeight ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Unloaded Weight</Typography>
            <Typography>{ticket.unloadedWeight ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Net Weight</Typography>
            <Typography>{ticket.netWeight ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Status</Typography>
            <Typography>{ticket.status || '-'}</Typography>
          </Grid>
        </Grid>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Second Date</Typography>
            <Typography>{ticket.secondDate || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Second Time</Typography>
            <Typography>{ticket.secondTime || '-'}</Typography>
          </Grid>
        </Grid>
      </Stack>
    </MainCard>
  );
}
