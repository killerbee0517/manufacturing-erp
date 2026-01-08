import PropTypes from 'prop-types';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useSWR from 'swr';

// material-ui
import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Snackbar from '@mui/material/Snackbar';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

// project imports
import MainCard from 'ui-component/cards/MainCard';
import { gridSpacing } from 'store/constant';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import PartyBankAccountsDialog from 'views/erp/pages/masters/PartyBankAccountsDialog';

const lookupEndpoints = {
  suppliers: '/api/suppliers',
  items: '/api/items',
  uoms: '/api/uoms',
  locations: '/api/locations',
  customers: '/api/customers',
  banks: '/api/banks/autocomplete',
  vehicles: '/api/vehicles',
  godowns: '/api/godowns',
  brokers: '/api/brokers',
  parties: '/api/parties/autocomplete',
  roles: '/api/roles',
  tickets: '/api/weighbridge/tickets',
  salesOrders: '/api/sales-orders'
};

const hiddenFieldNames = new Set(['id', 'createdAt', 'updatedAt', 'serialNo', 'rfqNo', 'poNo', 'grnNo']);
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

const buildEndpoint = (base, id) => {
  if (!base) return '';
  const normalized = base.endsWith('/') ? base.slice(0, -1) : base;
  return id ? `${normalized}/${id}` : normalized;
};

export default function ModulePage({ config }) {
  const navigate = useNavigate();
  const [filters, setFilters] = useState({ search: '', status: '' });
  const [debouncedFilters, setDebouncedFilters] = useState({ search: '', status: '' });
  const [formState, setFormState] = useState({});
  const [editingId, setEditingId] = useState(null);
  const [bankAccountsPartyId, setBankAccountsPartyId] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  const fields = config.fields || [];
  const columns = config.columns || [];
  const createEnabled = Boolean(config.createEndpoint);
  const useInlineCreate = config.useInlineCreate !== false;
  const hasWriteEndpoint = Boolean(config.createEndpoint || config.updateEndpoint);
  const allowInlineEdit = (config.inlineEditable ?? useInlineCreate) && hasWriteEndpoint;
  const hasNavigationActions = Boolean(config.detailRouteBase || config.editRouteBase);
  const showActions = hasNavigationActions || allowInlineEdit;
  const isEditing = Boolean(editingId);

  const visibleFields = useMemo(() => fields.filter((field) => !hiddenFieldNames.has(field.name)), [fields]);
  const visibleFieldNames = useMemo(() => new Set(visibleFields.map((field) => field.name)), [visibleFields]);
  const partyAutofillFields = useMemo(
    () => config.partyAutofillFields || partyAutofillDefaults,
    [config.partyAutofillFields]
  );

  useEffect(() => {
    const handle = setTimeout(() => {
      setDebouncedFilters(filters);
    }, 300);
    return () => clearTimeout(handle);
  }, [filters]);

  useEffect(() => {
    setFormState({});
    setEditingId(null);
    setFilters({ search: '', status: '' });
    setDebouncedFilters({ search: '', status: '' });
  }, [config.listEndpoint, config.createEndpoint]);

  const listKey = useMemo(() => {
    if (!config.listEndpoint) return null;
    return [config.listEndpoint, debouncedFilters.search || '', debouncedFilters.status || ''];
  }, [config.listEndpoint, debouncedFilters]);

  const fetcher = useCallback(async () => {
    if (!config.listEndpoint) return [];
    const extraParams = typeof config.listParams === 'function'
      ? config.listParams()
      : (config.listParams || {});
    const response = await apiClient.get(config.listEndpoint, {
      params: {
        q: debouncedFilters.search || undefined,
        status: debouncedFilters.status || undefined,
        ...extraParams
      }
    });
    const payload = response.data || [];
    const rowsPayload = payload.content || payload;
    return config.rowTransformer
      ? rowsPayload.map((row) => config.rowTransformer(row))
      : rowsPayload;
  }, [config.listEndpoint, config.listParams, config.rowTransformer, debouncedFilters]);

  const {
    data: rows = [],
    isLoading,
    isValidating,
    mutate
  } = useSWR(listKey, fetcher, {
    revalidateOnFocus: false,
    revalidateOnReconnect: false
  });

  const resetForm = () => {
    setFormState({});
    setEditingId(null);
  };

  const applyPartyAutofill = useCallback(
    (party) => {
      if (!party) return;
      setFormState((prev) => {
        const next = { ...prev, partyId: party.id ?? prev.partyId };
        partyAutofillFields.forEach((key) => {
          if (!visibleFieldNames.has(key) || !Object.prototype.hasOwnProperty.call(party, key)) return;
          next[key] = party[key] ?? '';
        });
        return next;
      });
    },
    [partyAutofillFields, visibleFieldNames]
  );

  const handlePartyChange = useCallback(
    async (nextValue) => {
      setFormState((prev) => ({ ...prev, partyId: nextValue || '' }));
      if (!nextValue || !config.partyAutofill) return;
      try {
        const response = await apiClient.get(`/api/parties/${nextValue}`);
        applyPartyAutofill(response.data);
      } catch {
        // ignore lookup failures
      }
    },
    [applyPartyAutofill, config.partyAutofill]
  );

  const handleSubmit = async (event) => {
    event.preventDefault();
    const endpointBase = config.createEndpoint || config.listEndpoint;
    if (!endpointBase) return;
    setActionLoading(true);
    const payload = config.buildPayload ? config.buildPayload(formState) : formState;
    try {
      if (isEditing) {
        await apiClient.put(buildEndpoint(config.updateEndpoint || endpointBase, editingId), payload);
        setSnackbar({ open: true, message: 'Updated successfully', severity: 'success' });
      } else {
        await apiClient.post(endpointBase, payload);
        setSnackbar({ open: true, message: 'Saved successfully', severity: 'success' });
      }
      resetForm();
      mutate();
    } catch (error) {
      setSnackbar({
        open: true,
        message: error?.response?.data?.message || 'Save failed. Check input values.',
        severity: 'error'
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleEdit = (row) => {
    const nextState = {};
    visibleFields.forEach((field) => {
      if (row[field.name] !== undefined) {
        nextState[field.name] = row[field.name] ?? '';
      }
    });
    setFormState(nextState);
    setEditingId(row.id);
  };

  const handleDelete = async (rowId) => {
    const endpointBase = config.deleteEndpoint || config.createEndpoint || config.listEndpoint;
    if (!endpointBase || !rowId) return;
    setActionLoading(true);
    try {
      await apiClient.delete(buildEndpoint(endpointBase, rowId));
      setSnackbar({ open: true, message: 'Record deleted', severity: 'success' });
      if (rowId === editingId) {
        resetForm();
      }
      mutate();
    } catch (error) {
      setSnackbar({
        open: true,
        message: error?.response?.data?.message || 'Delete failed. Please try again.',
        severity: 'error'
      });
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <>
      <Grid container spacing={gridSpacing}>
        <Grid size={12}>
          <MainCard
            title={
              <Stack spacing={0.5}>
                <Typography variant="overline" color="text.secondary">
                  {config.subtitle}
                </Typography>
                <Typography variant="h3">{config.title}</Typography>
              </Stack>
            }
            secondary={
              <Button
                variant="contained"
                color="secondary"
                disabled={!createEnabled}
                type={createEnabled && useInlineCreate ? 'submit' : 'button'}
                form={createEnabled && useInlineCreate ? 'module-form' : undefined}
                onClick={
                  config.createRoute
                    ? () => navigate(config.createRoute)
                    : undefined
                }
              >
                Create
              </Button>
            }
          >
            <Stack spacing={2}>
              {config.enableFilters && (
                <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems={{ xs: 'stretch', md: 'center' }}>
                  <TextField
                    fullWidth
                    label="Search"
                    value={filters.search}
                    onChange={(event) => setFilters({ ...filters, search: event.target.value })}
                  />
                  <TextField
                    fullWidth
                    select
                    label="Status"
                    value={filters.status}
                    onChange={(event) => setFilters({ ...filters, status: event.target.value })}
                  >
                    <MenuItem value="">All</MenuItem>
                    <MenuItem value="DRAFT">Draft</MenuItem>
                    <MenuItem value="SUBMITTED">Submitted</MenuItem>
                    <MenuItem value="APPROVED">Approved</MenuItem>
                  </TextField>
                </Stack>
              )}
              {createEnabled && useInlineCreate && visibleFields.length > 0 && (
                <>
                  <Divider />
                  <Box component="form" id="module-form" onSubmit={handleSubmit}>
                    <Grid container spacing={2}>
                      {visibleFields.map((field) => {
                        return (
                          <Grid key={field.name} size={{ xs: 12, md: 6 }}>
                            {field.type === 'select' && field.optionsSource ? (
                              <Stack spacing={1}>
                                <MasterAutocomplete
                                  label={field.label}
                                  endpoint={lookupEndpoints[field.optionsSource]}
                                  value={formState[field.name] || ''}
                                  onChange={(nextValue) => {
                                    if (field.name === 'partyId') {
                                      handlePartyChange(nextValue);
                                    } else {
                                      setFormState({ ...formState, [field.name]: nextValue });
                                    }
                                  }}
                                  optionLabelKey={field.optionLabel}
                                  optionValueKey={field.optionValue || 'id'}
                                  required={field.required}
                                  queryParams={field.queryParams}
                                />
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
                            ) : field.type === 'select' ? (
                              <TextField
                                fullWidth
                                select
                                label={field.label}
                                value={formState[field.name] || ''}
                                onChange={(event) => setFormState({ ...formState, [field.name]: event.target.value })}
                                required={field.required}
                              >
                                <MenuItem value="">Select</MenuItem>
                                {(field.options || []).map((option) => {
                                  const value = typeof option === 'object'
                                    ? option.value ?? option.id ?? option.code ?? option.name
                                    : option;
                                  const label = typeof option === 'object'
                                    ? option.label ?? option.name ?? option.code ?? option.value
                                    : (typeof option === 'boolean' ? (option ? 'Yes' : 'No') : option);
                                  return (
                                    <MenuItem key={String(value)} value={value}>
                                      {label}
                                    </MenuItem>
                                  );
                                })}
                              </TextField>
                            ) : (
                              <TextField
                                fullWidth
                                label={field.label}
                                type={field.type}
                                value={formState[field.name] || ''}
                                onChange={(event) => setFormState({ ...formState, [field.name]: event.target.value })}
                                required={field.required}
                                InputLabelProps={field.type === 'date' || field.type === 'time' ? { shrink: true } : undefined}
                              />
                            )}
                          </Grid>
                        );
                      })}
                    </Grid>
                    <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
                      <Button variant="contained" color="secondary" type="submit" disabled={actionLoading}>
                        {isEditing ? 'Update' : 'Save'}
                      </Button>
                      <Button variant="outlined" onClick={resetForm} disabled={actionLoading}>
                        {isEditing ? 'Cancel edit' : 'Clear'}
                      </Button>
                    </Stack>
                  </Box>
                </>
              )}
              {!createEnabled && (
                <Alert severity="info">Create action is not available for this module yet.</Alert>
              )}
              <Divider />
              <Box>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        {columns.length ? (
                          columns.map((column) => <TableCell key={column.label}>{column.label}</TableCell>)
                        ) : (
                          <TableCell>No data configured</TableCell>
                        )}
                        {showActions && <TableCell align="right">Actions</TableCell>}
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rows.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={(columns.length || 1) + (showActions ? 1 : 0)}>
                            {isLoading || isValidating ? (
                              <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                                <CircularProgress size={18} />
                                <span>Loading...</span>
                              </Stack>
                            ) : (
                              'No records found'
                            )}
                          </TableCell>
                        </TableRow>
                      )}
                      {rows.map((row) => (
                        <TableRow
                          key={row.id || JSON.stringify(row)}
                          hover={hasNavigationActions}
                          onClick={
                            config.detailRouteBase
                              ? () => navigate(`${config.detailRouteBase}/${row.id}`)
                              : undefined
                          }
                          sx={{ cursor: config.detailRouteBase ? 'pointer' : 'default' }}
                        >
                          {columns.map((column) => (
                            <TableCell key={`${row.id}-${column.field}`}>
                              {Array.isArray(row[column.field])
                                ? row[column.field].join(', ')
                                : row[column.field] ?? '-'}
                            </TableCell>
                          ))}
                          {showActions && (
                        <TableCell align="right" onClick={(event) => event.stopPropagation()}>
                          {config.detailRouteBase && (
                            <Button size="small" onClick={() => navigate(`${config.detailRouteBase}/${row.id}`)}>
                              View
                            </Button>
                          )}
                          {config.editRouteBase && (
                            <Button size="small" onClick={() => navigate(`${config.editRouteBase}/${row.id}/edit`)}>
                              Edit
                            </Button>
                          )}
                          {config.enableBankAccounts && (
                            <Button
                              size="small"
                              onClick={() => {
                                const partyId = config.bankAccountsPartyField ? row[config.bankAccountsPartyField] : row.id;
                                if (partyId) {
                                  setBankAccountsPartyId(partyId);
                                }
                              }}
                            >
                              Bank Accounts
                            </Button>
                          )}
                          {allowInlineEdit && (
                            <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end', mt: hasNavigationActions ? 1 : 0 }}>
                              <Button size="small" onClick={() => handleEdit(row)} disabled={actionLoading}>
                                Edit
                              </Button>
                              <Button
                                    size="small"
                                    color="error"
                                    onClick={() => handleDelete(row.id)}
                                    disabled={actionLoading}
                                  >
                                    Delete
                                  </Button>
                                </Stack>
                              )}
                            </TableCell>
                          )}
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            </Stack>
          </MainCard>
        </Grid>
      </Grid>
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
          {snackbar.message}
        </Alert>
      </Snackbar>
      <PartyBankAccountsDialog
        open={Boolean(bankAccountsPartyId)}
        partyId={bankAccountsPartyId}
        onClose={() => setBankAccountsPartyId(null)}
      />
    </>
  );
}

ModulePage.propTypes = {
  config: PropTypes.shape({
    title: PropTypes.string.isRequired,
    subtitle: PropTypes.string.isRequired,
    createEndpoint: PropTypes.string,
    updateEndpoint: PropTypes.string,
    deleteEndpoint: PropTypes.string,
    listEndpoint: PropTypes.string,
    listParams: PropTypes.oneOfType([PropTypes.object, PropTypes.func]),
    fields: PropTypes.array,
    columns: PropTypes.array,
    buildPayload: PropTypes.func,
    rowTransformer: PropTypes.func,
    useInlineCreate: PropTypes.bool,
    inlineEditable: PropTypes.bool,
    enableFilters: PropTypes.bool,
    enableBankAccounts: PropTypes.bool,
    bankAccountsPartyField: PropTypes.string,
    partyAutofill: PropTypes.bool,
    partyAutofillFields: PropTypes.arrayOf(PropTypes.string),
    detailRouteBase: PropTypes.string,
    editRouteBase: PropTypes.string
  }).isRequired
};
