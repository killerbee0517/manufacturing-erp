import { useEffect, useMemo, useState } from 'react';

import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';
import { productionApi } from 'api/production';

const createInputLine = () => ({
  itemId: '',
  uomId: '',
  qty: '',
  sourceType: 'GODOWN',
  godownId: '',
  sourceRefId: ''
});

const createOutputLine = () => ({
  itemId: '',
  uomId: '',
  qty: '',
  outputType: 'WIP',
  destGodownId: ''
});

export default function ProductionBatchesPage() {
  const [templates, setTemplates] = useState([]);
  const [batches, setBatches] = useState([]);
  const [selectedBatch, setSelectedBatch] = useState(null);
  const [items, setItems] = useState([]);
  const [uoms, setUoms] = useState([]);
  const [godowns, setGodowns] = useState([]);
  const [costSummary, setCostSummary] = useState(null);
  const [tab, setTab] = useState('runs');
  const [loading, setLoading] = useState(false);
  const [creatingBatch, setCreatingBatch] = useState(false);
  const [formValues, setFormValues] = useState({ templateId: '', plannedOutputQty: '', uomId: '' });
  const [runForm, setRunForm] = useState({ stepNo: '', runDate: '', notes: '' });
  const [runInputs, setRunInputs] = useState([createInputLine()]);
  const [runOutputs, setRunOutputs] = useState([createOutputLine()]);
  const [runs, setRuns] = useState([]);
  const [batchWip, setBatchWip] = useState([]);

  const columns = [
    { field: 'batchNo', headerName: 'Batch No' },
    { field: 'templateName', headerName: 'Template' },
    {
      field: 'status',
      headerName: 'Status',
      render: (row) => <Chip label={row.status} color={row.status === 'COMPLETED' ? 'success' : 'default'} size="small" />
    },
    { field: 'plannedOutputQty', headerName: 'Planned Qty' }
  ];

  const loadMasters = () => {
    apiClient.get('/api/items').then((response) => setItems(response.data || []));
    apiClient.get('/api/uoms').then((response) => setUoms(response.data || []));
    apiClient.get('/api/godowns').then((response) => setGodowns(response.data || []));
  };

  const loadTemplates = () => {
    productionApi
      .listTemplates()
      .then((response) => setTemplates(response.data || []))
      .catch(() => setTemplates([]));
  };

  const loadBatches = () => {
    setLoading(true);
    productionApi
      .listBatches()
      .then((response) => {
        const rows = response.data || [];
        setBatches(rows);
        if (rows.length > 0) {
          loadBatchDetail(rows[0].id);
        }
      })
      .catch(() => setBatches([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadMasters();
    loadTemplates();
    loadBatches();
  }, []);

  const loadRuns = (batchId) => {
    productionApi
      .listBatchRuns(batchId)
      .then((res) => setRuns(res.data || []))
      .catch(() => setRuns([]));
  };

  const loadBatchWip = (batchId) => {
    productionApi
      .listBatchWip(batchId)
      .then((res) => setBatchWip(res.data || []))
      .catch(() => setBatchWip([]));
  };

  const loadBatchDetail = (id) => {
    if (!id) return;
    productionApi.getBatch(id).then((response) => {
      setSelectedBatch(response.data);
      setTab('runs');
      productionApi
        .getCostSummary(id)
        .then((res) => setCostSummary(res.data))
        .catch(() => setCostSummary(null));
      loadRuns(id);
      loadBatchWip(id);
    });
  };

  const handleCreateBatch = async (event) => {
    event.preventDefault();
    setCreatingBatch(true);
    try {
      await productionApi.createBatch({
        templateId: Number(formValues.templateId),
        plannedOutputQty: formValues.plannedOutputQty ? Number(formValues.plannedOutputQty) : null,
        uomId: formValues.uomId ? Number(formValues.uomId) : null
      });
      setFormValues({ templateId: '', plannedOutputQty: '', uomId: '' });
      loadBatches();
    } finally {
      setCreatingBatch(false);
    }
  };

  const handleLineChange = (setter, index, field, value) => {
    setter((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
  };

  const handleAddLine = (setter, createFn) => setter((prev) => [...prev, createFn()]);
  const handleRemoveLine = (setter, index) => setter((prev) => prev.filter((_, idx) => idx !== index));

  const startBatch = async () => {
    await productionApi.startBatch(selectedBatch.id);
    loadBatchDetail(selectedBatch.id);
  };

  const completeBatch = async () => {
    await productionApi.completeBatch(selectedBatch.id);
    loadBatchDetail(selectedBatch.id);
  };

  const handleCreateRun = async (event) => {
    event.preventDefault();
    if (!selectedBatch) return;
    const stepNameFromTemplate =
      runForm.stepNo && selectedTemplate
        ? selectedTemplate.steps?.find((step) => step.stepNo === Number(runForm.stepNo))?.stepName
        : null;
    const payload = {
      stepNo: runForm.stepNo ? Number(runForm.stepNo) : null,
      stepName: stepNameFromTemplate,
      notes: runForm.notes || null,
      runDate: runForm.runDate || null,
      inputs: runInputs
        .filter((line) => line.itemId && line.uomId && line.qty)
        .map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          qty: Number(line.qty),
          sourceType: line.sourceType,
          sourceRefId: line.sourceType === 'WIP' && line.sourceRefId ? Number(line.sourceRefId) : null,
          godownId: line.sourceType === 'GODOWN' && line.godownId ? Number(line.godownId) : null
        })),
      outputs: runOutputs
        .filter((line) => line.itemId && line.uomId && line.qty)
        .map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          qty: Number(line.qty),
          outputType: line.outputType,
          destGodownId: line.outputType === 'FG' && line.destGodownId ? Number(line.destGodownId) : null
        }))
    };
    await productionApi.createRun(selectedBatch.id, payload);
    setRunForm({ stepNo: '', runDate: '', notes: '' });
    setRunInputs([createInputLine()]);
    setRunOutputs([createOutputLine()]);
    loadRuns(selectedBatch.id);
    loadBatchWip(selectedBatch.id);
  };

  const handlePostRun = async (runId) => {
    await productionApi.postRun(runId);
    loadRuns(selectedBatch.id);
    loadBatchWip(selectedBatch.id);
  };

  const selectedTemplate = useMemo(
    () => templates.find((t) => t.id === selectedBatch?.templateId),
    [templates, selectedBatch]
  );

  return (
    <MainCard>
      <PageHeader title="Production Batches" breadcrumbs={[{ label: 'Production' }, { label: 'Batches' }]} />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Stack spacing={2}>
            <DataTable
              columns={columns}
              rows={batches}
              loading={loading}
              onRowClick={(row) => loadBatchDetail(row.id)}
              emptyMessage="No batches found."
            />
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  New Batch
                </Typography>
                <Stack spacing={2} component="form" onSubmit={handleCreateBatch}>
                  <TextField
                    select
                    fullWidth
                    label="Template"
                    value={formValues.templateId}
                    onChange={(event) => setFormValues((prev) => ({ ...prev, templateId: event.target.value }))}
                    required
                  >
                    {templates.map((template) => (
                      <MenuItem key={template.id} value={template.id}>
                        {template.name}
                      </MenuItem>
                    ))}
                  </TextField>
                  <TextField
                    label="Planned Output Qty"
                    type="number"
                    value={formValues.plannedOutputQty}
                    onChange={(event) => setFormValues((prev) => ({ ...prev, plannedOutputQty: event.target.value }))}
                  />
                  <TextField
                    select
                    fullWidth
                    label="Output UOM"
                    value={formValues.uomId}
                    onChange={(event) => setFormValues((prev) => ({ ...prev, uomId: event.target.value }))}
                  >
                    <MenuItem value="">From Template</MenuItem>
                    {uoms.map((uom) => (
                      <MenuItem key={uom.id} value={uom.id}>
                        {uom.code}
                      </MenuItem>
                    ))}
                  </TextField>
                  <Button variant="contained" color="secondary" type="submit" disabled={creatingBatch}>
                    {creatingBatch ? 'Creating...' : 'Create Batch'}
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </Grid>
        <Grid size={{ xs: 12, md: 8 }}>
          {selectedBatch ? (
            <Stack spacing={2}>
              <Card variant="outlined">
                <CardContent>
                  <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={2}>
                    <div>
                      <Typography variant="h6">{selectedBatch.batchNo}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Template: {selectedBatch.templateName || 'N/A'}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Status: {selectedBatch.status}
                      </Typography>
                    </div>
                    <Stack direction="row" spacing={1}>
                      {selectedBatch.status === 'DRAFT' && (
                        <Button variant="outlined" onClick={startBatch}>
                          Start
                        </Button>
                      )}
                      {selectedBatch.status !== 'COMPLETED' && (
                        <Button variant="contained" color="secondary" onClick={completeBatch}>
                          Complete Batch
                        </Button>
                      )}
                    </Stack>
                  </Stack>
                </CardContent>
              </Card>
              <Card variant="outlined">
                <Tabs value={tab} onChange={(_, value) => setTab(value)} variant="scrollable" scrollButtons="auto">
                  <Tab value="runs" label="Runs" />
                  <Tab value="steps" label="Steps" />
                  <Tab value="summary" label="Summary" />
                </Tabs>
                <Divider />
                <CardContent>
                  {tab === 'runs' && (
                    <Stack spacing={3} component="form" onSubmit={handleCreateRun}>
                      <Stack spacing={1}>
                        <Typography variant="h6">New Run</Typography>
                        <Grid container spacing={2}>
                          <Grid size={{ xs: 12, md: 4 }}>
                            <TextField
                              select
                              fullWidth
                              label="Step"
                              value={runForm.stepNo}
                              onChange={(event) => setRunForm((prev) => ({ ...prev, stepNo: event.target.value }))}
                            >
                              <MenuItem value="">Select Step</MenuItem>
                              {selectedTemplate?.steps?.map((step) => (
                                <MenuItem key={step.id} value={step.stepNo}>
                                  {step.stepNo}. {step.stepName}
                                </MenuItem>
                              ))}
                            </TextField>
                          </Grid>
                          <Grid size={{ xs: 12, md: 4 }}>
                            <TextField
                              fullWidth
                              label="Run Date"
                              type="date"
                              value={runForm.runDate}
                              onChange={(event) => setRunForm((prev) => ({ ...prev, runDate: event.target.value }))}
                              InputLabelProps={{ shrink: true }}
                            />
                          </Grid>
                          <Grid size={{ xs: 12, md: 4 }}>
                            <TextField
                              fullWidth
                              label="Notes"
                              value={runForm.notes}
                              onChange={(event) => setRunForm((prev) => ({ ...prev, notes: event.target.value }))}
                            />
                          </Grid>
                        </Grid>
                      </Stack>
                      <Grid container spacing={3}>
                        <Grid size={{ xs: 12, md: 6 }}>
                          <Card variant="outlined">
                            <CardContent>
                              <Stack spacing={2}>
                                <Typography variant="h6">Inputs</Typography>
                                {runInputs.map((line, index) => (
                                  <Stack key={`input-${index}`} spacing={1}>
                                    <TextField
                                      select
                                      fullWidth
                                      label="Item"
                                      value={line.itemId}
                                      onChange={(event) => handleLineChange(setRunInputs, index, 'itemId', event.target.value)}
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
                                          onChange={(event) => handleLineChange(setRunInputs, index, 'uomId', event.target.value)}
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
                                          value={line.qty}
                                          onChange={(event) => handleLineChange(setRunInputs, index, 'qty', event.target.value)}
                                        />
                                      </Grid>
                                      <Grid size={{ xs: 12, md: 4 }}>
                                        <TextField
                                          select
                                          fullWidth
                                          label="Source Type"
                                          value={line.sourceType}
                                          onChange={(event) => handleLineChange(setRunInputs, index, 'sourceType', event.target.value)}
                                        >
                                          <MenuItem value="GODOWN">Godown</MenuItem>
                                          <MenuItem value="WIP">WIP</MenuItem>
                                        </TextField>
                                      </Grid>
                                    </Grid>
                                    <Grid container spacing={1}>
                                      {line.sourceType === 'GODOWN' ? (
                                        <Grid size={{ xs: 12, md: 6 }}>
                                          <TextField
                                            select
                                            fullWidth
                                            label="Godown"
                                            value={line.godownId}
                                            onChange={(event) => handleLineChange(setRunInputs, index, 'godownId', event.target.value)}
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
                                        <Grid size={{ xs: 12, md: 6 }}>
                                          <TextField
                                            select
                                            fullWidth
                                            label="WIP Source"
                                            value={line.sourceRefId}
                                            onChange={(event) => handleLineChange(setRunInputs, index, 'sourceRefId', event.target.value)}
                                          >
                                            <MenuItem value="">Select</MenuItem>
                                            {batchWip.map((wip) => (
                                              <MenuItem key={wip.id} value={wip.id}>
                                                {wip.itemName} (Avail: {wip.availableQuantity})
                                              </MenuItem>
                                            ))}
                                          </TextField>
                                        </Grid>
                                      )}
                                    </Grid>
                                    {runInputs.length > 1 && (
                                      <Button variant="text" color="error" onClick={() => handleRemoveLine(setRunInputs, index)}>
                                        Remove
                                      </Button>
                                    )}
                                    <Divider />
                                  </Stack>
                                ))}
                                <Button variant="outlined" onClick={() => handleAddLine(setRunInputs, createInputLine)}>
                                  Add Input
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
                                {runOutputs.map((line, index) => (
                                  <Stack key={`output-${index}`} spacing={1}>
                                    <TextField
                                      select
                                      fullWidth
                                      label="Item"
                                      value={line.itemId}
                                      onChange={(event) => handleLineChange(setRunOutputs, index, 'itemId', event.target.value)}
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
                                          onChange={(event) => handleLineChange(setRunOutputs, index, 'uomId', event.target.value)}
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
                                          value={line.qty}
                                          onChange={(event) => handleLineChange(setRunOutputs, index, 'qty', event.target.value)}
                                        />
                                      </Grid>
                                      <Grid size={{ xs: 12, md: 4 }}>
                                        <TextField
                                          select
                                          fullWidth
                                          label="Output Type"
                                          value={line.outputType}
                                          onChange={(event) => handleLineChange(setRunOutputs, index, 'outputType', event.target.value)}
                                        >
                                          <MenuItem value="WIP">WIP</MenuItem>
                                          <MenuItem value="FG">Finished</MenuItem>
                                          <MenuItem value="BYPRODUCT">Byproduct</MenuItem>
                                        </TextField>
                                      </Grid>
                                    </Grid>
                                    <Grid container spacing={1}>
                                      <Grid size={{ xs: 12, md: 6 }}>
                                        <TextField
                                          select
                                          fullWidth
                                          label="Destination Godown"
                                          value={line.destGodownId}
                                          onChange={(event) => handleLineChange(setRunOutputs, index, 'destGodownId', event.target.value)}
                                          disabled={line.outputType === 'WIP'}
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
                                    {runOutputs.length > 1 && (
                                      <Button variant="text" color="error" onClick={() => handleRemoveLine(setRunOutputs, index)}>
                                        Remove
                                      </Button>
                                    )}
                                    <Divider />
                                  </Stack>
                                ))}
                                <Button variant="outlined" onClick={() => handleAddLine(setRunOutputs, createOutputLine)}>
                                  Add Output
                                </Button>
                              </Stack>
                            </CardContent>
                          </Card>
                        </Grid>
                      </Grid>
                      <Stack direction="row" spacing={1} justifyContent="flex-end">
                        <Button type="submit" variant="contained" color="secondary" disabled={!selectedBatch}>
                          Save Run
                        </Button>
                      </Stack>
                      <Divider />
                      <Stack spacing={2}>
                        <Typography variant="subtitle1">Runs</Typography>
                        {runs.length === 0 && (
                          <Typography variant="body2" color="text.secondary">
                            No runs recorded yet.
                          </Typography>
                        )}
                        {runs.map((run) => (
                          <Card key={run.id} variant="outlined">
                            <CardContent>
                              <Stack direction="row" justifyContent="space-between" alignItems="center">
                                <div>
                                  <Typography variant="subtitle1">Run #{run.runNo || run.id}</Typography>
                                  <Typography variant="body2" color="text.secondary">
                                    Step: {run.stepNo ? `${run.stepNo}. ${run.stepName || ''}` : run.stepName || 'Adhoc'} | Status: {run.status}
                                  </Typography>
                                  <Typography variant="body2" color="text.secondary">
                                    Run Date: {run.runDate || '-'}
                                  </Typography>
                                </div>
                                {run.status !== 'COMPLETED' && (
                                  <Button variant="contained" size="small" onClick={() => handlePostRun(run.id)}>
                                    Post Run
                                  </Button>
                                )}
                              </Stack>
                              <Divider sx={{ my: 1 }} />
                              <Grid container spacing={2}>
                                <Grid size={{ xs: 12, md: 6 }}>
                                  <Typography variant="subtitle2">Inputs</Typography>
                                  <Stack spacing={0.5}>
                                    {run.inputs?.map((input) => (
                                      <Typography key={input.id} variant="body2">
                                        {input.itemName} - {input.qty} {input.uomCode} ({input.sourceType})
                                      </Typography>
                                    ))}
                                  </Stack>
                                </Grid>
                                <Grid size={{ xs: 12, md: 6 }}>
                                  <Typography variant="subtitle2">Outputs</Typography>
                                  <Stack spacing={0.5}>
                                    {run.outputs?.map((output) => (
                                      <Typography key={output.id} variant="body2">
                                        {output.itemName} - {output.qty} {output.uomCode} ({output.outputType})
                                      </Typography>
                                    ))}
                                  </Stack>
                                </Grid>
                              </Grid>
                            </CardContent>
                          </Card>
                        ))}
                      </Stack>
                    </Stack>
                  )}
                  {tab === 'steps' && (
                    <Stack spacing={1}>
                      {(selectedTemplate?.steps || []).map((step) => (
                        <Stack key={step.id || step.stepNo} direction="row" alignItems="center" justifyContent="space-between">
                          <Typography>
                            {step.stepNo}. {step.stepName}
                          </Typography>
                        </Stack>
                      ))}
                    </Stack>
                  )}
                  {tab === 'summary' && (
                    <Stack spacing={1}>
                      <Typography variant="body2">Consumption Qty: {costSummary?.totalConsumptionQty ?? '-'}</Typography>
                      <Typography variant="body2">Output Qty: {costSummary?.totalOutputQty ?? '-'}</Typography>
                      <Typography variant="body2">Unit Cost: {costSummary?.unitCost ?? '-'}</Typography>
                    </Stack>
                  )}
                </CardContent>
              </Card>
            </Stack>
          ) : (
            <Typography variant="body2" color="text.secondary">
              Select or create a batch to begin.
            </Typography>
          )}
        </Grid>
      </Grid>
    </MainCard>
  );
}
