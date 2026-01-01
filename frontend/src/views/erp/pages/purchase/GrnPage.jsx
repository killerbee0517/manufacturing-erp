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

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/api/grn')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <MainCard>
      <PageHeader
        title="GRN"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'GRN' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/qc')}>
            QC Inspections
          </Button>
        }
      />
      <Stack spacing={2}>
        <DataTable
          columns={[
            { field: 'grnNo', headerName: 'GRN No' },
            { field: 'supplierName', headerName: 'Supplier', render: (row) => row.supplierName || row.supplierId },
            { field: 'purchaseOrderNo', headerName: 'PO', render: (row) => row.purchaseOrderNo || row.purchaseOrderId },
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
