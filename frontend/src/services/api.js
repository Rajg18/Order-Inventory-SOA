import axios from 'axios';

const ORDER_SERVICE_URL = import.meta.env.VITE_ORDER_SERVICE_URL || 'http://localhost:8081';
const INVENTORY_SERVICE_URL = import.meta.env.VITE_INVENTORY_SERVICE_URL || 'http://localhost:8082';

export const orderApi = axios.create({
  baseURL: ORDER_SERVICE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 8000,
});

export const inventoryApi = axios.create({
  baseURL: INVENTORY_SERVICE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 8000,
});

// Centralized API Activity Store for API Activity Monitor (Phase 7 ready)
let apiLogs = [];
let listeners = [];

export function getApiLogs() {
  return [...apiLogs];
}

export function subscribeApiLogs(listener) {
  listeners.push(listener);
  return () => {
    listeners = listeners.filter((l) => l !== listener);
  };
}

export function clearApiLogs() {
  apiLogs = [];
  listeners.forEach((l) => l([...apiLogs]));
}

function notifyLog(entry) {
  apiLogs = [entry, ...apiLogs].slice(0, 100); // keep last 100 requests
  listeners.forEach((l) => l([...apiLogs]));
}

function attachInterceptors(instance, serviceName) {
  instance.interceptors.request.use(
    (config) => {
      config.metadata = { startTime: performance.now() };
      return config;
    },
    (error) => Promise.reject(error)
  );

  instance.interceptors.response.use(
    (response) => {
      const duration = Math.round(performance.now() - (response.config.metadata?.startTime || performance.now()));
      const entry = {
        id: Math.random().toString(36).substring(2, 9),
        service: serviceName,
        method: response.config.method?.toUpperCase() || 'GET',
        url: response.config.url || '',
        fullUrl: (response.config.baseURL || '') + (response.config.url || ''),
        status: response.status,
        duration,
        timestamp: new Date().toLocaleTimeString(),
        requestPayload: response.config.data ? (typeof response.config.data === 'string' ? JSON.parse(response.config.data) : response.config.data) : null,
        responsePayload: response.data,
        isError: false,
      };
      notifyLog(entry);
      return response;
    },
    (error) => {
      const duration = Math.round(performance.now() - (error.config?.metadata?.startTime || performance.now()));
      const status = error.response ? error.response.status : (error.code === 'ECONNABORTED' ? 408 : 503);
      const entry = {
        id: Math.random().toString(36).substring(2, 9),
        service: serviceName,
        method: error.config?.method?.toUpperCase() || 'UNKNOWN',
        url: error.config?.url || '',
        fullUrl: ((error.config?.baseURL || '') + (error.config?.url || '')) || 'Network Error',
        status,
        duration,
        timestamp: new Date().toLocaleTimeString(),
        requestPayload: error.config?.data ? (typeof error.config.data === 'string' ? JSON.parse(error.config.data) : error.config.data) : null,
        responsePayload: error.response?.data || { error: error.message || 'Service Connection Error' },
        isError: true,
      };
      notifyLog(entry);
      return Promise.reject(error);
    }
  );
}

attachInterceptors(orderApi, 'Order Service (8081)');
attachInterceptors(inventoryApi, 'Inventory Service (8082)');
