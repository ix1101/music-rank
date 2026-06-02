#!/bin/bash
# ============================================
# 音乐排行榜 — 一键部署脚本
# 在 Ubuntu 服务器上执行: bash deploy.sh
# ============================================
set -e

APP_DIR="/home/admin/music-rank"
echo "=== 音乐排行榜部署 ==="

# 1. 创建目录
mkdir -p $APP_DIR/{frontend,backend,data}
echo "[1/4] 目录已创建"

# 2. 停止旧服务
sudo systemctl stop music-backend music-kugou 2>/dev/null || true
echo "[2/4] 旧服务已停止"

# 3. 初始化 SQLite
if [ ! -f "$APP_DIR/data/music.db" ]; then
    echo "初始化数据库..."
    sqlite3 "$APP_DIR/data/music.db" < "$APP_DIR/backend/schema-sqlite.sql"
fi
echo "[3/4] 数据库就绪"

# 4. 启动后端
echo "启动后端..."
sudo tee /etc/systemd/system/music-backend.service > /dev/null << EOF
[Unit]
Description=Music Rank Backend
After=network.target
[Service]
Type=simple
User=admin
WorkingDirectory=$APP_DIR/backend
ExecStart=/usr/bin/java -Xms64m -Xmx128m -XX:+UseSerialGC -XX:MaxMetaspaceSize=64m -jar $APP_DIR/backend/music-rank-backend.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
[Install]
WantedBy=multi-user.target
EOF

# 5. 启动酷狗 API
sudo tee /etc/systemd/system/music-kugou.service > /dev/null << EOF
[Unit]
Description=KuGou Music API
After=network.target
[Service]
Type=simple
User=admin
WorkingDirectory=$APP_DIR/kugou
ExecStart=/usr/bin/node $APP_DIR/kugou/server.js
Environment=PORT=3000
Restart=on-failure
RestartSec=10
[Install]
WantedBy=multi-user.target
EOF

# 6. Nginx
if ! command -v nginx &> /dev/null; then
    sudo apt update -qq && sudo apt install -y nginx
fi
sudo cp deploy/nginx.conf /etc/nginx/sites-available/music-rank
sudo ln -sf /etc/nginx/sites-available/music-rank /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx

# 7. 启用开机自启
sudo systemctl daemon-reload
sudo systemctl enable music-backend music-kugou
sudo systemctl start music-backend music-kugou

echo "[4/4] 部署完成!"
echo "访问 http://$(curl -s ifconfig.me)"
