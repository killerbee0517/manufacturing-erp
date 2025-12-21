import apiClient from './client';

export const productionApi = {
  listTemplates: () => apiClient.get('/production/templates'),
  createTemplate: (payload) => apiClient.post('/production/templates', payload),
  listOrders: () => apiClient.get('/production/orders'),
  createOrder: (payload) => apiClient.post('/production/orders', payload),
  createExecution: (payload) => apiClient.post('/production/executions', payload)
};
