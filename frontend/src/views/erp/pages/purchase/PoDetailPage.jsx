import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';

export default function PoDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [po, setPo] = useState(null);
  const [loading, setLoading] = useState(false);
  const [itemMap, setItemMap] = useState({});
  const [uomMap, setUomMap] = useState({});
  const [supplierMap, setSupplierMap] = useState({});

  const loadPo = () => {
    setLoading(true);
    apiClient
      .get(`/api/purchase-orders/${id}`)
      .then((response) => setPo(response.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadPo();
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
  }, [id]);

  const handleApprove = async () => {
    await apiClient.post(`/api/purchase-orders/${id}/approve`);
    loadPo();
  };

  const handlePrint = () => {
    window.open(`/api/purchase-orders/${id}/print`, '_blank', 'noopener,noreferrer');
  };

  if (!po) {
    return (
      <MainCard>
        <Typography>{loading ? 'Loading...' : 'Purchase order not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`Purchase Order ${po.poNo}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/po' }, { label: 'Purchase Order Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={handlePrint}>
              Print PO
            </Button>
            <Button variant="outlined" onClick={() => navigate(`/purchase/po/${id}/edit`)}>
              Edit
            </Button>
            <Button variant="contained" color="secondary" disabled={po.status !== 'DRAFT'} onClick={handleApprove}>
              Approve
            </Button>
          </Stack>
        }
      />
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Supplier</Typography>
            <Typography>{supplierMap[po.supplierId] || po.supplierId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Order Date</Typography>
            <Typography>{po.poDate || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Delivery Date</Typography>
            <Typography>{po.deliveryDate || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Status</Typography>
            <Chip label={po.status} color={po.status === 'APPROVED' ? 'success' : 'default'} />
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Total</Typography>
            <Typography>{po.totalAmount || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Supplier Invoice No</Typography>
            <Typography>{po.supplierInvoiceNo || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Purchase Ledger</Typography>
            <Typography>{po.purchaseLedger || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Current Ledger Balance</Typography>
            <Typography>{po.currentLedgerBalance ?? 0}</Typography>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <Typography variant="subtitle2">Narration</Typography>
            <Typography>{po.narration || '-'}</Typography>
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
                <TableCell>Qty</TableCell>
                <TableCell>Rate</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Remarks</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {po.lines?.map((line) => (
                <TableRow key={line.id}>
                  <TableCell>{itemMap[line.itemId] || line.itemId}</TableCell>
                  <TableCell>{uomMap[line.uomId] || line.uomId}</TableCell>
                  <TableCell>{line.quantity}</TableCell>
                  <TableCell>{line.rate}</TableCell>
                  <TableCell>{line.amount ?? '-'}</TableCell>
                  <TableCell>{line.remarks ?? '-'}</TableCell>
                </TableRow>
              ))}
              {!po.lines?.length && (
                <TableRow>
                  <TableCell colSpan={6}>No line items</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Stack>
      </Stack>
    </MainCard>
  );
}
