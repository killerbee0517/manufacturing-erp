import PropTypes from 'prop-types';
import { useEffect, useMemo, useState } from 'react';

import Alert from '@mui/material/Alert';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Snackbar from '@mui/material/Snackbar';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import FormDrawer from 'components/common/FormDrawer';
import ConfirmDialog from 'components/common/ConfirmDialog';
import EntitySelect from 'components/common/EntitySelect';
import { settingsApi } from 'api/settings';

export default function SettingsEntityPage({ entity, config }) {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [activeRecord, setActiveRecord] = useState(null);
  const [formValues, setFormValues] = useState({});
  const [errors, setErrors] = useState({});
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [recordToDelete, setRecordToDelete] = useState(null);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  const fields = useMemo(() => config.fields || [], [config.fields]);
  const columns = useMemo(() => config.columns || [], [config.columns]);

  const loadRows = () => {
    setLoading(true);
    settingsApi
      .list(entity)
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRows();
  }, [entity]);

  const resetForm = () => {
    setFormValues({});
    setErrors({});
    setActiveRecord(null);
  };

  const handleOpenCreate = () => {
    resetForm();
    setDrawerOpen(true);
  };

  const handleEdit = (record) => {
    setActiveRecord(record);
    const nextValues = { ...record };
    fields.forEach((field) => {
      if (field.type === 'select' && record[field.name] && typeof record[field.name] !== 'object') {
        nextValues[field.name] = { id: record[field.name], name: record[field.name] };
      }
    });
    setFormValues(nextValues);
    setErrors({});
    setDrawerOpen(true);
  };

  const validate = () => {
    const nextErrors = {};
    fields.forEach((field) => {
      if (field.required && !formValues[field.name]) {
        nextErrors[field.name] = `${field.label} is required`;
      }
    });
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validate()) return;
    const payload = Object.entries(formValues).reduce((acc, [key, value]) => {
      if (value && typeof value === 'object') {
        acc[key] = value.name || value.label || value.title || value.id;
      } else {
        acc[key] = value;
      }
      return acc;
    }, {});
    try {
      if (activeRecord?.id) {
        await settingsApi.update(entity, activeRecord.id, payload);
        setSnackbar({ open: true, message: 'Updated successfully', severity: 'success' });
      } else {
        await settingsApi.create(entity, payload);
        setSnackbar({ open: true, message: 'Created successfully', severity: 'success' });
      }
      setDrawerOpen(false);
      resetForm();
      loadRows();
    } catch (error) {
      setSnackbar({ open: true, message: error.message || 'Unable to save record', severity: 'error' });
    }
  };

  const handleDeleteRequest = (record) => {
    setRecordToDelete(record);
    setConfirmOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (!recordToDelete?.id) return;
    try {
      await settingsApi.remove(entity, recordToDelete.id);
      setSnackbar({ open: true, message: 'Deleted successfully', severity: 'success' });
      loadRows();
    } catch (error) {
      setSnackbar({ open: true, message: error.message || 'Unable to delete record', severity: 'error' });
    } finally {
      setConfirmOpen(false);
      setRecordToDelete(null);
    }
  };

  const drawerTitle = activeRecord ? `Edit ${config.title}` : `Create ${config.title}`;

  return (
    <MainCard>
      <PageHeader
        title={config.title}
        breadcrumbs={[
          { label: 'Settings', to: '/settings/company' },
          { label: config.title }
        ]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleOpenCreate}>
            New
          </Button>
        }
      />
      <DataTable
        columns={columns}
        rows={rows}
        loading={loading}
        onEdit={handleEdit}
        onDelete={handleDeleteRequest}
        emptyMessage={`No ${config.title.toLowerCase()} found.`}
      />
      <FormDrawer
        open={drawerOpen}
        title={drawerTitle}
        onClose={() => setDrawerOpen(false)}
        onSubmit={handleSubmit}
        submitLabel={activeRecord ? 'Update' : 'Create'}
      >
        <Grid container spacing={2}>
          {fields.map((field) => (
            <Grid key={field.name} size={{ xs: 12 }}>
              {field.type === 'select' ? (
                <EntitySelect
                  label={field.label}
                  endpoint={field.endpoint}
                  value={formValues[field.name] || null}
                  onChange={(value) => setFormValues((prev) => ({ ...prev, [field.name]: value }))}
                  required={field.required}
                />
              ) : (
                <TextField
                  fullWidth
                  label={field.label}
                  value={formValues[field.name] || ''}
                  onChange={(event) => setFormValues((prev) => ({ ...prev, [field.name]: event.target.value }))}
                  type={field.type || 'text'}
                  error={Boolean(errors[field.name])}
                  helperText={errors[field.name]}
                  required={field.required}
                />
              )}
            </Grid>
          ))}
        </Grid>
        {fields.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            No fields configured for this module.
          </Typography>
        )}
      </FormDrawer>
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

SettingsEntityPage.propTypes = {
  entity: PropTypes.string.isRequired,
  config: PropTypes.shape({
    title: PropTypes.string.isRequired,
    fields: PropTypes.array.isRequired,
    columns: PropTypes.array.isRequired
  }).isRequired
};
