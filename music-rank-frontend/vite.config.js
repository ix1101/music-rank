import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import dotenv from 'dotenv'
import { resolve } from 'path'

dotenv.config()

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',
    proxy: {
      // 后端 API 代理
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 将 /kugou 开头的请求代理到本地 KuGouMusicApi (端口3000)
      '/kugou': {
        target: 'http://localhost:3000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/kugou/, ''),
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            // 设置浏览器请求头以绕过酷狗反爬
            proxyReq.setHeader(
              'User-Agent',
              'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36'
            )

            // ============ 合并 Cookie：浏览器原有 + 我们注入的 ============
            // 1. 先解析浏览器已有的 Cookie
            const browserCookie = req.headers['cookie'] || ''
            const cookieMap = {}
            browserCookie.split(';').forEach((pair) => {
              const idx = pair.indexOf('=')
              if (idx > 0) {
                const key = pair.substring(0, idx).trim()
                const val = pair.substring(idx + 1).trim()
                if (key && val) cookieMap[key] = val
              }
            })

            // 2. 注入 token（优先用浏览器传的 X-Kugou-Token，否则环境变量）
            const token =
              req.headers['x-kugou-token'] || process.env.VITE_KUGOU_TOKEN || cookieMap['token'] || ''
            if (token) cookieMap['token'] = token

            // 3. 注入持久设备指纹（request.js 读取 cookie.dfid 和 cookie.mid）
            const dfid = req.headers['x-kugou-dfid'] || process.env.VITE_KUGOU_DFID || '6ef29622fa1a716815dce182ef0356d5'
            const mid  = req.headers['x-kugou-mid']  || process.env.VITE_KUGOU_MID  || '39d79bd96d1ef95ba28f2bcee4c199ae'
            cookieMap['dfid'] = dfid
            cookieMap['mid']  = mid

            // 4. 确保 userid 存在
            if (!cookieMap['userid']) cookieMap['userid'] = '2150217007'

            // 5. 重新组装 Cookie
            const mergedCookie = Object.entries(cookieMap)
              .map(([k, v]) => `${k}=${v}`)
              .join('; ')
            proxyReq.setHeader('Cookie', mergedCookie)
          })
        }
      }
    }
  }
})
