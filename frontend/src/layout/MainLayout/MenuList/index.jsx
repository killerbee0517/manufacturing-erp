import PropTypes from 'prop-types';
import { Activity, memo, useState } from 'react';

import Divider from '@mui/material/Divider';
import List from '@mui/material/List';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';

// project imports
import NavItem from './NavItem';
import NavGroup from './NavGroup';
import menuItems from 'menu-items';

import { useGetMenuMaster } from 'api/menu';

// ==============================|| SIDEBAR MENU LIST ||============================== //

function MenuList({ itemsConfig = menuItems }) {
  const { menuMaster } = useGetMenuMaster();
  const drawerOpen = menuMaster.isDashboardDrawerOpened;

  const [selectedID, setSelectedID] = useState('');

  const getUserRoles = () => {
    try {
      const raw = localStorage.getItem('userRoles');
      const roles = raw ? JSON.parse(raw) : [];
      return Array.isArray(roles) ? roles : [];
    } catch (err) {
      return [];
    }
  };

  const hasRoleAccess = (itemRoles, userRoles) => {
    if (!itemRoles || itemRoles.length === 0) return true;
    if (userRoles.includes('ADMIN')) return true;
    if (userRoles.includes('HEAD') && !(itemRoles.length === 1 && itemRoles[0] === 'ADMIN')) {
      return true;
    }
    return userRoles.some((role) => itemRoles.includes(role));
  };

  const filterByRoles = (items, userRoles) => {
    if (!Array.isArray(items)) return [];
    return items
      .map((item) => {
        if (!hasRoleAccess(item.roles, userRoles)) {
          return null;
        }
        if (item.children) {
          const nextChildren = filterByRoles(item.children, userRoles);
          if (nextChildren.length === 0) {
            return null;
          }
          return { ...item, children: nextChildren };
        }
        return item;
      })
      .filter(Boolean);
  };

  const userRoles = getUserRoles();
  const filteredConfig = {
    ...itemsConfig,
    items: filterByRoles(itemsConfig.items, userRoles)
  };

  const lastItem = null;

  let lastItemIndex = filteredConfig.items.length - 1;
  let remItems = [];
  let lastItemId;

  if (lastItem && lastItem < filteredConfig.items.length) {
    lastItemId = filteredConfig.items[lastItem - 1].id;
    lastItemIndex = lastItem - 1;
    remItems = filteredConfig.items.slice(lastItem - 1, filteredConfig.items.length).map((item) => ({
      title: item.title,
      elements: item.children,
      icon: item.icon,
      ...(item.url && {
        url: item.url
      })
    }));
  }

  const navItems = filteredConfig.items.slice(0, lastItemIndex + 1).map((item, index) => {
    switch (item.type) {
      case 'group':
        if (item.url && item.id !== lastItemId) {
          return (
            <List key={item.id}>
              <NavItem item={item} level={1} isParents setSelectedID={() => setSelectedID('')} />
              <Activity mode={index !== 0 ? 'visible' : 'hidden'}>
                <Divider sx={{ py: 0.5 }} />
              </Activity>
            </List>
          );
        }

        return (
          <NavGroup
            key={item.id}
            setSelectedID={setSelectedID}
            selectedID={selectedID}
            item={item}
            lastItem={lastItem}
            remItems={remItems}
            lastItemId={lastItemId}
          />
        );
      default:
        return (
          <Typography key={item.id} variant="h6" align="center" sx={{ color: 'error.main' }}>
            Menu Items Error
          </Typography>
        );
    }
  });

  return <Box {...(drawerOpen && { sx: { mt: 0.5 } })}>{navItems}</Box>;
}

MenuList.propTypes = {
  itemsConfig: PropTypes.shape({
    items: PropTypes.array
  })
};

export default memo(MenuList);
