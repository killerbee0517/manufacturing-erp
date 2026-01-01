import { useEffect, useState } from 'react';

import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Stepper from '@mui/material/Stepper';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import { productionApi } from 'api/production';

const createStep = (stepNo = 1) => ({ stepNo, stepName: '', stepType: 'PROCESS', notes: '' });
const createInput = () => ({ itemId: '', uomId: '', defaultQty: '', optional: false, notes: '' });
const createOutput = () => ({ itemId: '', uomId: '', defaultRatio: '', outputType: 'FG', notes: '' });

export default function ProcessTemplatesPage() {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTemplate, setActiveTemplate] = useState(null);
  const [editingTemplateId, setEditingTemplateId] = useState(null);
  const [formValues, setFormValues] = useState({
    code: '',
    name: '',
    description: '',
    outputItemId: '',
    outputUomId: '',
    inputs: [createInput()],
    outputs: [createOutput()],
    steps: [createStep()]
  });

  const columns = [
    { field: 'name', headerName: 'Template Name' },
    { field: 'stepCount', headerName: 'Steps', render: (row) => row.steps?.length || 0 }
  ];

  const loadTemplates = () => {
    setLoading(true);
    productionApi
      .listTemplates()
      .then((response) => {
        const data = response.data || [];
        setTemplates(data);
        setActiveTemplate(data[0] || null);
      })
      .catch(() => setTemplates([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadTemplates();
  }, []);

  const resetForm = () => {
    setFormValues({
      code: '',
      name: '',
      description: '',
      outputItemId: '',
      outputUomId: '',
      inputs: [createInput()],
      outputs: [createOutput()],
      steps: [createStep()]
    });
    setEditingTemplateId(null);
  };

  const loadTemplateForEdit = async (templateId) => {
    if (!templateId) return;
    const response = await productionApi.getTemplate(templateId);
    const template = response.data;
    setFormValues({
      code: template.code || '',
      name: template.name || '',
      description: template.description || '',
      outputItemId: template.outputItemId || '',
      outputUomId: template.outputUomId || '',
      inputs: (template.inputs || []).length
        ? template.inputs.map((input) => ({
            itemId: input.itemId || '',
            uomId: input.uomId || '',
            defaultQty: input.defaultQty ?? '',
            optional: input.optional ?? false,
            notes: input.notes || ''
          }))
        : [createInput()],
      outputs: (template.outputs || []).length
        ? template.outputs.map((output) => ({
            itemId: output.itemId || '',
            uomId: output.uomId || '',
            defaultRatio: output.defaultRatio ?? '',
            outputType: output.outputType || 'FG',
            notes: output.notes || ''
          }))
        : [createOutput()],
      steps: (template.steps || []).length
        ? template.steps.map((step) => ({
            stepNo: step.stepNo || 1,
            stepName: step.stepName || '',
            stepType: step.stepType || 'PROCESS',
            notes: step.notes || ''
          }))
        : [createStep()]
    });
    setEditingTemplateId(template.id);
  };

  const handleStepChange = (index, field, value) => {
    setFormValues((prev) => {
      const updated = [...prev.steps];
      updated[index] = { ...updated[index], [field]: value };
      return { ...prev, steps: updated };
    });
  };

  const handleAddStep = () => {
    setFormValues((prev) => ({
      ...prev,
      steps: [...prev.steps, createStep(prev.steps.length + 1)]
    }));
  };

  const handleRemoveStep = (index) => {
    setFormValues((prev) => ({
      ...prev,
      steps: prev.steps.filter((_, idx) => idx !== index).map((step, idx) => ({ ...step, stepNo: idx + 1 }))
    }));
  };

  const handleCreateOrUpdate = async (event) => {
    event.preventDefault();
    const payload = {
      code: formValues.code || null,
      name: formValues.name,
      description: formValues.description,
      outputItemId: formValues.outputItemId ? Number(formValues.outputItemId) : null,
      outputUomId: formValues.outputUomId ? Number(formValues.outputUomId) : null,
      inputs: formValues.inputs
        .filter((input) => input.itemId && input.uomId && input.defaultQty)
        .map((input) => ({
          itemId: Number(input.itemId),
          uomId: Number(input.uomId),
          defaultQty: Number(input.defaultQty),
          optional: input.optional,
          notes: input.notes || null
        })),
      outputs: formValues.outputs
        .filter((output) => output.itemId && output.uomId && output.defaultRatio)
        .map((output) => ({
          itemId: Number(output.itemId),
          uomId: Number(output.uomId),
          defaultRatio: Number(output.defaultRatio),
          outputType: output.outputType || 'FG',
          notes: output.notes || null
        })),
      steps: formValues.steps.map((step) => ({
        stepNo: Number(step.stepNo || 1),
        stepName: step.stepName,
        stepType: step.stepType || 'PROCESS',
        notes: step.notes || null
      }))
    };
    if (editingTemplateId) {
      await productionApi.updateTemplate(editingTemplateId, payload);
    } else {
      await productionApi.createTemplate(payload);
    }
    resetForm();
    loadTemplates();
  };

  return (
    <MainCard>
      <PageHeader title="Process Templates" breadcrumbs={[{ label: 'Production' }, { label: 'Process Templates' }]} />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 5 }}>
          <Stack spacing={2}>
            <DataTable
              columns={columns}
              rows={templates}
              loading={loading}
              onRowClick={(row) => {
                setActiveTemplate(row);
                loadTemplateForEdit(row.id);
              }}
              emptyMessage="No templates configured."
            />
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {editingTemplateId ? 'Edit Template' : 'New Template'}
                </Typography>
                <Stack spacing={2} component="form" onSubmit={handleCreateOrUpdate}>
                  <TextField
                    label="Code"
                    value={formValues.code}
                    onChange={(event) => setFormValues((prev) => ({ ...prev, code: event.target.value }))}
                  />
                  <TextField
                    label="Template Name"
                    value={formValues.name}
                    onChange={(event) => setFormValues((prev) => ({ ...prev, name: event.target.value }))}
                    required
                  />
                  <TextField
                    label="Description"
                    value={formValues.description}
                    onChange={(event) => setFormValues((prev) => ({ ...prev, description: event.target.value }))}
                  />
                  <Grid container spacing={1}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <MasterAutocomplete
                        label="Output Item"
                        endpoint="/api/items"
                        value={formValues.outputItemId}
                        onChange={(value) => setFormValues((prev) => ({ ...prev, outputItemId: value }))}
                        placeholder="Select item"
                      />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <MasterAutocomplete
                        label="Output UOM"
                        endpoint="/api/uoms"
                        value={formValues.outputUomId}
                        onChange={(value) => setFormValues((prev) => ({ ...prev, outputUomId: value }))}
                        placeholder="Select UOM"
                      />
                    </Grid>
                  </Grid>
                  <Divider />
                  <Typography variant="subtitle1">Default Inputs</Typography>
                  {formValues.inputs.map((input, index) => (
                    <Stack key={`input-${index}`} spacing={1}>
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 6 }}>
                          <MasterAutocomplete
                            label="Item"
                            endpoint="/api/items"
                            value={input.itemId}
                            onChange={(value) => {
                              const updated = [...formValues.inputs];
                              updated[index] = { ...updated[index], itemId: value };
                              setFormValues((prev) => ({ ...prev, inputs: updated }));
                            }}
                            placeholder="Select item"
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 3 }}>
                          <MasterAutocomplete
                            label="UOM"
                            endpoint="/api/uoms"
                            value={input.uomId}
                            onChange={(value) => {
                              const updated = [...formValues.inputs];
                              updated[index] = { ...updated[index], uomId: value };
                              setFormValues((prev) => ({ ...prev, inputs: updated }));
                            }}
                            placeholder="Select UOM"
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 3 }}>
                          <TextField
                            fullWidth
                            label="Default Qty"
                            type="number"
                            value={input.defaultQty}
                            onChange={(event) => {
                              const updated = [...formValues.inputs];
                              updated[index] = { ...updated[index], defaultQty: event.target.value };
                              setFormValues((prev) => ({ ...prev, inputs: updated }));
                            }}
                          />
                        </Grid>
                      </Grid>
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 6 }}>
                          <TextField
                            fullWidth
                            label="Notes"
                            value={input.notes}
                            onChange={(event) => {
                              const updated = [...formValues.inputs];
                              updated[index] = { ...updated[index], notes: event.target.value };
                              setFormValues((prev) => ({ ...prev, inputs: updated }));
                            }}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 6 }}>
                          <TextField
                            select
                            fullWidth
                            label="Optional?"
                            value={input.optional ? 'yes' : 'no'}
                            onChange={(event) => {
                              const updated = [...formValues.inputs];
                              updated[index] = { ...updated[index], optional: event.target.value === 'yes' };
                              setFormValues((prev) => ({ ...prev, inputs: updated }));
                            }}
                          >
                            <MenuItem value="no">No</MenuItem>
                            <MenuItem value="yes">Yes</MenuItem>
                          </TextField>
                        </Grid>
                      </Grid>
                      {formValues.inputs.length > 1 && (
                        <Button
                          variant="text"
                          color="error"
                          onClick={() =>
                            setFormValues((prev) => ({
                              ...prev,
                              inputs: prev.inputs.filter((_, idx) => idx !== index)
                            }))
                          }
                        >
                          Remove Input
                        </Button>
                      )}
                      <Divider />
                    </Stack>
                  ))}
                  <Button
                    variant="outlined"
                    onClick={() =>
                      setFormValues((prev) => ({
                        ...prev,
                        inputs: [...prev.inputs, createInput()]
                      }))
                    }
                  >
                    Add Input
                  </Button>
                  <Divider />
                  <Typography variant="subtitle1">Default Outputs</Typography>
                  {formValues.outputs.map((output, index) => (
                    <Stack key={`output-${index}`} spacing={1}>
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 6 }}>
                          <MasterAutocomplete
                            label="Item"
                            endpoint="/api/items"
                            value={output.itemId}
                            onChange={(value) => {
                              const updated = [...formValues.outputs];
                              updated[index] = { ...updated[index], itemId: value };
                              setFormValues((prev) => ({ ...prev, outputs: updated }));
                            }}
                            placeholder="Select item"
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 3 }}>
                          <MasterAutocomplete
                            label="UOM"
                            endpoint="/api/uoms"
                            value={output.uomId}
                            onChange={(value) => {
                              const updated = [...formValues.outputs];
                              updated[index] = { ...updated[index], uomId: value };
                              setFormValues((prev) => ({ ...prev, outputs: updated }));
                            }}
                            placeholder="Select UOM"
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 3 }}>
                          <TextField
                            fullWidth
                            label="Default Ratio"
                            type="number"
                            value={output.defaultRatio}
                            onChange={(event) => {
                              const updated = [...formValues.outputs];
                              updated[index] = { ...updated[index], defaultRatio: event.target.value };
                              setFormValues((prev) => ({ ...prev, outputs: updated }));
                            }}
                          />
                        </Grid>
                      </Grid>
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 6 }}>
                          <TextField
                            select
                            fullWidth
                            label="Output Type"
                            value={output.outputType}
                            onChange={(event) => {
                              const updated = [...formValues.outputs];
                              updated[index] = { ...updated[index], outputType: event.target.value };
                              setFormValues((prev) => ({ ...prev, outputs: updated }));
                            }}
                          >
                            <MenuItem value="FG">Finished Good</MenuItem>
                            <MenuItem value="WIP">WIP</MenuItem>
                            <MenuItem value="BYPRODUCT">By-product</MenuItem>
                            <MenuItem value="EMPTY_BAG">Empty Bag</MenuItem>
                          </TextField>
                        </Grid>
                        <Grid size={{ xs: 12, md: 6 }}>
                          <TextField
                            fullWidth
                            label="Notes"
                            value={output.notes}
                            onChange={(event) => {
                              const updated = [...formValues.outputs];
                              updated[index] = { ...updated[index], notes: event.target.value };
                              setFormValues((prev) => ({ ...prev, outputs: updated }));
                            }}
                          />
                        </Grid>
                      </Grid>
                      {formValues.outputs.length > 1 && (
                        <Button
                          variant="text"
                          color="error"
                          onClick={() =>
                            setFormValues((prev) => ({
                              ...prev,
                              outputs: prev.outputs.filter((_, idx) => idx !== index)
                            }))
                          }
                        >
                          Remove Output
                        </Button>
                      )}
                      <Divider />
                    </Stack>
                  ))}
                  <Button
                    variant="outlined"
                    onClick={() =>
                      setFormValues((prev) => ({
                        ...prev,
                        outputs: [...prev.outputs, createOutput()]
                      }))
                    }
                  >
                    Add Output
                  </Button>
                  <Divider />
                  <Typography variant="subtitle1">Steps</Typography>
                  {formValues.steps.map((step, index) => (
                    <Stack key={`step-${index}`} spacing={1}>
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField
                            label="Step No"
                            type="number"
                            value={step.stepNo}
                            onChange={(event) => handleStepChange(index, 'stepNo', event.target.value)}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, md: 8 }}>
                          <TextField
                            label="Step Name"
                            value={step.stepName}
                            onChange={(event) => handleStepChange(index, 'stepName', event.target.value)}
                            required
                          />
                        </Grid>
                      </Grid>
                      <TextField
                        select
                        fullWidth
                        label="Step Type"
                        value={step.stepType}
                        onChange={(event) => handleStepChange(index, 'stepType', event.target.value)}
                      >
                        <MenuItem value="CONSUME">Consume</MenuItem>
                        <MenuItem value="PROCESS">Process</MenuItem>
                        <MenuItem value="PRODUCE">Produce</MenuItem>
                        <MenuItem value="QUALITY">Quality</MenuItem>
                      </TextField>
                      <TextField
                        label="Notes"
                        value={step.notes}
                        onChange={(event) => handleStepChange(index, 'notes', event.target.value)}
                      />
                      {formValues.steps.length > 1 && (
                        <Button variant="text" color="error" onClick={() => handleRemoveStep(index)}>
                          Remove Step
                        </Button>
                      )}
                      <Divider />
                    </Stack>
                  ))}
                  <Button variant="outlined" onClick={handleAddStep}>
                    Add Step
                  </Button>
                  <Stack direction="row" spacing={1}>
                    <Button variant="contained" color="secondary" type="submit">
                      {editingTemplateId ? 'Update Template' : 'Create Template'}
                    </Button>
                    {editingTemplateId && (
                      <Button variant="outlined" onClick={resetForm}>
                        Cancel
                      </Button>
                    )}
                  </Stack>
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </Grid>
        <Grid size={{ xs: 12, md: 7 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Template Steps</Typography>
            {activeTemplate ? (
              <>
                <Stepper activeStep={-1} orientation="vertical">
                  {activeTemplate.steps?.map((step) => (
                    <Step key={step.id}>
                      <StepLabel>
                        {step.stepNo}. {step.stepName}
                      </StepLabel>
                    </Step>
                  ))}
                </Stepper>
                <Divider />
                <Grid container spacing={2}>
                  {activeTemplate.steps?.map((step) => (
                    <Grid key={step.id} size={{ xs: 12 }}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="subtitle1">
                            {step.stepNo}. {step.stepName}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {step.stepType} {step.notes || ''}
                          </Typography>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </>
            ) : (
              <Typography variant="body2" color="text.secondary">
                Select a template to view steps.
              </Typography>
            )}
          </Stack>
        </Grid>
      </Grid>
    </MainCard>
  );
}
