import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function QcPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [poMap, setPoMap] = useState({});
  const [ticketMap, setTicketMap] = useState({});
  const [filters, setFilters] = useState({ status: '' });

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/api/qc/inspections')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));

    apiClient
      .get('/api/purchase-orders', { params: { page: 0, size: 200 } })
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
      .get('/api/weighbridge/tickets')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, ticket) => {
          acc[ticket.id] = ticket.serialNo;
          return acc;
        }, {});
        setTicketMap(lookup);
      })
      .catch(() => setTicketMap({}));
  }, []);

  const filteredRows = useMemo(() => {
    if (!filters.status) return rows;
    return rows.filter((row) => row.status === filters.status);
  }, [rows, filters.status]);

  return (
    <MainCard>
      <PageHeader
        title="QC Inspections"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'QC Inspection' }]}
        actions={null}
      />
      <Stack spacing={2}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <TextField
              fullWidth
              select
              label="Status"
              value={filters.status}
              onChange={(event) => setFilters((prev) => ({ ...prev, status: event.target.value }))}
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="DRAFT">DRAFT</MenuItem>
              <MenuItem value="SUBMITTED">SUBMITTED</MenuItem>
              <MenuItem value="APPROVED">APPROVED</MenuItem>
              <MenuItem value="REJECTED">REJECTED</MenuItem>
            </TextField>
          </Grid>
        </Grid>
        <DataTable
          columns={[
            { field: 'id', headerName: 'QC ID' },
            {
              field: 'purchaseOrderId',
              headerName: 'PO',
              render: (row) => poMap[row.purchaseOrderId] || row.purchaseOrderId || '-'
            },
            {
              field: 'weighbridgeTicketId',
              headerName: 'Ticket',
              render: (row) => ticketMap[row.weighbridgeTicketId] || row.weighbridgeTicketId || '-'
            },
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
          rows={filteredRows}
          loading={loading}
          emptyMessage="No QC inspections found."
          onRowClick={(row) => navigate(`/purchase/qc/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
