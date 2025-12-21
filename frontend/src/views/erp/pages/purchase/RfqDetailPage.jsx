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

export default function RfqDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [rfq, setRfq] = useState(null);
  const [loading, setLoading] = useState(false);
  const [itemMap, setItemMap] = useState({});
  const [uomMap, setUomMap] = useState({});
  const [supplierMap, setSupplierMap] = useState({});

  const loadRfq = () => {
    setLoading(true);
    apiClient
      .get(`/api/rfq/${id}`)
      .then((response) => setRfq(response.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRfq();
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

  const handleSubmit = async () => {
    await apiClient.post(`/api/rfq/${id}/submit`);
    loadRfq();
  };

  const handleApprove = async () => {
    await apiClient.post(`/api/rfq/${id}/approve`);
    loadRfq();
  };

  if (!rfq) {
    return (
      <MainCard>
        <Typography>{loading ? 'Loading...' : 'RFQ not found.'}</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`RFQ ${rfq.rfqNo}`}
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/rfq' }, { label: 'RFQ Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate(`/purchase/rfq/${id}/edit`)}>
              Edit
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
      <Stack spacing={3}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Supplier</Typography>
            <Typography>{supplierMap[rfq.supplierId] || rfq.supplierId || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">RFQ Date</Typography>
            <Typography>{rfq.rfqDate || '-'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <Typography variant="subtitle2">Status</Typography>
            <Chip label={rfq.status} color={rfq.status === 'APPROVED' ? 'success' : 'default'} />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <Typography variant="subtitle2">Remarks</Typography>
            <Typography>{rfq.remarks || '-'}</Typography>
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
                <TableCell>Expected Rate</TableCell>
                <TableCell>Remarks</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rfq.lines?.map((line) => (
                <TableRow key={line.id}>
                  <TableCell>{itemMap[line.itemId] || line.itemId}</TableCell>
                  <TableCell>{uomMap[line.uomId] || line.uomId}</TableCell>
                  <TableCell>{line.quantity}</TableCell>
                  <TableCell>{line.rateExpected ?? '-'}</TableCell>
                  <TableCell>{line.remarks ?? '-'}</TableCell>
                </TableRow>
              ))}
              {!rfq.lines?.length && (
                <TableRow>
                  <TableCell colSpan={5}>No line items</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Stack>
      </Stack>
    </MainCard>
  );
}
