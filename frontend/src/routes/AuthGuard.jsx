import PropTypes from 'prop-types';
import { Navigate, useLocation } from 'react-router-dom';

export default function AuthGuard({ children }) {
  const location = useLocation();
  const token = localStorage.getItem('token');
  const companyId = localStorage.getItem('companyId');

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (!companyId && location.pathname !== '/select-company') {
    return <Navigate to="/select-company" replace />;
  }

  return children;
}

AuthGuard.propTypes = {
  children: PropTypes.node
};
