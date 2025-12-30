import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Alert from '@mui/material/Alert';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';
import { mastersEntities } from './mastersConfig';

const lookupEndpoints = {
  suppliers: '/api/suppliers',
  items: '/api/items',
  uoms: '/api/uoms',
  locations: '/api/locations',
  customers: '/api/customers',
  banks: '/api/banks',
  godowns: '/api/godowns',
  brokers: '/api/brokers',
  roles: '/api/roles',
  tickets: '/api/weighbridge/tickets',
  salesOrders: '/api/sales-orders'
};

const buildLookupMap = (options) =>
  (options || []).reduce((acc, option) => {
    const id = option.id ?? option.code ?? option.name;
    const label = option.name || option.code || option.vehicleNo || option.ticketNo || option.id;
    acc[id] = label;
    return acc;
  }, {});

export default function MasterDetailPage() {
  const { entity, id } = useParams();
  const navigate = useNavigate();
  const config = mastersEntities[entity];

  const [record, setRecord] = useState(null);
  const [lookups, setLookups] = useState({});

  const fields = useMemo(() => config?.fields || [], [config]);

  useEffect(() => {
    if (!config?.listEndpoint) return;
    apiClient
      .get(`${config.listEndpoint}/${id}`)
      .then((response) => setRecord(response.data || null))
      .catch(() => setRecord(null));
  }, [config, id]);

  useEffect(() => {
    if (!config) return;
    fields
      .filter((field) => field.optionsSource)
      .forEach((sourceField) => {
        const source = sourceField.optionsSource;
        if (lookups[source]) return;
        const endpoint = lookupEndpoints[source];
        if (!endpoint) return;
        apiClient
          .get(endpoint)
          .then((response) => {
            setLookups((prev) => ({ ...prev, [source]: response.data || [] }));
          })
          .catch(() => {
            setLookups((prev) => ({ ...prev, [source]: [] }));
          });
      });
  }, [config, fields, lookups]);

  if (!config) {
    return <Alert severity="warning">Master module not configured.</Alert>;
  }

  if (!record) {
    return (
      <MainCard>
        <Typography>Record not found.</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={config.title}
        breadcrumbs={[
          { label: 'Masters', to: '/masters/suppliers' },
          { label: config.title, to: `/masters/${entity}` },
          { label: 'Detail' }
        ]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate(`/masters/${entity}/${id}/edit`)}>
              Edit
            </Button>
            <Button variant="outlined" onClick={() => navigate(`/masters/${entity}`)}>
              Back
            </Button>
          </Stack>
        }
      />
      <Grid container spacing={2}>
        {fields.map((field) => {
          const lookupMap = field.optionsSource ? buildLookupMap(lookups[field.optionsSource]) : {};
          const fieldName = field.name;
          const labelKey = fieldName.endsWith('Id') ? `${fieldName.slice(0, -2)}Name` : null;
          const rawValue = labelKey && record[labelKey] ? record[labelKey] : record[fieldName];
          const displayValue =
            field.type === 'select' && rawValue !== null && rawValue !== undefined
              ? lookupMap[rawValue] || rawValue
              : rawValue;
          return (
            <Grid key={field.name} size={{ xs: 12, md: 6 }}>
              <Typography variant="subtitle2" color="text.secondary">
                {field.label}
              </Typography>
              <Typography>{displayValue ?? '-'}</Typography>
            </Grid>
          );
        })}
      </Grid>
    </MainCard>
  );
}
