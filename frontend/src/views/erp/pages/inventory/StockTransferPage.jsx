import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';

import apiClient from 'api/client';
import DataTable from 'components/common/DataTable';
import PageHeader from 'components/common/PageHeader';
import MainCard from 'ui-component/cards/MainCard';

export default function StockTransferPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ status: '' });
  const [godownLookup, setGodownLookup] = useState({});

  const loadRows = (params = {}) => {
    setLoading(true);
    apiClient
      .get('/api/stock-transfers', { params })
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRows();
    apiClient
      .get('/api/godowns')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, godown) => {
          acc[godown.id] = godown.name;
          return acc;
        }, {});
        setGodownLookup(lookup);
      })
      .catch(() => setGodownLookup({}));
  }, []);

  useEffect(() => {
    const handle = setTimeout(() => {
      loadRows({ status: filters.status || undefined });
    }, 250);
    return () => clearTimeout(handle);
  }, [filters]);

  return (
    <MainCard>
      <PageHeader
        title="Stock Transfer"
        breadcrumbs={[{ label: 'Inventory' }, { label: 'Stock Transfer' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/inventory/stock-transfer/new')}>
            Create Transfer
          </Button>
        }
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
              <MenuItem value="DRAFT">Draft</MenuItem>
              <MenuItem value="POSTED">Posted</MenuItem>
            </TextField>
          </Grid>
        </Grid>
        <DataTable
          columns={[
            { field: 'transferNo', headerName: 'Transfer No' },
            { field: 'transferDate', headerName: 'Date' },
            {
              field: 'fromGodownId',
              headerName: 'From',
              render: (row) => godownLookup[row.fromGodownId] || row.fromGodownId
            },
            {
              field: 'toGodownId',
              headerName: 'To',
              render: (row) => godownLookup[row.toGodownId] || row.toGodownId
            },
            { field: 'status', headerName: 'Status' },
            {
              field: 'actions',
              headerName: 'Actions',
              render: (row) => (
                <Stack direction="row" spacing={1}>
                  <Button
                    size="small"
                    variant="text"
                    onClick={(event) => {
                      event.stopPropagation();
                      navigate(`/inventory/stock-transfer/${row.id}/edit`);
                    }}
                  >
                    Edit
                  </Button>
                </Stack>
              )
            }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No stock transfers found."
        />
      </Stack>
    </MainCard>
  );
}
