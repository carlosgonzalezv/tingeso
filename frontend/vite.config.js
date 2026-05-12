import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true, // Esto permitía que otros dispositivos en tu red vieran la app
    proxy: {
      // Configuramos el proxy para que /api se redirija al backend
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // ESTO ES LO QUE TENÍAMOS:
        // Si el frontend pide /api/v1/packs, lo manda tal cual al backend
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  }
})