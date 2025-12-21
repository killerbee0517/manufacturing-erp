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
import RfqCloseDialog from './components/RfqCloseDialog';

export default function RfqPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ q: '', status: '' });
  const [supplierMap, setSupplierMap] = useState({});
  const [closeTarget, setCloseTarget] = useState(null);
  const [closing, setClosing] = useState(false);

  const loadRows = (params = {}) => {
    setLoading(true);
    apiClient
      .get('/api/rfq', { params })
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
          acc[supplier.id] = supplier.name;
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

  const handleCloseRfq = async (reason) => {
    if (!closeTarget) return;
    setClosing(true);
    try {
      const response = await apiClient.post(`/api/rfq/${closeTarget.id}/close`, { closureReason: reason });
      const poId = response.data?.purchaseOrderId;
      if (poId) {
        navigate(`/purchase/po/${poId}`);
        return;
      }
      loadRows({
        q: filters.q || undefined,
        status: filters.status || undefined
      });
    } finally {
      setClosing(false);
      setCloseTarget(null);
    }
  };

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
              <MenuItem value="CLOSED">Closed</MenuItem>
            </TextField>
          </Grid>
        </Grid>
        <DataTable
          columns={[
            { field: 'rfqNo', headerName: 'RFQ No' },
            { field: 'supplierId', headerName: 'Supplier', render: (row) => supplierMap[row.supplierId] || row.supplierId },
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
                  <Button
                    size="small"
                    variant="text"
                    color="secondary"
                    onClick={(event) => {
                      event.stopPropagation();
                      setCloseTarget(row);
                    }}
                    disabled={row.status === 'CLOSED'}
                  >
                    Close
                  </Button>
                </Stack>
              )
            }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No RFQs found."
          onRowClick={(row) => navigate(`/purchase/rfq/${row.id}`)}
        />
      </Stack>
      <RfqCloseDialog
        open={Boolean(closeTarget)}
        onClose={() => setCloseTarget(null)}
        onConfirm={handleCloseRfq}
        loading={closing}
      />
    </MainCard>
  );
}
