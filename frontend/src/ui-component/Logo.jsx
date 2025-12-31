// material-ui
import { useTheme } from '@mui/material/styles';
import logoImage from 'assets/images/mothers.jpg';

// project imports

/**
 * if you want to use image instead of <svg> uncomment following.
 *
 * import logoDark from 'assets/images/logo-dark.svg';
 * import logo from 'assets/images/logo.svg';
 *
 */

// ==============================|| LOGO SVG ||============================== //

export default function Logo() {
  const theme = useTheme();
  const borderColor = theme.vars.palette.divider;

  return (
    <img
      src={logoImage}
      alt="Mother's Food"
      width="48"
      height="48"
      style={{
        objectFit: 'contain',
        objectPosition: 'center',
        borderRadius: '50%',
        border: `1px solid ${borderColor}`,
        backgroundColor: theme.vars.palette.background.paper,
        padding: 6
      }}
    />
  );
}
