import { useEffect, useState } from 'react';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';

import apiClient from 'api/client';
import DataTable from 'components/common/DataTable';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import PageHeader from 'components/common/PageHeader';
import MainCard from 'ui-component/cards/MainCard';

export default function ItemBalancesPage() {
  const [filters, setFilters] = useState({ itemId: '', godownId: '' });
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
      .get('/api/stock-on-hand', {
        params: {
          itemId: filters.itemId || undefined,
          godownId: filters.godownId || undefined
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
      <PageHeader title="Stock On Hand" breadcrumbs={[{ label: 'Inventory' }, { label: 'Stock On Hand' }]} />
      <Stack spacing={2}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
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
          <Grid size={{ xs: 12, md: 4 }}>
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
          <Grid size={{ xs: 12 }}>
            <Button variant="outlined" onClick={loadRows}>
              Apply Filters
            </Button>
          </Grid>
        </Grid>
        <DataTable
          columns={[
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
            { field: 'balance', headerName: 'Balance' }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No stock found."
        />
      </Stack>
    </MainCard>
  );
}
