import { useEffect, useState } from 'react';

// material-ui
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
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
import {
  IconShoppingCart,
  IconClipboardList,
  IconTruck,
  IconFileInvoice
} from '@tabler/icons-react';

const quickActions = [
  { label: 'Create RFQ', route: '/purchase/rfq/new' },
  { label: 'Create Purchase Order', route: '/purchase/po/new' },
  { label: 'Weighbridge In', route: '/purchase/weighbridge-in/new' },
  { label: 'Record Arrival', route: '/purchase/arrival/new' },
  { label: 'Create GRN', route: '/purchase/grn/new' }
];

const modules = [
  {
    title: 'Purchase',
    subtitle: 'RFQ → PO → GRN',
    metricLabel: 'Open GRNs',
    icon: IconShoppingCart,
    route: '/purchase/rfq'
  },
  {
    title: 'Inventory',
    subtitle: 'Stock & Transfers',
    metricLabel: 'Pending QC',
    icon: IconClipboardList,
    route: '/inventory/stock-ledger'
  },
  {
    title: 'Logistics',
    subtitle: 'Weighbridge & Receiving',
    metricLabel: 'Transfers In Flight',
    icon: IconTruck,
    route: '/purchase/weighbridge-in'
  },
  {
    title: 'Finance',
    subtitle: 'Invoices & Deductions',
    metricLabel: 'Invoices Ready',
    icon: IconFileInvoice,
    route: '/purchase/purchase-invoice'
  }
];

// ==============================|| ERP DASHBOARD ||============================== //

export default function Dashboard() {
  const [metrics, setMetrics] = useState({
    openGrns: 0,
    pendingQc: 0,
    invoicesReady: 0,
    transfersInFlight: 0
  });
  const navigate = useNavigate();

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
              <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/weighbridge-in/new')}>
                Create Entry
              </Button>
            }
          >
            <Stack spacing={2.5}>
              <Grid container spacing={2}>
                {modules.map((module) => {
                  const Icon = module.icon;
                  const metricValue = (() => {
                    switch (module.metricLabel) {
                      case 'Open GRNs':
                        return metrics.openGrns;
                      case 'Pending QC':
                        return metrics.pendingQc;
                      case 'Invoices Ready':
                        return metrics.invoicesReady;
                      case 'Transfers In Flight':
                        return metrics.transfersInFlight;
                      default:
                        return 0;
                    }
                  })();

                  return (
                    <Grid key={module.title} size={{ xs: 12, sm: 6, lg: 3 }}>
                      <Card
                        onClick={() => navigate(module.route)}
                        sx={(theme) => ({
                          height: '100%',
                          border: `1px solid ${theme.palette.divider}`,
                          borderRadius: 2,
                          cursor: 'pointer',
                          transition: 'all 0.2s ease',
                          background: theme.palette.background.paper,
                          '&:hover': {
                            boxShadow: theme.shadows[8],
                            borderColor: theme.palette.primary.light,
                            transform: 'translateY(-2px)'
                          }
                        })}
                      >
                        <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                          <Stack direction="row" spacing={1.5} alignItems="center">
                            <Avatar sx={{ bgcolor: 'secondary.light', color: 'secondary.dark', width: 44, height: 44 }}>
                              <Icon size={22} />
                            </Avatar>
                            <Box>
                              <Typography variant="h5">{module.title}</Typography>
                              <Typography variant="body2" color="text.secondary">
                                {module.subtitle}
                              </Typography>
                            </Box>
                          </Stack>
                          <Box>
                            <Typography variant="h3">{metricValue}</Typography>
                            <Typography variant="caption" color="text.secondary">
                              {module.metricLabel}
                            </Typography>
                          </Box>
                        </CardContent>
                      </Card>
                    </Grid>
                  );
                })}
              </Grid>
              <SubCard>
                <Typography variant="h5" gutterBottom>
                  Quick Actions
                </Typography>
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                  {quickActions.map((action) => (
                    <Button key={action.label} variant="outlined" color="secondary" onClick={() => navigate(action.route)}>
                      {action.label}
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
