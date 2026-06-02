# music-rank-frontend

个人音乐排行榜前端应用，基于 Vue 3 + Element Plus + Vite，支持音乐数据管理和酷狗歌单同步。

## 技术栈

- **Vue 3.5** — Composition API (`<script setup>`)
- **Element Plus 2.14** — UI 组件库
- **Vue Router 4** — 路由管理
- **Axios** — HTTP 请求
- **Vite 8** — 构建工具（含开发代理）

## 项目结构

```
src/
├── api/                    # API 请求层
│   ├── request.js          # Axios 实例（拦截器、错误处理）
│   ├── music.js            # 歌曲 API
│   ├── playlist.js         # 歌单 API
│   └── kugou.js            # 酷狗同步 API
├── components/             # 可复用组件
│   ├── FilterBar.vue       # 筛选栏（歌手/星级/是否打星）
│   ├── ActionBar.vue       # 操作栏（搜索/按钮组）
│   ├── MusicTable.vue      # 音乐数据表格
│   ├── PlaylistGrid.vue    # 歌单网格卡片
│   ├── AddMusicDialog.vue  # 单条添加弹窗
│   ├── TextImportDialog.vue # 文本批量导入弹窗
│   ├── LoginDialog.vue     # 酷狗登录弹窗
│   └── KugouSyncDialog.vue # 酷狗歌单同步弹窗
├── router/
│   └── index.js            # Vue Router 配置
├── views/
│   └── Home.vue            # 主页面（编排组件）
├── App.vue                 # 根组件
├── main.js                 # 应用入口
└── style.css               # 全局样式
```

## 快速开始

### 前提条件

- Node.js 18+
- 后端 `music-rank-backend` 运行在 `localhost:8080`
- KuGouMusicApi 运行在 `localhost:3000`

### 安装依赖

```bash
cd music-rank-frontend
npm install
```

### 配置环境变量

复制 `.env.example` 为 `.env`，填入酷狗 token：

```bash
VITE_KUGOU_TOKEN=your_token_here
```

### 启动开发服务器

```bash
npm run dev
```

访问 `http://localhost:5173`

### 构建生产版本

```bash
npm run build
npm run preview
```

## 功能说明

### 音乐数据管理
- 表格展示所有音乐，支持分页、搜索、筛选
- 按歌手、星级、是否打星筛选
- 单条添加 / 文本智能导入 / 批量删除
- 一键复制当前列表到剪贴板

### 歌单管理
- 歌单网格视图展示
- 点击歌单查看其中歌曲
- 支持按歌单分类浏览

### 酷狗同步
- 手机验证码登录酷狗（持久化 token）
- 获取用户自建歌单列表
- 选择歌单同步到本地系统
- 自动创建对应歌单并导入歌曲

## 开发代理

Vite 开发服务器配置了 `/kugou` 路径代理到 `localhost:3000`（KuGouMusicApi），自动注入认证 Cookie 和浏览器请求头，绕过酷狗反爬虫机制。
