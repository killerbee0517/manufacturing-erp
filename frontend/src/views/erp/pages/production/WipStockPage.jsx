import { useEffect, useState } from 'react';

import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import { productionApi } from 'api/production';

export default function WipStockPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  const columns = [
    { field: 'itemName', headerName: 'Item' },
    { field: 'uomCode', headerName: 'UOM' },
    { field: 'availableQuantity', headerName: 'Available' },
    { field: 'quantity', headerName: 'Produced' },
    { field: 'consumedQuantity', headerName: 'Consumed' },
    { field: 'batchId', headerName: 'Batch' }
  ];

  useEffect(() => {
    setLoading(true);
    productionApi
      .listWipBalances()
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <MainCard>
      <PageHeader title="WIP Stock" breadcrumbs={[{ label: 'Production' }, { label: 'WIP Stock' }]} />
      <Stack spacing={2}>
        <DataTable columns={columns} rows={rows} loading={loading} emptyMessage="No WIP stock available." />
        {rows.length === 0 && !loading && (
          <Typography variant="body2" color="text.secondary">
            WIP will appear after recording outputs as WIP from production batches.
          </Typography>
        )}
      </Stack>
    </MainCard>
  );
}
