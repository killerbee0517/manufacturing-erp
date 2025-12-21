import PropTypes from 'prop-types';
import { useEffect, useMemo, useState } from 'react';

// material-ui
import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Snackbar from '@mui/material/Snackbar';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';

// project imports
import MainCard from 'ui-component/cards/MainCard';
import Breadcrumbs from 'ui-component/extended/Breadcrumbs';
import { gridSpacing } from 'store/constant';
import apiClient from 'api/client';

const lookupEndpoints = {
  suppliers: '/api/suppliers',
  items: '/api/items',
  uoms: '/api/uoms',
  locations: '/api/locations',
  customers: '/api/customers',
  brokers: '/api/brokers',
  roles: '/api/roles',
  tickets: '/api/weighbridge/tickets',
  salesOrders: '/api/sales-orders'
};

export default function ModulePage({ config }) {
  const [filters, setFilters] = useState({ search: '', from: '', to: '', status: '' });
  const [formState, setFormState] = useState({});
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [lookups, setLookups] = useState({});
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  const fields = config.fields || [];
  const columns = config.columns || [];
  const createEnabled = Boolean(config.createEndpoint);

  const fieldLookups = useMemo(
    () => fields.filter((field) => field.optionsSource).map((field) => field.optionsSource),
    [fields]
  );

  useEffect(() => {
    if (!fieldLookups.length) return;
    fieldLookups.forEach((source) => {
      if (lookups[source]) return;
      const endpoint = lookupEndpoints[source];
      if (!endpoint) return;
      apiClient
        .get(endpoint)
        .then((response) => {
          setLookups((prev) => ({ ...prev, [source]: response.data || [] }));
        })
        .catch(() => {
          setLookups((prev) => ({ ...prev, [source]: [] }));
        });
    });
  }, [fieldLookups, lookups]);

  const loadRows = () => {
    if (!config.listEndpoint) {
      setRows([]);
      return;
    }
    setLoading(true);
    apiClient
      .get(config.listEndpoint)
      .then((response) => {
        setRows(response.data || []);
      })
      .catch(() => {
        setRows([]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRows();
  }, [config.listEndpoint]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!config.createEndpoint) return;
    setLoading(true);
    const payload = config.buildPayload ? config.buildPayload(formState) : formState;
    try {
      await apiClient.post(config.createEndpoint, payload);
      setSnackbar({ open: true, message: 'Saved successfully', severity: 'success' });
      setFormState({});
      loadRows();
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

  return (
    <>
      <Breadcrumbs card={false} divider={false} title />
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
              <Button variant="contained" color="secondary" disabled={!createEnabled} type="submit" form="module-form">
                Create
              </Button>
            }
          >
            <Stack spacing={2}>
              <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems={{ xs: 'stretch', md: 'center' }}>
                <TextField
                  fullWidth
                  label="Search"
                  value={filters.search}
                  onChange={(event) => setFilters({ ...filters, search: event.target.value })}
                />
                <TextField
                  fullWidth
                  type="date"
                  label="From"
                  InputLabelProps={{ shrink: true }}
                  value={filters.from}
                  onChange={(event) => setFilters({ ...filters, from: event.target.value })}
                />
                <TextField
                  fullWidth
                  type="date"
                  label="To"
                  InputLabelProps={{ shrink: true }}
                  value={filters.to}
                  onChange={(event) => setFilters({ ...filters, to: event.target.value })}
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
                  <MenuItem value="PENDING">Pending</MenuItem>
                  <MenuItem value="APPROVED">Approved</MenuItem>
                </TextField>
              </Stack>
              {createEnabled && fields.length > 0 && (
                <>
                  <Divider />
                  <Box component="form" id="module-form" onSubmit={handleSubmit}>
                    <Grid container spacing={2}>
                      {fields.map((field) => {
                        const options =
                          field.options ||
                          (field.optionsSource && lookups[field.optionsSource]) ||
                          [];
                        return (
                          <Grid key={field.name} size={{ xs: 12, md: 6 }}>
                            {field.type === 'select' ? (
                              <TextField
                                fullWidth
                                select
                                label={field.label}
                                value={formState[field.name] || ''}
                                onChange={(event) => setFormState({ ...formState, [field.name]: event.target.value })}
                              >
                                <MenuItem value="">Select</MenuItem>
                                {options.map((option) => {
                                  const value = typeof option === 'object'
                                    ? option[field.optionValue] || option.id || option.name
                                    : option;
                                  const label = typeof option === 'object'
                                    ? option[field.optionLabel] || option.name || option.code || option.vehicleNo || option.ticketNo
                                    : option;
                                  return (
                                    <MenuItem key={value} value={value}>
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
                                InputLabelProps={field.type === 'date' || field.type === 'time' ? { shrink: true } : undefined}
                              />
                            )}
                          </Grid>
                        );
                      })}
                    </Grid>
                    <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
                      <Button variant="contained" color="secondary" type="submit" disabled={loading}>
                        Save
                      </Button>
                      <Button variant="outlined" onClick={() => setFormState({})} disabled={loading}>
                        Clear
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
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rows.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={columns.length || 1}>{loading ? 'Loading...' : 'No records found'}</TableCell>
                        </TableRow>
                      )}
                      {rows.map((row) => (
                        <TableRow key={row.id || JSON.stringify(row)}>
                          {columns.map((column) => (
                            <TableCell key={`${row.id}-${column.field}`}>
                              {Array.isArray(row[column.field])
                                ? row[column.field].join(', ')
                                : row[column.field] ?? '-'}
                            </TableCell>
                          ))}
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
    </>
  );
}

ModulePage.propTypes = {
  config: PropTypes.shape({
    title: PropTypes.string.isRequired,
    subtitle: PropTypes.string.isRequired,
    createEndpoint: PropTypes.string,
    listEndpoint: PropTypes.string,
    fields: PropTypes.array,
    columns: PropTypes.array,
    buildPayload: PropTypes.func
  }).isRequired
};
