<template>
  <div class="playlists-grid">
    <div
      v-for="playlist in playlists"
      :key="playlist.id"
      class="playlist-card"
      @click="$emit('select', playlist)"
    >
      <div class="playlist-cover">
        <img
          :src="httpsUrl(playlist.coverUrl)"
          :alt="playlist.name"
          loading="lazy"
        />
        <div class="playlist-overlay">
          <span class="playlist-count">{{ playlist.trackCount || playlist.count || 0 }} 首</span>
        </div>
        <!-- 删除按钮 -->
        <button class="pl-del-btn" @click.stop="$emit('delete-playlist', playlist)" title="删除歌单">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
        </button>
      </div>
      <div class="playlist-body">
        <div class="playlist-name">{{ playlist.name }}</div>
        <div class="playlist-sub">{{ playlist.trackCount || playlist.count || 0 }} 首歌曲</div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!playlists.length" class="empty-state">
      <div class="empty-icon">📁</div>
      <div class="empty-title">还没有歌单</div>
      <div class="empty-desc">导入歌曲后自动按歌单分类</div>
    </div>
  </div>
</template>

<script setup>
import { httpsUrl } from '../api/request'
defineProps({ playlists: { type: Array, default: () => [] } })
defineEmits(['select', 'delete-playlist'])
</script>

<style scoped>
.playlists-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 4px 24px;
}

.playlist-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: var(--radius);
  overflow: hidden;
  max-width: 150px;
  margin: 0 auto;
  width: 100%;
}
.playlist-card:hover { transform: translateY(-3px); }
.playlist-card:active { transform: scale(0.97); }

.playlist-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius);
  overflow: hidden;
  background: #F0EBE2;
}
.playlist-cover img {
  width: 100%; height: 100%; object-fit: cover; display: block;
}
.playlist-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 24px 10px 8px;
  background: linear-gradient(transparent, rgba(0,0,0,.4));
}
.playlist-count {
  color: #fff; font-size: 12px; font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,.4);
}

/* 删除按钮 */
.pl-del-btn {
  position: absolute; top: 8px; right: 8px;
  width: 28px; height: 28px; border: none;
  background: rgba(255,255,255,.85); border-radius: 50%;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  color: var(--text-muted); opacity: 0; transition: all 0.2s;
  backdrop-filter: blur(4px);
}
.playlist-card:hover .pl-del-btn { opacity: 1; }
.pl-del-btn:hover { color: var(--danger); background: #fff; }
.pl-del-btn:active { transform: scale(0.9); }

.playlist-body { padding: 10px 4px 0; }
.playlist-name {
  font-size: 14px; font-weight: 500; text-align: center;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.playlist-sub {
  font-size: 12px; color: var(--text-muted); text-align: center; margin-top: 2px;
}

/* 空状态 */
.empty-state { grid-column: 1 / -1; text-align: center; padding: 40px 20px; }
.empty-icon { font-size: 40px; margin-bottom: 12px; opacity: 0.4; }
.empty-title { font-size: 14px; font-weight: 400; color: var(--text-secondary); margin-bottom: 4px; }
.empty-desc { font-size: 12px; color: var(--text-muted); }

/* 桌面端 3 列 */
@media (min-width: 768px) {
  .playlists-grid { grid-template-columns: repeat(3, 1fr); gap: 20px; padding: 4px 0; }
  .playlist-card { max-width: 140px; }
}
/* 移动端操作按钮始终可见 */
@media (max-width: 767px) {
  .pl-del-btn { opacity: 1; }
}
</style>
