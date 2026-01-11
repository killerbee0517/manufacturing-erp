import PropTypes from 'prop-types';
import { Navigate, useLocation } from 'react-router-dom';

const parseRoles = () => {
  try {
    const stored = localStorage.getItem('userRoles');
    const roles = stored ? JSON.parse(stored) : [];
    return Array.isArray(roles) ? roles : [];
  } catch (err) {
    return [];
  }
};

const isAdminOnly = (allowed) => Array.isArray(allowed) && allowed.length === 1 && allowed[0] === 'ADMIN';

export default function RoleGuard({ allowedRoles, children }) {
  const location = useLocation();
  if (!allowedRoles || allowedRoles.length === 0) {
    return children;
  }
  const roles = parseRoles();
  if (roles.includes('ADMIN')) {
    return children;
  }
  if (!isAdminOnly(allowedRoles) && roles.includes('HEAD')) {
    return children;
  }
  const hasAccess = roles.some((role) => allowedRoles.includes(role));
  if (!hasAccess) {
    return <Navigate to="/dashboard" replace state={{ from: location.pathname }} />;
  }
  return children;
}

RoleGuard.propTypes = {
  allowedRoles: PropTypes.arrayOf(PropTypes.string),
  children: PropTypes.node
};
