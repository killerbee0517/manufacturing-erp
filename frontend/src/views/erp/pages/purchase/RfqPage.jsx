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

export default function RfqPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ q: '', status: '' });
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [supplierMap, setSupplierMap] = useState({});

  const loadRows = (params = {}) => {
    setLoading(true);
    apiClient
      .get('/api/rfq', { params })
      .then((response) => {
        const payload = response.data || {};
        setRows(payload.content || []);
        setTotal(payload.totalElements ?? (payload.content?.length || 0));
        setPage(payload.number ?? 0);
        setPageSize(payload.size ?? params.size ?? pageSize);
      })
      .catch(() => {
        setRows([]);
        setTotal(0);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRows();
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
  }, []);

  useEffect(() => {
    loadRows({
      q: filters.q || undefined,
      status: filters.status || undefined,
      page,
      size: pageSize
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.status, filters.q, page, pageSize]);

  return (
    <MainCard>
      <PageHeader
        title="RFQ"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'RFQ' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/rfq/new')}>
            Create RFQ
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
              onChange={(event) => {
                setPage(0);
                setFilters((prev) => ({ ...prev, status: event.target.value }));
              }}
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="DRAFT">Draft</MenuItem>
              <MenuItem value="QUOTING">Quoting</MenuItem>
              <MenuItem value="AWARDED_PARTIAL">Partially Awarded</MenuItem>
              <MenuItem value="AWARDED_FULL">Fully Awarded</MenuItem>
              <MenuItem value="AWARDED">Awarded</MenuItem>
              <MenuItem value="CANCELLED">Cancelled</MenuItem>
            </TextField>
          </Grid>
        </Grid>
        <DataTable
          columns={[
            { field: 'rfqNo', headerName: 'RFQ No' },
            {
              field: 'suppliers',
              headerName: 'Suppliers',
              render: (row) => {
                const suppliers = row.suppliers || [];
                if (!suppliers.length) return '-';
                return suppliers
                  .map((s) => supplierMap[s.supplierId] || s.supplierId)
                  .filter(Boolean)
                  .join(', ');
              }
            },
            { field: 'rfqDate', headerName: 'Date' },
            { field: 'status', headerName: 'Status' },
            {
              field: 'actions',
              headerName: 'Actions',
              render: (row) => (
                <Stack direction="row" spacing={1}>
                  <Button size="small" variant="text" onClick={(event) => {
                    event.stopPropagation();
                    navigate(`/purchase/rfq/${row.id}`);
                  }}>
                    View
                  </Button>
                  <Button size="small" variant="text" onClick={(event) => {
                    event.stopPropagation();
                    navigate(`/purchase/rfq/${row.id}/edit`);
                  }}>
                    Edit
                  </Button>
                </Stack>
              )
            }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No RFQs found."
          serverPagination
          page={page}
          rowsPerPage={pageSize}
          totalCount={total}
          onPageChange={(nextPage) => setPage(nextPage)}
          onRowsPerPageChange={(nextSize) => {
            setPageSize(nextSize);
            setPage(0);
          }}
          onSearch={(value) => {
            setPage(0);
            setFilters((prev) => ({ ...prev, q: value }));
          }}
          onRowClick={(row) => navigate(`/purchase/rfq/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
