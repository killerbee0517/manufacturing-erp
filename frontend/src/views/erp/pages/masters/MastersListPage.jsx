import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Alert from '@mui/material/Alert';
import Button from '@mui/material/Button';
import Snackbar from '@mui/material/Snackbar';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import ConfirmDialog from 'components/common/ConfirmDialog';
import apiClient from 'api/client';
import { mastersEntities } from './mastersConfig';

export default function MastersListPage() {
  const { entity } = useParams();
  const navigate = useNavigate();
  const config = mastersEntities[entity];

  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [recordToDelete, setRecordToDelete] = useState(null);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  const columns = useMemo(
    () =>
      (config?.columns || []).map((column) => ({
        field: column.field,
        headerName: column.label || column.headerName || column.field
      })),
    [config]
  );

  const loadRows = () => {
    if (!config?.listEndpoint) {
      setRows([]);
      return;
    }
    setLoading(true);
    apiClient
      .get(config.listEndpoint)
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRows();
  }, [entity]);

  const handleDeleteRequest = (record) => {
    setRecordToDelete(record);
    setConfirmOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (!recordToDelete?.id || !config?.listEndpoint) {
      setConfirmOpen(false);
      return;
    }
    try {
      await apiClient.delete(`${config.listEndpoint}/${recordToDelete.id}`);
      setSnackbar({ open: true, message: 'Deleted successfully', severity: 'success' });
      loadRows();
    } catch (error) {
      setSnackbar({
        open: true,
        message: error?.response?.data?.message || 'Unable to delete record',
        severity: 'error'
      });
    } finally {
      setConfirmOpen(false);
      setRecordToDelete(null);
    }
  };

  if (!config) {
    return <Alert severity="warning">Master module not configured.</Alert>;
  }

  return (
    <MainCard>
      <PageHeader
        title={config.title}
        breadcrumbs={[
          { label: 'Masters', to: '/masters/suppliers' },
          { label: config.title }
        ]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate(`/masters/${entity}/new`)}>
            New
          </Button>
        }
      />
      <DataTable
        columns={columns}
        rows={rows}
        loading={loading}
        onRowClick={(row) => navigate(`/masters/${entity}/${row.id}`)}
        onEdit={(row) => navigate(`/masters/${entity}/${row.id}/edit`)}
        onDelete={handleDeleteRequest}
        emptyMessage={`No ${config.title.toLowerCase()} found.`}
        searchPlaceholder={`Search ${config.title.toLowerCase()}`}
      />
      <ConfirmDialog
        open={confirmOpen}
        title={`Delete ${config.title}`}
        description={`Are you sure you want to delete this ${config.title.toLowerCase()}?`}
        onConfirm={handleConfirmDelete}
        onClose={() => setConfirmOpen(false)}
      />
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </MainCard>
  );
}
