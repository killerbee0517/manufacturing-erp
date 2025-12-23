import { useEffect, useState } from 'react';
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

export default function PurchaseOrderPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ q: '', status: '' });
  const [supplierMap, setSupplierMap] = useState({});

  const loadRows = (params = {}) => {
    setLoading(true);
    apiClient
      .get('/api/purchase-orders', { params })
      .then((response) => {
        const payload = response.data || [];
        setRows(payload.content || payload);
      })
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRows();
    apiClient
      .get('/api/suppliers')
      .then((response) => {
        const lookup = (response.data || []).reduce((acc, supplier) => {
          acc[supplier.id] = { name: supplier.name, balance: supplier.currentBalance };
          return acc;
        }, {});
        setSupplierMap(lookup);
      })
      .catch(() => setSupplierMap({}));
  }, []);

  useEffect(() => {
    const handle = setTimeout(() => {
      loadRows({
        q: filters.q || undefined,
        status: filters.status || undefined
      });
    }, 300);
    return () => clearTimeout(handle);
  }, [filters]);

  return (
    <MainCard>
      <PageHeader
        title="Purchase Orders"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'Purchase Order' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/po/new')}>
            Create PO
          </Button>
        }
      />
      <Stack spacing={2}>
        <Grid container spacing={2}>
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
              label="Status"
              value={filters.status}
              onChange={(event) => setFilters((prev) => ({ ...prev, status: event.target.value }))}
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="DRAFT">Draft</MenuItem>
              <MenuItem value="SUBMITTED">Submitted</MenuItem>
              <MenuItem value="APPROVED">Approved</MenuItem>
            </TextField>
          </Grid>
        </Grid>
        <DataTable
          columns={[
            { field: 'poNo', headerName: 'PO No' },
            {
              field: 'supplierId',
              headerName: 'Supplier',
              render: (row) => supplierMap[row.supplierId]?.name || row.supplierId
            },
            {
              field: 'supplierBalance',
              headerName: 'Supplier Balance',
              render: (row) => {
                const balance = supplierMap[row.supplierId]?.balance;
                if (balance === null || balance === undefined) return '-';
                return Number(balance).toFixed(2);
              }
            },
            { field: 'poDate', headerName: 'Order Date' },
            { field: 'status', headerName: 'Status' },
            {
              field: 'actions',
              headerName: 'Actions',
              render: (row) => (
                <Stack direction="row" spacing={1}>
                  <Button size="small" variant="text" onClick={(event) => {
                    event.stopPropagation();
                    navigate(`/purchase/po/${row.id}`);
                  }}>
                    View
                  </Button>
                  <Button size="small" variant="text" onClick={(event) => {
                    event.stopPropagation();
                    navigate(`/purchase/po/${row.id}/edit`);
                  }}>
                    Edit
                  </Button>
                </Stack>
              )
            }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No purchase orders found."
          onRowClick={(row) => navigate(`/purchase/po/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
