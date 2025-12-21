import apiClient from './client';

export const vouchersApi = {
  create: (payload) => apiClient.post('/vouchers', payload),
  get: (id) => apiClient.get(`/vouchers/${id}`),
  list: (params) => apiClient.get('/vouchers', { params })
};
