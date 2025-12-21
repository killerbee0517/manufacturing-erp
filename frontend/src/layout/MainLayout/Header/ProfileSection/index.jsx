import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';

// material-ui
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

// assets
import User1 from 'assets/images/users/user-round.svg';
import { IconKey, IconLogout, IconUser } from '@tabler/icons-react';

// ==============================|| PROFILE MENU ||============================== //

export default function ProfileSection() {
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState(null);
  const open = Boolean(anchorEl);

  const profileName = useMemo(() => localStorage.getItem('profileName') || 'Warehouse Admin', []);

  const handleOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleNavigate = (path) => {
    handleClose();
    navigate(path);
  };

  const handleLogout = () => {
    handleClose();
    localStorage.removeItem('token');
    navigate('/login', { replace: true });
  };

  return (
    <>
      <IconButton onClick={handleOpen} sx={{ ml: 1 }}>
        <Avatar src={User1} alt="profile-avatar" sx={{ width: 40, height: 40 }} />
      </IconButton>
      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        sx={{ mt: 1 }}
      >
        <Box sx={{ px: 2, py: 1 }}>
          <Stack spacing={0.5}>
            <Typography variant="subtitle1">{profileName}</Typography>
            <Typography variant="caption" color="text.secondary">
              ERP Admin
            </Typography>
          </Stack>
        </Box>
        <Divider />
        <MenuItem onClick={() => handleNavigate('/profile')}>
          <ListItemIcon>
            <IconUser size={18} />
          </ListItemIcon>
          My Profile
        </MenuItem>
        <MenuItem onClick={() => handleNavigate('/profile/change-password')}>
          <ListItemIcon>
            <IconKey size={18} />
          </ListItemIcon>
          Change Password
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogout}>
          <ListItemIcon>
            <IconLogout size={18} />
          </ListItemIcon>
          Logout
        </MenuItem>
      </Menu>
    </>
  );
}
