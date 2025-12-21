import { useEffect, useMemo, useState } from 'react';

import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Stepper from '@mui/material/Stepper';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import EntitySelect from 'components/common/EntitySelect';
import { productionApi } from 'api/production';

export default function ProcessExecutionPage() {
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [activeStep, setActiveStep] = useState(0);
  const [movement, setMovement] = useState({ source: '', destination: '', qty: '' });

  useEffect(() => {
    productionApi
      .listOrders()
      .then((response) => setOrders(response.data || []))
      .catch(() => setOrders([]));
  }, []);

  const steps = useMemo(() => {
    if (!selectedOrder?.steps) return ['Start', 'Process', 'Finish'];
    return selectedOrder.steps.map((step) => step.name);
  }, [selectedOrder]);

  const handleExecute = async () => {
    if (!selectedOrder) return;
    await productionApi.createExecution({
      orderId: selectedOrder.id,
      step: steps[activeStep],
      movement
    });
    setActiveStep((prev) => Math.min(prev + 1, steps.length - 1));
    setMovement({ source: '', destination: '', qty: '' });
  };

  return (
    <MainCard>
      <PageHeader title="Process Execution" breadcrumbs={[{ label: 'Production' }, { label: 'Process Execution' }]} />
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid size={{ xs: 12, md: 6 }}>
          <EntitySelect
            label="Production Order"
            endpoint="/production/orders"
            value={selectedOrder}
            onChange={(value) => {
              setSelectedOrder(value);
              setActiveStep(0);
            }}
            getOptionLabel={(option) => option?.orderNo || option?.fgItem || ''}
          />
        </Grid>
      </Grid>
      <Stepper activeStep={activeStep} alternativeLabel>
        {steps.map((step) => (
          <Step key={step}>
            <StepLabel>{step}</StepLabel>
          </Step>
        ))}
      </Stepper>
      <Grid container spacing={2} sx={{ mt: 2 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <TextField
            fullWidth
            label="Source Location"
            value={movement.source}
            onChange={(event) => setMovement((prev) => ({ ...prev, source: event.target.value }))}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <TextField
            fullWidth
            label="Destination Location"
            value={movement.destination}
            onChange={(event) => setMovement((prev) => ({ ...prev, destination: event.target.value }))}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <TextField
            fullWidth
            label="Quantity"
            type="number"
            value={movement.qty}
            onChange={(event) => setMovement((prev) => ({ ...prev, qty: event.target.value }))}
          />
        </Grid>
      </Grid>
      <Button sx={{ mt: 2 }} variant="contained" color="secondary" onClick={handleExecute} disabled={!selectedOrder}>
        Record Step
      </Button>
      {!selectedOrder && (
        <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
          Select a production order to begin execution.
        </Typography>
      )}
    </MainCard>
  );
}
