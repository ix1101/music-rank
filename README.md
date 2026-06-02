# 🎵 Music Rank — 个人音乐排行榜

一个全栈音乐管理系统，支持音乐数据管理、评分筛选、酷狗歌单一键同步。

**在线地址**: [https://github.com/ix1101/music-rank](https://github.com/ix1101/music-rank)

---

## 📸 功能概览

| 功能 | 说明 |
|------|------|
| 🎧 **音乐数据管理** | 表格展示所有歌曲，支持分页、搜索、筛选 |
| ⭐ **评分系统** | 0-5 星评分（0.5 增量），可视化 ⭐ 展示 |
| 🔍 **多维筛选** | 按歌手、星级、是否打星、歌单筛选 |
| 📁 **歌单管理** | 创建歌单、歌曲归类，歌单网格卡片展示 |
| 🔄 **酷狗同步** | 一键同步酷狗自建歌单到本地，自动创建对应歌单 |
| 📝 **文本导入** | 智能解析 "🌟 5星 \n - 歌手 - 歌名" 格式，批量导入 |
| 📋 **一键导出** | 复制当前列表为文本，方便分享 |
| 🔐 **持久登录** | Token 存数据库，定时自动刷新，多设备共享 |

---

## 🏗️ 项目结构

```
music-rank/
├── KuGouMusicApi/          # 酷狗音乐 API 代理（Node.js）
│   ├── module/             # 123 个 API 模块
│   ├── util/               # 加密、签名、缓存工具
│   └── server.js           # Express 服务（端口 3000）
│
├── music-rank-backend/     # 后端服务（Spring Boot 3）
│   ├── controller/         # REST API 控制器
│   ├── service/            # 业务逻辑层
│   ├── mapper/             # MyBatis-Plus 数据访问
│   ├── entity/             # 数据库实体
│   └── common/             # 通用组件（Token管理、异常处理）
│
├── music-rank-frontend/    # 前端界面（Vue 3 + Element Plus）
│   ├── src/components/     # UI 组件（表格、弹窗、筛选栏等）
│   ├── src/api/            # API 请求层
│   ├── src/router/         # 路由配置
│   └── src/views/          # 页面视图
│
└── music_db.sql            # 数据库初始化脚本
```

---

## 🚀 快速开始

### 环境要求

| 组件 | 版本要求 |
|------|----------|
| Java | JDK 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |

### 1. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS music_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql -u root -p music_db < music_db.sql
```

### 2. 启动酷狗 API 代理

```bash
cd KuGouMusicApi
npm install
npm run dev
# 运行在 http://localhost:3000
```

### 3. 启动后端

```bash
cd music-rank-backend
# 配置环境变量（可选，有默认值）
export DB_USERNAME=root
export DB_PASSWORD=你的密码
mvn spring-boot:run
# 运行在 http://localhost:8080
```

### 4. 启动前端

```bash
cd music-rank-frontend
npm install
npm run dev
# 运行在 http://localhost:5173
```

### 5. 访问

浏览器打开 **http://localhost:5173**

---

## 📡 API 接口

所有接口返回统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1717334400000
}
```

### 歌曲管理 `/api/music`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询（keyword, singer, starRating, hasStar, playlistId） |
| GET | `/list` | 获取列表（导出用） |
| POST | `/add` | 添加单首歌曲 |
| POST | `/batch-import` | 批量导入 |
| POST | `/batch-delete` | 批量删除 |

### 歌单管理 `/api/playlist`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 获取所有歌单及歌曲数量 |

### 酷狗同步 `/api/sync`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/kugou/playlists` | 获取酷狗歌单列表 |
| POST | `/kugou/playlist/{id}` | 同步指定酷狗歌单 |
| POST | `/batch` | 批量导入歌曲到指定歌单 |
| POST | `/token` | 同步登录 token 到后端 |
| GET | `/token/status` | 查看 token 状态 |
| POST | `/token/refresh` | 手动刷新 token |

---

## 🗄️ 数据库

| 表 | 说明 |
|------|------|
| `music_rank` | 歌曲数据（歌手+歌名唯一约束） |
| `playlist` | 歌单 |
| `playlist_music` | 歌单-歌曲关联（级联删除） |
| `system_config` | 系统配置（token 持久化） |

---

## 🔧 技术栈

### 前端
- **Vue 3** — Composition API
- **Element Plus** — UI 组件库
- **Vue Router 4** — 路由
- **Axios** — HTTP 请求（统一拦截器）
- **Vite 8** — 构建工具（含开发代理）

### 后端
- **Spring Boot 3.5** — 应用框架
- **MyBatis-Plus 3.5** — ORM + 分页
- **MySQL 8.0** — 数据库
- **Hutool 5.8** — 工具类库

### 酷狗 API
- **Express 4** — Web 框架
- **Axios** — 上游请求
- **AES/RSA/MD5** — Android 签名算法

---

## 🔐 Token 持久化

- Token 存储在数据库 `system_config` 表，重启不丢失
- 前端登录后自动同步 token 到后端
- 后端定时任务（每周一）调用 `/login/token` 刷新
- 多设备共享同一 token，无需重复登录
- 只在刷新也失败时才需要重新登录（约每月一次）

---

## ⚠️ 注意事项

1. **不要频繁调用登录接口**，否则可能被酷狗风控
2. 登录成功后 token 有效期约 28 天，到期前会自动刷新
3. 酷狗 API 使用设备指纹（dfid/mid）绑定，已配置固定值
4. 不同版本平台（概念版/手机版）的 token 不通用

---

## 📄 许可

MIT License
