import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from "axios";
import { createPinia } from "pinia";

import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/assets/quill.css'
import '@/assets/font/font.css';


axios.defaults.baseURL = 'http://114.55.246.213:8800'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
