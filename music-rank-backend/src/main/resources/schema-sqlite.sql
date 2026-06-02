-- SQLite 初始化（云服务器低内存部署用）
CREATE TABLE IF NOT EXISTS music_rank (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  title       TEXT    NOT NULL,
  artist      TEXT    NOT NULL,
  star_rating REAL    DEFAULT 0.0,
  album       TEXT,
  cover_url   TEXT    DEFAULT '',
  notes       TEXT,
  create_time TEXT    DEFAULT (datetime('now','localtime')),
  update_time TEXT    DEFAULT (datetime('now','localtime')),
  UNIQUE(artist, title)
);

CREATE TABLE IF NOT EXISTS playlist (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  name        TEXT    NOT NULL,
  cover_url   TEXT    DEFAULT '',
  description TEXT    DEFAULT '',
  create_time TEXT    DEFAULT (datetime('now','localtime')),
  update_time TEXT    DEFAULT (datetime('now','localtime')),
  UNIQUE(name)
);

CREATE TABLE IF NOT EXISTS playlist_music (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  playlist_id INTEGER NOT NULL REFERENCES playlist(id) ON DELETE CASCADE,
  music_id    INTEGER NOT NULL REFERENCES music_rank(id) ON DELETE CASCADE,
  create_time TEXT    DEFAULT (datetime('now','localtime')),
  UNIQUE(playlist_id, music_id)
);

CREATE TABLE IF NOT EXISTS system_config (
  config_key   TEXT PRIMARY KEY,
  config_value TEXT,
  create_time  TEXT DEFAULT (datetime('now','localtime')),
  update_time  TEXT DEFAULT (datetime('now','localtime'))
);

-- 初始数据
INSERT OR IGNORE INTO playlist (id, name, cover_url, description) VALUES (1, '未分类', 'https://picsum.photos/200/200', '默认歌单');
INSERT OR IGNORE INTO system_config (config_key, config_value) VALUES ('kugou_token', '');
