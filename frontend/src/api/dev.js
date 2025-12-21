import apiClient from './client';

export const devApi = {
  seedSettings: () => apiClient.post('/dev/seed/settings'),
  seedDemoTransactions: () => apiClient.post('/dev/seed/demo-transactions')
};
