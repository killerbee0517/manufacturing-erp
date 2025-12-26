import { useEffect, useState } from 'react';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';

import apiClient from 'api/client';
import DataTable from 'components/common/DataTable';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import PageHeader from 'components/common/PageHeader';
import MainCard from 'ui-component/cards/MainCard';

export default function StockLedgerPage() {
  const [filters, setFilters] = useState({
    itemId: '',
    godownId: '',
    from: '',
    to: ''
  });
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [itemLookup, setItemLookup] = useState({});
  const [godownLookup, setGodownLookup] = useState({});

  const loadLookups = () => {
    apiClient
      .get('/api/items')
      .then((response) => {
        const map = (response.data || []).reduce((acc, item) => {
          acc[item.id] = item.name;
          return acc;
        }, {});
        setItemLookup(map);
      })
      .catch(() => setItemLookup({}));
    apiClient
      .get('/api/godowns')
      .then((response) => {
        const map = (response.data || []).reduce((acc, item) => {
          acc[item.id] = item.name;
          return acc;
        }, {});
        setGodownLookup(map);
      })
      .catch(() => setGodownLookup({}));
  };

  const loadRows = () => {
    setLoading(true);
    apiClient
      .get('/api/stock-ledger', {
        params: {
          itemId: filters.itemId || undefined,
          godownId: filters.godownId || undefined,
          from: filters.from || undefined,
          to: filters.to || undefined
        }
      })
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadLookups();
    loadRows();
  }, []);

  return (
    <MainCard>
      <PageHeader title="Stock Ledger" breadcrumbs={[{ label: 'Inventory' }, { label: 'Stock Ledger' }]} />
      <Stack spacing={2}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
            <MasterAutocomplete
              label="Item"
              endpoint="/api/items"
              value={filters.itemId}
              onChange={(value) => setFilters((prev) => ({ ...prev, itemId: value }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="All items"
            />
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <MasterAutocomplete
              label="Godown"
              endpoint="/api/godowns"
              value={filters.godownId}
              onChange={(value) => setFilters((prev) => ({ ...prev, godownId: value }))}
              optionLabelKey="name"
              optionValueKey="id"
              placeholder="All godowns"
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
          <Grid size={{ xs: 12 }}>
            <Button variant="outlined" onClick={loadRows}>
              Apply Filters
            </Button>
          </Grid>
        </Grid>
        <DataTable
          columns={[
            { field: 'postedAt', headerName: 'Posted At' },
            { field: 'docType', headerName: 'Doc Type' },
            { field: 'docId', headerName: 'Doc ID' },
            {
              field: 'itemId',
              headerName: 'Item',
              render: (row) => itemLookup[row.itemId] || row.itemId
            },
            {
              field: 'godownId',
              headerName: 'Godown',
              render: (row) => godownLookup[row.godownId] || row.godownId
            },
            { field: 'qtyIn', headerName: 'Qty In' },
            { field: 'qtyOut', headerName: 'Qty Out' },
            { field: 'txnType', headerName: 'Txn Type' },
            { field: 'status', headerName: 'Status' }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No stock ledger entries."
        />
      </Stack>
    </MainCard>
  );
}
