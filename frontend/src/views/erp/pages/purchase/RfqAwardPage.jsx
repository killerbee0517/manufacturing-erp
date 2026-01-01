import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import apiClient from 'api/client';
import PageHeader from 'components/common/PageHeader';
import MainCard from 'ui-component/cards/MainCard';
import MasterAutocomplete from 'components/common/MasterAutocomplete';

export default function RfqAwardPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [rfq, setRfq] = useState(null);
  const [awards, setAwards] = useState([]);
  const [saving, setSaving] = useState(false);
  const [supplierOptions, setSupplierOptions] = useState([]);

  useEffect(() => {
    apiClient.get(`/api/rfq/${id}`).then((response) => {
      const data = response.data;
      setRfq(data);
      const baseSuppliers = (data.suppliers || []).map((s) => s.supplierId);
      setSupplierOptions(baseSuppliers);
      const seed = (data.lines || []).flatMap((line) => {
        if (!baseSuppliers.length) return [];
        return [
          {
            key: `${line.id}-0`,
            rfqLineId: line.id,
            supplierId: baseSuppliers[0],
            qty: line.quantity,
            rate: line.rateExpected || ''
          }
        ];
      });
      setAwards(seed);
    });
  }, [id]);

  const addAwardRow = (lineId) => {
    setAwards((prev) => [
      ...prev,
      { key: `${lineId}-${Date.now()}`, rfqLineId: lineId, supplierId: supplierOptions[0] || '', qty: '', rate: '' }
    ]);
  };

  const updateAward = (key, patch) => {
    setAwards((prev) => prev.map((award) => (award.key === key ? { ...award, ...patch } : award)));
  };

  const removeAward = (key) => {
    setAwards((prev) => prev.filter((award) => award.key !== key));
  };

  const handleSubmit = async () => {
    setSaving(true);
    try {
      const payload = {
        awards: awards.map((award) => ({
          rfqLineId: award.rfqLineId,
          supplierId: Number(award.supplierId),
          awardQty: Number(award.qty || 0),
          awardRate: Number(award.rate || 0)
        })),
        closeRemaining: false
      };
      await apiClient.post(`/api/rfq/${id}/award`, payload);
      navigate(`/purchase/rfq/${id}`);
    } finally {
      setSaving(false);
    }
  };

  if (!rfq) {
    return (
      <MainCard>
        <Typography>Loading...</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={`Award RFQ ${rfq.rfqNo}`}
        breadcrumbs={[
          { label: 'Purchase', to: '/purchase/rfq' },
          { label: `RFQ ${rfq.rfqNo}`, to: `/purchase/rfq/${id}` },
          { label: 'Award' }
        ]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleSubmit} disabled={saving}>
            Confirm Awards
          </Button>
        }
      />
      <Stack spacing={2}>
        <Typography variant="body1">
          Assign quantities and rates per supplier. Remaining quantity must not exceed requested quantity.
        </Typography>
        <Divider />
        {(rfq.lines || []).map((line) => (
          <Stack key={line.id} spacing={1}>
            <Typography variant="h6">
              {line.quantity} units for Item #{line.itemId} (UOM #{line.uomId})
            </Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Supplier</TableCell>
                  <TableCell>Quantity</TableCell>
                  <TableCell>Rate</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {awards
                  .filter((award) => award.rfqLineId === line.id)
                  .map((award) => (
                    <TableRow key={award.key}>
                      <TableCell>
                        <MasterAutocomplete
                          label="Supplier"
                          endpoint="/api/suppliers"
                          value={award.supplierId}
                          onChange={(nextValue) => updateAward(award.key, { supplierId: nextValue })}
                          optionLabelKey="name"
                          optionValueKey="id"
                          placeholder="Choose supplier"
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <TextField
                          size="small"
                          type="number"
                          value={award.qty}
                          onChange={(event) => updateAward(award.key, { qty: event.target.value })}
                        />
                      </TableCell>
                      <TableCell>
                        <TextField
                          size="small"
                          type="number"
                          value={award.rate}
                          onChange={(event) => updateAward(award.key, { rate: event.target.value })}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Button color="error" onClick={() => removeAward(award.key)}>
                          Remove
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                <TableRow>
                  <TableCell colSpan={4}>
                    <Button onClick={() => addAwardRow(line.id)}>Add supplier award</Button>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </Stack>
        ))}
      </Stack>
    </MainCard>
  );
}
