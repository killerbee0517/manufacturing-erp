// material-ui
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

export default function Footer() {
  return (
    <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'flex-start', pt: 3, mt: 'auto' }}>
      <Typography variant="caption">Manufacturing ERP</Typography>
    </Stack>
  );
}
