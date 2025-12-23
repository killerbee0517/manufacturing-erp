import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function WeighbridgeListPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [supplierMap, setSupplierMap] = useState({});
  const [itemMap, setItemMap] = useState({});
  const [vehicleMap, setVehicleMap] = useState({});

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/api/weighbridge/tickets')
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
  }, []);

  return (
    <MainCard>
      <PageHeader
        title="Weighbridge In"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'Weighbridge In' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/weighbridge-in/new')}>
            New Entry
          </Button>
        }
      />
      <Stack spacing={2}>
        <DataTable
          columns={[
            { field: 'serialNo', headerName: 'Ticket No' },
            { field: 'vehicleId', headerName: 'Vehicle', render: (row) => vehicleMap[row.vehicleId] || row.vehicleId },
            { field: 'supplierId', headerName: 'Supplier', render: (row) => supplierMap[row.supplierId] || row.supplierId },
            { field: 'itemId', headerName: 'Item', render: (row) => itemMap[row.itemId] || row.itemId },
            { field: 'grossWeight', headerName: 'Gross' },
            { field: 'unloadedWeight', headerName: 'Unloaded' },
            { field: 'netWeight', headerName: 'Net' }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No weighbridge entries found."
          onRowClick={(row) => navigate(`/purchase/weighbridge-in/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
