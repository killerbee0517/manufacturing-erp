import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

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
  const [searchParams] = useSearchParams();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedGrn, setSelectedGrn] = useState('');
  const [selectedPo, setSelectedPo] = useState('');
  const [creating, setCreating] = useState(false);
  const [creatingPo, setCreatingPo] = useState(false);

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

  useEffect(() => {
    const poId = searchParams.get('poId');
    if (poId) {
      setSelectedPo(poId);
    }
  }, [searchParams]);

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

  const handleCreateFromPo = async () => {
    if (!selectedPo) return;
    setCreatingPo(true);
    try {
      const response = await apiClient.post(`/api/purchase-invoices/from-po/${selectedPo}`);
      navigate(`/purchase/purchase-invoice/${response.data.id}`);
    } finally {
      setCreatingPo(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="Purchase Invoices"
        breadcrumbs={[{ label: 'Purchase' }, { label: 'Purchase Invoice' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="contained" color="secondary" onClick={handleCreate} disabled={!selectedGrn || creating}>
              Create from GRN
            </Button>
            <Button variant="contained" color="primary" onClick={handleCreateFromPo} disabled={!selectedPo || creatingPo}>
              Create from PO
            </Button>
          </Stack>
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
        <Typography>Select a purchase order to create or open its purchase invoice.</Typography>
        <MasterAutocomplete
          label="Purchase Order"
          endpoint="/api/purchase-orders"
          value={selectedPo}
          onChange={setSelectedPo}
          optionLabelKey="poNo"
          optionValueKey="id"
          placeholder="Search purchase orders"
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
