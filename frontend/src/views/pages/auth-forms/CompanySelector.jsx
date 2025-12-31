import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CircularProgress from '@mui/material/CircularProgress';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Alert from '@mui/material/Alert';
import apiClient from 'api/client';

export default function CompanySelector() {
  const navigate = useNavigate();
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login', { replace: true });
      return;
    }
    const pending = sessionStorage.getItem('pendingCompanies');
    if (pending) {
      try {
        const parsed = JSON.parse(pending);
        if (Array.isArray(parsed) && parsed.length > 0) {
          setCompanies(parsed);
          setLoading(false);
          return;
        }
      } catch {
        // ignore parsing errors and fallback to fetch
      }
    }
    apiClient
      .get('/api/companies/my')
      .then((response) => {
        setCompanies(response.data || []);
      })
      .catch(() => setError('Unable to load companies'))
      .finally(() => setLoading(false));
  }, [navigate]);

  const handleSelect = (company) => {
    localStorage.setItem('companyId', company.id);
    localStorage.setItem('companyName', company.name);
    sessionStorage.removeItem('pendingCompanies');
    navigate('/dashboard', { replace: true });
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 540, margin: '0 auto', mt: 6 }}>
      <Card>
        <CardContent>
          <Typography variant="h4" gutterBottom>
            Select Company
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Choose the company you want to work with for this session.
          </Typography>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          <Stack spacing={2}>
            {companies.map((company) => (
              <Button key={company.id} variant="outlined" fullWidth onClick={() => handleSelect(company)}>
                <Stack direction="column" spacing={0.5} sx={{ width: '100%' }}>
                  <Typography variant="subtitle1" sx={{ textAlign: 'left' }}>
                    {company.name}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" sx={{ textAlign: 'left' }}>
                    {company.code}
                  </Typography>
                </Stack>
              </Button>
            ))}
            {companies.length === 0 && (
              <Typography variant="body2" color="text.secondary">
                No companies assigned. Please contact your administrator.
              </Typography>
            )}
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
