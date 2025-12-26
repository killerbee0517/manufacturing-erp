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

export default function SalesOrderPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ status: '' });
  const [customerLookup, setCustomerLookup] = useState({});

  const loadRows = (params = {}) => {
    setLoading(true);
    apiClient
      .get('/api/sales-orders', { params })
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRows();
    apiClient
      .get('/api/customers')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, customer) => {
          acc[customer.id] = customer.name;
          return acc;
        }, {});
        setCustomerLookup(lookup);
      })
      .catch(() => setCustomerLookup({}));
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
        title="Sales Orders"
        breadcrumbs={[{ label: 'Sales' }, { label: 'Sales Order' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/sales/sales-order/new')}>
            Create Sales Order
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
              <MenuItem value="CLOSED">Closed</MenuItem>
            </TextField>
          </Grid>
        </Grid>
        <DataTable
          columns={[
            { field: 'soNo', headerName: 'SO No' },
            {
              field: 'customerId',
              headerName: 'Customer',
              render: (row) => customerLookup[row.customerId] || row.customerId
            },
            { field: 'orderDate', headerName: 'Order Date' },
            { field: 'status', headerName: 'Status' },
            {
              field: 'actions',
              headerName: 'Actions',
              render: (row) => (
                <Stack direction="row" spacing={1}>
                  <Button size="small" variant="text" onClick={(event) => {
                    event.stopPropagation();
                    navigate(`/sales/sales-order/${row.id}/edit`);
                  }}>
                    Edit
                  </Button>
                </Stack>
              )
            }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No sales orders found."
        />
      </Stack>
    </MainCard>
  );
}
