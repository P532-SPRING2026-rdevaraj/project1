import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// When VITE_DEPLOY_MODE=ghpages the build goes to dist/ for GitHub Pages deployment.
// In all other cases it goes to ../src/main/resources/static for the Spring Boot JAR.
const isGhPages = process.env.VITE_DEPLOY_MODE === 'ghpages'

export default defineConfig({
  plugins: [react()],
  // Base path for GitHub Pages: /Project1/ (repo name).
  // Leave as '/' for local dev and the Spring Boot JAR build.
  base: isGhPages ? '/project1/' : '/',
  build: {
    outDir: isGhPages ? 'dist' : '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
