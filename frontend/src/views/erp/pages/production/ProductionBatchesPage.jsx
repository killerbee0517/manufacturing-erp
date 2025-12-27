import { useEffect, useMemo, useState } from 'react';

import { LoadingButton } from '@mui/lab';
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

const createIssueLine = () => ({
  itemId: '',
  uomId: '',
  qty: '',
  sourceType: 'GODOWN',
  sourceGodownId: '',
  sourceRefId: '',
  issuedAt: ''
});

const createOutputLine = () => ({
  itemId: '',
  uomId: '',
  qty: '',
  outputType: 'WIP',
  destinationGodownId: '',
  producedAt: ''
});

export default function ProductionBatchesPage() {
  const [templates, setTemplates] = useState([]);
  const [batches, setBatches] = useState([]);
  const [selectedBatch, setSelectedBatch] = useState(null);
  const [items, setItems] = useState([]);
  const [uoms, setUoms] = useState([]);
  const [godowns, setGodowns] = useState([]);
  const [wipBalances, setWipBalances] = useState([]);
  const [costSummary, setCostSummary] = useState(null);
  const [tab, setTab] = useState('inputs');
  const [loading, setLoading] = useState(false);
  const [creatingBatch, setCreatingBatch] = useState(false);
  const [formValues, setFormValues] = useState({ templateId: '', plannedOutputQty: '', uomId: '' });
  const [issueLines, setIssueLines] = useState([createIssueLine()]);
  const [outputLines, setOutputLines] = useState([createOutputLine()]);

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
    apiClient
      .get('/api/items')
      .then((response) => setItems(response.data || []));
    apiClient
      .get('/api/uoms')
      .then((response) => setUoms(response.data || []));
    apiClient
      .get('/api/godowns')
      .then((response) => setGodowns(response.data || []));
    productionApi
      .listWipBalances()
      .then((response) => setWipBalances(response.data || []))
      .catch(() => setWipBalances([]));
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

  const loadBatchDetail = (id) => {
    if (!id) return;
    productionApi
      .getBatch(id)
      .then((response) => {
        setSelectedBatch(response.data);
        setTab('inputs');
        productionApi
          .getCostSummary(id)
          .then((res) => setCostSummary(res.data))
          .catch(() => setCostSummary(null));
        productionApi
          .listWipOutputs(id)
          .then((res) => setWipBalances(res.data || []))
          .catch(() => setWipBalances([]));
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

  const handleIssue = async (event) => {
    event.preventDefault();
    await productionApi.issueBatch(selectedBatch.id, {
      inputs: issueLines
        .filter((line) => line.itemId && line.uomId && line.qty)
        .map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          qty: Number(line.qty),
          sourceType: line.sourceType,
          sourceGodownId: line.sourceType === 'GODOWN' && line.sourceGodownId ? Number(line.sourceGodownId) : null,
          sourceRefId: line.sourceType === 'WIP' && line.sourceRefId ? Number(line.sourceRefId) : null,
          issuedAt: line.issuedAt || null
        }))
    });
    setIssueLines([createIssueLine()]);
    loadBatchDetail(selectedBatch.id);
  };

  const handleProduce = async (event) => {
    event.preventDefault();
    await productionApi.produceOutput(selectedBatch.id, {
      outputs: outputLines
        .filter((line) => line.itemId && line.uomId && line.qty)
        .map((line) => ({
          itemId: Number(line.itemId),
          uomId: Number(line.uomId),
          qty: Number(line.qty),
          outputType: line.outputType,
          destinationGodownId: line.outputType === 'FG' && line.destinationGodownId ? Number(line.destinationGodownId) : null,
          producedAt: line.producedAt || null
        }))
    });
    setOutputLines([createOutputLine()]);
    loadBatchDetail(selectedBatch.id);
  };

  const startBatch = async () => {
    await productionApi.startBatch(selectedBatch.id);
    loadBatchDetail(selectedBatch.id);
  };

  const completeBatch = async () => {
    await productionApi.completeBatch(selectedBatch.id);
    loadBatchDetail(selectedBatch.id);
  };

  const selectedTemplate = useMemo(
    () => templates.find((t) => t.id === selectedBatch?.templateId),
    [templates, selectedBatch]
  );

  const markStepDone = async (stepNo) => {
    await productionApi.completeStep(selectedBatch.id, stepNo, {});
    loadBatchDetail(selectedBatch.id);
  };

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
                  <LoadingButton loading={creatingBatch} variant="contained" color="secondary" type="submit">
                    Create Batch
                  </LoadingButton>
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
                  <Tab value="inputs" label="Inputs" />
                  <Tab value="steps" label="Steps" />
                  <Tab value="outputs" label="Outputs" />
                  <Tab value="summary" label="Summary" />
                </Tabs>
                <Divider />
                <CardContent>
                  {tab === 'inputs' && (
                    <Stack spacing={2} component="form" onSubmit={handleIssue}>
                      {issueLines.map((line, index) => (
                        <Stack key={`issue-${index}`} spacing={1}>
                          <TextField
                            select
                            fullWidth
                            label="Item"
                            value={line.itemId}
                            onChange={(event) => handleLineChange(setIssueLines, index, 'itemId', event.target.value)}
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
                                onChange={(event) => handleLineChange(setIssueLines, index, 'uomId', event.target.value)}
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
                                onChange={(event) => handleLineChange(setIssueLines, index, 'qty', event.target.value)}
                              />
                            </Grid>
                            <Grid size={{ xs: 12, md: 4 }}>
                              <TextField
                                fullWidth
                                label="Issued At"
                                type="datetime-local"
                                value={line.issuedAt}
                                onChange={(event) => handleLineChange(setIssueLines, index, 'issuedAt', event.target.value)}
                                InputLabelProps={{ shrink: true }}
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
                                onChange={(event) => handleLineChange(setIssueLines, index, 'sourceType', event.target.value)}
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
                                  onChange={(event) =>
                                    handleLineChange(setIssueLines, index, 'sourceGodownId', event.target.value)
                                  }
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
                                  label="WIP Source"
                                  value={line.sourceRefId}
                                  onChange={(event) => handleLineChange(setIssueLines, index, 'sourceRefId', event.target.value)}
                                >
                                  <MenuItem value="">Select</MenuItem>
                                  {wipBalances.map((wip) => (
                                    <MenuItem key={wip.id} value={wip.id}>
                                      {wip.itemName} (Avail: {wip.availableQuantity})
                                    </MenuItem>
                                  ))}
                                </TextField>
                              </Grid>
                            )}
                          </Grid>
                          {issueLines.length > 1 && (
                            <Button variant="text" color="error" onClick={() => handleRemoveLine(setIssueLines, index)}>
                              Remove
                            </Button>
                          )}
                          <Divider />
                        </Stack>
                      ))}
                      <Stack direction="row" spacing={1}>
                        <Button variant="outlined" onClick={() => handleAddLine(setIssueLines, createIssueLine)}>
                          Add Line
                        </Button>
                        <Button type="submit" variant="contained" color="secondary" disabled={!selectedBatch}>
                          Issue Materials
                        </Button>
                      </Stack>
                      <Divider />
                      <Typography variant="subtitle2">Issued so far</Typography>
                      <Stack spacing={1}>
                        {selectedBatch.inputs?.map((input) => (
                          <Typography key={input.id} variant="body2">
                            {input.itemName} - {input.qty} {input.uomCode} ({input.sourceType})
                          </Typography>
                        ))}
                      </Stack>
                    </Stack>
                  )}
                  {tab === 'steps' && (
                    <Stack spacing={1}>
                      {(selectedBatch.steps || selectedTemplate?.steps || []).map((step) => (
                        <Stack
                          key={step.id || step.stepNo}
                          direction="row"
                          alignItems="center"
                          justifyContent="space-between"
                        >
                          <Typography>
                            {step.stepNo}. {step.stepName} ({step.status || 'PENDING'})
                          </Typography>
                          {step.status === 'PENDING' && (
                            <Button variant="outlined" size="small" onClick={() => markStepDone(step.stepNo)}>
                              Mark Done
                            </Button>
                          )}
                        </Stack>
                      ))}
                    </Stack>
                  )}
                  {tab === 'outputs' && (
                    <Stack spacing={2} component="form" onSubmit={handleProduce}>
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
                                onChange={(event) =>
                                  handleLineChange(setOutputLines, index, 'outputType', event.target.value)
                                }
                              >
                                <MenuItem value="WIP">WIP</MenuItem>
                                <MenuItem value="FG">Finished</MenuItem>
                              </TextField>
                            </Grid>
                          </Grid>
                          <Grid container spacing={1}>
                            <Grid size={{ xs: 12, md: 6 }}>
                              <TextField
                                select
                                fullWidth
                                label="Destination Godown"
                                value={line.destinationGodownId}
                                onChange={(event) =>
                                  handleLineChange(setOutputLines, index, 'destinationGodownId', event.target.value)
                                }
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
                            <Grid size={{ xs: 12, md: 6 }}>
                              <TextField
                                fullWidth
                                label="Produced At"
                                type="datetime-local"
                                value={line.producedAt}
                                onChange={(event) =>
                                  handleLineChange(setOutputLines, index, 'producedAt', event.target.value)
                                }
                                InputLabelProps={{ shrink: true }}
                              />
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
                      <Stack direction="row" spacing={1}>
                        <Button variant="outlined" onClick={() => handleAddLine(setOutputLines, createOutputLine)}>
                          Add Output
                        </Button>
                        <Button type="submit" variant="contained" color="secondary" disabled={!selectedBatch}>
                          Record Output
                        </Button>
                      </Stack>
                      <Divider />
                      <Typography variant="subtitle2">Outputs so far</Typography>
                      <Stack spacing={1}>
                        {selectedBatch.outputs?.map((output) => (
                          <Typography key={output.id} variant="body2">
                            {output.itemName} - {output.qty} {output.uomCode} ({output.outputType})
                          </Typography>
                        ))}
                      </Stack>
                    </Stack>
                  )}
                  {tab === 'summary' && (
                    <Stack spacing={1}>
                      <Typography variant="body2">
                        Consumption Qty: {costSummary?.totalConsumptionQty ?? '-'}
                      </Typography>
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
