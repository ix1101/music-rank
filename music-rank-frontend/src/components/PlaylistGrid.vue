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
          :src="playlist.coverUrl || 'https://picsum.photos/200/200'"
          :alt="playlist.name"
          loading="lazy"
        />
        <div class="playlist-count">{{ playlist.trackCount || playlist.count || 0 }}首</div>
      </div>
      <div class="playlist-name">{{ playlist.name }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  playlists: { type: Array, default: () => [] }
})

defineEmits(['select'])
</script>

<style scoped>
.playlists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.playlist-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.playlist-card:hover {
  transform: translateY(-5px);
}

.playlist-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 8px;
}

.playlist-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.playlist-count {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.playlist-name {
  text-align: center;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .playlists-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 15px;
  }
}
</style>
