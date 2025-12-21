import { useEffect, useState } from 'react';

import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import FormDrawer from 'components/common/FormDrawer';
import { reportsApi } from 'api/reports';

export default function OutstandingReportPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [activeRow, setActiveRow] = useState(null);

  const columns = [
    { field: 'party', headerName: 'Party' },
    { field: 'pendingAmount', headerName: 'Pending Amount' },
    { field: 'dueDate', headerName: 'Due Date' },
    { field: 'overdueDays', headerName: 'Overdue Days' }
  ];

  const detailColumns = [
    { field: 'invoiceNo', headerName: 'Invoice No' },
    { field: 'dueDate', headerName: 'Due Date' },
    { field: 'pendingAmount', headerName: 'Pending Amount' }
  ];

  const loadReport = () => {
    setLoading(true);
    reportsApi
      .outstanding()
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
      <PageHeader title="Outstanding" breadcrumbs={[{ label: 'Reports' }, { label: 'Outstanding' }]} />
      <DataTable columns={columns} rows={rows} loading={loading} onRowClick={handleRowClick} />
      <FormDrawer open={detailOpen} title="Outstanding Drilldown" onClose={() => setDetailOpen(false)} onSubmit={(e) => e.preventDefault()} submitLabel="Close">
        <Box>
          <Typography variant="subtitle2">Party</Typography>
          <Typography variant="body2" sx={{ mb: 2 }}>{activeRow?.party || '-'}</Typography>
          <Typography variant="subtitle2">Pending Amount</Typography>
          <Typography variant="body2" sx={{ mb: 2 }}>{activeRow?.pendingAmount || 0}</Typography>
          <Typography variant="subtitle2">Due Date</Typography>
          <Typography variant="body2">{activeRow?.dueDate || '-'}</Typography>
        </Box>
        <Box sx={{ mt: 2 }}>
          <DataTable
            columns={detailColumns}
            rows={
              activeRow
                ? [
                    {
                      id: activeRow.id,
                      invoiceNo: `INV-${activeRow.id}`,
                      dueDate: activeRow.dueDate,
                      pendingAmount: activeRow.pendingAmount
                    }
                  ]
                : []
            }
            loading={false}
            emptyMessage="No invoices found."
          />
        </Box>
      </FormDrawer>
    </MainCard>
  );
}
