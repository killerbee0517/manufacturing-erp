import { useEffect, useState } from 'react';

import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';

import apiClient from 'api/client';

export default function CompanyField() {
  const [companies, setCompanies] = useState([]);
  const [selected, setSelected] = useState(localStorage.getItem('companyId') || '');

  useEffect(() => {
    let active = true;
    apiClient
      .get('/api/companies/my')
      .then((response) => {
        if (!active) return;
        const list = response.data || [];
        setCompanies(Array.isArray(list) ? list : []);
      })
      .catch(() => {
        if (!active) return;
        setCompanies([]);
      });
    return () => {
      active = false;
    };
  }, []);

  const handleChange = (event) => {
    const value = event.target.value;
    setSelected(value);
    const selectedCompany = companies.find((company) => String(company.id) === String(value));
    if (selectedCompany) {
      localStorage.setItem('companyId', selectedCompany.id);
      localStorage.setItem('companyName', selectedCompany.name);
      window.location.reload();
    }
  };

  if (companies.length <= 1) {
    const companyName = localStorage.getItem('companyName') || '';
    return (
      <TextField fullWidth label="Company" value={companyName} InputProps={{ readOnly: true }} />
    );
  }

  return (
    <TextField select fullWidth label="Company" value={selected} onChange={handleChange}>
      {companies.map((company) => (
        <MenuItem key={company.id} value={company.id}>
          {company.name}
        </MenuItem>
      ))}
    </TextField>
  );
}
