import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// El proxy envía todo lo que empiece con /api al backend (así no hay problemas de CORS en desarrollo)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } },
  },
});
