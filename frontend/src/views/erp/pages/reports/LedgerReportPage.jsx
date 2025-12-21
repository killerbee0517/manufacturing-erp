import { useEffect, useState } from 'react';

import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import FormDrawer from 'components/common/FormDrawer';
import EntitySelect from 'components/common/EntitySelect';
import { reportsApi } from 'api/reports';

export default function LedgerReportPage() {
  const [filters, setFilters] = useState({ ledger: null, from: '', to: '' });
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [activeRow, setActiveRow] = useState(null);

  const columns = [
    { field: 'date', headerName: 'Date' },
    { field: 'particulars', headerName: 'Particulars' },
    { field: 'voucherType', headerName: 'Voucher Type' },
    { field: 'voucherNo', headerName: 'Voucher No' },
    { field: 'debit', headerName: 'Debit' },
    { field: 'credit', headerName: 'Credit' },
    { field: 'balance', headerName: 'Balance' }
  ];

  const loadReport = () => {
    setLoading(true);
    reportsApi
      .ledger({
        ledger: filters.ledger?.name || '',
        from: filters.from,
        to: filters.to
      })
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadReport();
  }, []);

  const handleRowClick = (row) => {
    setActiveRow(row);
    setDetailOpen(true);
  };

  return (
    <MainCard>
      <PageHeader title="Ledger Report" breadcrumbs={[{ label: 'Reports' }, { label: 'Ledger' }]} />
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <EntitySelect
            label="Ledger"
            endpoint="/settings/ledgers"
            value={filters.ledger}
            onChange={(value) => setFilters((prev) => ({ ...prev, ledger: value }))}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <TextField
            fullWidth
            type="date"
            label="From"
            value={filters.from}
            onChange={(event) => setFilters((prev) => ({ ...prev, from: event.target.value }))}
            InputLabelProps={{ shrink: true }}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <TextField
            fullWidth
            type="date"
            label="To"
            value={filters.to}
            onChange={(event) => setFilters((prev) => ({ ...prev, to: event.target.value }))}
            InputLabelProps={{ shrink: true }}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 2 }}>
          <Button variant="contained" color="secondary" fullWidth onClick={loadReport}>
            Apply
          </Button>
        </Grid>
      </Grid>
      <DataTable columns={columns} rows={rows} loading={loading} onRowClick={handleRowClick} />
      <FormDrawer open={detailOpen} title="Voucher Details" onClose={() => setDetailOpen(false)} onSubmit={(e) => e.preventDefault()} submitLabel="Close">
        <Box>
          <Typography variant="subtitle2">Voucher No</Typography>
          <Typography variant="body2" sx={{ mb: 2 }}>{activeRow?.voucherNo || '-'}</Typography>
          <Typography variant="subtitle2">Voucher Type</Typography>
          <Typography variant="body2" sx={{ mb: 2 }}>{activeRow?.voucherType || '-'}</Typography>
          <Typography variant="subtitle2">Particulars</Typography>
          <Typography variant="body2" sx={{ mb: 2 }}>{activeRow?.particulars || '-'}</Typography>
          <Typography variant="subtitle2">Debit / Credit</Typography>
          <Typography variant="body2">{activeRow?.debit || 0} / {activeRow?.credit || 0}</Typography>
        </Box>
      </FormDrawer>
    </MainCard>
  );
}
