import apiClient from './client';

export const productionApi = {
  listBoms: () => apiClient.get('/api/production/bom'),
  getBom: (id) => apiClient.get(`/api/production/bom/${id}`),
  createBom: (payload) => apiClient.post('/api/production/bom', payload),
  updateBom: (id, payload) => apiClient.put(`/api/production/bom/${id}`, payload),
  deleteBom: (id) => apiClient.delete(`/api/production/bom/${id}`),
  listTemplates: () => apiClient.get('/api/production/templates'),
  getTemplate: (id) => apiClient.get(`/api/production/templates/${id}`),
  createTemplate: (payload) => apiClient.post('/api/production/templates', payload),
  updateTemplate: (id, payload) => apiClient.put(`/api/production/templates/${id}`, payload),
  listOrders: () => apiClient.get('/api/production/orders'),
  createOrder: (payload) => apiClient.post('/api/production/orders', payload),
  updateOrder: (id, payload) => apiClient.put(`/api/production/orders/${id}`, payload),
  createBatch: (payload) => apiClient.post('/api/production/batches', payload),
  listBatches: (status, templateId) => apiClient.get('/api/production/batches', { params: { status, templateId } }),
  getBatch: (id) => apiClient.get(`/api/production/batches/${id}`),
  startBatch: (id) => apiClient.post(`/api/production/batches/${id}/start`),
  issueBatch: (id, payload) => apiClient.post(`/api/production/batches/${id}/issue`, payload),
  completeStep: (id, stepNo, payload) => apiClient.post(`/api/production/batches/${id}/step/${stepNo}/complete`, payload),
  produceOutput: (id, payload) => apiClient.post(`/api/production/batches/${id}/produce`, payload),
  completeBatch: (id) => apiClient.post(`/api/production/batches/${id}/complete`),
  listWipOutputs: (batchId) => apiClient.get(`/api/production/batches/${batchId}/wip-outputs`),
  getCostSummary: (batchId) => apiClient.get(`/api/production/batches/${batchId}/cost-summary`),
  listWipBalances: () => apiClient.get('/api/production/wip/balances')
};
