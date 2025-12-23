import PropTypes from 'prop-types';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

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
import { gridSpacing } from 'store/constant';
import apiClient from 'api/client';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

const lookupEndpoints = {
  suppliers: '/api/suppliers',
  items: '/api/items',
  uoms: '/api/uoms',
  locations: '/api/locations',
  customers: '/api/customers',
  banks: '/api/banks',
  vehicles: '/api/vehicles',
  godowns: '/api/godowns',
  brokers: '/api/brokers',
  roles: '/api/roles',
  tickets: '/api/weighbridge/tickets',
  salesOrders: '/api/sales-orders'
};

const hiddenFieldNames = new Set(['id', 'createdAt', 'updatedAt', 'serialNo', 'rfqNo', 'poNo', 'grnNo']);

export default function ModulePage({ config }) {
  const navigate = useNavigate();
  const [filters, setFilters] = useState({ search: '', from: '', to: '', status: '' });
  const [formState, setFormState] = useState({});
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  const fields = config.fields || [];
  const columns = config.columns || [];
  const createEnabled = Boolean(config.createEndpoint);
  const useInlineCreate = config.useInlineCreate !== false;
  const showActions = Boolean(config.detailRouteBase || config.editRouteBase);

  const visibleFields = useMemo(() => fields.filter((field) => !hiddenFieldNames.has(field.name)), [fields]);

  const loadRows = (params = {}) => {
    if (!config.listEndpoint) {
      setRows([]);
      return;
    }
    setLoading(true);
    apiClient
      .get(config.listEndpoint, { params })
      .then((response) => {
        const payload = response.data || [];
        setRows(payload.content || payload);
      })
      .catch(() => {
        setRows([]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadRows();
  }, [config.listEndpoint]);

  useEffect(() => {
    if (!config.enableFilters) return;
    const handle = setTimeout(() => {
      loadRows({
        q: filters.search || undefined,
        status: filters.status || undefined
      });
    }, 300);
    return () => clearTimeout(handle);
  }, [filters, config.enableFilters]);

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
                              <MasterAutocomplete
                                label={field.label}
                                endpoint={lookupEndpoints[field.optionsSource]}
                                value={formState[field.name] || ''}
                                onChange={(nextValue) => setFormState({ ...formState, [field.name]: nextValue })}
                                optionLabelKey={field.optionLabel}
                                optionValueKey={field.optionValue || 'id'}
                                required={field.required}
                              />
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
                                {(field.options || []).map((option) => (
                                  <MenuItem key={option} value={option}>
                                    {option}
                                  </MenuItem>
                                ))}
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
                        {showActions && <TableCell align="right">Actions</TableCell>}
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rows.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={(columns.length || 1) + (showActions ? 1 : 0)}>
                            {loading ? 'Loading...' : 'No records found'}
                          </TableCell>
                        </TableRow>
                      )}
                      {rows.map((row) => (
                        <TableRow
                          key={row.id || JSON.stringify(row)}
                          hover={Boolean(config.detailRouteBase)}
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
