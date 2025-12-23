import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function PurchaseArrivalPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [poMap, setPoMap] = useState({});
  const [godownMap, setGodownMap] = useState({});

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/api/purchase-arrivals')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
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
  }, []);

  return (
    <MainCard>
      <PageHeader
        title="Purchase Arrival"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'Arrival' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/arrival/new')}>
            New Arrival
          </Button>
        }
      />
      <Stack spacing={2}>
        <DataTable
          columns={[
            { field: 'id', headerName: 'Arrival ID' },
            { field: 'purchaseOrderId', headerName: 'PO', render: (row) => poMap[row.purchaseOrderId] || row.purchaseOrderId },
            { field: 'godownId', headerName: 'Godown', render: (row) => godownMap[row.godownId] || row.godownId },
            { field: 'grossAmount', headerName: 'Gross Amount' },
            { field: 'netPayable', headerName: 'Net Payable' }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No purchase arrivals found."
          onRowClick={(row) => navigate(`/purchase/arrival/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
