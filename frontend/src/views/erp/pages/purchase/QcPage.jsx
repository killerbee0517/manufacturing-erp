import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function QcPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/api/qc/inspections')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <MainCard>
      <PageHeader
        title="QC Inspections"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'QC Inspection' }]}
        actions={
          <Button variant="outlined" onClick={() => navigate('/purchase/weighbridge-in')}>
            Weighbridge In
          </Button>
        }
      />
      <Stack spacing={2}>
        <DataTable
          columns={[
            { field: 'id', headerName: 'QC ID' },
            { field: 'purchaseOrderId', headerName: 'PO ID' },
            { field: 'weighbridgeTicketId', headerName: 'Ticket ID' },
            { field: 'inspectionDate', headerName: 'Inspection Date' },
            { field: 'status', headerName: 'Status' },
            {
              field: 'actions',
              headerName: 'Actions',
              render: (row) => (
                <Button
                  size="small"
                  variant="text"
                  onClick={(event) => {
                    event.stopPropagation();
                    navigate(`/purchase/qc/${row.id}`);
                  }}
                >
                  Open
                </Button>
              )
            }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No QC inspections found."
          onRowClick={(row) => navigate(`/purchase/qc/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
