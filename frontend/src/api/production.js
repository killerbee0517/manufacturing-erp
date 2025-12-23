import apiClient from './client';

export const productionApi = {
  listTemplates: () => apiClient.get('/api/production/templates'),
  createTemplate: (payload) => apiClient.post('/api/production/templates', payload),
  updateTemplate: (id, payload) => apiClient.put(`/api/production/templates/${id}`, payload),
  listOrders: () => apiClient.get('/api/production/orders'),
  createOrder: (payload) => apiClient.post('/api/production/orders', payload),
  updateOrder: (id, payload) => apiClient.put(`/api/production/orders/${id}`, payload),
  startBatch: (id) => apiClient.post(`/api/production/orders/${id}/batches`),
  listBatches: (orderId) => apiClient.get('/api/production/batches', { params: { orderId } }),
  createRun: (payload) => apiClient.post('/api/production/runs', payload),
  listRuns: (batchId) => apiClient.get('/api/production/runs', { params: { batchId } }),
  getCostSummary: (batchId) => apiClient.get(`/api/production/batches/${batchId}/cost-summary`)
};
