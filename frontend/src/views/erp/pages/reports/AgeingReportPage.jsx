import { useEffect, useMemo, useState } from 'react';

import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import { reportsApi } from 'api/reports';

export default function AgeingReportPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  const columns = [
    { field: 'bucket', headerName: 'Bucket' },
    { field: 'amount', headerName: 'Amount' }
  ];

  const totals = useMemo(() => rows.reduce((sum, row) => sum + Number(row.amount || 0), 0), [rows]);

  useEffect(() => {
    setLoading(true);
    reportsApi
      .ageing()
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <MainCard>
      <PageHeader title="Ageing" breadcrumbs={[{ label: 'Reports' }, { label: 'Ageing' }]} />
      <DataTable columns={columns} rows={rows} loading={loading} />
      <Box sx={{ mt: 2 }}>
        <Typography variant="subtitle1">Totals</Typography>
        <Typography variant="h6">{totals.toFixed(2)}</Typography>
      </Box>
    </MainCard>
  );
}
