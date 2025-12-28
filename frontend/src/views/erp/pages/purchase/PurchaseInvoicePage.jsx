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

export default function PurchaseInvoicePage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedGrn, setSelectedGrn] = useState('');
  const [creating, setCreating] = useState(false);

  const fetchInvoices = () => {
    setLoading(true);
    apiClient
      .get('/api/purchase-invoices')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchInvoices();
  }, []);

  const handleCreate = async () => {
    if (!selectedGrn) return;
    setCreating(true);
    try {
      const response = await apiClient.post(`/api/purchase-invoices/from-grn/${selectedGrn}`);
      navigate(`/purchase/purchase-invoice/${response.data.id}`);
    } finally {
      setCreating(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="Purchase Invoices"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'Purchase Invoice' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleCreate} disabled={!selectedGrn || creating}>
            Create from GRN
          </Button>
        }
      />
      <Stack spacing={2}>
        <Typography>Select a posted GRN to create or open its purchase invoice.</Typography>
        <MasterAutocomplete
          label="GRN"
          endpoint="/api/grn"
          queryParams={{ status: 'POSTED' }}
          value={selectedGrn}
          onChange={setSelectedGrn}
          optionLabelKey="grnNo"
          optionValueKey="id"
          placeholder="Search posted GRNs"
        />
        <DataTable
          columns={[
            { field: 'invoiceNo', headerName: 'Invoice No' },
            { field: 'supplierName', headerName: 'Supplier', render: (row) => row.supplierName || row.supplierId },
            { field: 'purchaseOrderNo', headerName: 'PO', render: (row) => row.purchaseOrderNo || row.purchaseOrderId },
            { field: 'grnNo', headerName: 'GRN', render: (row) => row.grnNo || row.grnId },
            { field: 'invoiceDate', headerName: 'Invoice Date' },
            { field: 'grandTotal', headerName: 'Total' },
            { field: 'status', headerName: 'Status' }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No purchase invoices found."
          onRowClick={(row) => navigate(`/purchase/purchase-invoice/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
