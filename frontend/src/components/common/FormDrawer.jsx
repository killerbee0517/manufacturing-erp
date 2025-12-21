import PropTypes from 'prop-types';

import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Drawer from '@mui/material/Drawer';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

export default function FormDrawer({ open, title, onClose, onSubmit, children, submitLabel }) {
  return (
    <Drawer anchor="right" open={open} onClose={onClose} PaperProps={{ sx: { width: { xs: '100%', md: 480 } } }}>
      <Stack spacing={2} sx={{ p: 3, height: '100%' }}>
        <Stack spacing={0.5}>
          <Typography variant="h4">{title}</Typography>
          <Typography variant="caption" color="text.secondary">
            Complete the details and save.
          </Typography>
        </Stack>
        <Divider />
        <Box component="form" onSubmit={onSubmit} sx={{ flexGrow: 1, overflowY: 'auto', pr: 1 }}>
          <Stack spacing={2}>{children}</Stack>
          <Divider sx={{ my: 3 }} />
          <Stack direction="row" spacing={2} justifyContent="flex-end">
            <Button variant="outlined" onClick={onClose}>
              Cancel
            </Button>
            <Button variant="contained" color="secondary" type="submit">
              {submitLabel}
            </Button>
          </Stack>
        </Box>
      </Stack>
    </Drawer>
  );
}

FormDrawer.propTypes = {
  open: PropTypes.bool.isRequired,
  title: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  children: PropTypes.node.isRequired,
  submitLabel: PropTypes.string
};

FormDrawer.defaultProps = {
  submitLabel: 'Save'
};
