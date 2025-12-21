import { useEffect, useState } from 'react';

// material-ui
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

// project imports
import MainCard from 'ui-component/cards/MainCard';
import SubCard from 'ui-component/cards/SubCard';
import Breadcrumbs from 'ui-component/extended/Breadcrumbs';
import { gridSpacing } from 'store/constant';
import apiClient from 'api/client';
import { useNavigate } from 'react-router-dom';

const quickActions = ['Record Weighbridge In', 'Create Purchase Order', 'Post QC Result', 'Transfer to UNRESTRICTED'];

// ==============================|| ERP DASHBOARD ||============================== //

export default function Dashboard() {
  const [apiStatus, setApiStatus] = useState({ loading: true, ok: false, message: 'Checking backend...' });
  const [metrics, setMetrics] = useState({
    openGrns: 0,
    pendingQc: 0,
    invoicesReady: 0,
    transfersInFlight: 0
  });
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;
    apiClient
      .get('/actuator/health')
      .then((response) => {
        if (!active) return;
        const status = response?.data?.status || 'UP';
        setApiStatus({ loading: false, ok: status === 'UP', message: status });
      })
      .catch(() => {
        if (!active) return;
        setApiStatus({ loading: false, ok: false, message: 'Backend not connected' });
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    apiClient
      .get('/api/metrics/dashboard')
      .then((response) => {
        setMetrics(response.data || metrics);
      })
      .catch(() => {
        setMetrics(metrics);
      });
  }, []);

  return (
    <>
      <Breadcrumbs card={false} divider={false} title />
      <Grid container spacing={gridSpacing}>
        <Grid size={12}>
          <MainCard
            title="Manufacturing ERP Overview"
            secondary={
              <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/weighbridge-in')}>
                Create Entry
              </Button>
            }
          >
            <Stack spacing={2.5}>
              <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
                <SubCard sx={{ flex: 1 }}>
                  <Typography variant="h4">{metrics.openGrns}</Typography>
                  <Typography variant="subtitle2">Open GRNs</Typography>
                  <Typography variant="caption" color="text.secondary">
                    Pending receipts
                  </Typography>
                </SubCard>
                <SubCard sx={{ flex: 1 }}>
                  <Typography variant="h4">{metrics.pendingQc}</Typography>
                  <Typography variant="subtitle2">Pending QC Lots</Typography>
                  <Typography variant="caption" color="text.secondary">
                    Awaiting inspection
                  </Typography>
                </SubCard>
                <SubCard sx={{ flex: 1 }}>
                  <Typography variant="h4">{metrics.invoicesReady}</Typography>
                  <Typography variant="subtitle2">Invoices Ready</Typography>
                  <Typography variant="caption" color="text.secondary">
                    Purchase invoices
                  </Typography>
                </SubCard>
                <SubCard sx={{ flex: 1 }}>
                  <Typography variant="h4">{metrics.transfersInFlight}</Typography>
                  <Typography variant="subtitle2">Transfers In Flight</Typography>
                  <Typography variant="caption" color="text.secondary">
                    Stock movements
                  </Typography>
                </SubCard>
              </Stack>
              <Box>
                <SubCard>
                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }}>
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="h5">API Status</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Health check from backend /actuator/health
                      </Typography>
                    </Box>
                    <Chip
                      color={apiStatus.ok ? 'success' : 'warning'}
                      label={apiStatus.loading ? 'Checking...' : apiStatus.message}
                      variant={apiStatus.ok ? 'filled' : 'outlined'}
                    />
                  </Stack>
                </SubCard>
              </Box>
              <SubCard>
                <Typography variant="h5" gutterBottom>
                  Quick Actions
                </Typography>
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                  {quickActions.map((action) => (
                    <Button key={action} variant="outlined" color="secondary" onClick={() => navigate('/purchase/weighbridge-in')}>
                      {action}
                    </Button>
                  ))}
                </Stack>
              </SubCard>
            </Stack>
          </MainCard>
        </Grid>
      </Grid>
    </>
  );
}
