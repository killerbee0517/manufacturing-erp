import { useEffect, useState } from 'react';

import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Checkbox from '@mui/material/Checkbox';
import Divider from '@mui/material/Divider';
import FormControlLabel from '@mui/material/FormControlLabel';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';
import { productionApi } from 'api/production';

const createLine = () => ({ componentItemId: '', uomId: '', qtyPerUnit: '', scrapPercent: '' });

export default function BomPage() {
  const [boms, setBoms] = useState([]);
  const [items, setItems] = useState([]);
  const [uoms, setUoms] = useState([]);
  const [formValues, setFormValues] = useState({
    finishedItemId: '',
    name: '',
    version: '',
    enabled: true,
    lines: [createLine()]
  });

  const columns = [
    { field: 'name', headerName: 'BOM Name' },
    { field: 'finishedItemName', headerName: 'Finished Item' },
    { field: 'version', headerName: 'Version' },
    { field: 'enabled', headerName: 'Enabled', render: (row) => (row.enabled ? 'Yes' : 'No') },
    { field: 'components', headerName: 'Components', render: (row) => row.lines?.length || 0 }
  ];

  const loadLookups = () => {
    productionApi
      .listBoms()
      .then((response) => setBoms(response.data || []))
      .catch(() => setBoms([]));
    apiClient
      .get('/api/items')
      .then((response) => setItems(response.data || []))
      .catch(() => setItems([]));
    apiClient
      .get('/api/uoms')
      .then((response) => setUoms(response.data || []))
      .catch(() => setUoms([]));
  };

  useEffect(() => {
    loadLookups();
  }, []);

  const handleLineChange = (index, field, value) => {
    setFormValues((prev) => {
      const updated = [...prev.lines];
      updated[index] = { ...updated[index], [field]: value };
      return { ...prev, lines: updated };
    });
  };

  const handleAddLine = () => setFormValues((prev) => ({ ...prev, lines: [...prev.lines, createLine()] }));
  const handleRemoveLine = (index) =>
    setFormValues((prev) => ({ ...prev, lines: prev.lines.filter((_, idx) => idx !== index) }));

  const handleSubmit = async (event) => {
    event.preventDefault();
    await productionApi.createBom({
      finishedItemId: Number(formValues.finishedItemId),
      name: formValues.name,
      version: formValues.version || null,
      enabled: formValues.enabled,
      lines: formValues.lines
        .filter((line) => line.componentItemId && line.uomId && line.qtyPerUnit)
        .map((line) => ({
          componentItemId: Number(line.componentItemId),
          uomId: Number(line.uomId),
          qtyPerUnit: Number(line.qtyPerUnit),
          scrapPercent: line.scrapPercent ? Number(line.scrapPercent) : null
        }))
    });
    setFormValues({ finishedItemId: '', name: '', version: '', enabled: true, lines: [createLine()] });
    loadLookups();
  };

  return (
    <MainCard>
      <PageHeader title="BOM" breadcrumbs={[{ label: 'Production' }, { label: 'BOM' }]} />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <DataTable columns={columns} rows={boms} emptyMessage="No BOMs yet." />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Create BOM
              </Typography>
              <Stack spacing={2} component="form" onSubmit={handleSubmit}>
                <TextField
                  select
                  label="Finished Item"
                  value={formValues.finishedItemId}
                  onChange={(event) => setFormValues((prev) => ({ ...prev, finishedItemId: event.target.value }))}
                  required
                >
                  {items.map((item) => (
                    <MenuItem key={item.id} value={item.id}>
                      {item.name}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  label="BOM Name"
                  value={formValues.name}
                  onChange={(event) => setFormValues((prev) => ({ ...prev, name: event.target.value }))}
                  required
                />
                <TextField
                  label="Version"
                  value={formValues.version}
                  onChange={(event) => setFormValues((prev) => ({ ...prev, version: event.target.value }))}
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={formValues.enabled}
                      onChange={(event) => setFormValues((prev) => ({ ...prev, enabled: event.target.checked }))}
                    />
                  }
                  label="Enabled"
                />
                <Divider />
                <Typography variant="subtitle1">Components</Typography>
                {formValues.lines.map((line, index) => (
                  <Stack key={`line-${index}`} spacing={1}>
                    <TextField
                      select
                      fullWidth
                      label="Component Item"
                      value={line.componentItemId}
                      onChange={(event) => handleLineChange(index, 'componentItemId', event.target.value)}
                      required
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
                          onChange={(event) => handleLineChange(index, 'uomId', event.target.value)}
                          required
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
                          label="Qty / Unit"
                          type="number"
                          value={line.qtyPerUnit}
                          onChange={(event) => handleLineChange(index, 'qtyPerUnit', event.target.value)}
                          required
                        />
                      </Grid>
                      <Grid size={{ xs: 12, md: 4 }}>
                        <TextField
                          fullWidth
                          label="Scrap %"
                          type="number"
                          value={line.scrapPercent}
                          onChange={(event) => handleLineChange(index, 'scrapPercent', event.target.value)}
                        />
                      </Grid>
                    </Grid>
                    {formValues.lines.length > 1 && (
                      <Button color="error" variant="text" onClick={() => handleRemoveLine(index)}>
                        Remove
                      </Button>
                    )}
                    <Divider />
                  </Stack>
                ))}
                <Button variant="outlined" onClick={handleAddLine}>
                  Add Component
                </Button>
                <Button variant="contained" color="secondary" type="submit">
                  Save BOM
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </MainCard>
  );
}
