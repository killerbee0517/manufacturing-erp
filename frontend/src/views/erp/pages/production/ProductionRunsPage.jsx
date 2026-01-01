import { useEffect, useMemo, useState } from 'react';

import Autocomplete from '@mui/material/Autocomplete';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
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
  outputType: 'FG',
  destGodownId: '',
  rate: '',
  amount: ''
});

const todayValue = () => new Date().toISOString().slice(0, 10);

export default function ProductionRunsPage() {
  const [batchId, setBatchId] = useState('');
  const [selectedBatch, setSelectedBatch] = useState(null);
  const [template, setTemplate] = useState(null);
  const [runs, setRuns] = useState([]);
  const [wipOptions, setWipOptions] = useState([]);
  const [formValues, setFormValues] = useState({ stepNo: '', runDate: todayValue(), notes: '', moisturePercent: '' });
  const [consumptions, setConsumptions] = useState([createInputLine()]);
  const [outputs, setOutputs] = useState([createOutputLine()]);
  const [posting, setPosting] = useState(false);
  const [selectedRunId, setSelectedRunId] = useState(null);
  const [costSummary, setCostSummary] = useState(null);

  useEffect(() => {
    if (!batchId) {
      setSelectedBatch(null);
      setTemplate(null);
      setRuns([]);
      setWipOptions([]);
      setConsumptions([createInputLine()]);
      setOutputs([createOutputLine()]);
      setSelectedRunId(null);
      setCostSummary(null);
      return;
    }

    const load = async () => {
      setSelectedRunId(null);
      setCostSummary(null);
      const batchResponse = await productionApi.getBatch(batchId);
      const batch = batchResponse.data;
      setSelectedBatch(batch);

      if (batch?.templateId) {
        const templateResponse = await productionApi.getTemplate(batch.templateId);
        const templateData = templateResponse.data;
        setTemplate(templateData);

        const inputDefaults = (templateData.inputs || []).map((input) => ({
          itemId: input.itemId || '',
          uomId: input.uomId || '',
          quantity: input.defaultQty ?? '',
          sourceType: 'GODOWN',
          sourceGodownId: '',
          sourceRunOutputId: '',
          rate: '',
          amount: ''
        }));
        const outputDefaults = (templateData.outputs || []).map((output) => ({
          itemId: output.itemId || '',
          uomId: output.uomId || '',
          quantity: output.defaultRatio ?? '',
          outputType: output.outputType || 'FG',
          destGodownId: '',
          rate: '',
          amount: ''
        }));
        setConsumptions(inputDefaults.length ? inputDefaults : [createInputLine()]);
        setOutputs(outputDefaults.length ? outputDefaults : [createOutputLine()]);
        if (templateData.steps?.length) {
          setFormValues((prev) => ({ ...prev, stepNo: templateData.steps[0].stepNo || '' }));
        }
      } else {
        setTemplate(null);
      }

      const runsResponse = await productionApi.listBatchRuns(batchId);
      setRuns(runsResponse.data || []);

      const wipResponse = await productionApi.listBatchWip(batchId);
      setWipOptions(wipResponse.data || []);
    };

    load().catch(() => null);
  }, [batchId]);

  useEffect(() => {
    if (!selectedRunId) {
      setCostSummary(null);
      return;
    }
    productionApi
      .getRunCostSummary(selectedRunId)
      .then((response) => setCostSummary(response.data))
      .catch(() => setCostSummary(null));
  }, [selectedRunId]);

  const stepOptions = useMemo(() => template?.steps || [], [template]);

  const handleLineChange = (setter, index, field, value) => {
    setter((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
  };

  const handleAddLine = (setter, createFn) => setter((prev) => [...prev, createFn()]);

  const handleRemoveLine = (setter, index) => setter((prev) => prev.filter((_, idx) => idx !== index));

  const handlePostRun = async (event) => {
    event.preventDefault();
    if (!batchId) return;
    setPosting(true);
    try {
      const payload = {
        stepNo: formValues.stepNo ? Number(formValues.stepNo) : null,
        stepName: null,
        moisturePercent: formValues.moisturePercent ? Number(formValues.moisturePercent) : null,
        notes: formValues.notes || null,
        runDate: formValues.runDate || null,
        inputs: consumptions
          .filter((line) => line.itemId && line.uomId && line.quantity)
          .map((line) => ({
            itemId: Number(line.itemId),
            uomId: Number(line.uomId),
            qty: Number(line.quantity),
            sourceType: line.sourceType,
            sourceRefId: line.sourceType === 'WIP' && line.sourceRunOutputId ? Number(line.sourceRunOutputId) : null,
            godownId: line.sourceType === 'GODOWN' && line.sourceGodownId ? Number(line.sourceGodownId) : null,
            rate: line.rate ? Number(line.rate) : null,
            amount: line.amount ? Number(line.amount) : null
          })),
        outputs: outputs
          .filter((line) => line.itemId && line.uomId && line.quantity)
          .map((line) => ({
            itemId: Number(line.itemId),
            uomId: Number(line.uomId),
            qty: Number(line.quantity),
            outputType: line.outputType,
            destGodownId: line.destGodownId ? Number(line.destGodownId) : null,
            rate: line.rate ? Number(line.rate) : null,
            amount: line.amount ? Number(line.amount) : null
          }))
      };

      const runResponse = await productionApi.createRun(Number(batchId), payload);
      const postedResponse = await productionApi.postRun(runResponse.data.id);
      setSelectedRunId(postedResponse.data.id);
      setFormValues((prev) => ({ ...prev, runDate: todayValue(), notes: '', moisturePercent: '' }));

      const runsResponse = await productionApi.listBatchRuns(batchId);
      setRuns(runsResponse.data || []);

      const wipResponse = await productionApi.listBatchWip(batchId);
      setWipOptions(wipResponse.data || []);
    } finally {
      setPosting(false);
    }
  };

  const runColumns = [
    { field: 'runNo', headerName: 'Run No' },
    { field: 'stepName', headerName: 'Step' },
    { field: 'status', headerName: 'Status' },
    { field: 'runDate', headerName: 'Run Date' }
  ];

  return (
    <MainCard>
      <PageHeader title="Production Runs" breadcrumbs={[{ label: 'Production' }, { label: 'Production Runs' }]} />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Stack spacing={2} component="form" onSubmit={handlePostRun}>
            <Card variant="outlined">
              <CardContent>
                <Stack spacing={2}>
                  <MasterAutocomplete
                    label="Batch"
                    endpoint="/api/production/batches"
                    value={batchId}
                    onChange={(value) => setBatchId(value)}
                    optionLabelKey="batchNo"
                    placeholder="Select batch"
                  />
                  {selectedBatch && (
                    <Typography variant="body2" color="text.secondary">
                      Template: {selectedBatch.templateName || '-'}
                    </Typography>
                  )}
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <Autocomplete
                        options={[{ stepNo: '', stepName: 'Adhoc' }, ...stepOptions]}
                        value={
                          formValues.stepNo
                            ? stepOptions.find((step) => step.stepNo === formValues.stepNo) || null
                            : { stepNo: '', stepName: 'Adhoc' }
                        }
                        onChange={(_, newValue) =>
                          setFormValues((prev) => ({ ...prev, stepNo: newValue?.stepNo || '' }))
                        }
                        getOptionLabel={(option) =>
                          option.stepNo ? `${option.stepNo}. ${option.stepName}` : option.stepName || 'Adhoc'
                        }
                        isOptionEqualToValue={(option, selected) => option.stepNo === selected?.stepNo}
                        renderInput={(params) => <TextField {...params} label="Process Step" />}
                      />
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <TextField
                        fullWidth
                        label="Run Date"
                        type="date"
                        value={formValues.runDate}
                        onChange={(event) => setFormValues((prev) => ({ ...prev, runDate: event.target.value }))}
                        InputLabelProps={{ shrink: true }}
                      />
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <TextField
                        fullWidth
                        label="Moisture %"
                        type="number"
                        value={formValues.moisturePercent}
                        onChange={(event) => setFormValues((prev) => ({ ...prev, moisturePercent: event.target.value }))}
                      />
                    </Grid>
                  </Grid>
                  <TextField
                    fullWidth
                    label="Notes"
                    value={formValues.notes}
                    onChange={(event) => setFormValues((prev) => ({ ...prev, notes: event.target.value }))}
                  />
                </Stack>
              </CardContent>
            </Card>

            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Card variant="outlined">
                  <CardContent>
                    <Stack spacing={2}>
                      <Typography variant="h6">Inputs</Typography>
                      {consumptions.map((line, index) => {
                        const selectedWip = wipOptions.find((option) => option.id === line.sourceRunOutputId) || null;
                        return (
                          <Stack key={`consumption-${index}`} spacing={1}>
                            <MasterAutocomplete
                              label="Item"
                              endpoint="/api/items"
                              value={line.itemId}
                              onChange={(value) => handleLineChange(setConsumptions, index, 'itemId', value)}
                              placeholder="Select item"
                            />
                            <Grid container spacing={1}>
                              <Grid size={{ xs: 12, md: 6 }}>
                                <MasterAutocomplete
                                  label="UOM"
                                  endpoint="/api/uoms"
                                  value={line.uomId}
                                  onChange={(value) => handleLineChange(setConsumptions, index, 'uomId', value)}
                                  placeholder="Select UOM"
                                />
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
                                <Grid size={{ xs: 12, md: 8 }}>
                                  <MasterAutocomplete
                                    label="Godown"
                                    endpoint="/api/godowns"
                                    value={line.sourceGodownId}
                                    onChange={(value) => handleLineChange(setConsumptions, index, 'sourceGodownId', value)}
                                    placeholder="Select godown"
                                  />
                                </Grid>
                              ) : (
                                <Grid size={{ xs: 12, md: 8 }}>
                                  <Autocomplete
                                    options={wipOptions}
                                    value={selectedWip}
                                    onChange={(_, newValue) =>
                                      handleLineChange(setConsumptions, index, 'sourceRunOutputId', newValue?.id || '')
                                    }
                                    getOptionLabel={(option) =>
                                      `${option.itemName} (Avail: ${option.availableQuantity})`
                                    }
                                    isOptionEqualToValue={(option, selected) => option.id === selected?.id}
                                    renderInput={(params) => <TextField {...params} label="WIP Output" />}
                                  />
                                </Grid>
                              )}
                            </Grid>
                            <Grid container spacing={1}>
                              <Grid size={{ xs: 12, md: 6 }}>
                                <TextField
                                  fullWidth
                                  label="Rate"
                                  type="number"
                                  value={line.rate}
                                  onChange={(event) => handleLineChange(setConsumptions, index, 'rate', event.target.value)}
                                />
                              </Grid>
                              <Grid size={{ xs: 12, md: 6 }}>
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
                            <Divider />
                          </Stack>
                        );
                      })}
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
                          <MasterAutocomplete
                            label="Item"
                            endpoint="/api/items"
                            value={line.itemId}
                            onChange={(value) => handleLineChange(setOutputs, index, 'itemId', value)}
                            placeholder="Select item"
                          />
                          <Grid container spacing={1}>
                            <Grid size={{ xs: 12, md: 4 }}>
                              <MasterAutocomplete
                                label="UOM"
                                endpoint="/api/uoms"
                                value={line.uomId}
                                onChange={(value) => handleLineChange(setOutputs, index, 'uomId', value)}
                                placeholder="Select UOM"
                              />
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
                                <MenuItem value="FG">Finished Good</MenuItem>
                                <MenuItem value="WIP">WIP</MenuItem>
                                <MenuItem value="BYPRODUCT">By-product</MenuItem>
                                <MenuItem value="EMPTY_BAG">Empty Bag</MenuItem>
                              </TextField>
                            </Grid>
                          </Grid>
                          <Grid container spacing={1}>
                            <Grid size={{ xs: 12, md: 6 }}>
                              <MasterAutocomplete
                                label="Destination Godown"
                                endpoint="/api/godowns"
                                value={line.destGodownId}
                                onChange={(value) => handleLineChange(setOutputs, index, 'destGodownId', value)}
                                placeholder="Select godown"
                                disabled={line.outputType === 'WIP'}
                              />
                            </Grid>
                            <Grid size={{ xs: 12, md: 3 }}>
                              <TextField
                                fullWidth
                                label="Rate"
                                type="number"
                                value={line.rate}
                                onChange={(event) => handleLineChange(setOutputs, index, 'rate', event.target.value)}
                              />
                            </Grid>
                            <Grid size={{ xs: 12, md: 3 }}>
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
                          <Divider />
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

            <Stack direction="row" spacing={1} justifyContent="flex-end">
              <Button variant="contained" color="secondary" type="submit" disabled={!batchId || posting}>
                {posting ? 'Posting...' : 'Post Run'}
              </Button>
            </Stack>
          </Stack>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Stack spacing={2}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Runs
                </Typography>
                <DataTable
                  columns={runColumns}
                  rows={runs}
                  onRowClick={(row) => setSelectedRunId(row.id)}
                  emptyMessage="No runs recorded yet."
                />
              </CardContent>
            </Card>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Cost Summary
                </Typography>
                {costSummary ? (
                  <Stack spacing={1}>
                    <Typography variant="body2">Total Input Qty: {costSummary.totalInputQty}</Typography>
                    <Typography variant="body2">Total Input Amount: {costSummary.totalInputAmount}</Typography>
                    <Typography variant="body2">Output Qty (alloc): {costSummary.totalOutputQty}</Typography>
                    <Typography variant="body2">Output Amount: {costSummary.totalOutputAmount}</Typography>
                    <Typography variant="body2">Yield %: {costSummary.yieldPercent}</Typography>
                    <Typography variant="body2">Moisture %: {costSummary.moisturePercent ?? '-'}</Typography>
                    <Typography variant="body2">Shrink %: {costSummary.shrinkPercent}</Typography>
                    <Typography variant="body2">Unit Cost: {costSummary.unitCost}</Typography>
                  </Stack>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    Select a run to view cost summary.
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Stack>
        </Grid>
      </Grid>
    </MainCard>
  );
}
