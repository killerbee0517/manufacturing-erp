import { memo, useMemo } from 'react';

import useMediaQuery from '@mui/material/useMediaQuery';
import Drawer from '@mui/material/Drawer';
import Box from '@mui/material/Box';

// project imports
import MenuList from '../MenuList';
import NavItem from '../MenuList/NavItem';
import LogoSection from '../LogoSection';
import MiniDrawerStyled from './MiniDrawerStyled';

import menuItems from 'menu-items';
import useConfig from 'hooks/useConfig';
import { drawerWidth } from 'store/constant';
import SimpleBar from 'ui-component/third-party/SimpleBar';

import { handlerDrawerOpen, useGetMenuMaster } from 'api/menu';

// ==============================|| SIDEBAR DRAWER ||============================== //

function Sidebar() {
  const downMD = useMediaQuery((theme) => theme.breakpoints.down('md'));

  const { menuMaster } = useGetMenuMaster();
  const drawerOpen = menuMaster.isDashboardDrawerOpened;

  const {
    state: { miniDrawer }
  } = useConfig();

  const logo = useMemo(
    () => (
      <Box sx={{ display: 'flex', px: 2, py: 1 }}>
        <LogoSection />
      </Box>
    ),
    []
  );

  const drawer = useMemo(() => {
    const rootMenu = menuItems.items?.[0];
    const settingsLink = rootMenu?.children?.find((child) => child.id === 'settings');
    const primaryMenu = rootMenu
      ? {
          items: [{ ...rootMenu, children: rootMenu.children?.filter((child) => child.id !== 'settings') }]
        }
      : { items: [] };

    const drawerSX = { px: 2, py: 1 };

    return (
      <>
        {downMD ? (
          <Box sx={{ ...drawerSX, display: 'flex', flexDirection: 'column', gap: 1, height: '100vh' }}>
            <Box sx={{ flexGrow: 1 }}>
              <MenuList itemsConfig={primaryMenu} />
            </Box>
            {settingsLink && (
              <Box
                sx={{
                  pt: 1.5,
                  borderTop: (theme) => `1px dashed ${theme.palette.divider}`
                }}
              >
                <NavItem item={settingsLink} level={1} />
              </Box>
            )}
          </Box>
        ) : (
          <SimpleBar
            sx={{
              height: '100vh',
              display: 'flex',
              flexDirection: 'column',
              ...drawerSX
            }}
          >
            <Box sx={{ flexGrow: 1 }}>
              <MenuList itemsConfig={primaryMenu} />
            </Box>
            {settingsLink && (
              <Box
                sx={{
                  pt: 1.5,
                  borderTop: (theme) => `1px dashed ${theme.palette.divider}`
                }}
              >
                <NavItem item={settingsLink} level={1} />
              </Box>
            )}
          </SimpleBar>
        )}
      </>
    );
  }, [downMD]);

  return (
    <Box component="nav" sx={{ flexShrink: { md: 0 }, width: { xs: 'auto', md: drawerWidth } }} aria-label="mailbox folders">
      {downMD || (miniDrawer && drawerOpen) ? (
        <Drawer
          variant={downMD ? 'temporary' : 'persistent'}
          anchor="left"
          open={drawerOpen}
          onClose={() => handlerDrawerOpen(!drawerOpen)}
          slotProps={{
            paper: {
              sx: {
                mt: 0,
                zIndex: 1099,
                width: drawerWidth,
                bgcolor: 'background.default',
                color: 'text.primary',
                borderRight: 'none'
              }
            }
          }}
          ModalProps={{ keepMounted: true }}
          color="inherit"
        >
          {downMD && logo}
          {drawer}
        </Drawer>
      ) : (
        <MiniDrawerStyled variant="permanent" open={drawerOpen}>
          {logo}
          {drawer}
        </MiniDrawerStyled>
      )}
    </Box>
  );
}

export default memo(Sidebar);
