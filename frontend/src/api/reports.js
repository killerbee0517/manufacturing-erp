import apiClient from './client';

export const reportsApi = {
  ledger: (params) => apiClient.get('/reports/ledger', { params }),
  outstanding: (params) => apiClient.get('/reports/outstanding', { params }),
  ageing: (params) => apiClient.get('/reports/ageing', { params })
};
