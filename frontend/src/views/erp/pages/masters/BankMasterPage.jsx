import { useCallback, useEffect, useMemo, useState } from 'react';
import useSWR from 'swr';

import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Grid from '@mui/material/Grid';
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
import ModulePage from 'views/erp/ModulePage';
import { moduleConfigs } from 'views/erp/moduleConfig';

export default function BankMasterPage() {
  const [filters, setFilters] = useState({ search: '' });
  const [debouncedFilters, setDebouncedFilters] = useState({ search: '' });

  useEffect(() => {
    const handle = setTimeout(() => setDebouncedFilters(filters), 300);
    return () => clearTimeout(handle);
  }, [filters]);

  const listKey = useMemo(
    () => ['party-bank-accounts', debouncedFilters.search],
    [debouncedFilters.search]
  );

  const fetcher = useCallback(async () => {
    const response = await apiClient.get('/api/party-bank-accounts', {
      params: { search: debouncedFilters.search || undefined }
    });
    return response.data || [];
  }, [debouncedFilters.search]);

  const {
    data: accounts = [],
    isLoading,
    isValidating,
    error
  } = useSWR(listKey, fetcher, {
    revalidateOnFocus: false,
    revalidateOnReconnect: false
  });

  return (
    <>
      <ModulePage config={moduleConfigs.banks} />
      <Grid container spacing={gridSpacing} sx={{ mt: 2 }}>
        <Grid size={12}>
          <MainCard
            title={
              <Stack spacing={0.5}>
                <Typography variant="overline" color="text.secondary">
                  Masters
                </Typography>
                <Typography variant="h3">Party Bank Accounts</Typography>
              </Stack>
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
              </Stack>
              {error && (
                <Alert severity="error">Unable to load party bank accounts.</Alert>
              )}
              <Box>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Party</TableCell>
                        <TableCell>Bank</TableCell>
                        <TableCell>Account No</TableCell>
                        <TableCell>IFSC</TableCell>
                        <TableCell>Type</TableCell>
                        <TableCell>Default</TableCell>
                        <TableCell>Active</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {accounts.length === 0 && (
                        <TableRow>
                          <TableCell colSpan={7}>
                            {isLoading || isValidating ? (
                              <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                                <CircularProgress size={18} />
                                <span>Loading...</span>
                              </Stack>
                            ) : (
                              'No bank accounts found'
                            )}
                          </TableCell>
                        </TableRow>
                      )}
                      {accounts.map((account) => (
                        <TableRow key={account.id}>
                          <TableCell>{account.partyName || '-'}</TableCell>
                          <TableCell>{account.bankName || '-'}</TableCell>
                          <TableCell>{account.accountNo || '-'}</TableCell>
                          <TableCell>{account.ifsc || '-'}</TableCell>
                          <TableCell>{account.accountType || '-'}</TableCell>
                          <TableCell>{account.isDefault ? 'Yes' : 'No'}</TableCell>
                          <TableCell>{account.active ? 'Yes' : 'No'}</TableCell>
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
    </>
  );
}
