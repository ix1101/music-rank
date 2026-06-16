# 🎵 Music Rank — 我的音乐精选排行

个人音乐收藏管理与评分系统。支持从酷狗歌单同步歌曲、精确打分（0.1 精度）、多维度筛选与导出。

## 功能

- **歌曲管理** — 添加 / 编辑 / 删除 / 批量导入，自动去重
- **精确评分** — 0.0 ~ 5.0 星，支持 0.1 精度
- **歌单系统** — 多歌单归类，歌曲可属于多个歌单
- **酷狗同步** — 手机验证码登录，一键同步酷狗歌单
- **多维筛选** — 按歌手、评分区间、是否有星筛选
- **搜索防抖** — 关键词搜索 200ms 防抖
- **导入导出** — 支持文本解析导入，多格式导出（横杠 / 竖线 / 文字 / json）
- **响应式布局** — 桌面端与移动端双适配
- **暗色主题** — 原生暗色 UI

## 技术栈

| 层 | 技术 |
|------|------|
| 前端 | Vue 3, Element Plus (按需加载), Vite 8, Axios |
| 后端 | Spring Boot 3.5, MyBatis-Plus, SQLite / MySQL |
| 中间件 | KuGouMusicApi (Node.js), Nginx |
| 部署 | 阿里云 ECS, Ubuntu 24.04, systemd |

## 项目结构

```
├── music-rank-frontend/   # Vue 3 前端
│   ├── src/
│   │   ├── api/           # Axios 封装 + 接口
│   │   ├── components/    # 组件 (MusicTable, FilterBar, PlaylistGrid...)
│   │   ├── views/         # 主页面 Home.vue
│   │   ├── router/        # Vue Router
│   │   └── style.css      # 全局样式 (CSS 变量 + 暗色主题)
│   └── vite.config.js     # Vite 配置 + 代理 + 组件按需引入
├── music-rank-backend/    # Spring Boot 后端
│   └── src/main/
│       ├── java/com/musicrank/
│       │   ├── controller/ # REST 控制器
│       │   ├── service/    # 业务逻辑 + 酷狗同步
│       │   ├── mapper/     # MyBatis-Plus 映射
│       │   └── config/     # SQLite 优化器 + 定时任务
│       └── resources/
│           ├── application.yml      # 开发环境 (MySQL)
│           └── application-prod.yml # 生产环境 (SQLite)
└── KuGouMusicApi/         # 酷狗 API 代理 (Node.js)
```

## 快速开始

### 1. 后端

```bash
cd music-rank-backend
# 开发环境 (MySQL)
mvn spring-boot:run

# 生产环境 (SQLite)
mvn package -DskipTests
java -jar target/music-rank-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 2. 前端

```bash
cd music-rank-frontend
cp .env.example .env   # 编辑填入酷狗 token（可选）
npm install
npm run dev             # http://localhost:5173
```

### 3. 酷狗 API（同步功能需要）

```bash
cd KuGouMusicApi
npm install
node index.js           # 监听 localhost:3000
```

## 生产部署

### 架构

```
浏览器 ──→ Nginx (:443) ──→ 静态文件 (/var/www/music-rank/)
                  ├── /api/*  → localhost:8080 (Spring Boot)
                  └── /kugou/* → localhost:3000 (酷狗 API)
```

### 服务配置

后端以 `systemd` 服务运行：

```
[Service]
User=admin
ExecStart=java -Xms64m -Xmx128m -XX:+UseSerialGC \
  -jar /home/admin/music-rank/app.jar --spring.profiles.active=prod
Restart=on-failure
```

### 部署步骤

```bash
# 构建
cd music-rank-backend && mvn package -DskipTests
cd music-rank-frontend && npm run build

# 上传
scp backend/target/*.jar root@server:/home/admin/music-rank/app.jar
scp -r frontend/dist/* root@server:/var/www/music-rank/

# 服务器重启后端
ssh root@server "systemctl restart music-backend"
```

## API 概览

| 路径 | 方法 | 说明 |
|------|------|------|
| `/api/music/page` | GET | 分页查询（支持筛选） |
| `/api/music/list` | GET | 全量导出 |
| `/api/music/add` | POST | 添加歌曲 |
| `/api/music/{id}` | PUT | 更新歌曲 |
| `/api/music/{id}` | DELETE | 删除歌曲 |
| `/api/music/batch-import` | POST | 批量导入 |
| `/api/music/batch-delete` | POST | 批量删除 |
| `/api/playlist/list` | GET | 歌单列表 |
| `/api/sync/kugou/playlists` | GET | 获取酷狗歌单 |
| `/api/sync/kugou/playlist/{id}` | POST | 同步指定歌单 |
| `/api/sync/token` | POST | 更新酷狗 Token |

## 性能优化

- **Element Plus 按需加载**：JS 1MB → 339KB，CSS 385KB → 86KB
- **歌手列表缓存**：localStorage 缓存，减少 HTTP 请求
- **SQLite 连接池**：HikariCP 3 连接（单写数据库）
- **搜索防抖**：200ms 防抖，体感响应更快
- **JVM 调优**：SerialGC, 128MB 堆上限, 64MB Metaspace
- **SQLite 优化**：WAL 模式, 64MB 缓存, 索引覆盖

## License

MIT
