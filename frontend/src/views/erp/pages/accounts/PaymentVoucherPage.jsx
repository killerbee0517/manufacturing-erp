import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function PaymentVoucherPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

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
        <DataTable
          columns={[
            { field: 'voucherNo', headerName: 'Voucher No' },
            { field: 'voucherDate', headerName: 'Date' },
            { field: 'partyType', headerName: 'Party Type' },
            { field: 'partyName', headerName: 'Party' },
            { field: 'amount', headerName: 'Amount' },
            { field: 'status', headerName: 'Status' }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No payment vouchers found."
          onRowClick={(row) => navigate(`/accounts/payments/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
