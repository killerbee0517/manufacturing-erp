import PropTypes from 'prop-types';
import { useEffect, useState } from 'react';

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

const closureOptions = [
  { value: 'CANCELLED', label: 'Cancel RFQ' },
  { value: 'CANCEL_REMAINING', label: 'Cancel remaining quantity' },
  { value: 'NO_RESPONSE', label: 'No response from suppliers' },
  { value: 'OTHER', label: 'Other' }
];

export default function RfqCloseDialog({ open, onClose, onConfirm, loading }) {
  const [reason, setReason] = useState('');

  useEffect(() => {
    if (!open) {
      setReason('');
    }
  }, [open]);

  const handleConfirm = () => {
    onConfirm(reason);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Close RFQ</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            select
            label="Closure Reason"
            value={reason}
            onChange={(event) => setReason(event.target.value)}
            fullWidth
          >
            {closureOptions.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Cancel
        </Button>
        <Button variant="contained" color="secondary" onClick={handleConfirm} disabled={!reason || loading}>
          Confirm Close
        </Button>
      </DialogActions>
    </Dialog>
  );
}

RfqCloseDialog.propTypes = {
  open: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  loading: PropTypes.bool
};

RfqCloseDialog.defaultProps = {
  loading: false
};
