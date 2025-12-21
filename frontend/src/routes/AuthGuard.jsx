import PropTypes from 'prop-types';
import { Navigate, useLocation } from 'react-router-dom';

export default function AuthGuard({ children }) {
  const location = useLocation();
  const token = localStorage.getItem('token');

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return children;
}

AuthGuard.propTypes = {
  children: PropTypes.node
};
