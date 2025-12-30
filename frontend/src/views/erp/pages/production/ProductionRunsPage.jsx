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

const createInputLine = () => ({
  itemId: '',
  uomId: '',
  quantity: '',
  sourceType: 'GODOWN',
  sourceGodownId: '',
  sourceRunOutputId: '',
  rate: '',
  amount: ''
});

const createOutputLine = () => ({
  itemId: '',
  uomId: '',
  quantity: '',
  outputType: 'WIP',
  destGodownId: '',
  rate: '',
  amount: ''
});

export default function ProductionRunsPage() {
  const [orders, setOrders] = useState([]);
  const [batches, setBatches] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [items, setItems] = useState([]);
  const [uoms, setUoms] = useState([]);
  const [godowns, setGodowns] = useState([]);
  const [wipOutputs, setWipOutputs] = useState([]);
  const [formValues, setFormValues] = useState({ orderId: '', batchId: '', stepId: '', runDate: '' });
  const [consumptions, setConsumptions] = useState([createInputLine()]);
  const [outputs, setOutputs] = useState([createOutputLine()]);

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

  useEffect(() => {
    if (!formValues.batchId) {
      setWipOutputs([]);
      return;
    }
    productionApi
      .listBatchWip(formValues.batchId)
      .then((response) => setWipOutputs(response.data || []))
      .catch(() => setWipOutputs([]));
  }, [formValues.batchId]);

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

  const handleAddLine = (setter, createFn) => {
    setter((prev) => [...prev, createFn()]);
  };

  const handleRemoveLine = (setter, index) => {
    setter((prev) => prev.filter((_, idx) => idx !== index));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    await productionApi.createRun(Number(formValues.batchId), {
      stepNo: formValues.stepId ? Number(formValues.stepId) : null,
      runDate: formValues.runDate || null,
      inputs: consumptions
        .filter((line) => line.itemId && line.uomId && line.quantity)
        .map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          qty: Number(line.quantity),
          sourceType: line.sourceType,
          sourceRefId: line.sourceType === 'WIP' && line.sourceRunOutputId ? Number(line.sourceRunOutputId) : null,
          godownId: line.sourceType === 'GODOWN' && line.sourceGodownId ? Number(line.sourceGodownId) : null
        })),
      outputs: outputs
        .filter((line) => line.itemId && line.uomId && line.quantity)
        .map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          qty: Number(line.quantity),
          outputType: line.outputType,
          destGodownId: line.destGodownId ? Number(line.destGodownId) : null
        }))
    });
    setConsumptions([createInputLine()]);
    setOutputs([createOutputLine()]);
    productionApi
      .listWipOutputs(formValues.batchId)
      .then((response) => setWipOutputs(response.data || []))
      .catch(() => setWipOutputs([]));
  };

  return (
    <MainCard>
      <PageHeader title="Process Runs" breadcrumbs={[{ label: 'Production' }, { label: 'Process Runs' }]} />
      <Stack spacing={3} component="form" onSubmit={handleSubmit}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 3 }}>
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
          <Grid size={{ xs: 12, md: 3 }}>
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
          <Grid size={{ xs: 12, md: 3 }}>
            <TextField
              fullWidth
              select
              label="Process Step"
              value={formValues.stepId}
              onChange={(event) => setFormValues((prev) => ({ ...prev, stepId: event.target.value }))}
            >
              <MenuItem value="">Adhoc</MenuItem>
              {selectedTemplate?.steps?.map((step) => (
                <MenuItem key={step.id} value={step.id}>
                  {step.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, md: 3 }}>
            <TextField
              fullWidth
              label="Run Date"
              type="date"
              value={formValues.runDate}
              onChange={(event) => setFormValues((prev) => ({ ...prev, runDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
        </Grid>

        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Card variant="outlined">
              <CardContent>
                <Stack spacing={2}>
                  <Typography variant="h6">Inputs</Typography>
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
                        <Grid size={{ xs: 12, md: 6 }}>
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
                        <Grid size={{ xs: 12, md: 6 }}>
                          <TextField
                            fullWidth
                            label="Quantity"
                            type="number"
                            value={line.quantity}
                            onChange={(event) => handleLineChange(setConsumptions, index, 'quantity', event.target.value)}
                          />
                        </Grid>
                      </Grid>
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            select
                            fullWidth
                            label="Source Type"
                            value={line.sourceType}
                            onChange={(event) => handleLineChange(setConsumptions, index, 'sourceType', event.target.value)}
                          >
                            <MenuItem value="GODOWN">Godown</MenuItem>
                            <MenuItem value="WIP">WIP</MenuItem>
                          </TextField>
                        </Grid>
                        {line.sourceType === 'GODOWN' ? (
                          <Grid size={{ xs: 12, md: 4 }}>
                            <TextField
                              select
                              fullWidth
                              label="Godown"
                              value={line.sourceGodownId}
                              onChange={(event) => handleLineChange(setConsumptions, index, 'sourceGodownId', event.target.value)}
                            >
                              <MenuItem value="">Select</MenuItem>
                              {godowns.map((godown) => (
                                <MenuItem key={godown.id} value={godown.id}>
                                  {godown.name}
                                </MenuItem>
                              ))}
                            </TextField>
                          </Grid>
                        ) : (
                          <Grid size={{ xs: 12, md: 4 }}>
                            <TextField
                              select
                              fullWidth
                              label="WIP Output"
                              value={line.sourceRunOutputId}
                              onChange={(event) => handleLineChange(setConsumptions, index, 'sourceRunOutputId', event.target.value)}
                            >
                              <MenuItem value="">Select</MenuItem>
                              {wipOutputs.map((output) => (
                                <MenuItem key={output.id} value={output.id}>
                                  {output.itemName} (Avail: {output.availableQuantity})
                                </MenuItem>
                              ))}
                            </TextField>
                          </Grid>
                        )}
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            fullWidth
                            label="Rate"
                            type="number"
                            value={line.rate}
                            onChange={(event) => handleLineChange(setConsumptions, index, 'rate', event.target.value)}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            fullWidth
                            label="Amount"
                            type="number"
                            value={line.amount}
                            onChange={(event) => handleLineChange(setConsumptions, index, 'amount', event.target.value)}
                          />
                        </Grid>
                      </Grid>
                      {consumptions.length > 1 && (
                        <Button variant="text" color="error" onClick={() => handleRemoveLine(setConsumptions, index)}>
                          Remove Line
                        </Button>
                      )}
                    </Stack>
                  ))}
                  <Button variant="outlined" onClick={() => handleAddLine(setConsumptions, createInputLine)}>
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
                  <Typography variant="h6">Outputs</Typography>
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
                            label="Output Type"
                            value={line.outputType}
                            onChange={(event) => handleLineChange(setOutputs, index, 'outputType', event.target.value)}
                          >
                            <MenuItem value="WIP">WIP</MenuItem>
                            <MenuItem value="FG">Finished Good</MenuItem>
                            <MenuItem value="BYPRODUCT">Byproduct</MenuItem>
                          </TextField>
                        </Grid>
                      </Grid>
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            select
                            fullWidth
                            label="Destination Godown"
                            value={line.destGodownId}
                            onChange={(event) => handleLineChange(setOutputs, index, 'destGodownId', event.target.value)}
                            disabled={line.outputType !== 'FG'}
                          >
                            <MenuItem value="">None</MenuItem>
                            {godowns.map((godown) => (
                              <MenuItem key={godown.id} value={godown.id}>
                                {godown.name}
                              </MenuItem>
                            ))}
                          </TextField>
                        </Grid>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            fullWidth
                            label="Rate"
                            type="number"
                            value={line.rate}
                            onChange={(event) => handleLineChange(setOutputs, index, 'rate', event.target.value)}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            fullWidth
                            label="Amount"
                            type="number"
                            value={line.amount}
                            onChange={(event) => handleLineChange(setOutputs, index, 'amount', event.target.value)}
                          />
                        </Grid>
                      </Grid>
                      {outputs.length > 1 && (
                        <Button variant="text" color="error" onClick={() => handleRemoveLine(setOutputs, index)}>
                          Remove Line
                        </Button>
                      )}
                    </Stack>
                  ))}
                  <Button variant="outlined" onClick={() => handleAddLine(setOutputs, createOutputLine)}>
                    Add Output
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
        <Button variant="contained" color="secondary" type="submit" disabled={!formValues.batchId}>
          Post Run
        </Button>
      </Stack>
    </MainCard>
  );
}
