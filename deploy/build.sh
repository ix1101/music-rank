#!/bin/bash
# ============================================
# 音乐排行榜 — 构建打包脚本（在本地执行）
# ============================================
set -e

echo "=== 构建音乐排行榜 ==="

# 1. 构建后端
echo "[1/3] 构建后端..."
cd music-rank-backend
mvn clean package -DskipTests -q
cd ..

# 2. 构建前端
echo "[2/3] 构建前端..."
cd music-rank-frontend
npm run build --silent
cd ..

# 3. 打包部署文件
echo "[3/3] 打包..."
mkdir -p dist_pkg/frontend dist_pkg/backend dist_pkg/kugou
cp music-rank-backend/target/music-rank-backend-*-SNAPSHOT.jar dist_pkg/backend/music-rank-backend.jar
cp music-rank-backend/src/main/resources/schema-sqlite.sql dist_pkg/backend/
cp -r music-rank-frontend/dist/* dist_pkg/frontend/
cp -r KuGouMusicApi/{module,util,server.js,main.js,index.js,package.json,package-lock.json,.env.example} dist_pkg/kugou/ 2>/dev/null || true
cp deploy/deploy.sh dist_pkg/
cp deploy/nginx.conf dist_pkg/

echo "打包完成: dist_pkg/"
echo "上传到服务器: scp -r dist_pkg/* admin@8.218.235.31:/home/admin/music-rank/"
