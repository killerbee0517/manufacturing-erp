import PropTypes from 'prop-types';
import { useEffect, useState } from 'react';

import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Checkbox from '@mui/material/Checkbox';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Divider from '@mui/material/Divider';
import FormControlLabel from '@mui/material/FormControlLabel';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';

import apiClient from 'api/client';

const initialFormState = {
  bankName: '',
  branch: '',
  accountNo: '',
  ifsc: '',
  swiftCode: '',
  accountType: '',
  isDefault: false,
  active: true
};
const accountTypeOptions = [
  { value: 'SAVINGS', label: 'Savings' },
  { value: 'CURRENT', label: 'Current' },
  { value: 'ALL', label: 'All' }
];

export default function PartyBankAccountsDialog({ open, partyId, onClose }) {
  const [accounts, setAccounts] = useState([]);
  const [formState, setFormState] = useState(initialFormState);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const resetForm = () => {
    setFormState(initialFormState);
    setEditingId(null);
  };

  const loadAccounts = async () => {
    if (!partyId) return;
    try {
      const response = await apiClient.get(`/api/parties/${partyId}/bank-accounts`);
      setAccounts(response.data || []);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load bank accounts.');
    }
  };

  useEffect(() => {
    if (!open) return;
    loadAccounts();
  }, [open, partyId]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!partyId) return;
    setSaving(true);
    try {
      if (editingId) {
        await apiClient.put(`/api/parties/${partyId}/bank-accounts/${editingId}`, formState);
      } else {
        await apiClient.post(`/api/parties/${partyId}/bank-accounts`, formState);
      }
      await loadAccounts();
      resetForm();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save bank account.');
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (account) => {
    setEditingId(account.id);
    setFormState({
      bankName: account.bankName || '',
      branch: account.branch || '',
      accountNo: account.accountNo || '',
      ifsc: account.ifsc || '',
      swiftCode: account.swiftCode || '',
      accountType: account.accountType || '',
      isDefault: Boolean(account.isDefault),
      active: account.active !== false
    });
  };

  const handleDelete = async (accountId) => {
    if (!partyId || !accountId) return;
    setSaving(true);
    try {
      await apiClient.delete(`/api/parties/${partyId}/bank-accounts/${accountId}`);
      await loadAccounts();
      if (editingId === accountId) {
        resetForm();
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to delete bank account.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>Party Bank Accounts</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <Stack spacing={2}>
          <Box component="form" onSubmit={handleSubmit}>
            <Stack spacing={2}>
              <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
                <TextField
                  fullWidth
                  label="Bank Name"
                  value={formState.bankName}
                  onChange={(event) => setFormState({ ...formState, bankName: event.target.value })}
                  required
                />
                <TextField
                  fullWidth
                  label="Branch"
                  value={formState.branch}
                  onChange={(event) => setFormState({ ...formState, branch: event.target.value })}
                />
              </Stack>
              <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
                <TextField
                  fullWidth
                  label="Account Number"
                  value={formState.accountNo}
                  onChange={(event) => setFormState({ ...formState, accountNo: event.target.value })}
                />
                <TextField
                  fullWidth
                  label="IFSC"
                  value={formState.ifsc}
                  onChange={(event) => setFormState({ ...formState, ifsc: event.target.value })}
                />
              </Stack>
              <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
                <TextField
                  fullWidth
                  label="SWIFT Code"
                  value={formState.swiftCode}
                  onChange={(event) => setFormState({ ...formState, swiftCode: event.target.value })}
                />
                <TextField
                  fullWidth
                  select
                  label="Account Type"
                  value={formState.accountType}
                  onChange={(event) => setFormState({ ...formState, accountType: event.target.value })}
                >
                  <MenuItem value="">Select</MenuItem>
                  {accountTypeOptions.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </TextField>
              </Stack>
              <Stack direction="row" spacing={2} alignItems="center">
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={formState.isDefault}
                      onChange={(event) => setFormState({ ...formState, isDefault: event.target.checked })}
                    />
                  }
                  label="Default"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={formState.active}
                      onChange={(event) => setFormState({ ...formState, active: event.target.checked })}
                    />
                  }
                  label="Active"
                />
                <Stack direction="row" spacing={1}>
                  <Button variant="contained" color="secondary" type="submit" disabled={saving}>
                    {editingId ? 'Update' : 'Add'}
                  </Button>
                  <Button variant="outlined" onClick={resetForm} disabled={saving}>
                    Clear
                  </Button>
                </Stack>
              </Stack>
            </Stack>
          </Box>
          <Divider />
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Bank</TableCell>
                <TableCell>Account No</TableCell>
                <TableCell>IFSC</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Default</TableCell>
                <TableCell>Active</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {accounts.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7}>No bank accounts added.</TableCell>
                </TableRow>
              )}
              {accounts.map((account) => (
                <TableRow key={account.id}>
                  <TableCell>{account.bankName}</TableCell>
                  <TableCell>{account.accountNo || '-'}</TableCell>
                  <TableCell>{account.ifsc || '-'}</TableCell>
                  <TableCell>{account.accountType || '-'}</TableCell>
                  <TableCell>{account.isDefault ? 'Yes' : 'No'}</TableCell>
                  <TableCell>{account.active ? 'Yes' : 'No'}</TableCell>
                  <TableCell align="right">
                    <Stack direction="row" spacing={1} justifyContent="flex-end">
                      <Button size="small" onClick={() => handleEdit(account)}>
                        Edit
                      </Button>
                      <Button size="small" color="error" onClick={() => handleDelete(account.id)} disabled={saving}>
                        Delete
                      </Button>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
}

PartyBankAccountsDialog.propTypes = {
  open: PropTypes.bool.isRequired,
  partyId: PropTypes.number,
  onClose: PropTypes.func.isRequired
};
