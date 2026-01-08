import PropTypes from 'prop-types';
import { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';

import apiClient from 'api/client';

export default function AuthGuard({ children }) {
  const location = useLocation();
  const token = localStorage.getItem('token');
  const companyId = localStorage.getItem('companyId');
  const [checkedCompany, setCheckedCompany] = useState(false);
  const [companyValid, setCompanyValid] = useState(true);

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (!companyId && location.pathname !== '/select-company') {
    return <Navigate to="/select-company" replace />;
  }

  useEffect(() => {
    if (!token || !companyId || location.pathname === '/select-company') {
      setCheckedCompany(true);
      return;
    }
    let active = true;
    apiClient
      .get('/api/companies/my')
      .then((response) => {
        if (!active) return;
        const companies = response.data || [];
        const hasCompany = Array.isArray(companies)
          && companies.some((company) => String(company.id) === String(companyId));
        setCompanyValid(hasCompany);
        setCheckedCompany(true);
      })
      .catch(() => {
        if (!active) return;
        setCompanyValid(false);
        setCheckedCompany(true);
      });
    return () => {
      active = false;
    };
  }, [companyId, location.pathname, token]);

  if (companyId && location.pathname !== '/select-company' && !checkedCompany) {
    return null;
  }

  if (companyId && !companyValid && location.pathname !== '/select-company') {
    localStorage.removeItem('companyId');
    localStorage.removeItem('companyName');
    return <Navigate to="/select-company" replace />;
  }

  return children;
}

AuthGuard.propTypes = {
  children: PropTypes.node
};
