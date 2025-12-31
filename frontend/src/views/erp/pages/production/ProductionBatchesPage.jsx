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
  const [tab, setTab] = useState('issue');
  const [loading, setLoading] = useState(false);
  const [creatingBatch, setCreatingBatch] = useState(false);
  const [formValues, setFormValues] = useState({ templateId: '', plannedOutputQty: '', uomId: '' });
  const [issueInputs, setIssueInputs] = useState([createInputLine()]);
  const [outputLines, setOutputLines] = useState([createOutputLine()]);
  const [batchWip, setBatchWip] = useState([]);

  const columns = [
    { field: 'batchNo', headerName: 'Batch No' },
    { field: 'templateName', headerName: 'Template' },
    {
      field: 'currentStep',
      headerName: 'Current Step',
      render: (row) => {
        const pending = row.steps?.find((step) => step.status === 'PENDING');
        if (pending) {
          return `${pending.stepNo}. ${pending.stepName}`;
        }
        return row.status === 'COMPLETED' ? 'Completed' : 'No pending steps';
      }
    },
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

  const loadBatches = (preferredId) => {
    setLoading(true);
    productionApi
      .listBatches()
      .then((response) => {
        const rows = response.data || [];
        setBatches(rows);
        const nextId = preferredId || selectedBatch?.id || rows[0]?.id;
        if (nextId) {
          loadBatchDetail(nextId);
        } else {
          setSelectedBatch(null);
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
      setTab('issue');
      productionApi
        .getCostSummary(id)
        .then((res) => setCostSummary(res.data))
        .catch(() => setCostSummary(null));
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
    loadBatches(selectedBatch.id);
  };

  const completeBatch = async () => {
    await productionApi.completeBatch(selectedBatch.id);
    loadBatches(selectedBatch.id);
  };

  const handleIssueMaterials = async (event) => {
    event.preventDefault();
    if (!selectedBatch || !currentStep) return;
    const payload = {
      inputs: issueInputs
        .filter((line) => line.itemId && line.uomId && line.qty)
        .map((line) => ({
          stepNo: currentStep.stepNo,
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          qty: Number(line.qty),
          sourceType: line.sourceType,
          sourceRefId: line.sourceType === 'WIP' && line.sourceRefId ? Number(line.sourceRefId) : null,
          sourceGodownId: line.sourceType === 'GODOWN' && line.godownId ? Number(line.godownId) : null
        }))
    };
    await productionApi.issueBatch(selectedBatch.id, payload);
    setIssueInputs([createInputLine()]);
    loadBatches(selectedBatch.id);
  };

  const handleProduceOutput = async (event) => {
    event.preventDefault();
    if (!selectedBatch || !currentStep) return;
    const payload = {
      outputs: outputLines
        .filter((line) => line.itemId && line.uomId && line.qty)
        .map((line) => ({
          stepNo: currentStep.stepNo,
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          qty: Number(line.qty),
          outputType: line.outputType,
          destinationGodownId: line.outputType !== 'WIP' && line.destGodownId ? Number(line.destGodownId) : null
        }))
    };
    await productionApi.produceOutput(selectedBatch.id, payload);
    setOutputLines([createOutputLine()]);
    loadBatches(selectedBatch.id);
  };

  const selectedTemplate = useMemo(
    () => templates.find((t) => t.id === selectedBatch?.templateId),
    [templates, selectedBatch]
  );
  const currentStep = useMemo(
    () => selectedBatch?.steps?.find((step) => step.status === 'PENDING') || null,
    [selectedBatch]
  );
  const canWorkOnStep = selectedBatch?.status === 'RUNNING' && currentStep;

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
                      <Typography variant="body2" color="text.secondary">
                        Current Step: {currentStep ? `${currentStep.stepNo}. ${currentStep.stepName}` : 'No pending steps'}
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
                  <Tab value="issue" label="Issue" />
                  <Tab value="output" label="Output" />
                  <Tab value="steps" label="Steps" />
                  <Tab value="summary" label="Summary" />
                </Tabs>
                <Divider />
                <CardContent>
                  {tab === 'issue' && (
                    <Stack spacing={3} component="form" onSubmit={handleIssueMaterials}>
                      <Stack spacing={1}>
                        <Typography variant="h6">Issue Materials</Typography>
                        <Typography variant="body2" color="text.secondary">
                          Current Step: {currentStep ? `${currentStep.stepNo}. ${currentStep.stepName}` : 'No pending steps'}
                        </Typography>
                      </Stack>
                      <Card variant="outlined">
                        <CardContent>
                          <Stack spacing={2}>
                            {issueInputs.map((line, index) => (
                              <Stack key={`input-${index}`} spacing={1}>
                                <TextField
                                  select
                                  fullWidth
                                  label="Item"
                                  value={line.itemId}
                                  onChange={(event) => handleLineChange(setIssueInputs, index, 'itemId', event.target.value)}
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
                                      onChange={(event) => handleLineChange(setIssueInputs, index, 'uomId', event.target.value)}
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
                                      onChange={(event) => handleLineChange(setIssueInputs, index, 'qty', event.target.value)}
                                    />
                                  </Grid>
                                  <Grid size={{ xs: 12, md: 4 }}>
                                    <TextField
                                      select
                                      fullWidth
                                      label="Source Type"
                                      value={line.sourceType}
                                      onChange={(event) => handleLineChange(setIssueInputs, index, 'sourceType', event.target.value)}
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
                                        onChange={(event) => handleLineChange(setIssueInputs, index, 'godownId', event.target.value)}
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
                                        onChange={(event) => handleLineChange(setIssueInputs, index, 'sourceRefId', event.target.value)}
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
                                {issueInputs.length > 1 && (
                                  <Button variant="text" color="error" onClick={() => handleRemoveLine(setIssueInputs, index)}>
                                    Remove
                                  </Button>
                                )}
                                <Divider />
                              </Stack>
                            ))}
                            <Button variant="outlined" onClick={() => handleAddLine(setIssueInputs, createInputLine)}>
                              Add Input
                            </Button>
                          </Stack>
                        </CardContent>
                      </Card>
                      <Stack direction="row" spacing={1} justifyContent="flex-end">
                        <Button type="submit" variant="contained" color="secondary" disabled={!canWorkOnStep}>
                          Issue Materials
                        </Button>
                      </Stack>
                    </Stack>
                  )}
                  {tab === 'output' && (
                    <Stack spacing={3} component="form" onSubmit={handleProduceOutput}>
                      <Stack spacing={1}>
                        <Typography variant="h6">Record Output</Typography>
                        <Typography variant="body2" color="text.secondary">
                          Current Step: {currentStep ? `${currentStep.stepNo}. ${currentStep.stepName}` : 'No pending steps'}
                        </Typography>
                      </Stack>
                      <Card variant="outlined">
                        <CardContent>
                          <Stack spacing={2}>
                            {outputLines.map((line, index) => (
                              <Stack key={`output-${index}`} spacing={1}>
                                <TextField
                                  select
                                  fullWidth
                                  label="Item"
                                  value={line.itemId}
                                  onChange={(event) => handleLineChange(setOutputLines, index, 'itemId', event.target.value)}
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
                                      onChange={(event) => handleLineChange(setOutputLines, index, 'uomId', event.target.value)}
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
                                      onChange={(event) => handleLineChange(setOutputLines, index, 'qty', event.target.value)}
                                    />
                                  </Grid>
                                  <Grid size={{ xs: 12, md: 4 }}>
                                    <TextField
                                      select
                                      fullWidth
                                      label="Output Type"
                                      value={line.outputType}
                                      onChange={(event) => handleLineChange(setOutputLines, index, 'outputType', event.target.value)}
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
                                      onChange={(event) => handleLineChange(setOutputLines, index, 'destGodownId', event.target.value)}
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
                                {outputLines.length > 1 && (
                                  <Button variant="text" color="error" onClick={() => handleRemoveLine(setOutputLines, index)}>
                                    Remove
                                  </Button>
                                )}
                                <Divider />
                              </Stack>
                            ))}
                            <Button variant="outlined" onClick={() => handleAddLine(setOutputLines, createOutputLine)}>
                              Add Output
                            </Button>
                          </Stack>
                        </CardContent>
                      </Card>
                      <Stack direction="row" spacing={1} justifyContent="flex-end">
                        <Button type="submit" variant="contained" color="secondary" disabled={!canWorkOnStep}>
                          Record Output
                        </Button>
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
                          <Chip
                            label={selectedBatch?.steps?.find((batchStep) => batchStep.stepNo === step.stepNo)?.status || 'PENDING'}
                            size="small"
                          />
                        </Stack>
                      ))}
                    </Stack>
                  )}
                  {tab === 'summary' && (
                    <Stack spacing={2}>
                      <Typography variant="body2">Consumption Qty: {costSummary?.totalConsumptionQty ?? '-'}</Typography>
                      <Typography variant="body2">Output Qty: {costSummary?.totalOutputQty ?? '-'}</Typography>
                      <Typography variant="body2">Unit Cost: {costSummary?.unitCost ?? '-'}</Typography>
                      <Divider />
                      <Typography variant="subtitle2">Inputs</Typography>
                      {(selectedBatch?.inputs || []).length === 0 && (
                        <Typography variant="body2" color="text.secondary">
                          No inputs recorded.
                        </Typography>
                      )}
                      {(selectedBatch?.inputs || []).map((input) => (
                        <Typography key={input.id} variant="body2">
                          Step {input.stepNo}: {input.itemName} - {input.qty} {input.uomCode} ({input.sourceType})
                        </Typography>
                      ))}
                      <Divider />
                      <Typography variant="subtitle2">Outputs</Typography>
                      {(selectedBatch?.outputs || []).length === 0 && (
                        <Typography variant="body2" color="text.secondary">
                          No outputs recorded.
                        </Typography>
                      )}
                      {(selectedBatch?.outputs || []).map((output) => (
                        <Typography key={output.id} variant="body2">
                          Step {output.stepNo}: {output.itemName} - {output.qty} {output.uomCode} ({output.outputType})
                        </Typography>
                      ))}
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
