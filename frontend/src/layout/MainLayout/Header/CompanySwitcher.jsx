import { useEffect, useMemo, useState } from 'react';

import Box from '@mui/material/Box';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import apiClient from 'api/client';

const loadRoles = () => {
  try {
    const raw = localStorage.getItem('userRoles');
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

export default function CompanySwitcher() {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(localStorage.getItem('companyId') || '');

  const isAdmin = useMemo(() => loadRoles().includes('ADMIN'), []);

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
      })
      .finally(() => {
        if (!active) return;
        setLoading(false);
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
    }
    window.location.reload();
  };

  if (loading || companies.length <= 1) {
    const companyName = localStorage.getItem('companyName') || '';
    if (!companyName) return null;
    return (
      <Box sx={{ mr: 2 }}>
        <Typography variant="subtitle2" color="text.secondary">
          {companyName}
        </Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ minWidth: 220, mr: 2 }}>
      <TextField
        select
        size="small"
        label={isAdmin ? 'Company (admin)' : 'Company'}
        value={selected}
        onChange={handleChange}
      >
        {companies.map((company) => (
          <MenuItem key={company.id} value={company.id}>
            {company.name}
          </MenuItem>
        ))}
      </TextField>
    </Box>
  );
}
