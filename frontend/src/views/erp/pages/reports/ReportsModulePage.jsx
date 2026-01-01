import { useMemo, useState } from 'react';

import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import apiClient from 'api/client';

const downloadBlob = (data, filename) => {
  const url = window.URL.createObjectURL(data);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  window.URL.revokeObjectURL(url);
};

const reportDefinitions = [
  { id: 'daily-production', title: 'Daily Production Chart', filters: ['fromDate', 'toDate', 'itemId', 'godownId'] },
  { id: 'monthly-production', title: 'Monthly Production Chart', filters: ['fromDate', 'toDate', 'itemId', 'godownId'] },
  { id: 'rice-daily-summary', title: 'Rice Daily Summary', filters: ['fromDate', 'toDate', 'itemId'] },
  { id: 'purchase-statement-rice', title: 'Purchase Statement (Rice)', filters: ['fromDate', 'toDate', 'partyId', 'itemId'] },
  { id: 'purchase-statement-agro', title: 'Purchase Statement (Agro)', filters: ['fromDate', 'toDate', 'partyId', 'itemId'] },
  { id: 'partywise-purchase-contract', title: 'Partywise Purchase Contract', filters: ['fromDate', 'toDate', 'partyId'] },
  { id: 'spices-agro-report', title: 'Spices & Agro Report', filters: ['fromDate', 'toDate', 'itemId', 'godownId'] },
  { id: 'foods-report', title: 'Foods Report', filters: ['asOnDate'] },
  { id: 'bank-payment-summary', title: 'Bank Payment Summary', filters: ['fromDate', 'toDate', 'bankId', 'partyId'] },
  { id: 'tds-report', title: 'TDS Report', filters: ['fromDate', 'toDate', 'partyId'] }
];

const buildColumns = (headers) =>
  headers.map((header, index) => ({
    field: `c${index}`,
    headerName: header
  }));

const buildRows = (rows) =>
  rows.map((row, index) => {
    const record = { id: index + 1 };
    row.forEach((value, idx) => {
      record[`c${idx}`] = value;
    });
    return record;
  });

function ReportSection({ report }) {
  const [filters, setFilters] = useState({
    fromDate: '',
    toDate: '',
    asOnDate: '',
    partyId: '',
    itemId: '',
    godownId: '',
    bankId: ''
  });
  const [loading, setLoading] = useState(false);
  const [headers, setHeaders] = useState([]);
  const [rows, setRows] = useState([]);

  const columns = useMemo(() => buildColumns(headers), [headers]);
  const tableRows = useMemo(() => buildRows(rows), [rows]);

  const buildParams = () => {
    const params = {};
    if (report.filters.includes('fromDate') && filters.fromDate) params.fromDate = filters.fromDate;
    if (report.filters.includes('toDate') && filters.toDate) params.toDate = filters.toDate;
    if (report.filters.includes('asOnDate') && filters.asOnDate) params.asOnDate = filters.asOnDate;
    if (report.filters.includes('partyId') && filters.partyId) params.partyId = filters.partyId;
    if (report.filters.includes('itemId') && filters.itemId) params.itemId = filters.itemId;
    if (report.filters.includes('godownId') && filters.godownId) params.godownId = filters.godownId;
    if (report.filters.includes('bankId') && filters.bankId) params.bankId = filters.bankId;
    return params;
  };

  const handleFetch = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`/api/reports/templates/${report.id}`, { params: buildParams() });
      setHeaders(response.data.headers || []);
      setRows(response.data.rows || []);
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`/api/reports/templates/${report.id}/export`, {
        params: buildParams(),
        responseType: 'blob'
      });
      downloadBlob(new Blob([response.data]), `${report.id}.xlsx`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <Typography variant="h6">{report.title}</Typography>
          <Grid container spacing={2}>
            {report.filters.includes('fromDate') && (
              <Grid size={{ xs: 12, md: 3 }}>
                <TextField
                  fullWidth
                  type="date"
                  label="From Date"
                  value={filters.fromDate}
                  onChange={(event) => setFilters((prev) => ({ ...prev, fromDate: event.target.value }))}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
            )}
            {report.filters.includes('toDate') && (
              <Grid size={{ xs: 12, md: 3 }}>
                <TextField
                  fullWidth
                  type="date"
                  label="To Date"
                  value={filters.toDate}
                  onChange={(event) => setFilters((prev) => ({ ...prev, toDate: event.target.value }))}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
            )}
            {report.filters.includes('asOnDate') && (
              <Grid size={{ xs: 12, md: 3 }}>
                <TextField
                  fullWidth
                  type="date"
                  label="As On Date"
                  value={filters.asOnDate}
                  onChange={(event) => setFilters((prev) => ({ ...prev, asOnDate: event.target.value }))}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
            )}
            {report.filters.includes('partyId') && (
              <Grid size={{ xs: 12, md: 4 }}>
                <MasterAutocomplete
                  label="Supplier/Party"
                  endpoint="/api/suppliers"
                  value={filters.partyId}
                  onChange={(value) => setFilters((prev) => ({ ...prev, partyId: value }))}
                  placeholder="Select supplier"
                />
              </Grid>
            )}
            {report.filters.includes('itemId') && (
              <Grid size={{ xs: 12, md: 4 }}>
                <MasterAutocomplete
                  label="Item"
                  endpoint="/api/items"
                  value={filters.itemId}
                  onChange={(value) => setFilters((prev) => ({ ...prev, itemId: value }))}
                  placeholder="Select item"
                />
              </Grid>
            )}
            {report.filters.includes('godownId') && (
              <Grid size={{ xs: 12, md: 4 }}>
                <MasterAutocomplete
                  label="Godown"
                  endpoint="/api/godowns"
                  value={filters.godownId}
                  onChange={(value) => setFilters((prev) => ({ ...prev, godownId: value }))}
                  placeholder="Select godown"
                />
              </Grid>
            )}
            {report.filters.includes('bankId') && (
              <Grid size={{ xs: 12, md: 4 }}>
                <MasterAutocomplete
                  label="Bank"
                  endpoint="/api/banks"
                  value={filters.bankId}
                  onChange={(value) => setFilters((prev) => ({ ...prev, bankId: value }))}
                  placeholder="Select bank"
                />
              </Grid>
            )}
          </Grid>
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={handleFetch} disabled={loading}>
              Load
            </Button>
            <Button variant="contained" onClick={handleExport} disabled={loading}>
              Export Excel
            </Button>
          </Stack>
          <DataTable
            columns={columns}
            rows={tableRows}
            loading={loading}
            emptyMessage="No report data found."
            searchPlaceholder="Search report data"
          />
        </Stack>
      </CardContent>
    </Card>
  );
}

export default function ReportsModulePage() {
  return (
    <MainCard>
      <PageHeader title="Reports Module" breadcrumbs={[{ label: 'Reports' }, { label: 'Reports Module' }]} />
      <Stack spacing={3}>
        {reportDefinitions.map((report) => (
          <ReportSection key={report.id} report={report} />
        ))}
      </Stack>
    </MainCard>
  );
}
