import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';

export default function GrnCreatePage() {
  const navigate = useNavigate();

  return (
    <MainCard>
      <PageHeader
        title="Create GRN"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/grn' }, { label: 'Create' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={() => navigate('/purchase/qc')}>
            QC Inspections
          </Button>
        }
      />
      <Stack spacing={2}>
        <Typography color="text.secondary">
          GRNs are created automatically when a QC inspection is approved. Open the QC inspection to review and approve.
        </Typography>
      </Stack>
    </MainCard>
  );
}
