import PropTypes from 'prop-types';
import { Link as RouterLink } from 'react-router-dom';

import Breadcrumbs from '@mui/material/Breadcrumbs';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Link from '@mui/material/Link';

export default function PageHeader({ title, breadcrumbs, actions }) {
  return (
    <Stack spacing={1} sx={{ mb: 3 }}>
      {breadcrumbs?.length ? (
        <Breadcrumbs aria-label="breadcrumb">
          {breadcrumbs.map((crumb, index) =>
            crumb.to ? (
              <Link key={`${crumb.label}-${index}`} component={RouterLink} to={crumb.to} color="inherit" underline="hover">
                {crumb.label}
              </Link>
            ) : (
              <Typography key={`${crumb.label}-${index}`} color="text.primary">
                {crumb.label}
              </Typography>
            )
          )}
        </Breadcrumbs>
      ) : null}
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems={{ xs: 'flex-start', md: 'center' }}>
        <Typography variant="h3">{title}</Typography>
        <Stack direction="row" spacing={1} sx={{ ml: 'auto' }}>
          {actions || null}
        </Stack>
      </Stack>
    </Stack>
  );
}

PageHeader.propTypes = {
  title: PropTypes.string.isRequired,
  breadcrumbs: PropTypes.arrayOf(
    PropTypes.shape({
      label: PropTypes.string.isRequired,
      to: PropTypes.string
    })
  ),
  actions: PropTypes.node
};

PageHeader.defaultProps = {
  breadcrumbs: [],
  actions: null
};
