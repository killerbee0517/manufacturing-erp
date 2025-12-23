import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Typography from '@mui/material/Typography';

import Button from '@mui/material/Button';
import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';

export default function GrnDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [grn, setGrn] = useState(null);
  const [loading, setLoading] = useState(false);
  const [supplierMap, setSupplierMap] = useState({});
  const [itemMap, setItemMap] = useState({});
  const [uomMap, setUomMap] = useState({});
  const [poMap, setPoMap] = useState({});
  const [godownMap, setGodownMap] = useState({});

  useEffect(() => {
    setLoading(true);
    apiClient
      .get(`/api/grn/${id}`)
      .then((response) => setGrn(response.data))
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
  }, [id]);

  if (!grn) {
    return (
      <MainCard>
        <Typography>{loading ? 'Loading...' : 'GRN not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`GRN ${grn.grnNo}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/grn' }, { label: 'GRN Detail' }]}
        actions={
          <Button variant="outlined" onClick={() => navigate('/purchase/grn')}>
            Back to GRN
          </Button>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Supplier</Typography>
            <Typography>{supplierMap[grn.supplierId] || grn.supplierId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Purchase Order</Typography>
            <Typography>{poMap[grn.purchaseOrderId] || grn.purchaseOrderId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Godown</Typography>
            <Typography>{godownMap[grn.godownId] || grn.godownId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">GRN Date</Typography>
            <Typography>{grn.grnDate || '-'}</Typography>
          </Grid>
        </Grid>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">1st Weight</Typography>
            <Typography>{grn.firstWeight ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">2nd Weight</Typography>
            <Typography>{grn.secondWeight ?? '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Typography variant="subtitle2">Net Weight</Typography>
            <Typography>{grn.netWeight ?? '-'}</Typography>
          </Grid>
        </Grid>
        <Divider />
        <Stack spacing={1}>
          <Typography variant="h5">Line Items</Typography>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Item</TableCell>
                <TableCell>UOM</TableCell>
                <TableCell>Quantity</TableCell>
                <TableCell>Weight</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {grn.lines?.map((line) => (
                <TableRow key={line.id}>
                  <TableCell>{itemMap[line.itemId] || line.itemId}</TableCell>
                  <TableCell>{uomMap[line.uomId] || line.uomId}</TableCell>
                  <TableCell>{line.quantity}</TableCell>
                  <TableCell>{line.weight}</TableCell>
                </TableRow>
              ))}
              {!grn.lines?.length && (
                <TableRow>
                  <TableCell colSpan={4}>No line items found.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Stack>
      </Stack>
    </MainCard>
  );
}
