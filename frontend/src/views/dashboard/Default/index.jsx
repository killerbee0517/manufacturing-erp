import { useEffect, useMemo, useState } from 'react';

import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

import apiClient from 'api/client';
import { useNavigate } from 'react-router-dom';
import MainCard from 'ui-component/cards/MainCard';
import SubCard from 'ui-component/cards/SubCard';
import Breadcrumbs from 'ui-component/extended/Breadcrumbs';
import { gridSpacing } from 'store/constant';

const quickActions = [
  { label: 'Create RFQ', route: '/purchase/rfq/new' },
  { label: 'Create Purchase Order', route: '/purchase/po/new' },
  { label: 'Weighbridge In', route: '/purchase/weighbridge-in/new' },
  { label: 'Record Arrival', route: '/purchase/arrival/new' },
  { label: 'Create GRN', route: '/purchase/grn/new' }
];

const numberFormatter = new Intl.NumberFormat('en-IN');
const amountFormatter = new Intl.NumberFormat('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const formatCount = (value) => numberFormatter.format(value ?? 0);
const formatAmount = (value) => amountFormatter.format(Number(value ?? 0));

// ==============================|| ERP DASHBOARD ||============================== //

export default function Dashboard() {
  const [metrics, setMetrics] = useState({
    openGrns: 0,
    pendingQc: 0,
    invoicesReady: 0,
    transfersInFlight: 0,
    pdcOverdue: 0,
    pdcDueToday: 0,
    pdcFuture: 0,
    purchaseOrdersToday: 0,
    poPendingApproval: 0,
    salesInvoicesToday: 0,
    productionOutputToday: 0,
    stockValue: 0,
    purchaseOrdersMonth: 0,
    salesInvoicesMonth: 0,
    productionOutputMonth: 0
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

  const tileSections = useMemo(
    () => [
      {
        title: 'Purchasing',
        items: [
          { label: 'Purchase Orders Today', value: formatCount(metrics.purchaseOrdersToday), route: '/purchase/po', hint: 'Created today' },
          { label: 'PO Approvals Pending', value: formatCount(metrics.poPendingApproval), route: '/purchase/po', hint: 'Awaiting approval' },
          { label: 'GRNs Pending', value: formatCount(metrics.openGrns), route: '/purchase/grn', hint: 'Not posted' },
          { label: 'QC Pending', value: formatCount(metrics.pendingQc), route: '/purchase/qc', hint: 'Draft or submitted' },
          { label: 'Purchase Invoices', value: formatCount(metrics.invoicesReady), route: '/purchase/purchase-invoice', hint: 'Ready to review' }
        ]
      },
      {
        title: 'Inventory & Logistics',
        items: [
          { label: 'Transfers In Flight', value: formatCount(metrics.transfersInFlight), route: '/inventory/stock-transfer', hint: 'Active transfers' },
          { label: 'Stock Value', value: formatAmount(metrics.stockValue), route: '/inventory/stock-on-hand', hint: 'Current valuation' }
        ]
      },
      {
        title: 'Sales & Production',
        items: [
          { label: 'Sales Invoices Today', value: formatCount(metrics.salesInvoicesToday), route: '/sales/tax-invoice', hint: 'Issued today' },
          { label: 'Production Output Today', value: formatAmount(metrics.productionOutputToday), route: '/production/runs', hint: 'Total quantity' }
        ]
      },
      {
        title: 'Finance',
        items: [
          { label: 'PDC Overdue', value: formatCount(metrics.pdcOverdue), route: '/accounts/payments', hint: 'Needs action' },
          { label: 'PDC Due Today', value: formatCount(metrics.pdcDueToday), route: '/accounts/payments', hint: 'Due today' },
          { label: 'PDC Future', value: formatCount(metrics.pdcFuture), route: '/accounts/payments', hint: 'Upcoming' }
        ]
      }
    ],
    [metrics]
  );

  return (
    <>
      <Breadcrumbs card={false} divider={false} title />
      <Grid container spacing={gridSpacing}>
        <Grid size={12}>
          <MainCard
            title="ERP Launchpad"
            secondary={
              <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/weighbridge-in/new')}>
                Create Entry
              </Button>
            }
          >
            <Stack spacing={3}>
              {tileSections.map((section) => (
                <Box key={section.title}>
                  <Typography variant="h5" sx={{ mb: 2 }}>
                    {section.title}
                  </Typography>
                  <Grid container spacing={2}>
                    {section.items.map((item) => (
                      <Grid key={item.label} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
                        <Card
                          onClick={() => navigate(item.route)}
                          sx={(theme) => ({
                            height: '100%',
                            border: `1px solid ${theme.palette.divider}`,
                            borderRadius: 2,
                            cursor: 'pointer',
                            transition: 'all 0.2s ease',
                            '&:hover': {
                              borderColor: theme.palette.primary.light,
                              boxShadow: theme.shadows[4],
                              transform: 'translateY(-2px)'
                            }
                          })}
                        >
                          <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                            <Typography variant="subtitle2" color="text.secondary">
                              {item.label}
                            </Typography>
                            <Typography variant="h3">{item.value}</Typography>
                            <Typography variant="caption" color="text.secondary">
                              {item.hint}
                            </Typography>
                          </CardContent>
                        </Card>
                      </Grid>
                    ))}
                  </Grid>
                </Box>
              ))}

              <Divider />

              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 7 }}>
                  <SubCard>
                    <Typography variant="h5" gutterBottom>
                      Quick Actions
                    </Typography>
                    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} flexWrap="wrap">
                      {quickActions.map((action) => (
                        <Button key={action.label} variant="outlined" color="secondary" onClick={() => navigate(action.route)}>
                          {action.label}
                        </Button>
                      ))}
                    </Stack>
                  </SubCard>
                </Grid>
                <Grid size={{ xs: 12, md: 5 }}>
                  <SubCard>
                    <Stack spacing={2}>
                      <Typography variant="h5">Monthly Analytics</Typography>
                      <Grid container spacing={2}>
                        <Grid size={{ xs: 12 }}>
                          <Stack direction="row" justifyContent="space-between">
                            <Typography variant="subtitle2">Purchase Orders (MTD)</Typography>
                            <Typography variant="h6">{formatCount(metrics.purchaseOrdersMonth)}</Typography>
                          </Stack>
                        </Grid>
                        <Grid size={{ xs: 12 }}>
                          <Stack direction="row" justifyContent="space-between">
                            <Typography variant="subtitle2">Sales Invoices (MTD)</Typography>
                            <Typography variant="h6">{formatCount(metrics.salesInvoicesMonth)}</Typography>
                          </Stack>
                        </Grid>
                        <Grid size={{ xs: 12 }}>
                          <Stack direction="row" justifyContent="space-between">
                            <Typography variant="subtitle2">Production Output (MTD)</Typography>
                            <Typography variant="h6">{formatAmount(metrics.productionOutputMonth)}</Typography>
                          </Stack>
                        </Grid>
                      </Grid>
                    </Stack>
                  </SubCard>
                </Grid>
              </Grid>
            </Stack>
          </MainCard>
        </Grid>
      </Grid>
    </>
  );
}
