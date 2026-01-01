import { useState } from 'react';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';

const downloadBlob = (data, filename) => {
  const url = window.URL.createObjectURL(data);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  window.URL.revokeObjectURL(url);
};

export default function PaymentsReportsPage() {
  const [bankFromDate, setBankFromDate] = useState('');
  const [bankToDate, setBankToDate] = useState('');
  const [tdsFromDate, setTdsFromDate] = useState('');
  const [tdsToDate, setTdsToDate] = useState('');
  const [bankLoading, setBankLoading] = useState(false);
  const [tdsLoading, setTdsLoading] = useState(false);

  const handleExport = async (endpoint, params, filename, setLoading) => {
    setLoading(true);
    try {
      const response = await apiClient.get(endpoint, {
        params,
        responseType: 'blob'
      });
      downloadBlob(new Blob([response.data]), filename);
    } finally {
      setLoading(false);
    }
  };

  return (
    <MainCard>
      <PageHeader title="Payment Reports" breadcrumbs={[{ label: 'Reports' }, { label: 'Payment Reports' }]} />
      <Stack spacing={4}>
        <Stack spacing={2}>
          <Typography variant="h5">Bank Payment Summary</Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                fullWidth
                type="date"
                label="From Date"
                value={bankFromDate}
                onChange={(event) => setBankFromDate(event.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                fullWidth
                type="date"
                label="To Date"
                value={bankToDate}
                onChange={(event) => setBankToDate(event.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Button
                variant="contained"
                color="primary"
                onClick={() =>
                  handleExport(
                    '/api/reports/bank-payment-summary/export',
                    { fromDate: bankFromDate || undefined, toDate: bankToDate || undefined },
                    'bank-payment-summary.xlsx',
                    setBankLoading
                  )
                }
                disabled={bankLoading}
              >
                Export Excel
              </Button>
            </Grid>
          </Grid>
        </Stack>
        <Stack spacing={2}>
          <Typography variant="h5">TDS Report</Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                fullWidth
                type="date"
                label="From Date"
                value={tdsFromDate}
                onChange={(event) => setTdsFromDate(event.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                fullWidth
                type="date"
                label="To Date"
                value={tdsToDate}
                onChange={(event) => setTdsToDate(event.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Button
                variant="contained"
                color="primary"
                onClick={() =>
                  handleExport(
                    '/api/reports/tds/export',
                    { fromDate: tdsFromDate || undefined, toDate: tdsToDate || undefined },
                    'tds-report.xlsx',
                    setTdsLoading
                  )
                }
                disabled={tdsLoading}
              >
                Export Excel
              </Button>
            </Grid>
          </Grid>
        </Stack>
      </Stack>
    </MainCard>
  );
}
