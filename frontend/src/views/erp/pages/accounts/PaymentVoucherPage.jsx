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

export default function PaymentVoucherPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ mode: '' });

  const fetchVouchers = () => {
    setLoading(true);
    apiClient
      .get('/api/payment-vouchers', { params: { size: 50 } })
      .then((response) => {
        const payload = response.data?.content || response.data || [];
        setRows(payload);
      })
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchVouchers();
  }, []);

  return (
    <MainCard>
      <PageHeader
        title="Payments"
        breadcrumbs={[{ label: 'Accounts' }, { label: 'Payments' }]}
        actions={
          <Button variant="contained" color="primary" onClick={() => navigate('/accounts/payments/new')}>
            New Payment
          </Button>
        }
      />
      <Stack spacing={2}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <TextField
              select
              fullWidth
              label="Payment Mode"
              value={filters.mode}
              onChange={(event) => setFilters({ mode: event.target.value })}
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="BANK">Bank</MenuItem>
              <MenuItem value="CASH">Cash</MenuItem>
              <MenuItem value="PDC">PDC</MenuItem>
            </TextField>
          </Grid>
        </Grid>
        <DataTable
          columns={[
            { field: 'voucherNo', headerName: 'Voucher No' },
            { field: 'voucherDate', headerName: 'Date' },
            { field: 'partyType', headerName: 'Party Type' },
            { field: 'partyName', headerName: 'Party' },
            { field: 'paymentMode', headerName: 'Mode' },
            { field: 'amount', headerName: 'Amount' },
            { field: 'status', headerName: 'Status' }
          ]}
          rows={filters.mode ? rows.filter((row) => row.paymentMode === filters.mode) : rows}
          loading={loading}
          emptyMessage="No payment vouchers found."
          onRowClick={(row) => navigate(`/accounts/payments/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
