# music-rank-backend

个人音乐排行榜后端服务，基于 Spring Boot 3 + MyBatis-Plus，支持音乐数据管理和酷狗音乐歌单同步。

## 技术栈

- **Java 17**
- **Spring Boot 3.5.x**
- **MyBatis-Plus 3.5.5** — ORM 框架，内置分页
- **MySQL 8.0**
- **Hutool 5.8** — Java 工具类库（HTTP 请求、JSON 解析）
- **Lombok** — 减少样板代码
- **Maven** — 项目构建

## 项目结构

```
src/main/java/com/musicrank/
├── MusicRankBackendApplication.java   # Spring Boot 启动类
├── common/
│   ├── Result.java                    # 统一 API 响应包装
│   └── GlobalExceptionHandler.java    # 全局异常处理器
├── config/
│   ├── MyBatisPlusConfig.java        # MyBatis-Plus 分页插件配置
│   └── WebConfig.java                # CORS 跨域配置
├── controller/
│   ├── MusicRankController.java      # 歌曲管理 API
│   ├── PlaylistController.java       # 歌单 API
│   └── KugouSyncController.java      # 酷狗同步 API
├── dto/
│   ├── MusicQueryDTO.java            # 查询参数对象
│   └── SyncResultDTO.java            # 同步结果对象
├── entity/
│   ├── MusicRank.java                # 歌曲实体
│   ├── Playlist.java                 # 歌单实体
│   └── PlaylistMusic.java           # 歌单-歌曲关联实体
├── mapper/
│   ├── MusicRankMapper.java          # 歌曲 Mapper
│   ├── PlaylistMapper.java           # 歌单 Mapper
│   └── PlaylistMusicMapper.java     # 关联 Mapper
├── service/
│   ├── MusicRankService.java         # 歌曲服务接口
│   ├── PlaylistService.java          # 歌单服务接口
│   ├── KugouSyncService.java         # 酷狗同步服务接口
│   └── impl/
│       ├── MusicRankServiceImpl.java
│       ├── PlaylistServiceImpl.java
│       └── KugouSyncServiceImpl.java
└── resources/
    ├── application.yml               # 配置文件
    └── mapper/
        └── MusicRankMapper.xml       # MyBatis SQL 映射
```

## 快速开始

### 前提条件

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- 酷狗 API 服务运行在 `localhost:3000`（使用 KuGouMusicApi 项目）

### 1. 创建数据库

```bash
mysql -u root -p < music_db.sql
```

### 2. 配置环境变量

```bash
# 数据库配置（可选，默认 root/root）
export DB_USERNAME=root
export DB_PASSWORD=your_password

# 酷狗登录 token（必需，用于同步功能）
export KUGOU_TOKEN=your_kugou_token
```

### 3. 启动服务

```bash
cd music-rank-backend
mvn spring-boot:run
```

服务运行在 `http://localhost:8080`

### 4. 验证

```bash
curl http://localhost:8080/api/music/page?current=1&size=10
# 返回格式: {"code":200,"message":"success","data":{...},"timestamp":...}
```

## API 接口

### 歌曲管理 `/api/music`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询（支持 keyword, singer, starRating, hasStar, playlistId） |
| GET | `/list` | 获取列表（用于导出） |
| POST | `/add` | 添加单首歌曲 |
| POST | `/batch-import` | 批量导入（JSON 数组） |
| POST | `/batch-delete` | 批量删除（ID 数组） |

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

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1717334400000
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 409 | 数据重复冲突 |
| 500 | 服务器内部错误 |

## 数据库表

- **music_rank** — 歌曲表（歌手+歌名唯一约束）
- **playlist** — 歌单表
- **playlist_music** — 歌单-歌曲关联表（级联删除）
