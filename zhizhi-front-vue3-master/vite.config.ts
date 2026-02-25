import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    host: true,
    hmr: true,
    port: 5173,
    strictPort: false,
    proxy: {
      '/api/ws': {
        target: 'http://localhost:8091',
        changeOrigin: true,
        ws: true,
        rewrite: (path: string) => path.replace(/^\/api\/ws/, '/ws')
      },
      '/api': {
        target: 'http://localhost:8091',
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/api/, '/api')
      }
    }
  },
  plugins: [
    vue(),
    vueJsx()
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: { api: 'modern-compiler' }
    }
  },
  build: {
    // 生产环境优化
    minify: 'terser',
    terserOptions: {
      compress: {
        // 移除 console.log 和 debugger
        drop_console: true,
        drop_debugger: true,
        // 保留 console.error 和 console.warn
        pure_funcs: ['console.log', 'console.debug', 'console.info']
      }
    },
    // 代码分割优化
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus'],
          'md-editor': ['md-editor-v3']
        }
      }
    },
    // 启用 gzip 压缩提示
    reportCompressedSize: true,
    // chunk 大小警告限制
    chunkSizeWarningLimit: 1000
  }
})
