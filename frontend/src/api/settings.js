import apiClient from './client';

export const settingsApi = {
  list: (entity) => apiClient.get(`/settings/${entity}`),
  create: (entity, payload) => apiClient.post(`/settings/${entity}`, payload),
  update: (entity, id, payload) => apiClient.put(`/settings/${entity}/${id}`, payload),
  remove: (entity, id) => apiClient.delete(`/settings/${entity}/${id}`)
};
