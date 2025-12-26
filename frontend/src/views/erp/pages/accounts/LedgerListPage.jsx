import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function LedgerListPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ q: '', type: '' });

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/api/ledgers', {
        params: {
          q: filters.q || undefined,
          type: filters.type || undefined
        }
      })
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, [filters]);

  return (
    <MainCard>
      <PageHeader title="Ledgers" breadcrumbs={[{ label: 'Accounts' }, { label: 'Ledgers' }]} />
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField
            fullWidth
            label="Search"
            value={filters.q}
            onChange={(event) => setFilters((prev) => ({ ...prev, q: event.target.value }))}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <TextField
            fullWidth
            select
            label="Type"
            value={filters.type}
            onChange={(event) => setFilters((prev) => ({ ...prev, type: event.target.value }))}
            SelectProps={{ native: true }}
          >
            <option value="">All</option>
            <option value="SUPPLIER">SUPPLIER</option>
            <option value="CUSTOMER">CUSTOMER</option>
            <option value="EXPENSE">EXPENSE</option>
            <option value="BANK">BANK</option>
            <option value="GENERAL">GENERAL</option>
          </TextField>
        </Grid>
      </Grid>
      <DataTable
        rows={rows}
        loading={loading}
        columns={[
          { field: 'name', headerName: 'Ledger' },
          { field: 'type', headerName: 'Type' },
          { field: 'enabled', headerName: 'Enabled', render: (row) => (row.enabled ? 'Yes' : 'No') },
          {
            field: 'balance',
            headerName: 'Balance',
            render: (row) => (row.balance !== null && row.balance !== undefined ? Number(row.balance).toFixed(2) : '-')
          },
          {
            field: 'actions',
            headerName: 'Actions',
            render: (row) => (
              <Stack direction="row" spacing={1}>
                <Button size="small" variant="text" onClick={() => navigate(`/accounts/ledgers/${row.id}`)}>
                  Statement
                </Button>
              </Stack>
            )
          }
        ]}
        emptyMessage="No ledgers found."
        onRowClick={(row) => navigate(`/accounts/ledgers/${row.id}`)}
      />
    </MainCard>
  );
}
