import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

import MainCard from 'ui-component/cards/MainCard';
import PageHeader from 'components/common/PageHeader';
import MasterAutocomplete from 'components/common/MasterAutocomplete';
import apiClient from 'api/client';

export default function GrnCreatePage() {
  const navigate = useNavigate();
  const [ticketId, setTicketId] = useState('');
  const [creating, setCreating] = useState(false);

  const handleCreate = async () => {
    if (!ticketId) return;
    setCreating(true);
    try {
      const response = await apiClient.post(`/api/grn/from-weighbridge/${ticketId}`);
      navigate(`/purchase/grn/${response.data.id}`);
    } finally {
      setCreating(false);
    }
  };

  return (
    <MainCard>
      <PageHeader
        title="Create GRN"
        breadcrumbs={[{ label: 'Purchase', to: '/purchase/grn' }, { label: 'Create' }]}
        actions={
          <Button variant="contained" color="secondary" onClick={handleCreate} disabled={!ticketId || creating}>
            Create from Weighbridge
          </Button>
        }
      />
      <Stack spacing={3}>
        <Typography color="text.secondary">
          Select a completed weighbridge ticket to auto-create a GRN. Supplier, PO, items and weights will be
          populated automatically. You can set the godown and narration on the detail screen.
        </Typography>
        <MasterAutocomplete
          label="Weighbridge Ticket"
          endpoint="/api/weighbridge/tickets"
          queryParams={{ status: 'COMPLETED' }}
          value={ticketId}
          onChange={setTicketId}
          optionLabelKey="serialNo"
          optionValueKey="id"
          placeholder="Search completed tickets"
          required
        />
      </Stack>
    </MainCard>
  );
}
