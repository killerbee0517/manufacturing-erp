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
import apiClient from 'api/client';
import { productionApi } from 'api/production';

const createStep = (sequenceNo = 1) => ({ name: '', description: '', sequenceNo, sourceGodownId: '', destGodownId: '' });

export default function ProcessTemplatesPage() {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTemplate, setActiveTemplate] = useState(null);
  const [godowns, setGodowns] = useState([]);
  const [formValues, setFormValues] = useState({ name: '', description: '', steps: [createStep()] });

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
    apiClient
      .get('/api/godowns')
      .then((response) => setGodowns(response.data || []))
      .catch(() => setGodowns([]));
  }, []);

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
      steps: prev.steps.filter((_, idx) => idx !== index).map((step, idx) => ({ ...step, sequenceNo: idx + 1 }))
    }));
  };

  const handleCreate = async (event) => {
    event.preventDefault();
    await productionApi.createTemplate({
      name: formValues.name,
      description: formValues.description,
      steps: formValues.steps.map((step) => ({
        name: step.name,
        description: step.description,
        sequenceNo: Number(step.sequenceNo || 0) || 1,
        sourceGodownId: step.sourceGodownId || null,
        destGodownId: step.destGodownId || null
      }))
    });
    setFormValues({ name: '', description: '', steps: [createStep()] });
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
              onRowClick={(row) => setActiveTemplate(row)}
              emptyMessage="No templates configured."
            />
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  New Template
                </Typography>
                <Stack spacing={2} component="form" onSubmit={handleCreate}>
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
                  <Divider />
                  <Typography variant="subtitle1">Steps</Typography>
                  {formValues.steps.map((step, index) => (
                    <Stack key={`step-${index}`} spacing={1}>
                      <TextField
                        label={`Step ${index + 1} Name`}
                        value={step.name}
                        onChange={(event) => handleStepChange(index, 'name', event.target.value)}
                        required
                      />
                      <TextField
                        label="Description"
                        value={step.description}
                        onChange={(event) => handleStepChange(index, 'description', event.target.value)}
                      />
                      <Grid container spacing={1}>
                        <Grid size={{ xs: 12, md: 6 }}>
                          <TextField
                            select
                            fullWidth
                            label="Source Godown"
                            value={step.sourceGodownId}
                            onChange={(event) => handleStepChange(index, 'sourceGodownId', event.target.value)}
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
                            select
                            fullWidth
                            label="Destination Godown"
                            value={step.destGodownId}
                            onChange={(event) => handleStepChange(index, 'destGodownId', event.target.value)}
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
                  <Button variant="contained" color="secondary" type="submit">
                    Create Template
                  </Button>
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
                      <StepLabel>{step.name}</StepLabel>
                    </Step>
                  ))}
                </Stepper>
                <Divider />
                <Grid container spacing={2}>
                  {activeTemplate.steps?.map((step) => (
                    <Grid key={step.id} size={{ xs: 12 }}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="subtitle1">{step.name}</Typography>
                          <Typography variant="body2" color="text.secondary">
                            {step.description || 'No description'}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {step.sourceGodownName ? `From: ${step.sourceGodownName}` : 'No source godown'}
                            {' · '}
                            {step.destGodownName ? `To: ${step.destGodownName}` : 'No destination godown'}
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
