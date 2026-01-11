import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import DataTable from 'components/common/DataTable';
import apiClient from 'api/client';

const formatTime = (value) => (value ? value.slice(0, 5) : '-');

export default function SalesAttendancePage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiClient
      .get('/api/sales-attendance')
      .then((response) => setRows(response.data || []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <MainCard>
      <PageHeader
        title="Sales Attendance"
        breadcrumbs={[{ label: 'Sales' }, { label: 'Attendance' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/sales/attendance/new')}>
            New Attendance
          </Button>
        }
      />
      <Stack spacing={2}>
        <DataTable
          columns={[
            { field: 'attendanceDate', headerName: 'Date' },
            { field: 'userName', headerName: 'Salesman' },
            { field: 'checkInTime', headerName: 'Check In', render: (row) => formatTime(row.checkInTime) },
            { field: 'checkOutTime', headerName: 'Check Out', render: (row) => formatTime(row.checkOutTime) },
            { field: 'travelKm', headerName: 'KM' },
            { field: 'totalAmount', headerName: 'Total Amount' }
          ]}
          rows={rows}
          loading={loading}
          emptyMessage="No attendance records found."
          onRowClick={(row) => navigate(`/sales/attendance/${row.id}`)}
        />
      </Stack>
    </MainCard>
  );
}
