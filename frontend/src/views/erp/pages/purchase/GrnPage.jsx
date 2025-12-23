import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function GrnPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [supplierMap, setSupplierMap] = useState({});
  const [poMap, setPoMap] = useState({});

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/api/grn')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
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
  }, []);

  return (
    <MainCard>
      <PageHeader
        title="GRN"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'GRN' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/grn/new')}>
            Create GRN
          </Button>
        }
      />
      <Stack spacing={2}>
        <DataTable
          columns={[
            { field: 'grnNo', headerName: 'GRN No' },
            { field: 'supplierId', headerName: 'Supplier', render: (row) => supplierMap[row.supplierId] || row.supplierId },
            { field: 'purchaseOrderId', headerName: 'PO', render: (row) => poMap[row.purchaseOrderId] || row.purchaseOrderId },
            { field: 'grnDate', headerName: 'Date' },
            { field: 'status', headerName: 'Status' }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No GRNs found."
          onRowClick={(row) => navigate(`/purchase/grn/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
