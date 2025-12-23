import { useEffect, useMemo, useState } from 'react';

import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import { productionApi } from 'api/production';

const createLine = () => ({ itemId: '', uomId: '', quantity: '', godownId: '' });

export default function ProductionRunsPage() {
  const [orders, setOrders] = useState([]);
  const [batches, setBatches] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [items, setItems] = useState([]);
  const [uoms, setUoms] = useState([]);
  const [godowns, setGodowns] = useState([]);
  const [formValues, setFormValues] = useState({ orderId: '', batchId: '', stepId: '', runDate: '' });
  const [consumptions, setConsumptions] = useState([createLine()]);
  const [outputs, setOutputs] = useState([createLine()]);

  useEffect(() => {
    productionApi
      .listOrders()
      .then((response) => setOrders(response.data || []))
      .catch(() => setOrders([]));
    productionApi
      .listTemplates()
      .then((response) => setTemplates(response.data || []))
      .catch(() => setTemplates([]));
    apiClient
      .get('/api/items')
      .then((response) => setItems(response.data || []))
      .catch(() => setItems([]));
    apiClient
      .get('/api/uoms')
      .then((response) => setUoms(response.data || []))
      .catch(() => setUoms([]));
    apiClient
      .get('/api/godowns')
      .then((response) => setGodowns(response.data || []))
      .catch(() => setGodowns([]));
  }, []);

  useEffect(() => {
    if (!formValues.orderId) {
      setBatches([]);
      return;
    }
    productionApi
      .listBatches(formValues.orderId)
      .then((response) => setBatches(response.data || []))
      .catch(() => setBatches([]));
  }, [formValues.orderId]);

  const selectedOrder = orders.find((order) => order.id === Number(formValues.orderId));
  const selectedTemplate = useMemo(() => {
    if (!selectedOrder?.templateId) return null;
    return templates.find((template) => template.id === selectedOrder.templateId) || null;
  }, [selectedOrder, templates]);

  const handleLineChange = (setter, index, field, value) => {
    setter((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
  };

  const handleAddLine = (setter) => {
    setter((prev) => [...prev, createLine()]);
  };

  const handleRemoveLine = (setter, index) => {
    setter((prev) => prev.filter((_, idx) => idx !== index));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    await productionApi.createRun({
      batchId: Number(formValues.batchId),
      stepId: Number(formValues.stepId),
      runDate: formValues.runDate,
      consumptions: consumptions
        .filter((line) => line.itemId && line.uomId && line.quantity)
        .map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          quantity: Number(line.quantity),
          godownId: line.godownId ? Number(line.godownId) : null
        })),
      outputs: outputs
        .filter((line) => line.itemId && line.uomId && line.quantity)
        .map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          quantity: Number(line.quantity),
          godownId: line.godownId ? Number(line.godownId) : null
        }))
    });
    setConsumptions([createLine()]);
    setOutputs([createLine()]);
  };

  return (
    <MainCard>
      <PageHeader title="Process Runs" breadcrumbs={[{ label: 'Production' }, { label: 'Process Runs' }]} />
      <Stack spacing={3} component="form" onSubmit={handleSubmit}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Production Order"
              value={formValues.orderId}
              onChange={(event) => setFormValues((prev) => ({ ...prev, orderId: event.target.value, batchId: '', stepId: '' }))}
            >
              {orders.map((order) => (
                <MenuItem key={order.id} value={order.id}>
                  {order.orderNo}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Batch"
              value={formValues.batchId}
              onChange={(event) => setFormValues((prev) => ({ ...prev, batchId: event.target.value }))}
            >
              {batches.map((batch) => (
                <MenuItem key={batch.id} value={batch.id}>
                  {batch.batchNo}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              select
              label="Process Step"
              value={formValues.stepId}
              onChange={(event) => setFormValues((prev) => ({ ...prev, stepId: event.target.value }))}
            >
              {selectedTemplate?.steps?.map((step) => (
                <MenuItem key={step.id} value={step.id}>
                  {step.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Run Date"
              type="date"
              value={formValues.runDate}
              onChange={(event) => setFormValues((prev) => ({ ...prev, runDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
        </Grid>

        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Card variant="outlined">
              <CardContent>
                <Stack spacing={2}>
                  <Typography variant="h6">Consumption</Typography>
                  {consumptions.map((line, index) => (
                    <Stack key={`consumption-${index}`} spacing={1}>
                      <TextField
                        select
                        fullWidth
                        label="Item"
                        value={line.itemId}
                        onChange={(event) => handleLineChange(setConsumptions, index, 'itemId', event.target.value)}
                      >
                        {items.map((item) => (
                          <MenuItem key={item.id} value={item.id}>
                            {item.name}
                          </MenuItem>
                        ))}
                      </TextField>
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            select
                            fullWidth
                            label="UOM"
                            value={line.uomId}
                            onChange={(event) => handleLineChange(setConsumptions, index, 'uomId', event.target.value)}
                          >
                            {uoms.map((uom) => (
                              <MenuItem key={uom.id} value={uom.id}>
                                {uom.code}
                              </MenuItem>
                            ))}
                          </TextField>
                        </Grid>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            fullWidth
                            label="Quantity"
                            type="number"
                            value={line.quantity}
                            onChange={(event) => handleLineChange(setConsumptions, index, 'quantity', event.target.value)}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            select
                            fullWidth
                            label="Godown"
                            value={line.godownId}
                            onChange={(event) => handleLineChange(setConsumptions, index, 'godownId', event.target.value)}
                          >
                            <MenuItem value="">None</MenuItem>
                            {godowns.map((godown) => (
                              <MenuItem key={godown.id} value={godown.id}>
                                {godown.name}
                              </MenuItem>
                            ))}
                          </TextField>
                        </Grid>
                      </Grid>
                      {consumptions.length > 1 && (
                        <Button variant="text" color="error" onClick={() => handleRemoveLine(setConsumptions, index)}>
                          Remove Line
                        </Button>
                      )}
                    </Stack>
                  ))}
                  <Button variant="outlined" onClick={() => handleAddLine(setConsumptions)}>
                    Add Consumption
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Card variant="outlined">
              <CardContent>
                <Stack spacing={2}>
                  <Typography variant="h6">Output</Typography>
                  {outputs.map((line, index) => (
                    <Stack key={`output-${index}`} spacing={1}>
                      <TextField
                        select
                        fullWidth
                        label="Item"
                        value={line.itemId}
                        onChange={(event) => handleLineChange(setOutputs, index, 'itemId', event.target.value)}
                      >
                        {items.map((item) => (
                          <MenuItem key={item.id} value={item.id}>
                            {item.name}
                          </MenuItem>
                        ))}
                      </TextField>
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            select
                            fullWidth
                            label="UOM"
                            value={line.uomId}
                            onChange={(event) => handleLineChange(setOutputs, index, 'uomId', event.target.value)}
                          >
                            {uoms.map((uom) => (
                              <MenuItem key={uom.id} value={uom.id}>
                                {uom.code}
                              </MenuItem>
                            ))}
                          </TextField>
                        </Grid>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            fullWidth
                            label="Quantity"
                            type="number"
                            value={line.quantity}
                            onChange={(event) => handleLineChange(setOutputs, index, 'quantity', event.target.value)}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            select
                            fullWidth
                            label="Godown"
                            value={line.godownId}
                            onChange={(event) => handleLineChange(setOutputs, index, 'godownId', event.target.value)}
                          >
                            <MenuItem value="">None</MenuItem>
                            {godowns.map((godown) => (
                              <MenuItem key={godown.id} value={godown.id}>
                                {godown.name}
                              </MenuItem>
                            ))}
                          </TextField>
                        </Grid>
                      </Grid>
                      {outputs.length > 1 && (
                        <Button variant="text" color="error" onClick={() => handleRemoveLine(setOutputs, index)}>
                          Remove Line
                        </Button>
                      )}
                    </Stack>
                  ))}
                  <Button variant="outlined" onClick={() => handleAddLine(setOutputs)}>
                    Add Output
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
        <Button variant="contained" color="secondary" type="submit" disabled={!formValues.batchId || !formValues.stepId}>
          Post Run
        </Button>
      </Stack>
    </MainCard>
  );
}
