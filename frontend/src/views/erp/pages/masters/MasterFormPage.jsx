import PropTypes from 'prop-types';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Alert from '@mui/material/Alert';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Snackbar from '@mui/material/Snackbar';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import { mastersEntities } from './mastersConfig';

const lookupEndpoints = {
  suppliers: '/api/suppliers',
  items: '/api/items',
  uoms: '/api/uoms',
  locations: '/api/locations',
  customers: '/api/customers',
  banks: '/api/banks/autocomplete',
  godowns: '/api/godowns',
  brokers: '/api/brokers',
  parties: '/api/parties/autocomplete',
  roles: '/api/roles',
  tickets: '/api/weighbridge/tickets',
  salesOrders: '/api/sales-orders'
};
const partyAutofillDefaults = [
  'name',
  'address',
  'state',
  'country',
  'pinCode',
  'pan',
  'gstNo',
  'contact',
  'email',
  'bankId'
];

export default function MasterFormPage({ mode }) {
  const { entity, id } = useParams();
  const navigate = useNavigate();
  const config = mastersEntities[entity];

  const [formValues, setFormValues] = useState({});
  const [errors, setErrors] = useState({});
  const [lookups, setLookups] = useState({});
  const [loading, setLoading] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  const fields = useMemo(() => config?.fields || [], [config]);
  const fieldNames = useMemo(() => new Set(fields.map((field) => field.name)), [fields]);
  const partyAutofillFields = useMemo(
    () => config?.partyAutofillFields || partyAutofillDefaults,
    [config?.partyAutofillFields]
  );

  const fieldLookups = useMemo(
    () => fields.filter((field) => field.optionsSource).map((field) => field.optionsSource),
    [fields]
  );

  useEffect(() => {
    if (!config) return;
    fieldLookups.forEach((source) => {
      if (lookups[source]) return;
      const endpoint = lookupEndpoints[source];
      if (!endpoint) return;
      apiClient
        .get(endpoint)
        .then((response) => {
          const payload = response.data || [];
          const options = Array.isArray(payload) ? payload : (payload.content || []);
          setLookups((prev) => ({ ...prev, [source]: options }));
        })
        .catch(() => {
          setLookups((prev) => ({ ...prev, [source]: [] }));
        });
    });
  }, [config, fieldLookups, lookups]);

  useEffect(() => {
    if (!config || mode !== 'edit' || !id) return;
    setLoading(true);
    apiClient
      .get(`${config.listEndpoint}/${id}`)
      .then((response) => {
        setFormValues(response.data || {});
      })
      .catch(() => {
        setSnackbar({ open: true, message: 'Unable to load record', severity: 'error' });
      })
      .finally(() => setLoading(false));
  }, [config, id, mode]);

  const validate = () => {
    const nextErrors = {};
    fields.forEach((field) => {
      const value = formValues[field.name];
      if (field.required && (value === undefined || value === null || value === '')) {
        nextErrors[field.name] = `${field.label} is required`;
      }
    });
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const buildPayload = () => {
    if (config?.buildPayload) {
      return config.buildPayload(formValues);
    }
    return fields.reduce((acc, field) => {
      const value = formValues[field.name];
      if (value === undefined || value === '') {
        acc[field.name] = null;
      } else if (field.type === 'number') {
        acc[field.name] = Number(value);
      } else {
        acc[field.name] = value;
      }
      return acc;
    }, {});
  };

  const applyPartyAutofill = (party) => {
    if (!party) return;
    setFormValues((prev) => {
      const next = { ...prev, partyId: party.id ?? prev.partyId };
      partyAutofillFields.forEach((key) => {
        if (!fieldNames.has(key) || !Object.prototype.hasOwnProperty.call(party, key)) return;
        next[key] = party[key] ?? '';
      });
      return next;
    });
  };

  const handlePartyChange = async (nextValue) => {
    setFormValues((prev) => ({ ...prev, partyId: nextValue || '' }));
    if (!nextValue || !config?.partyAutofill) return;
    try {
      const response = await apiClient.get(`/api/parties/${nextValue}`);
      applyPartyAutofill(response.data);
    } catch {
      // ignore lookup failures
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!config) return;
    if (!validate()) return;
    setLoading(true);
    const payload = buildPayload();
    try {
      if (mode === 'edit') {
        await apiClient.put(`${config.listEndpoint}/${id}`, payload);
        setSnackbar({ open: true, message: 'Updated successfully', severity: 'success' });
        navigate(`/masters/${entity}/${id}`);
      } else {
        await apiClient.post(config.createEndpoint, payload);
        setSnackbar({ open: true, message: 'Created successfully', severity: 'success' });
        navigate(`/masters/${entity}`);
      }
    } catch (error) {
      setSnackbar({
        open: true,
        message: error?.response?.data?.message || 'Save failed. Check input values.',
        severity: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  if (!config) {
    return <Alert severity="warning">Master module not configured.</Alert>;
  }

  const pageTitle = mode === 'edit' ? `Edit ${config.title}` : `Create ${config.title}`;

  return (
    <MainCard>
      <PageHeader
        title={pageTitle}
        breadcrumbs={[
          { label: 'Masters', to: '/masters/suppliers' },
          { label: config.title, to: `/masters/${entity}` },
          { label: mode === 'edit' ? 'Edit' : 'Create' }
        ]}
      />
      <form onSubmit={handleSubmit}>
        <Grid container spacing={2}>
          {fields.map((field) => {
            const options =
              field.options ||
              (field.optionsSource && lookups[field.optionsSource]) ||
              [];
            return (
              <Grid key={field.name} size={{ xs: 12, md: 6 }}>
                {field.type === 'select' ? (
                  <Stack spacing={1}>
                    <TextField
                      fullWidth
                      select
                      label={field.label}
                      value={formValues[field.name] ?? ''}
                      onChange={(event) => {
                        const nextValue = event.target.value;
                        if (field.name === 'partyId') {
                          handlePartyChange(nextValue);
                        } else {
                          setFormValues((prev) => ({ ...prev, [field.name]: nextValue }));
                        }
                      }}
                      required={field.required}
                      error={Boolean(errors[field.name])}
                      helperText={errors[field.name]}
                    >
                      <MenuItem value="">Select</MenuItem>
                      {options.map((option) => {
                        const value = typeof option === 'object'
                          ? option[field.optionValue] || option.id || option.name
                          : option;
                        const label = typeof option === 'object'
                          ? option[field.optionLabel] || option.name || option.code || option.vehicleNo || option.ticketNo
                          : (typeof option === 'boolean' ? (option ? 'Yes' : 'No') : option);
                        return (
                          <MenuItem key={String(value)} value={value}>
                            {label}
                          </MenuItem>
                        );
                      })}
                    </TextField>
                    {field.manageRoute && (
                      <Button
                        size="small"
                        variant="outlined"
                        onClick={() => navigate(field.manageRoute)}
                        sx={{ alignSelf: 'flex-start' }}
                      >
                        {field.manageLabel || 'Manage'}
                      </Button>
                    )}
                  </Stack>
                ) : (
                  <TextField
                    fullWidth
                    label={field.label}
                    type={field.type || 'text'}
                    value={formValues[field.name] ?? ''}
                    onChange={(event) => setFormValues((prev) => ({ ...prev, [field.name]: event.target.value }))}
                    required={field.required}
                    error={Boolean(errors[field.name])}
                    helperText={errors[field.name]}
                    InputLabelProps={field.type === 'date' || field.type === 'time' ? { shrink: true } : undefined}
                  />
                )}
              </Grid>
            );
          })}
        </Grid>
        <Stack direction="row" spacing={2} sx={{ mt: 3 }}>
          <Button variant="contained" color="secondary" type="submit" disabled={loading}>
            {mode === 'edit' ? 'Update' : 'Save'}
          </Button>
          <Button variant="outlined" onClick={() => navigate(`/masters/${entity}`)} disabled={loading}>
            Cancel
          </Button>
        </Stack>
      </form>
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

MasterFormPage.propTypes = {
  mode: PropTypes.oneOf(['create', 'edit']).isRequired
};
