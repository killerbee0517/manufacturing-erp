import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const appBaseName = import.meta.env.VITE_APP_BASE_NAME || '/';
const loginPath = appBaseName.endsWith('/') ? `${appBaseName}login` : `${appBaseName}/login`;

const apiClient = axios.create({ baseURL });
const authClient = axios.create({ baseURL });

let isRefreshing = false;
let pendingRequests = [];

const clearAuth = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
};

const redirectToLogin = () => {
  window.location.assign(loginPath);
};

const processPending = (error, token) => {
  pendingRequests.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token);
    }
  });
  pendingRequests = [];
};

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const companyId = localStorage.getItem('companyId');
  if (companyId) {
    config.headers['X-Company-Id'] = companyId;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error?.response?.status;
    const originalRequest = error?.config;

    if ((status === 401 || status === 403) && originalRequest && !originalRequest._retry) {
      if (originalRequest.url?.includes('/api/auth/refresh')) {
        clearAuth();
        redirectToLogin();
        return Promise.reject(error);
      }

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        clearAuth();
        redirectToLogin();
        return Promise.reject(error);
      }

      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingRequests.push({ resolve, reject });
        }).then((newToken) => {
          originalRequest.headers = originalRequest.headers || {};
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        });
      }

      isRefreshing = true;
      try {
        const refreshResponse = await authClient.post('/api/auth/refresh', { refreshToken });
        const newToken = refreshResponse.data.token;
        const newRefreshToken = refreshResponse.data.refreshToken;
        localStorage.setItem('token', newToken);
        localStorage.setItem('refreshToken', newRefreshToken);
        processPending(null, newToken);
        originalRequest.headers = originalRequest.headers || {};
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        processPending(refreshError, null);
        clearAuth();
        redirectToLogin();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    const message = error?.response?.data?.message || 'Something went wrong. Please try again.';
    return Promise.reject({ ...error, message });
  }
);

export default apiClient;
