import { useCallback, useEffect, useMemo, useState } from 'react';
import useSWR from 'swr';

import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Checkbox from '@mui/material/Checkbox';
import CircularProgress from '@mui/material/CircularProgress';
import Divider from '@mui/material/Divider';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormGroup from '@mui/material/FormGroup';
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

import apiClient from 'api/client';
import MainCard from 'ui-component/cards/MainCard';
import { gridSpacing } from 'store/constant';
import PartyBankAccountsDialog from 'views/erp/pages/masters/PartyBankAccountsDialog';

const roleTypes = ['SUPPLIER', 'CUSTOMER', 'BROKER', 'EXPENSE'];
const brokerCommissionTypes = ['PERCENT', 'PER_QTY', 'FIXED'];
const brokeragePaidByOptions = ['COMPANY', 'SUPPLIER'];
const partyStatusOptions = ['ACTIVE', 'INACTIVE'];

const buildEmptyRoles = () => ({
  SUPPLIER: { selected: false, creditPeriodDays: '' },
  CUSTOMER: { selected: false, creditPeriodDays: '' },
  BROKER: { selected: false, brokerCommissionType: '', brokerCommissionRate: '', brokeragePaidBy: '' },
  EXPENSE: { selected: false }
});

const emptyPartyForm = {
  partyCode: '',
  name: '',
  address: '',
  state: '',
  country: '',
  pinCode: '',
  pan: '',
  gstNo: '',
  contact: '',
  email: '',
  status: 'ACTIVE',
  roles: buildEmptyRoles()
};

const buildRolePayloads = (rolesState) => {
  return roleTypes
    .filter((role) => rolesState[role]?.selected)
    .map((role) => {
      const data = rolesState[role] || {};
      if (role === 'SUPPLIER') {
        return {
          roleType: role,
          creditPeriodDays: data.creditPeriodDays ? Number(data.creditPeriodDays) : null,
          active: true
        };
      }
      if (role === 'CUSTOMER') {
        return {
          roleType: role,
          creditPeriodDays: data.creditPeriodDays ? Number(data.creditPeriodDays) : null,
          active: true
        };
      }
      if (role === 'BROKER') {
        return {
          roleType: role,
          brokerCommissionType: data.brokerCommissionType || null,
          brokerCommissionRate: data.brokerCommissionRate ? Number(data.brokerCommissionRate) : null,
          brokeragePaidBy: data.brokeragePaidBy || null,
          active: true
        };
      }
      return { roleType: role, active: true };
    });
};

export default function PartyMasterPage() {
  const [filters, setFilters] = useState({ search: '' });
  const [debouncedFilters, setDebouncedFilters] = useState({ search: '' });
  const [formState, setFormState] = useState(emptyPartyForm);
  const [editingId, setEditingId] = useState(null);
  const [bankAccountsPartyId, setBankAccountsPartyId] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  useEffect(() => {
    const handle = setTimeout(() => setDebouncedFilters(filters), 300);
    return () => clearTimeout(handle);
  }, [filters]);

  const listKey = useMemo(() => ['parties', debouncedFilters.search], [debouncedFilters.search]);

  const fetcher = useCallback(async () => {
    const response = await apiClient.get('/api/parties', {
      params: {
        search: debouncedFilters.search || undefined,
        page: 0,
        size: 50
      }
    });
    const payload = response.data || [];
    return payload.content || payload;
  }, [debouncedFilters.search]);

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
    setFormState(emptyPartyForm);
    setEditingId(null);
  };

  const applyPartyToForm = (party) => {
    const nextRoles = buildEmptyRoles();
    (party.roles || []).forEach((role) => {
      const type = role.roleType;
      if (!type || !nextRoles[type]) return;
      nextRoles[type].selected = role.active !== false;
      if (type === 'SUPPLIER') {
        nextRoles[type].creditPeriodDays = role.creditPeriodDays ?? '';
      } else if (type === 'CUSTOMER') {
        nextRoles[type].creditPeriodDays = role.creditPeriodDays ?? '';
      } else if (type === 'BROKER') {
        nextRoles[type].brokerCommissionType = role.brokerCommissionType || '';
        nextRoles[type].brokerCommissionRate = role.brokerCommissionRate ?? '';
        nextRoles[type].brokeragePaidBy = role.brokeragePaidBy || '';
      }
    });

    setFormState({
      partyCode: party.partyCode || '',
      name: party.name || '',
      address: party.address || '',
      state: party.state || '',
      country: party.country || '',
      pinCode: party.pinCode || '',
      pan: party.pan || '',
      gstNo: party.gstNo || '',
      contact: party.contact || '',
      email: party.email || '',
      status: party.status || 'ACTIVE',
      roles: nextRoles
    });
  };

  const handleEdit = (party) => {
    applyPartyToForm(party);
    setEditingId(party.id);
  };

  const buildPayload = () => ({
    partyCode: formState.partyCode || null,
    name: formState.name,
    address: formState.address || null,
    state: formState.state || null,
    country: formState.country || null,
    pinCode: formState.pinCode || null,
    pan: formState.pan || null,
    gstNo: formState.gstNo || null,
    contact: formState.contact || null,
    email: formState.email || null,
    status: formState.status || 'ACTIVE',
    roles: buildRolePayloads(formState.roles)
  });

  const handleSubmit = async (event) => {
    event.preventDefault();
    setActionLoading(true);
    const payload = buildPayload();
    try {
      if (editingId) {
        await apiClient.put(`/api/parties/${editingId}`, payload);
        setSnackbar({ open: true, message: 'Party updated', severity: 'success' });
      } else {
        const response = await apiClient.post('/api/parties', payload);
        setEditingId(response.data?.id || null);
        setSnackbar({ open: true, message: 'Party created', severity: 'success' });
      }
      await mutate();
      resetForm();
      setBankAccountsPartyId(null);
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

  const handleOpenBankAccounts = async () => {
    if (editingId) {
      setBankAccountsPartyId(editingId);
      return;
    }
    setActionLoading(true);
    const payload = buildPayload();
    try {
      const response = await apiClient.post('/api/parties', payload);
      const newId = response.data?.id || null;
      setEditingId(newId);
      if (newId) {
        setBankAccountsPartyId(newId);
      }
      setSnackbar({ open: true, message: 'Party created', severity: 'success' });
      await mutate();
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

  const updateRoleField = (role, key, value) => {
    setFormState((prev) => ({
      ...prev,
      roles: {
        ...prev.roles,
        [role]: {
          ...prev.roles[role],
          [key]: value
        }
      }
    }));
  };

  return (
    <Grid container spacing={gridSpacing}>
      <Grid size={12}>
        <MainCard
          title={
            <Stack spacing={0.5}>
              <Typography variant="overline" color="text.secondary">
                Masters
              </Typography>
              <Typography variant="h3">Parties</Typography>
            </Stack>
          }
          secondary={
            <Button
              variant="contained"
              color="secondary"
              type="submit"
              form="party-form"
              disabled={actionLoading}
            >
              {editingId ? 'Update' : 'Create'}
            </Button>
          }
        >
          <Stack spacing={2}>
            <Box component="form" id="party-form" onSubmit={handleSubmit}>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Party Code"
                    value={formState.partyCode}
                    onChange={(event) => setFormState({ ...formState, partyCode: event.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Party Name"
                    value={formState.name}
                    onChange={(event) => setFormState({ ...formState, name: event.target.value })}
                    required
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Address"
                    value={formState.address}
                    onChange={(event) => setFormState({ ...formState, address: event.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="State"
                    value={formState.state}
                    onChange={(event) => setFormState({ ...formState, state: event.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Country"
                    value={formState.country}
                    onChange={(event) => setFormState({ ...formState, country: event.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Pin Code"
                    value={formState.pinCode}
                    onChange={(event) => setFormState({ ...formState, pinCode: event.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="PAN"
                    value={formState.pan}
                    onChange={(event) => setFormState({ ...formState, pan: event.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="GST No"
                    value={formState.gstNo}
                    onChange={(event) => setFormState({ ...formState, gstNo: event.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Contact"
                    value={formState.contact}
                    onChange={(event) => setFormState({ ...formState, contact: event.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    label="Email"
                    value={formState.email}
                    onChange={(event) => setFormState({ ...formState, email: event.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField
                    fullWidth
                    select
                    label="Status"
                    value={formState.status}
                    onChange={(event) => setFormState({ ...formState, status: event.target.value })}
                  >
                    {partyStatusOptions.map((option) => (
                      <MenuItem key={option} value={option}>
                        {option}
                      </MenuItem>
                    ))}
                  </TextField>
                </Grid>
                <Grid size={12}>
                  <Typography variant="subtitle1" sx={{ mb: 1 }}>
                    Roles
                  </Typography>
                  <FormGroup row>
                    {roleTypes.map((role) => (
                      <FormControlLabel
                        key={role}
                        control={
                          <Checkbox
                            checked={Boolean(formState.roles[role]?.selected)}
                            onChange={(event) => updateRoleField(role, 'selected', event.target.checked)}
                          />
                        }
                        label={role}
                      />
                    ))}
                  </FormGroup>
                </Grid>
                {formState.roles.SUPPLIER?.selected && (
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="Supplier Credit Period (days)"
                      type="number"
                      value={formState.roles.SUPPLIER.creditPeriodDays || ''}
                      onChange={(event) => updateRoleField('SUPPLIER', 'creditPeriodDays', event.target.value)}
                    />
                  </Grid>
                )}
                {formState.roles.CUSTOMER?.selected && (
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="Customer Credit Period (days)"
                      type="number"
                      value={formState.roles.CUSTOMER.creditPeriodDays || ''}
                      onChange={(event) => updateRoleField('CUSTOMER', 'creditPeriodDays', event.target.value)}
                    />
                  </Grid>
                )}
                {formState.roles.BROKER?.selected && (
                  <>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField
                        fullWidth
                        select
                        label="Commission Type"
                        value={formState.roles.BROKER.brokerCommissionType || ''}
                        onChange={(event) => updateRoleField('BROKER', 'brokerCommissionType', event.target.value)}
                      >
                        <MenuItem value="">Select</MenuItem>
                        {brokerCommissionTypes.map((option) => (
                          <MenuItem key={option} value={option}>
                            {option}
                          </MenuItem>
                        ))}
                      </TextField>
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField
                        fullWidth
                        label="Commission Rate"
                        type="number"
                        value={formState.roles.BROKER.brokerCommissionRate || ''}
                        onChange={(event) => updateRoleField('BROKER', 'brokerCommissionRate', event.target.value)}
                      />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField
                        fullWidth
                        select
                        label="Brokerage Paid By"
                        value={formState.roles.BROKER.brokeragePaidBy || ''}
                        onChange={(event) => updateRoleField('BROKER', 'brokeragePaidBy', event.target.value)}
                      >
                        <MenuItem value="">Select</MenuItem>
                        {brokeragePaidByOptions.map((option) => (
                          <MenuItem key={option} value={option}>
                            {option}
                          </MenuItem>
                        ))}
                      </TextField>
                    </Grid>
                  </>
                )}
              </Grid>
              <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
                <Button variant="contained" color="secondary" type="submit" disabled={actionLoading}>
                  {editingId ? 'Update' : 'Save'}
                </Button>
                <Button variant="outlined" onClick={resetForm} disabled={actionLoading}>
                  {editingId ? 'Cancel edit' : 'Clear'}
                </Button>
                <Button variant="outlined" onClick={handleOpenBankAccounts} disabled={actionLoading}>
                  Bank Accounts
                </Button>
              </Stack>
            </Box>
            <Divider />
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems={{ xs: 'stretch', md: 'center' }}>
              <TextField
                fullWidth
                label="Search"
                value={filters.search}
                onChange={(event) => setFilters({ ...filters, search: event.target.value })}
              />
            </Stack>
            <Divider />
            <Box>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Party Code</TableCell>
                      <TableCell>Name</TableCell>
                      <TableCell>Roles</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Contact</TableCell>
                      <TableCell align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {rows.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={6}>
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
                    {rows.map((party) => (
                      <TableRow key={party.id}>
                        <TableCell>{party.partyCode || '-'}</TableCell>
                        <TableCell>{party.name || '-'}</TableCell>
                        <TableCell>
                          {(party.roles || []).map((role) => role.roleType).filter(Boolean).join(', ') || '-'}
                        </TableCell>
                        <TableCell>{party.status || '-'}</TableCell>
                        <TableCell>{party.contact || '-'}</TableCell>
                        <TableCell align="right">
                          <Stack direction="row" spacing={1} justifyContent="flex-end">
                            <Button size="small" onClick={() => handleEdit(party)}>
                              Edit
                            </Button>
                            <Button size="small" onClick={() => setBankAccountsPartyId(party.id)}>
                              Bank Accounts
                            </Button>
                          </Stack>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          </Stack>
        </MainCard>
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
    </Grid>
  );
}
