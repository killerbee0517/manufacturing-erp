import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

export default function LedgerListPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/api/ledgers')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <MainCard>
      <PageHeader title="Ledgers" breadcrumbs={[{ label: 'Accounts' }, { label: 'Ledgers' }]} />
      <DataTable
        rows={rows}
        loading={loading}
        columns={[
          { field: 'name', headerName: 'Ledger' },
          { field: 'type', headerName: 'Type' },
          {
            field: 'balance',
            headerName: 'Balance',
            render: (row) => (row.balance !== null && row.balance !== undefined ? Number(row.balance).toFixed(2) : '-')
          },
          {
            field: 'actions',
            headerName: 'Actions',
            render: (row) => (
              <Stack direction="row" spacing={1}>
                <Button size="small" variant="text" onClick={() => navigate(`/accounts/ledgers/${row.id}`)}>
                  Statement
                </Button>
              </Stack>
            )
          }
        ]}
        emptyMessage="No ledgers found."
        onRowClick={(row) => navigate(`/accounts/ledgers/${row.id}`)}
      />
    </MainCard>
  );
}
