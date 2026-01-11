import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Alert from '@mui/material/Alert';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import apiClient from 'api/client';

const nowDate = () => new Date().toISOString().slice(0, 10);
const nowTime = () => new Date().toISOString().slice(11, 16);

export default function SalesAttendanceDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNew = id === 'new';
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    attendanceDate: nowDate(),
    checkInTime: '',
    checkOutTime: '',
    checkInLocation: '',
    checkOutLocation: '',
    checkInLat: '',
    checkInLng: '',
    checkOutLat: '',
    checkOutLng: '',
    travelKm: '',
    ratePerKm: '',
    daAmount: '',
    remarks: ''
  });

  useEffect(() => {
    if (isNew) {
      setLoading(false);
      return;
    }
    setLoading(true);
    apiClient
      .get(`/api/sales-attendance/${id}`)
      .then((response) => {
        const payload = response.data;
        setForm({
          attendanceDate: payload.attendanceDate || nowDate(),
          checkInTime: payload.checkInTime || '',
          checkOutTime: payload.checkOutTime || '',
          checkInLocation: payload.checkInLocation || '',
          checkOutLocation: payload.checkOutLocation || '',
          checkInLat: payload.checkInLat ?? '',
          checkInLng: payload.checkInLng ?? '',
          checkOutLat: payload.checkOutLat ?? '',
          checkOutLng: payload.checkOutLng ?? '',
          travelKm: payload.travelKm ?? '',
          ratePerKm: payload.ratePerKm ?? '',
          daAmount: payload.daAmount ?? '',
          remarks: payload.remarks || ''
        });
        setError('');
      })
      .catch(() => {
        setError('Attendance record not found.');
      })
      .finally(() => setLoading(false));
  }, [id, isNew]);

  const taAmount = useMemo(() => {
    const km = Number(form.travelKm || 0);
    const rate = Number(form.ratePerKm || 0);
    return km && rate ? (km * rate).toFixed(2) : '0.00';
  }, [form.travelKm, form.ratePerKm]);

  const totalAmount = useMemo(() => {
    const ta = Number(taAmount || 0);
    const da = Number(form.daAmount || 0);
    return (ta + da).toFixed(2);
  }, [taAmount, form.daAmount]);

  const captureLocation = (type) => {
    if (!navigator.geolocation) {
      setError('GPS is not supported on this device.');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        setForm((prev) => ({
          ...prev,
          [type === 'in' ? 'checkInLat' : 'checkOutLat']: latitude,
          [type === 'in' ? 'checkInLng' : 'checkOutLng']: longitude
        }));
        setError('');
      },
      () => setError('Unable to read GPS location. Check browser permissions.')
    );
  };

  const setTimeNow = (key) => {
    setForm((prev) => ({ ...prev, [key]: nowTime() }));
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const payload = {
        attendanceDate: form.attendanceDate || null,
        checkInTime: form.checkInTime || null,
        checkOutTime: form.checkOutTime || null,
        checkInLocation: form.checkInLocation || null,
        checkOutLocation: form.checkOutLocation || null,
        checkInLat: form.checkInLat === '' ? null : Number(form.checkInLat),
        checkInLng: form.checkInLng === '' ? null : Number(form.checkInLng),
        checkOutLat: form.checkOutLat === '' ? null : Number(form.checkOutLat),
        checkOutLng: form.checkOutLng === '' ? null : Number(form.checkOutLng),
        travelKm: form.travelKm === '' ? null : Number(form.travelKm),
        ratePerKm: form.ratePerKm === '' ? null : Number(form.ratePerKm),
        daAmount: form.daAmount === '' ? null : Number(form.daAmount),
        remarks: form.remarks || null
      };
      const response = isNew
        ? await apiClient.post('/api/sales-attendance', payload)
        : await apiClient.put(`/api/sales-attendance/${id}`, payload);
      navigate(`/sales/attendance/${response.data.id}`);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save attendance.');
    } finally {
      setSaving(false);
    }
  };

  if (!isNew && loading) {
    return (
      <MainCard>
        <Typography>Loading...</Typography>
      </MainCard>
    );
  }

  return (
    <MainCard>
      <PageHeader
        title={isNew ? 'New Sales Attendance' : 'Sales Attendance Detail'}
        breadcrumbs={[{ label: 'Sales', to: '/sales/attendance' }, { label: isNew ? 'New' : 'Detail' }]}
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate('/sales/attendance')}>
              Back to List
            </Button>
            <Button variant="contained" color="secondary" onClick={handleSave} disabled={saving}>
              Save
            </Button>
          </Stack>
        }
      />
      <Stack spacing={3}>
        {error && <Alert severity="error">{error}</Alert>}
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="date"
              label="Attendance Date"
              value={form.attendanceDate}
              onChange={(event) => setForm((prev) => ({ ...prev, attendanceDate: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
        </Grid>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="time"
              label="Check In Time"
              value={form.checkInTime}
              onChange={(event) => setForm((prev) => ({ ...prev, checkInTime: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <Button variant="outlined" onClick={() => setTimeNow('checkInTime')}>
              Set Now
            </Button>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              label="Check In Location"
              value={form.checkInLocation}
              onChange={(event) => setForm((prev) => ({ ...prev, checkInLocation: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <Button variant="outlined" onClick={() => captureLocation('in')}>
              Use GPS
            </Button>
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="In Lat"
              value={form.checkInLat}
              InputProps={{ readOnly: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="In Lng"
              value={form.checkInLng}
              InputProps={{ readOnly: true }}
            />
          </Grid>
        </Grid>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="time"
              label="Check Out Time"
              value={form.checkOutTime}
              onChange={(event) => setForm((prev) => ({ ...prev, checkOutTime: event.target.value }))}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <Button variant="outlined" onClick={() => setTimeNow('checkOutTime')}>
              Set Now
            </Button>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              label="Check Out Location"
              value={form.checkOutLocation}
              onChange={(event) => setForm((prev) => ({ ...prev, checkOutLocation: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <Button variant="outlined" onClick={() => captureLocation('out')}>
              Use GPS
            </Button>
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Out Lat"
              value={form.checkOutLat}
              InputProps={{ readOnly: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Out Lng"
              value={form.checkOutLng}
              InputProps={{ readOnly: true }}
            />
          </Grid>
        </Grid>
        <Divider />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="Travel KM"
              value={form.travelKm}
              onChange={(event) => setForm((prev) => ({ ...prev, travelKm: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="Rate / KM"
              value={form.ratePerKm}
              onChange={(event) => setForm((prev) => ({ ...prev, ratePerKm: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="TA Amount"
              value={taAmount}
              InputProps={{ readOnly: true }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              type="number"
              label="DA Amount"
              value={form.daAmount}
              onChange={(event) => setForm((prev) => ({ ...prev, daAmount: event.target.value }))}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <TextField
              fullWidth
              label="Total Amount"
              value={totalAmount}
              InputProps={{ readOnly: true }}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="Remarks"
              value={form.remarks}
              onChange={(event) => setForm((prev) => ({ ...prev, remarks: event.target.value }))}
            />
          </Grid>
        </Grid>
      </Stack>
    </MainCard>
  );
}
