import { useEffect, useState } from 'react';

import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Stepper from '@mui/material/Stepper';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import { productionApi } from 'api/production';

export default function ProcessTemplatesPage() {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTemplate, setActiveTemplate] = useState(null);

  const columns = [
    { field: 'name', headerName: 'Template Name' },
    { field: 'stepCount', headerName: 'Steps', render: (row) => row.steps?.length || 0 }
  ];

  useEffect(() => {
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
  }, []);

  return (
    <MainCard>
      <PageHeader title="Process Templates" breadcrumbs={[{ label: 'Production' }, { label: 'Process Templates' }]} />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 5 }}>
          <DataTable
            columns={columns}
            rows={templates}
            loading={loading}
            onRowClick={(row) => setActiveTemplate(row)}
            emptyMessage="No templates configured."
          />
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
                            {step.description}
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
