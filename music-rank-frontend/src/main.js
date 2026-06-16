import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'
// ElMessage / ElMessageBox 是 JS 调用，按需插件不会注入样式，需手动引入
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'

const app = createApp(App)
app.use(router)
app.mount('#app')
