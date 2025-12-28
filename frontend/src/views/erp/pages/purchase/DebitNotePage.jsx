import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import apiClient from 'api/client';

export default function DebitNotePage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState('');
  const [creating, setCreating] = useState(false);

  const fetchNotes = () => {
    setLoading(true);
    apiClient
      .get('/api/debit-notes')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchNotes();
  }, []);

  const handleCreate = async () => {
    if (!selectedInvoice) return;
    setCreating(true);
    try {
      const response = await apiClient.post(`/api/debit-notes/from-invoice/${selectedInvoice}`);
      navigate(`/purchase/debit-note/${response.data.id}`);
    } finally {
      setCreating(false);
    }
  };

  return (
    <MainCard>
      <PageHeader title="Debit Notes" breadcrumbs={[{ label: 'Purchase' }, { label: 'Debit Note' }]} />
      <Stack spacing={2}>
        <Typography>Select a purchase invoice to create or open its debit note.</Typography>
        <MasterAutocomplete
          label="Purchase Invoice"
          endpoint="/api/purchase-invoices"
          value={selectedInvoice}
          onChange={setSelectedInvoice}
          optionLabelKey="invoiceNo"
          optionValueKey="id"
          placeholder="Search purchase invoices"
        />
        <Button variant="contained" color="secondary" onClick={handleCreate} disabled={!selectedInvoice || creating}>
          Create from Invoice
        </Button>
        <DataTable
          columns={[
            { field: 'dnNo', headerName: 'DN No' },
            { field: 'supplierName', headerName: 'Supplier', render: (row) => row.supplierName || row.supplierId },
            {
              field: 'purchaseInvoiceNo',
              headerName: 'Purchase Invoice',
              render: (row) => row.purchaseInvoiceNo || row.purchaseInvoiceId
            },
            { field: 'dnDate', headerName: 'Date' },
            { field: 'totalDeduction', headerName: 'Total Deduction' },
            { field: 'status', headerName: 'Status' }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No debit notes found."
          onRowClick={(row) => navigate(`/purchase/debit-note/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
