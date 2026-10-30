import axios from 'axios';

// Todas las llamadas van a /api (el proxy de Vite las manda a http://localhost:8080)
const api = axios.create({ baseURL: '/api' });

// Agrega el token JWT a cada petición
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Si el token expiró (401) vuelve al login
api.interceptors.response.use(
  (r) => r,
  (err) => {
    const url = err.config?.url || '';
    if (err.response?.status === 401 && !url.includes('/auth/')) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// Mensaje amigable que viene del backend (campo "mensaje")
export const mensajeError = (err) =>
  err?.response?.data?.mensaje || 'No se pudo conectar con el servidor. Verifica que la API esté en ejecución.';

// Errores por campo (validaciones del backend)
export const erroresCampos = (err) => err?.response?.data?.errores || {};

export default api;
