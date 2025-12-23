import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function LedgerStatementPage() {
  const { id } = useParams();
  const [ledger, setLedger] = useState(null);
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ from: '', to: '' });

  const loadStatement = (params = {}) => {
    setLoading(true);
    apiClient
      .get(`/api/ledgers/${id}/statement`, { params })
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    apiClient
      .get('/api/ledgers')
      .then((response) => {
        const list = response.data || [];
        setLedger(list.find((item) => item.id === Number(id)) || null);
      })
      .catch(() => setLedger(null));
  }, [id]);

  useEffect(() => {
    loadStatement({
      from: filters.from || undefined,
      to: filters.to || undefined
    });
  }, [filters, id]);

  return (
    <MainCard>
      <PageHeader
        title={ledger ? `Ledger Statement - ${ledger.name}` : 'Ledger Statement'}
        breadcrumbs={[{ label: 'Accounts' }, { label: 'Ledgers' }, { label: 'Statement' }]}
      />
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid size={{ xs: 12, md: 3 }}>
          <TextField
            fullWidth
            label="From"
            type="date"
            value={filters.from}
            onChange={(event) => setFilters((prev) => ({ ...prev, from: event.target.value }))}
            InputLabelProps={{ shrink: true }}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <TextField
            fullWidth
            label="To"
            type="date"
            value={filters.to}
            onChange={(event) => setFilters((prev) => ({ ...prev, to: event.target.value }))}
            InputLabelProps={{ shrink: true }}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          {ledger && (
            <Typography variant="subtitle1" sx={{ mt: 1 }}>
              Current Balance: {ledger.balance?.toFixed ? ledger.balance.toFixed(2) : ledger.balance}
            </Typography>
          )}
        </Grid>
      </Grid>
      <DataTable
        rows={rows}
        loading={loading}
        columns={[
          { field: 'voucherDate', headerName: 'Date' },
          { field: 'voucherNo', headerName: 'Voucher No' },
          { field: 'narration', headerName: 'Narration' },
          { field: 'drAmount', headerName: 'Debit' },
          { field: 'crAmount', headerName: 'Credit' },
          { field: 'runningBalance', headerName: 'Running Balance' }
        ]}
        emptyMessage="No entries found."
      />
    </MainCard>
  );
}
